package app.airsignal.weather.view.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.adapter.AddressListAdapter
import app.airsignal.weather.adapter.OnAdapterItemClick
import app.airsignal.weather.dao.AdapterModel
import app.airsignal.weather.dao.StaticDataObject.CURRENT_GPS_ID
import app.airsignal.weather.dao.StaticDataObject.LANG_KR
import app.airsignal.weather.dao.StaticDataObject.TEXT_SCALE_BIG
import app.airsignal.weather.dao.StaticDataObject.TEXT_SCALE_SMALL
import app.airsignal.weather.db.room.model.GpsEntity
import app.airsignal.weather.db.room.repository.GpsRepository
import app.airsignal.weather.util.KeyboardController
import app.airsignal.weather.util.`object`.DataTypeParser.convertAddress
import app.airsignal.weather.util.`object`.DataTypeParser.getCurrentTime
import app.airsignal.weather.util.`object`.GetAppInfo.getCurrentLocation
import app.airsignal.weather.util.`object`.GetAppInfo.getUserFontScale
import app.airsignal.weather.util.`object`.GetAppInfo.getUserLastAddress
import app.airsignal.weather.util.`object`.GetAppInfo.getUserLocation
import app.airsignal.weather.util.`object`.GetSystemInfo.getLocale
import app.airsignal.weather.util.`object`.SetAppInfo.setUserLastAddr
import app.airsignal.weather.util.`object`.SetSystemInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import java.util.*

/**
 * @author : Lee Jae Young
 * @since : 2023-04-11 오전 11:53
 **/
class SearchDialog(
    mActivity: Activity,
    lId: Int, private val fm: FragmentManager, private val tagId: String?,
) : BottomSheetDialogFragment() {
    private val activity = mActivity
    private val layoutId = lId
    val currentList = ArrayList<AdapterModel.AddressListItem>()
    private val currentAdapter = AddressListAdapter(activity, currentList)
    private val db by lazy { GpsRepository(activity) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return if (layoutId == 0) {
            inflater.inflate(R.layout.dialog_address_change, container, false)
        } else {
            inflater.inflate(R.layout.dialog_address_search, container, false)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 텍스트 폰트 크기 적용
        when (getUserFontScale(activity)) {
            TEXT_SCALE_SMALL -> {
                SetSystemInfo.setTextSizeSmall(activity)
            }
            TEXT_SCALE_BIG -> {
                SetSystemInfo.setTextSizeLarge(activity)
            }
            else -> {
                SetSystemInfo.setTextSizeDefault(activity)
            }
        }

        if (layoutId == 0) {  // 등록된 주소 다이얼로그
            val changeAddressView: TextView = view.findViewById(R.id.changeAddressView)
            val currentAddress: TextView = view.findViewById(R.id.changeAddressText)
            val currentGpsImg: ImageView = view.findViewById(R.id.changeAddressImg)

            // 주소 등록 EditText 클릭
            changeAddressView.setOnClickListener {
                changeAddressView.clearFocus()
                dismissNow() // 현재 다이얼로그 사라짐
                SearchDialog(activity, 1, fm, tagId).showNow(fm, tagId) // 다이얼로그 재호출
            }

            // 삭제 버튼 클릭
            val deleteList: TextView = view.findViewById(R.id.changeAddressEdit)
            deleteList.setOnClickListener {
                if (!currentAdapter.getCheckBoxVisible()) {
                    currentAdapter.updateCheckBoxVisible(true)
                } else {
                    currentAdapter.updateCheckBoxVisible(false)
                }
            }

            // 현재 주소 클릭 시 현재 주소로 데이터 호출
            currentAddress.setOnClickListener {
                dismissNow()
                CoroutineScope(Dispatchers.IO).launch {
                    dbUpdate(db.findById(CURRENT_GPS_ID).addrKr,db.findById(CURRENT_GPS_ID).addrEn)

                    withContext(Dispatchers.Main) {
                        dismissNow()
                        delay(100)
                        activity.recreate()
                    }
                }
            }

            // 등록 된 주소 보여주기 세팅
            val rv: RecyclerView = view.findViewById(R.id.changeAddressRv)
            rv.adapter = currentAdapter
            GlobalScope.launch {
                GpsRepository(activity).findAll().forEach { entity ->
                    withContext(Dispatchers.Main) {
                        if (entity.name == CURRENT_GPS_ID) {
                            currentAddress.text = getCurrentLocation(activity).replace(getString(R.string.korea), "")
                            if (getCurrentLocation(activity) == getUserLastAddress(activity)) {
                                currentAddress.setTextColor(activity.getColor(R.color.main_blue_color))
                                currentGpsImg.imageTintList =
                                    ColorStateList.valueOf(activity.getColor(R.color.main_blue_color))
                            } else {
                                currentAddress.setTextColor(activity.getColor(R.color.theme_text_color))
                                currentGpsImg.imageTintList =
                                    ColorStateList.valueOf(activity.getColor(R.color.theme_text_color))
                            }
                        } else {
                            addCurrentItem(entity.addrKr.toString(), entity.addrEn.toString())
                        }
                    }
                }
            }

            // 등록 된 주소 클릭 시 등록 된 주소로 데이터 호출
            currentAdapter.setOnItemClickListener(object : OnAdapterItemClick.OnAdapterItemClick {
                override fun onItemClick(v: View, position: Int) {
                    CoroutineScope(Dispatchers.Default).launch {
                        val currentAddr = currentList[position]
                        dbUpdate(currentAddr.kr,currentAddr.en)

                        withContext(Dispatchers.Main) {
                            dismissNow()
                            activity.recreate()
                        }
                    }
                }
            })

            currentAdapter.notifyDataSetChanged()
        }
        // 주소 등록 다이얼로그 생성
        else {
            val searchView: EditText = view.findViewById(R.id.searchAddressView)
            val searchBack: ImageView = view.findViewById(R.id.searchBack)
            val noResult: TextView = view.findViewById(R.id.searchAddressNoResult)
            searchBack.setOnClickListener {
                dismissNow()
                show(0)
            }
            val listView: ListView = view.findViewById(R.id.searchAddressListView)

            searchEditListener(listView, searchView, noResult)
            KeyboardController().onKeyboardUp(requireContext(), searchView)
        }
    }

    // 검색창 리스너
    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun searchEditListener(listView: ListView, editText: EditText, noResult: TextView) {
        @SuppressLint("InflateParams")
        val searchItem = ArrayList<String>()
        val allTextArray =
            if (isKorea()) resources.getStringArray(R.array.address_korean)
            else resources.getStringArray(R.array.address_english)
        val adapter = CustomArrayAdapter(editText, dataList = searchItem)
        listView.adapter = adapter

        editText.setOnTouchListener { _, motionEvent ->
            try {
                if (motionEvent.action == MotionEvent.ACTION_UP &&
                    motionEvent.rawX >= editText.right - editText.compoundDrawablesRelative[2].bounds.width()
                ) {
                    editText.text.clear()
                    return@setOnTouchListener true
                }

            } catch (e: java.lang.NullPointerException) {
                e.printStackTrace()
            }

            false
        }

        // 서치 뷰 텍스트 변환 콜벡
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s!!.isNotEmpty()) {
                    searchItem.clear()
                    editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0, 0, R.drawable.ico_search_x, 0
                    )
                    noResult.visibility = View.GONE

                    allTextArray.forEach { allList ->
                        val nonSpacing = s.toString().replace(" ", "").lowercase()
                        if (allList.replace(" ", "").lowercase().contains(nonSpacing) ||
                            convertAddress(allList).replace(" ", "").lowercase()
                                .contains(nonSpacing)
                        ) searchItem.add(allList)
                    }
                } else {
                    noResult.visibility = View.VISIBLE
                    searchItem.clear()
                    editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0, 0, android.R.color.transparent, 0
                    )
                }
                adapter.notifyDataSetChanged()
            }
        })

        // 검색주소 리스트
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val builder = Dialog(activity)
                val viewSearched = LayoutInflater.from(activity.applicationContext)
                    .inflate(R.layout.dialog_alert_double_btn, null)
                builder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
                builder.setContentView(viewSearched)
                builder.create()

                val cancel = viewSearched.findViewById<AppCompatButton>(R.id.alertDoubleCancelBtn)
                val apply = viewSearched.findViewById<AppCompatButton>(R.id.alertDoubleApplyBtn)
                val title = viewSearched.findViewById<TextView>(R.id.alertDoubleTitle)

                val span =
                    if (resources.configuration.locales[0] == Locale.KOREA)
                    SpannableStringBuilder("${searchItem[position]}을(를)\n추가하시겠습니까?")
                    else SpannableStringBuilder("Add ${searchItem[position]}?")

                span.setSpan(
                    ForegroundColorSpan(
                        ResourcesCompat.getColor(
                            activity.resources,
                            R.color.main_blue_color, null
                        )
                    ), if (isKorea()) 0 else 4,
                   if (isKorea()) searchItem[position].length else 4 + searchItem[position].length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                title.text = span
                apply.text = activity.getString(R.string.add)
                apply.backgroundTintList = ColorStateList.valueOf(
                    activity.getColor(R.color.main_blue_color)
                )
                cancel.text = activity.getString(R.string.cancel)

                apply.setOnClickListener {
                    builder.dismiss()
                    CoroutineScope(Dispatchers.IO).launch {
                        val model = GpsEntity()
                        model.name = searchItem[position]

                        val addrArray =  resources.getStringArray(
                            if (!isKorea()) R.array.address_english
                            else R.array.address_korean)

                        addrArray.forEachIndexed { index, s ->
                            if(s == searchItem[position]) {
                                model.position = index
                                model.addrEn = resources.getStringArray(R.array.address_english)[index]
                                model.addrKr = resources.getStringArray(R.array.address_korean)[index]
                            }
                        }
                        db.insert(model)
                        dbUpdate(model.addrKr,model.addrEn)

                        withContext(Dispatchers.Main) {
                            dismissNow()
                            delay(100)
                            activity.recreate()
                        }
                    }
                }

                cancel.setOnClickListener { builder.dismiss() }

                builder.show()
            }
    }

    // 다이얼로그 생성
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(true)

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            bottomSheetDialog.behavior.isDraggable = layoutId != 1
            setupRatio(bottomSheetDialog, if (layoutId == 1) 100 else 90)
        }

        return dialog
    }

    // 레이아웃 노출
    fun show(layoutId: Int) { SearchDialog(activity, layoutId, fm, tagId).showNow(fm, tagId) }

    private suspend fun dbUpdate(addrKr: String?, addrEn: String?) {
        withContext(Dispatchers.Default) {
            val model = GpsEntity()
            model.name = CURRENT_GPS_ID
            model.addrKr = addrKr
            model.addrEn = addrEn
            model.timeStamp = getCurrentTime()
            db.update(model)

            setUserLastAddr(activity, addrKr!!)
        }
    }

    private fun isKorea(): Boolean {
        val systemLang = getLocale(activity)
        return getUserLocation(activity) == LANG_KR || systemLang == Locale.KOREA
    }

    // 리스트 아이템 추가
    private fun addCurrentItem(addrKr: String?, addrEn: String?): SearchDialog {
        val item = AdapterModel.AddressListItem(
            addrKr!!.replace("null", ""),
            addrEn!!.replace("null", "")
        )
        currentList.add(item)
        return this
    }

    // 바텀 다이얼로그 세팅
    private fun setupRatio(bottomSheetDialog: BottomSheetDialog, ratio: Int) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = getBottomSheetDialogDefaultHeight(ratio)
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    // 바텀 다이얼로그 비율설정
    private fun getBottomSheetDialogDefaultHeight(per: Int): Int {
        return getWindowHeight() * per / 100
    }

    // 디바이스 높이 구하기
    private fun getWindowHeight(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    // 커스텀 어댑터 클래스
    private inner class CustomArrayAdapter(private val editText: EditText, dataList: List<String>) :
        ArrayAdapter<String>(activity, R.layout.list_item_searced_address, dataList) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent) as TextView
            val fullText = getItem(position)!!
            val editableText = fullText.lowercase()

            val startIndex = editableText.indexOf(editText.text.toString().lowercase())

            if (startIndex != -1) {
                val coloredText = android.text.SpannableString(getItem(position))
                coloredText.setSpan(
                    ForegroundColorSpan(activity.getColor(R.color.main_blue_color)),
                    startIndex,
                    startIndex + editText.text.toString().length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                view.text = coloredText
            } else  view.text = fullText

            return view
        }
    }
}
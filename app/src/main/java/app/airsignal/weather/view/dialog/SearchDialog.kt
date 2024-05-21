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
import app.airsignal.weather.dao.AdapterModel
import app.airsignal.weather.dao.StaticDataObject
import app.airsignal.weather.db.room.model.GpsEntity
import app.airsignal.weather.db.room.repository.GpsRepository
import app.airsignal.weather.db.sp.*
import app.airsignal.weather.util.KeyboardController
import app.airsignal.weather.util.OnAdapterItemSingleClick
import app.airsignal.weather.util.`object`.DataTypeParser
import app.airsignal.weather.view.activity.MainActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.CompletableFuture

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

    init {
        when (GetAppInfo.getUserLocation(activity)) {
            StaticDataObject.LANG_KR -> { SetSystemInfo.updateConfiguration(activity, Locale.KOREA) }
            StaticDataObject.LANG_EN -> { SetSystemInfo.updateConfiguration(activity, Locale.ENGLISH) }
            else -> { SetSystemInfo.updateConfiguration(activity, Locale.getDefault()) }
        }
        // 텍스트 폰트 크기 적용
        when (GetAppInfo.getUserFontScale(activity)) {
            SpDao.TEXT_SCALE_SMALL -> { SetSystemInfo.setTextSizeSmall(activity) }
            SpDao.TEXT_SCALE_BIG -> { SetSystemInfo.setTextSizeLarge(activity) }
            else -> { SetSystemInfo.setTextSizeDefault(activity) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return if (layoutId == 0)
            inflater.inflate(R.layout.dialog_address_change, container, false)
        else inflater.inflate(R.layout.dialog_address_search, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (layoutId == 0) {  // 등록된 주소 다이얼로그
            val changeAddressView: TextView = view.findViewById(R.id.changeAddressView)
            val currentAddress: TextView = view.findViewById(R.id.changeAddressText)
            val currentGpsImg: ImageView = view.findViewById(R.id.changeAddressImg)

            // 주소 등록 EditText 클릭
            changeAddressView.setOnClickListener {
                changeAddressView.clearFocus()
                this@SearchDialog.dismiss() // 현재 다이얼로그 사라짐
                SearchDialog(activity, 1, fm, tagId).showNow(fm, tagId) // 다이얼로그 재호출
            }

            // 삭제 버튼 클릭
            val deleteList: TextView = view.findViewById(R.id.changeAddressEdit)
            deleteList.setOnClickListener {
                if (!currentAdapter.getCheckBoxVisible())
                    currentAdapter.updateCheckBoxVisible(true)
                else currentAdapter.updateCheckBoxVisible(false)

            }

            // 현재 주소 클릭 시 현재 주소로 데이터 호출
            currentAddress.setOnClickListener {
                this@SearchDialog.dismiss()
                CoroutineScope(Dispatchers.IO).launch {
                    val dbFind = db.findByName(SpDao.CURRENT_GPS_ID)
                    dbUpdate(dbFind.addrKr,dbFind.addrEn,SpDao.CURRENT_GPS_ID)

                    withContext(Dispatchers.Main) {
                        this@SearchDialog.dismiss()
                        delay(300)
                        if (activity is MainActivity) activity.recreateMainActivity(dbFind.addrKr,dbFind.addrEn)
                    }
                }
            }

            // 등록 된 주소 보여주기 세팅
            val rv: RecyclerView = view.findViewById(R.id.changeAddressRv)
            rv.adapter = currentAdapter
            CoroutineScope(Dispatchers.IO).launch {
                val lastAddr = GetAppInfo.getUserLastAddress(activity)
                GpsRepository(activity).findAll().forEach { entity ->
                    withContext(Dispatchers.Main) {
                        if (entity.name == SpDao.CURRENT_GPS_ID) {
                            currentAddress.text = entity.addrKr?.replace(getString(R.string.korea), "") ?: ""
                            if (entity.addrKr == lastAddr) {
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
                            currentAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            // 등록 된 주소 클릭 시 등록 된 주소로 데이터 호출
            currentAdapter.setOnItemClickListener(object : OnAdapterItemSingleClick() {
                override fun onSingleClick(v: View?, position: Int) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val currentAddr = currentList[position]
                        dbUpdate(currentAddr.kr,currentAddr.en,currentAddr.kr ?: "")

                        withContext(Dispatchers.Main) {
                            withContext(Dispatchers.Main) {
                                this@SearchDialog.dismiss()
                                delay(300)
                                if (activity is MainActivity) activity.recreateMainActivity(currentAddr.kr,currentAddr.en)
                            }
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
                CompletableFuture.supplyAsync { this@SearchDialog.dismiss() }.thenAccept { show(0) }
            }

            val listView: ListView = view.findViewById(R.id.searchAddressListView)

            searchEditListener(listView, searchView, noResult)

            KeyboardController.onKeyboardUp(requireContext(), searchView)
        }
    }

    // 검색창 리스너
    @SuppressLint("ClickableViewAccessibility")
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
            } catch (e: java.lang.NullPointerException) { e.printStackTrace() }

            false
        }

        // 서치 뷰 텍스트 변환 콜벡
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.isNotEmpty()) {
                    searchItem.clear()
                    editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0, 0, R.drawable.ico_search_x, 0
                    )
                    noResult.visibility = View.GONE

                    allTextArray.forEach { allList ->
                        val nonSpacing = s.toString().replace(" ", "").lowercase()
                        if (allList.replace(" ", "").lowercase().contains(nonSpacing) ||
                            DataTypeParser.convertAddress(allList).replace(" ", "").lowercase()
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
                        val model = GpsEntity(
                            name = searchItem[position],
                            lat = null,
                            lng = null,
                            addrEn = null,
                            addrKr = null
                        )

                        val addrArray =  resources.getStringArray(
                            if (!isKorea()) R.array.address_english
                            else R.array.address_korean)

                        addrArray.forEachIndexed { index, s ->
                            if (s == searchItem[position]) {
                                model.addrEn = resources.getStringArray(R.array.address_english)[index]
                                model.addrKr = resources.getStringArray(R.array.address_korean)[index]
                            }
                        }
                        db.insert(model)
                        dbUpdate(model.addrKr,model.addrEn,model.name)

                        withContext(Dispatchers.Main) {
                            this@SearchDialog.dismiss()
                            delay(300)
                            if (activity is MainActivity) activity.recreateMainActivity(model.addrKr,model.addrEn)
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

    private fun dbUpdate(addrKr: String?, addrEn: String?, name: String) {
        val model = GpsEntity(
            name = name,
            lat = null,
            lng = null,
            addrEn = addrEn,
            addrKr = addrKr
        )

        db.update(model)
        SetAppInfo.setUserLastAddr(activity, addrKr ?: "")
    }

    private fun isKorea(): Boolean {
        val systemLang = GetSystemInfo.getLocale(activity)
        return GetAppInfo.getUserLocation(activity) == SpDao.LANG_KR || systemLang == Locale.KOREA
    }

    // 리스트 아이템 추가
    private fun addCurrentItem(addrKr: String?, addrEn: String?): SearchDialog {
        val item = AdapterModel.AddressListItem(
            addrKr?.replace("null", ""),
            addrEn?.replace("null", "")
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
    private fun getBottomSheetDialogDefaultHeight(per: Int): Int { return getWindowHeight() * per / 100 }

    // 디바이스 높이 구하기
    private fun getWindowHeight(): Int {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (context as Activity?)?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    // 커스텀 어댑터 클래스
    private inner class CustomArrayAdapter(private val editText: EditText, dataList: List<String>) :
        ArrayAdapter<String>(activity, R.layout.list_item_searced_address, dataList) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent) as TextView
            val fullText = getItem(position) ?: ""
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
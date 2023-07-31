package com.example.airsignal_app.view

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
import com.example.airsignal_app.R
import com.example.airsignal_app.adapter.AddressListAdapter
import com.example.airsignal_app.dao.StaticDataObject.CURRENT_GPS_ID
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.db.room.repository.GpsRepository
import com.example.airsignal_app.util.KeyboardController
import com.example.airsignal_app.util.`object`.DataTypeParser.convertAddress
import com.example.airsignal_app.util.`object`.DataTypeParser.getCurrentTime
import com.example.airsignal_app.util.`object`.GetAppInfo.getCurrentLocation
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserFontScale
import com.example.airsignal_app.util.`object`.GetAppInfo.getUserLastAddress
import com.example.airsignal_app.util.`object`.SetAppInfo.setUserLastAddr
import com.example.airsignal_app.util.`object`.SetSystemInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import timber.log.Timber

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
    val currentList = ArrayList<String>()
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (getUserFontScale(activity)) {
            "small" -> {
                SetSystemInfo.setTextSizeSmall(activity)
            }
            "big" -> {
                SetSystemInfo.setTextSizeLarge(activity)
            }
            else -> {
                SetSystemInfo.setTextSizeDefault(activity)
            }
        }
        if (layoutId == 0) {
            val changeAddressView: TextView = view.findViewById(R.id.changeAddressView)
            val currentAddress: TextView = view.findViewById(R.id.changeAddressText)
            val currentGpsImg: ImageView = view.findViewById(R.id.changeAddressImg)

            changeAddressView.setOnClickListener {
                changeAddressView.clearFocus()
                dismissNow()
                SearchDialog(activity, 1, fm, tagId).showNow(fm, tagId)
            }

            val editList: TextView = view.findViewById(R.id.changeAddressEdit)
            editList.setOnClickListener {
                if (!currentAdapter.getCheckBoxVisible()) {
                    currentAdapter.updateCheckBoxVisible(true)
                } else {
                    currentAdapter.updateCheckBoxVisible(false)
                }
            }

            val rv: RecyclerView = view.findViewById(R.id.changeAddressRv)
            rv.adapter = currentAdapter
            GpsRepository(activity).findAll().forEach { entity ->
                if (entity.name == CURRENT_GPS_ID) {
                    currentAddress.text = getCurrentLocation(activity)

                    entity.addr = getCurrentLocation(activity)

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
                    addCurrentItem(entity.addr.toString())
                }
            }

            currentAdapter.setOnItemClickListener(object : AddressListAdapter.OnItemClickListener {
                override fun onItemClick(v: View, position: Int) {
                    CoroutineScope(Dispatchers.Default).launch {
                        val currentAddr = currentList[position].replace("null", "")
                        dbUpdate(currentAddr)

                        withContext(Dispatchers.Main) {
                            dismissNow()
                            activity.recreate()
                        }
                    }
                }
            })

            currentAdapter.notifyDataSetChanged()
        } else {
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
    @SuppressLint("ClickableViewAccessibility")
    private fun searchEditListener(listView: ListView, editText: EditText, noResult: TextView) {
        @SuppressLint("InflateParams")
        val searchItem = java.util.ArrayList<String>()
        val allTextArray = resources.getStringArray(R.array.address)
        val adapter =
            CustomArrayAdapter(editText, dataList = searchItem)
        listView.adapter = adapter

        editText.setOnTouchListener { _, motionEvent ->
            try {
                if (motionEvent.action == MotionEvent.ACTION_UP &&
                    motionEvent.rawX >= editText.right - editText.compoundDrawablesRelative[2].bounds.width()
                ) {
                    // Clear the EditText when the clear button is clicked
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
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

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
                        ) {
                            searchItem.add(allList)
                        }
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

                val span = SpannableStringBuilder("${searchItem[position]}을(를)\n추가하시겠습니까?")
                span.setSpan(
                    ForegroundColorSpan(
                        ResourcesCompat.getColor(
                            activity.resources,
                            R.color.main_blue_color, null
                        )
                    ), 0,
                    searchItem[position].length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
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
                        model.addr = searchItem[position]
                        db.insert(model)
                        dbUpdate(model.addr)

                        withContext(Dispatchers.Main) {
                            dismissNow()
                            delay(100)
                            activity.recreate()
                        }
                    }
                }

                cancel.setOnClickListener {
                    builder.dismiss()
                }

                builder.show()
            }
    }

    // 다이얼로그 생성
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(true)

        if (layoutId == 1) {
            dialog.setOnShowListener { dialogInterface ->
                val bottomSheetDialog = dialogInterface as BottomSheetDialog
                bottomSheetDialog.behavior.isDraggable = false
                setupRatio(bottomSheetDialog, 100)
            }
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimationSide
        } else {
            dialog.setOnShowListener { dialogInterface ->
                val bottomSheetDialog = dialogInterface as BottomSheetDialog
                setupRatio(bottomSheetDialog, 90)
                bottomSheetDialog.behavior.isDraggable = true
            }
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimationUp
        }

        return dialog
    }

    // 레이아웃 노출
    fun show(layoutId: Int) {
        SearchDialog(activity, layoutId, fm, tagId).showNow(fm, tagId)
    }

    private suspend fun dbUpdate(addr: String?) {
        withContext(Dispatchers.Default) {
            val model = GpsEntity()
            model.name = CURRENT_GPS_ID
            model.addr = addr
            model.timeStamp = getCurrentTime()
            db.update(model)

            addr?.let {
                setUserLastAddr(activity, it)
            }
        }
    }

    // 리스트 아이템 추가
    private fun addCurrentItem(address: String): SearchDialog {
        currentList.add(address.replace("null", ""))
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
        @Suppress("DEPRECATION") (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(
            displayMetrics
        )
        return displayMetrics.heightPixels
    }

    private inner class CustomArrayAdapter(private val editText: EditText, dataList: List<String>) :
        ArrayAdapter<String>(activity, R.layout.list_item_searced_address, dataList) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent) as TextView
            val fullText = getItem(position)!!
//            val editableText = convertAddressInv(getItem(position)!!).lowercase()

            // Find the starting index of the input text in the item's text
            val startIndex = fullText.indexOf(editText.text.toString())

            // If the input text is found in the item's text, set the color of the input text to red
            if (startIndex != -1) {
                val coloredText = android.text.SpannableString(getItem(position))
                coloredText.setSpan(
                    ForegroundColorSpan(activity.getColor(R.color.main_blue_color)),
                    startIndex,
                    startIndex + editText.text.toString().length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                view.text = coloredText
            } else {
                view.text = fullText
            }

            return view
        }
    }
}
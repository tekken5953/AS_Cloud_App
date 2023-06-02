package com.example.airsignal_app.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.adapter.AddressListAdapter
import com.example.airsignal_app.dao.IgnoredKeyFile.lastAddress
import com.example.airsignal_app.dao.StaticDataObject.TAG_D
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.db.room.repository.GpsRepository
import com.example.airsignal_app.util.ConvertDataType
import com.example.airsignal_app.util.ConvertDataType.convertAddress
import com.example.airsignal_app.util.KeyboardController
import com.example.airsignal_app.util.RefreshUtils
import com.example.airsignal_app.view.activity.MainActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


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
        when(SharedPreferenceManager(activity).getString("scale")) {
            "small" -> {
                ConvertDataType.setTextSizeSmall(activity)
            }
            "big" -> {
                ConvertDataType.setTextSizeLarge(activity)
            }
            else -> {
                ConvertDataType.setTextSizeDefault(activity)
            }
        }
        if (layoutId == 0) {
            val changeAddressView: TextView = view.findViewById(R.id.changeAddressView)
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
            GpsRepository(activity).findAll().forEach {
                Log.d(TAG_D, "검색리스트 아이템 추가 : ${it.id}, ${it.name}, ${it.addr}")
                addCurrentItem(it.addr.toString())
            }

            currentAdapter.setOnItemClickListener(object : AddressListAdapter.OnItemClickListener {
                override fun onItemClick(v: View, position: Int) {
                    SharedPreferenceManager(activity).setString(
                        lastAddress,
                        currentList[position].replace("null", "")
                    )
                    dismissNow()
                    activity.recreate()
                }
            })

            currentAdapter.notifyDataSetChanged()
        } else {
            val searchView: EditText = view.findViewById(R.id.searchAddressView)
            val searchBack: ImageView = view.findViewById(R.id.searchBack)
            searchBack.setOnClickListener {
                dismissNow()
                show(0)
            }
            val listView: ListView = view.findViewById(R.id.searchAddressListView)

            searchEditListener(listView, searchView)
            KeyboardController().onKeyboardUp(requireContext(),searchView)
        }
    }

    // 검색창 리스너
    private fun searchEditListener(listView: ListView, editText: EditText) {
        @SuppressLint("InflateParams")
        val searchItem = java.util.ArrayList<String>()
        val allTextArray = resources.getStringArray(R.array.address)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, searchItem)
        listView.adapter = adapter

        // 서치 뷰 텍스트 변환 콜벡
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.isNotEmpty()) {
                    searchItem.clear()

                    allTextArray.forEach { allList ->
                        val nonSpacing = p0.toString().replace(" ","").lowercase()
                        if (allList.replace(" ","").lowercase().contains(nonSpacing) ||
                                convertAddress(allList).replace(" ","").lowercase().contains(nonSpacing)) {
                            searchItem.add(allList)
                        }
                    }
                } else {
                    searchItem.clear()
                }
                adapter.notifyDataSetChanged()
            }
        })

        // 검색주소 리스트
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val db = GpsRepository(requireContext())
                val model = GpsEntity()
                model.name = searchItem[position]
                model.addr = searchItem[position]
                db.insert(model)
                SharedPreferenceManager(activity).setString(lastAddress,model.addr!!)
                this.dismissNow()
                RefreshUtils(activity).refreshActivity()
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
                setupRatio(bottomSheetDialog,100)
            }
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimationSide
        }
        else {
            dialog.setOnShowListener { dialogInterface ->
                val bottomSheetDialog = dialogInterface as BottomSheetDialog
                setupRatio(bottomSheetDialog,90)
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
}
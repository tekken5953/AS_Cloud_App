package com.example.airsignal_app.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.adapter.AddressListAdapter
import com.example.airsignal_app.dao.IgnoredKeyFile.lastAddress
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.db.room.GpsRepository
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.util.ConvertDataType.getCurrentTime
import com.example.airsignal_app.util.RefreshUtils
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
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (layoutId == 0) {
            val changeAddressView: TextView = view.findViewById(R.id.changeAddressView)
            changeAddressView.setOnClickListener {
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
            val db = GpsRepository(requireContext())
            db.findAll().forEach {
                addCurrentItem(it.addr.toString())
            }

            currentAdapter.setOnItemClickListener(object : AddressListAdapter.OnItemClickListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onItemClick(v: View, position: Int) {
                    dismissNow()
                    SharedPreferenceManager(activity).setString(
                        lastAddress,
                        currentList[position].replace("null", "")
                    )
                    RefreshUtils(activity).refreshActivityAfterSecond(1, null)
                }
            })

            currentAdapter.notifyDataSetChanged()
        } else {
            val searchView: EditText = view.findViewById(R.id.searchAddressView)
            searchView.requestFocus()

            val searchBack: ImageView = view.findViewById(R.id.searchBack)
            searchBack.setOnClickListener {
                dismissNow()
                show(0)
            }
            val listView: ListView = view.findViewById(R.id.searchAddressListView)

            searchEditListener(listView, searchView)
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
        editText.requestFocus()

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
                        if (allList.contains(p0)) {
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
                val model = GpsEntity(
                    searchItem[position],
                    null,
                    null,
                    searchItem[position],
                    getCurrentTime()
                )
                db.insert(model)
                this.dismissNow()
                show(0)
            }
    }

    // 다이얼로그 생성
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            setupRatio(bottomSheetDialog)
        }

        if (layoutId == 1)
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimationSide
        else {
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
    private fun setupRatio(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = getBottomSheetDialogDefaultHeight(90)
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
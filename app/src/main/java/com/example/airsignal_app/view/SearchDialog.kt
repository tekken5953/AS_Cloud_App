package com.example.airsignal_app.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.PopupMenu.OnDismissListener
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.airsignal_app.R
import com.example.airsignal_app.adapter.AddressListAdapter
import com.example.airsignal_app.adapter.AirQualityAdapter
import com.example.airsignal_app.dao.IgnoredKeyFile.lastAddress
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.db.room.GpsRepository
import com.example.airsignal_app.db.room.model.GpsEntity
import com.example.airsignal_app.util.ConvertDataType.getCurrentTime
import com.example.airsignal_app.util.RefreshUtils
import com.example.airsignal_app.view.activity.MainActivity
import com.example.airsignal_app.vmodel.GetWeatherViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.orhanobut.logger.Logger
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * @author : Lee Jae Young
 * @since : 2023-04-11 오전 11:53
 **/
class SearchDialog(lId: Int, private val fm: FragmentManager, private val tagId: String?) : BottomSheetDialogFragment() {
    private val layoutId = lId
    private val currentList = ArrayList<String>()
    private val currentAdapter by lazy { AddressListAdapter(requireContext(),currentList) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
                SearchDialog(1,fm,tagId).showNow(fm,tagId)
            }

            val rv: RecyclerView = view.findViewById(R.id.changeAddressRv)
            rv.adapter = currentAdapter
            val db = GpsRepository(requireContext())
            db.findAll().reversed().forEach {
                addCurrentItem(it.addr.toString())
            }

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

            searchEditListener(listView,searchView)
        }
    }

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
            AdapterView.OnItemClickListener { parent, view, position, id ->
                Logger.t("searchView").d("$position : ${searchItem[position]}")
                val db = GpsRepository(requireContext())
                val model = GpsEntity(db.findAll().size, null, null, searchItem[position], getCurrentTime())
                db.insert(model)
                this.dismissNow()
                show(0)
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            setupRatio(bottomSheetDialog)
        }

        if (layoutId == 1)
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimationSide
        else {
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimationUp

            dialog.setDismissMessage(msg)
        }

        return dialog
    }

    fun show(layoutId: Int) {
        if (layoutId == 1) {
            SearchDialog(layoutId,fm,tagId).apply {
                showNow(fm,tagId)
            }
        } else {
            SearchDialog(layoutId,fm,tagId).showNow(fm, tagId)
        }
    }

    private fun addCurrentItem(address: String) : SearchDialog {
        currentList.add(address)
        currentAdapter.setOnItemClickListener(object : AddressListAdapter.OnItemClickListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemClick(v: View, position: Int) {
                dismissNow()
                SharedPreferenceManager(v.context).setString(lastAddress, currentList[position])
            }
        })
        return this
    }

    private fun setupRatio(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = getBottomSheetDialogDefaultHeight(90)
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun getBottomSheetDialogDefaultHeight(per: Int): Int {
        return getWindowHeight() * per / 100
    }

    private fun getWindowHeight(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION") (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }
}
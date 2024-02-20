package app.airsignal.weather.as_eye.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.adapter.AddGroupAdapter
import app.airsignal.weather.as_eye.adapter.EyeCategoryAdapter
import app.airsignal.weather.as_eye.adapter.EyeDeviceAdapter
import app.airsignal.weather.as_eye.customview.EyeGroupSelectorView
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.databinding.ActivityEyeListBinding
import app.airsignal.weather.db.SharedPreferenceManager
import app.airsignal.weather.db.room.database.GroupDataBase
import app.airsignal.weather.db.room.model.EyeGroupEntity
import app.airsignal.weather.db.room.repository.EyeGroupRepository
import app.airsignal.weather.db.sp.SpDao
import app.airsignal.weather.firebase.fcm.SubFCM
import app.airsignal.weather.location.GetLocation
import app.airsignal.weather.network.ErrorCode
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.util.OnAdapterItemClick
import app.airsignal.weather.util.RefreshUtils
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.util.ToastUtils
import app.airsignal.weather.view.custom_view.MakeDoubleDialog
import app.airsignal.weather.view.custom_view.ShowDialogClass
import app.airsignal.weather.viewmodel.GetEyeDeviceListViewModel
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.time.LocalDateTime

class EyeListActivity : BaseEyeActivity<ActivityEyeListBinding>() {
    override val resID: Int get() = R.layout.activity_eye_list

    companion object { const val ENTIRE_GROUP = "전체" }

    private val deviceListItem = ArrayList<EyeDataModel.Device>()
    private val deviceListAdapter by lazy { EyeDeviceAdapter(this, deviceListItem) }
    private val allDevicesList = ArrayList<EyeDataModel.Device>()
    private val categoryItem = ArrayList<EyeDataModel.Category>()
    private val categoryAdapter by lazy { EyeCategoryAdapter(this, categoryItem) }
    private val groupList = ArrayList<EyeDataModel.Group>()
    private val groupAdapter by lazy { AddGroupAdapter(this, groupList) }
    private val checkedArray = ArrayList<EyeDataModel.Device>()
    private val db by lazy { GroupDataBase.getGroupInstance(this).groupRepository() }

    private val deviceListViewModel by viewModel<GetEyeDeviceListViewModel>()

    override fun onResume() {
        super.onResume()
        loadDeviceList()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        applyDeviceList()

        binding.aeListDeviceRv.adapter = deviceListAdapter
        binding.aeListCategoryRv.adapter = categoryAdapter

        addCategoryItem(ENTIRE_GROUP, deviceListItem)
        categoryAdapter.notifyDataSetChanged()

        categoryAdapter.setOnItemClickListener(object : OnAdapterItemClick.OnAdapterItemClick {
            override fun onItemClick(v: View, position: Int) {
                deviceListItem.clear()
                categoryAdapter.changeSelected(position)
                if (position == 0) {
                    loadDeviceList()
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val group = db.findByCategoryName(categoryItem[position].name)
                        group.device.forEach { pDevice ->
                            pDevice.detail?.let { pDetail ->
                                addListItem(
                                    pDevice.isMaster,
                                    pDevice.sort,
                                    pDevice.alias,
                                    pDevice.serial,
                                    EyeDataModel.DeviceDetail(
                                        pDetail.ssid,
                                        pDetail.report,
                                        pDetail.power
                                    )
                                )
                            }
                        }
                    }
                    categoryAdapter.changeSelected(position)
                }
                deviceListAdapter.notifyDataSetChanged()
            }
        })

        deviceListAdapter.notifyDataSetChanged()

        CoroutineScope(Dispatchers.IO).launch {
            val allGroups = getAllGroup()
            allGroups.forEach {
                //TODO 디바이스가 현재 등록된 상태인지 검사하여 분기
                addCategoryItem(it.name, it.device)
            }
        }

        deviceListAdapter.setOnItemClickListener(object : OnAdapterItemClick.OnAdapterItemClick {
            override fun onItemClick(v: View, position: Int) {
                deviceListItem[position].serial?.let { pSerial ->
                    val intent = Intent(this@EyeListActivity, EyeDetailActivity::class.java)
                    intent.apply {
                        putExtra("alias", deviceListItem[position].alias)
                        putExtra("serial", pSerial)
                        deviceListItem[position].detail?.let { pDetail ->
                            putExtra("ssid", pDetail.ssid)
                        }
                    }
                    startActivity(intent)
                    finish()
                } ?: run {
                    val intent = Intent(this@EyeListActivity, AddEyeDeviceActivity::class.java)
                    startActivity(intent)
                }
            }
        })

        binding.aeListBack.setOnClickListener { finish() }

        binding.aeListCategorySelector.setOnClickListener {
            val selectorBuilder = Dialog(this@EyeListActivity)
            val selectorView = LayoutInflater.from(this@EyeListActivity)
                .inflate(R.layout.dialog_add_group_selector,binding.aeListRoot,false)
            selectorBuilder.setContentView(selectorView)
            val add = selectorView.findViewById<EyeGroupSelectorView>(R.id.dialogAddGroupAdd)
            val delete = selectorView.findViewById<EyeGroupSelectorView>(R.id.dialogAddGroupDelete)
            val edit = selectorView.findViewById<EyeGroupSelectorView>(R.id.dialogAddGroupEdit)

            if (categoryAdapter.selectedPosition == 0) {
                delete.fetchColor(false)
                edit.fetchColor(false)
                add.fetchColor(true)
            } else {
                delete.fetchColor(true)
                edit.fetchColor(true)
                add.fetchColor(false)
            }

            add.setOnClickListener {
                selectorBuilder.dismiss()
                showAddGroupDialog()
            }

            delete.setOnClickListener {
                selectorBuilder.dismiss()
                showDeleteGroupDialog()
            }

            edit.setOnClickListener {
                selectorBuilder.dismiss()
                showEditGroupDialog()
            }

            selectorBuilder.show()
        }
    }

    private fun showEditGroupDialog() {
        val item = categoryItem[categoryAdapter.selectedPosition]
        val editBuilder = AlertDialog.Builder(this@EyeListActivity)
        val editView = LayoutInflater.from(this@EyeListActivity)
            .inflate(R.layout.dialog_edit_group_name, binding.aeListRoot,false)
        editBuilder.setView(editView)
        val dialog = editBuilder.create()

        val aliasEt = editView.findViewById<EditText>(R.id.dialogEditGroupNameEt)
        val editBtn = editView.findViewById<AppCompatButton>(R.id.dialogEditGroupNameBtn)
        val cancelIv = editView.findViewById<ImageView>(R.id.dialogEditGroupNameCancel)

        cancelIv.setOnClickListener { dialog.dismiss() }

        aliasEt.setText(item.name)

        aliasEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
               s?.let {
                   if (aliasEt.text.isNotBlank() && aliasEt.text.toString() != item.name) {
                       editBtn.isEnabled = true
                       editBtn.setTextColor(getColor(R.color.white))
                   } else {
                       editBtn.isEnabled = false
                       editBtn.setTextColor(getColor(R.color.eye_btn_disable_color))
                   }
               } ?: apply {
                   editBtn.isEnabled = false
                   editBtn.setTextColor(getColor(R.color.eye_btn_disable_color))
               }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        editBtn.setOnClickListener {
            if (editBtn.isEnabled) {
                CoroutineScope(Dispatchers.IO).launch {
                    EyeGroupRepository(this@EyeListActivity)
                        .update(categoryItem[categoryAdapter.selectedPosition].name, aliasEt.text.toString())
                    delay(1000)

                    withContext(Dispatchers.Main) {
                        dialog.dismiss()
                        ToastUtils(this@EyeListActivity).showMessage("그룹명 변경에 성공했습니다")
                        RefreshUtils(this@EyeListActivity).refreshActivity()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun showDeleteGroupDialog() {
        val itemName = categoryItem[categoryAdapter.selectedPosition].name
        val dialog = MakeDoubleDialog(this@EyeListActivity)
        val span = SpannableStringBuilder("${itemName}을 삭제하시겠습니까?")
        span.setSpan(ForegroundColorSpan(getColor(R.color.main_blue_color)),0,itemName.length+1,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val make = dialog.make(span, "삭제","취소", android.R.color.holo_red_light)

        make.first.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                db.deleteFromSerialWithCoroutine(itemName)
                delay(1000)

                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    ToastUtils(this@EyeListActivity).showMessage("그룹 삭제에 성공했습니다")
                    RefreshUtils(this@EyeListActivity).refreshActivity()
                }
            }
        }
    }

    private fun showAddGroupDialog() {
        val groupView: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_ae_add_group, null)
        val dialog = ShowDialogClass(this)
            .setBackPressed(groupView.findViewById(R.id.addGroupBack))
        val aliasEt = groupView.findViewById<EditText>(R.id.addGroupEt)
        val addBtn = groupView.findViewById<AppCompatButton>(R.id.addGroupAddBtn)

        val etText = "그룹${categoryItem.size}"
        aliasEt.setText(etText)

        val rv = groupView.findViewById<RecyclerView>(R.id.addGroupRv)
        rv.adapter = groupAdapter

        addBtn.setOnClickListener {
            if (groupAdapter.getCheckedCount() == 0) {
                val emptyCategoryDialog = MakeDoubleDialog(this)
                emptyCategoryDialog.make(getString(R.string.create_empty_category), getString(R.string.yes), getString(
                    R.string.no), R.color.ae_good_main)
                    .apply {
                        first.setOnClickListener {
                            addCategoryItem(aliasEt.text.toString(), null)
                            categoryAdapter.notifyDataSetChanged()
                            emptyCategoryDialog.builder.dismiss()
                        }
                        second.setOnClickListener {
                            emptyCategoryDialog.builder.dismiss()
                        }
                    }
            } else {
                checkedArray.clear()
                groupList.forEachIndexed { index, data ->
                    if (groupAdapter.getChecked(index)) {
                        checkedArray.add(data.device)
                    }
                }
                addGroupIntoDB(EyeDataModel.Category(aliasEt.text.toString(), checkedArray))
                addCategoryItem(aliasEt.text.toString(), checkedArray)
                categoryAdapter.notifyDataSetChanged()
                dialog.dismiss()
            }
        }

        addBtn.animation =
            AnimationUtils.loadAnimation(this, R.anim.trans_bottom_to_top_add_group)
        rv.animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_group_add)
        groupList.clear()
        allDevicesList.forEachIndexed { i, d ->
            if (i != allDevicesList.lastIndex) {
                addGroupList(EyeDataModel.Group(false, d))
            }
        }

        dialog.show(groupView, true, null)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addGroupList(item: EyeDataModel.Group) {
        groupList.add(item)
        groupAdapter.notifyDataSetChanged()
    }

    private fun addGroupIntoDB(item: EyeDataModel.Category) {
        CoroutineScope(Dispatchers.IO).launch {
            db.insertGroupWithCoroutine(EyeGroupEntity(item.name, item.device))
        }
    }

    private suspend fun getAllGroup(): List<EyeGroupEntity> {
        return db.findAll()
    }

    private fun addListItem(
        isMaster: Boolean,
        sort: String?,
        alias: String?,
        serial: String?,
        detail: EyeDataModel.DeviceDetail?
    ) {
        serial?.let {
            val item =
                EyeDataModel.Device(
                    isMaster,
                    sort,
                    SharedPreferenceManager(this@EyeListActivity).getString(SpDao.userEmail),
                    alias,
                    serial,
                    detail)

            deviceListItem.add(item)
        }
    }

    private fun addCategoryItem(name: String, device: MutableList<EyeDataModel.Device>?) {
        device?.let { devices ->
            val item = EyeDataModel.Category(name, devices)
            categoryItem.add(item)
        }
    }

    private fun addLastAddItem() {
        addListItem(false, null, "", "",
            EyeDataModel.DeviceDetail(null,false,false))
    }

    private fun loadDeviceList() {
        deviceListViewModel.loadDataResult()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun applyDeviceList() {
        try {
            if (!deviceListViewModel.fetchData().hasObservers()) {
                deviceListViewModel.fetchData().observe(this) { result ->
                    result?.let { list ->
                        when (list) {
                            // 통신 성공
                            is BaseRepository.ApiState.Success -> {
                                deviceListItem.clear()
                                hidePb()
                                list.data?.let { pList ->
                                    pList.forEachIndexed { index, device ->
                                        TimberUtil().d("eyetest", "device list is $device")
                                        val detail = device.detail?.let { pDetail ->
                                            EyeDataModel.DeviceDetail(pDetail.ssid, pDetail.report, pDetail.power)
                                        } ?:  EyeDataModel.DeviceDetail("",false,false)

                                        addListItem(device.isMaster, device.sort, device.alias, device.serial, detail)

                                        deviceListAdapter.notifyDataSetChanged()

                                        if (index == pList.lastIndex) {
                                            addLastAddItem()
                                            TimberUtil().d("eyetest","inser last index $index")
                                            deviceListAdapter.notifyDataSetChanged()
                                        }
                                    }
                                }
                            }

                            // 통신 실패
                            is BaseRepository.ApiState.Error -> {
                                hidePb()
                                TimberUtil().e("eyetest", result.toString())
                            }

                            // 통신 중
                            is BaseRepository.ApiState.Loading -> showPb()
                        }
                    }
                }
            }
        } catch(e: IOException) {
            TimberUtil().e("eyetest", "장치를 불러오는데 실패했습니다")
        }
    }

    private fun showPb() {
        binding.asListPb.speed = 1.2f
        binding.asListPb.bringToFront()
        binding.asListPb.visibility = View.VISIBLE
    }

    private fun hidePb() {
        binding.asListPb.visibility = View.GONE
    }
}
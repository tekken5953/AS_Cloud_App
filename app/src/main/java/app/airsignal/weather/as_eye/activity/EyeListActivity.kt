package app.airsignal.weather.as_eye.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.adapter.AddGroupAdapter
import app.airsignal.weather.as_eye.adapter.EyeCategoryAdapter
import app.airsignal.weather.as_eye.adapter.EyeDeviceAdapter
import app.airsignal.weather.as_eye.customview.EyeGroupSelectorView
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.databinding.ActivityEyeListBinding
import app.airsignal.weather.db.room.database.GroupDataBase
import app.airsignal.weather.db.room.model.EyeGroupEntity
import app.airsignal.weather.db.room.repository.EyeGroupRepository
import app.airsignal.weather.firebase.fcm.SubFCM
import app.airsignal.weather.util.OnAdapterItemClick
import app.airsignal.weather.util.RefreshUtils
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.util.ToastUtils
import app.airsignal.weather.view.custom_view.MakeDoubleDialog
import app.airsignal.weather.view.custom_view.ShowDialogClass
import app.airsignal.weather.view.custom_view.SnackBarUtils
import kotlinx.coroutines.*
import java.time.LocalDateTime

class EyeListActivity : BaseEyeActivity<ActivityEyeListBinding>() {
    override val resID: Int get() = R.layout.activity_eye_list

    companion object {
        const val ENTIRE_GROUP = "전체"
    }

    private val deviceListItem = ArrayList<EyeDataModel.Device>()
    private val deviceListAdapter by lazy { EyeDeviceAdapter(this, deviceListItem) }
    private val allDevicesList = ArrayList<EyeDataModel.Device>()
    private val categoryItem = ArrayList<EyeDataModel.Category>()
    private val categoryAdapter by lazy { EyeCategoryAdapter(this, categoryItem) }
    private val groupList = ArrayList<EyeDataModel.Group>()
    private val groupAdapter by lazy { AddGroupAdapter(this, groupList) }
    private val checkedArray = ArrayList<EyeDataModel.Device>()
    private val db by lazy { GroupDataBase.getGroupInstance(this).groupRepository() }


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        binding.aeListDeviceRv.adapter = deviceListAdapter
        binding.aeListCategoryRv.adapter = categoryAdapter

        addCategoryItem(ENTIRE_GROUP, deviceListItem)
        categoryAdapter.notifyDataSetChanged()

        categoryAdapter.setOnItemClickListener(object : OnAdapterItemClick.OnAdapterItemClick {
            override fun onItemClick(v: View, position: Int) {
                deviceListItem.clear()
                categoryAdapter.changeSelected(position)
                if (position == 0) {
                    deviceListItem.addAll(allDevicesList)
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val group = db.findByCategoryName(categoryItem[position].name)
                        group.device.forEach {
                            addListItem(
                                it.isMaster,
                                it.alias,
                                it.serial.serial,
                                it.serial.report,
                                it.serial.power
                            )
                        }
                    }
                    categoryAdapter.changeSelected(position)
                }
                deviceListAdapter.notifyDataSetChanged()
            }
        })

        addListItem(true, "사무실", "AOA0000001F539", isReport = true, isPower = true)
        addListItem(false, "1층", "AOA0000001F538", isReport = true, isPower = true)
        addListItem(true, "2층", "AOA0000001F537", isReport = false, isPower = false)
        addListItem(false, "3층", "AOA0000001F536", isReport = false, isPower = false)
        addLastAddItem()

        SubFCM().subTopic("AOA0000001F539")

        allDevicesList.addAll(deviceListItem)
        deviceListAdapter.notifyDataSetChanged()

        CoroutineScope(Dispatchers.IO).launch {
            val allGroups = getAllGroup()
            allGroups.forEach {
                //TODO 디바이스가 현재 등록된 상태인지 검사하여 분기
                addCategoryItem(it.name, it.device)
                TimberUtil().d("testtest", "add Category ${it.name}")
            }
        }

        deviceListAdapter.setOnItemClickListener(object : OnAdapterItemClick.OnAdapterItemClick {
            override fun onItemClick(v: View, position: Int) {
                deviceListItem[position].serial.serial?.let {
                    val intent = Intent(this@EyeListActivity, EyeDetailActivity::class.java)
                    intent.apply {
                        putExtra("name", deviceListItem[position].alias)
                        putExtra("serial", it)
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
        val make = dialog.make("${itemName}을 삭제하시겠습니까?",
        "삭제","취소", android.R.color.holo_red_light)

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

    private fun getCurrentLocal(): LocalDateTime {
        return LocalDateTime.now()
    }

    private fun addListItem(
        isMaster: Boolean,
        alias: String,
        serial: String?,
        isReport: Boolean,
        isPower: Boolean
    ) {
        val serialObject = EyeDataModel.Serial(getCurrentLocal(), serial , isReport, isPower)
        val item =
            EyeDataModel.Device(getCurrentLocal(), isMaster, "test@email.com", alias, serialObject)
        deviceListItem.add(item)
    }

    private fun addCategoryItem(name: String, device: MutableList<EyeDataModel.Device>?) {
        device?.let { devices ->
            val item = EyeDataModel.Category(name, devices)
            categoryItem.add(item)
        }
    }

    private fun addLastAddItem() {
        addListItem(false, "", null, isReport = false, isPower = true)
    }
}
package app.airsignal.weather.as_eye.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.adapter.AddGroupAdapter
import app.airsignal.weather.as_eye.adapter.EyeCategoryAdapter
import app.airsignal.weather.as_eye.adapter.EyeDeviceAdapter
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.databinding.ActivityEyeListBinding
import app.airsignal.weather.db.room.database.GroupDataBase
import app.airsignal.weather.db.room.model.EyeGroupEntity
import app.airsignal.weather.firebase.fcm.SubFCM
import app.airsignal.weather.util.OnAdapterItemClick
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.view.custom_view.MakeDoubleDialog
import app.airsignal.weather.view.custom_view.ShowDialogClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        addListItem(false, "", null, isReport = false, isPower = true)

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
                if (position != deviceListItem.lastIndex) {
                    val intent = Intent(this@EyeListActivity, EyeDetailActivity::class.java)
                    intent.apply {
                        putExtra("name", deviceListItem[position].alias)
                        putExtra("serial", deviceListItem[position].serial.serial)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@EyeListActivity, AddEyeDeviceActivity::class.java)
                    startActivity(intent)
                }
            }
        })

        binding.aeListBack.setOnClickListener { finish() }

        binding.aeListCategoryAdd.setOnClickListener {
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
        val serialObject = EyeDataModel.Serial(getCurrentLocal(), serial ?: "", isReport, isPower)
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
}
package app.airsignal.weather.as_eye.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.databinding.ActivityEyeListBinding
import app.airsignal.weather.as_eye.adapter.AddGroupAdapter
import app.airsignal.weather.as_eye.adapter.EyeCategoryAdapter
import app.airsignal.weather.as_eye.adapter.EyeDeviceAdapter
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.util.OnAdapterItemClick
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.view.custom_view.MakeDoubleDialog
import app.airsignal.weather.view.custom_view.ShowDialogClass
import java.time.LocalDateTime

class EyeListActivity : AppCompatActivity() {

    companion object {
        const val ENTIRE_GROUP = "전체"
    }

    private lateinit var binding: ActivityEyeListBinding

    private val deviceListItem = ArrayList<EyeDataModel.Device>()
    private val deviceListAdapter by lazy { EyeDeviceAdapter(this, deviceListItem) }
    private val allDevicesList = ArrayList<EyeDataModel.Device>()
    private val categoryItem = ArrayList<EyeDataModel.Category>()
    private val categoryAdapter by lazy { EyeCategoryAdapter(this, categoryItem) }
    private val groupList = ArrayList<EyeDataModel.Group>()
    private val groupAdapter by lazy { AddGroupAdapter(this, groupList) }
    private val checkedArray = ArrayList<EyeDataModel.Device>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_eye_list)

        binding.aeListDeviceRv.adapter = deviceListAdapter
        binding.aeListCategoryRv.adapter = categoryAdapter

        addCategoryItem("전체", deviceListItem)
        categoryAdapter.notifyDataSetChanged()

        categoryAdapter.setOnItemClickListener(object : OnAdapterItemClick.OnAdapterItemClick {
            override fun onItemClick(v: View, position: Int) {
                deviceListItem.clear()
                categoryAdapter.changeSelected(position)
                if (position == 0) {
                    deviceListItem.addAll(allDevicesList)
                } else {
                    checkedArray.forEach {
                        addListItem(
                            it.isMaster,
                            it.alias,
                            it.serial.serial,
                            it.serial.report,
                            it.serial.power
                        )
                    }
                    categoryAdapter.changeSelected(position)
                }
                deviceListAdapter.notifyDataSetChanged()
            }
        })

        addListItem(true, "사무실", "AS-442421", isReport = true, isPower = true)
        addListItem(false, "1층", "AS-123456", isReport = true, isPower = true)
        addListItem(true, "2층", "AS-345678", isReport = false, isPower = false)
        addListItem(false, "3층", "AS-678908", isReport = false, isPower = false)
        addListItem(false, "", null, isReport = false, isPower = true)

        allDevicesList.addAll(deviceListItem)
        deviceListAdapter.notifyDataSetChanged()

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
                    emptyCategoryDialog.make("빈 그룹을 생성하시겠습니까?", "예", "아니오", R.color.ae_good_main)
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
                    addCategoryItem(aliasEt.text.toString(), checkedArray)
                    categoryAdapter.notifyDataSetChanged()
                    dialog.dismiss()
                }
            }

            addBtn.animation = AnimationUtils.loadAnimation(this, R.anim.trans_bottom_to_top_add_group)
            rv.animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_group_add)
            groupList.clear()
            allDevicesList.forEachIndexed { i, d ->
                if (i != allDevicesList.lastIndex) {
                    addGroupList(EyeDataModel.Group(false, d))
                }
            }

            dialog.show(groupView, true)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addGroupList(item: EyeDataModel.Group) {
        groupList.add(item)
        groupAdapter.notifyDataSetChanged()
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

    private fun addCategoryItem(name: String, device: List<EyeDataModel.Device>?) {
        val item = EyeDataModel.Category(name, device)
        categoryItem.add(item)
    }
}
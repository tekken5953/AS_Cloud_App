package app.airsignal.weather.view.aseye.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import app.airsignal.weather.R
import app.airsignal.weather.adapter.OnAdapterItemClick
import app.airsignal.weather.databinding.ActivityEyeListBinding
import app.airsignal.weather.view.aseye.dao.EyeDataModel
import app.airsignal.weather.view.aseye.adapter.EyeCategoryAdapter
import app.airsignal.weather.view.aseye.adapter.EyeDeviceAdapter

class EyeListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEyeListBinding

    private val listItem = ArrayList<EyeDataModel.DeviceModel>()
    private val listAdapter by lazy { EyeDeviceAdapter(this,listItem) }
    private val categoryItem = ArrayList<String>()
    private val categoryAdapter by lazy { EyeCategoryAdapter(this,categoryItem) }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_eye_list)

        binding.aeListDeviceRv.adapter = listAdapter
        binding.aeListCategoryRv.adapter = categoryAdapter

        addCategoryItem("전체")
        addCategoryItem("목록1")
        addCategoryItem("목록2")
        categoryAdapter.notifyDataSetChanged()

        categoryAdapter.setOnItemClickListener(object : OnAdapterItemClick.OnAdapterItemClick {
            override fun onItemClick(v: View, position: Int) {
                listItem.clear()
                categoryAdapter.changeSelected(position)

                when(position) {
                    0 -> {
                        addListItem("사무실","AS-442421",true,2,false)
                        addListItem("1층","AS-123456",true,1,false)
                        addListItem("2층","AS-345678",false,null,false)
                        addListItem("3층","AS-678908",false,null,false)
                        addListItem("","",true,null,true)
                    }
                    1 -> {
                        addListItem("1층","AS-123456",true,1,false)
                        addListItem("2층","AS-345678",false,null,false)
                        addListItem("3층","AS-678908",false,null,false)
                        addListItem("","",true,null,true)
                    }

                    2 -> {
                        addListItem("사무실","AS-442421",true,2,false)
                        addListItem("1층","AS-123456",true,1,false)
                        addListItem("","",true,null,true)
                    }
                }
                listAdapter.notifyDataSetChanged()
            }
        })

        addListItem("사무실","AS-442421",true,2, false)
        addListItem("1층","AS-123456",true,1,false)
        addListItem("2층","AS-345678",false,null,false)
        addListItem("3층","AS-678908",false,null,false)
        addListItem("","",true,null,true)
        listAdapter.notifyDataSetChanged()

        listAdapter.setOnItemClickListener(object : OnAdapterItemClick.OnAdapterItemClick{
            override fun onItemClick(v: View, position: Int) {
                if (position != listItem.lastIndex) {
                    val intent = Intent(this@EyeListActivity, EyeDetailActivity::class.java)
                    intent.apply {
                        putExtra("name",listItem[position].name)
                        putExtra("serial",listItem[position].serial)
                    }
                    startActivity(intent)
                } else {

                }
            }
        })

        binding.aeListBack.setOnClickListener { finish() }
    }

    private fun addListItem(name: String, serial: String, power: Boolean, report: Int?, isAdd: Boolean) {
        val item = EyeDataModel.DeviceModel(name,serial,power,report,isAdd)
        listItem.add(item)
    }

    private fun addCategoryItem(name: String) {
        categoryItem.add(name)
    }
}
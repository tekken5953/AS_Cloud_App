package app.airsignal.weather.as_eye.activity

import android.animation.ValueAnimator
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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.adapter.AddInGroupDeviceAdapter
import app.airsignal.weather.as_eye.adapter.EyeCategoryAdapter
import app.airsignal.weather.as_eye.adapter.EyeDeviceAdapter
import app.airsignal.weather.as_eye.adapter.TutorialViewPagerAdapter
import app.airsignal.weather.as_eye.customview.EyeGroupSelectorView
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.databinding.ActivityEyeListBinding
import app.airsignal.weather.db.SharedPreferenceManager
import app.airsignal.weather.db.room.database.GroupDataBase
import app.airsignal.weather.db.room.model.EyeGroupEntity
import app.airsignal.weather.db.room.repository.EyeGroupRepository
import app.airsignal.weather.db.sp.SpDao
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.util.OnAdapterItemClick
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.util.ToastUtils
import app.airsignal.weather.view.custom_view.MakeDoubleDialog
import app.airsignal.weather.view.custom_view.ShowDialogClass
import app.airsignal.weather.viewmodel.GetEyeDeviceListViewModel
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException

@SuppressLint("NotifyDataSetChanged")
class EyeListActivity : BaseEyeActivity<ActivityEyeListBinding>() {
    override val resID: Int get() = R.layout.activity_eye_list

    companion object { const val ENTIRE_GROUP = "전체" }

    private val deviceListItem = ArrayList<EyeDataModel.Device>()
    private val deviceListAdapter by lazy { EyeDeviceAdapter(this, deviceListItem) }
    private val allDevicesList = ArrayList<EyeDataModel.Device>()
    private val categoryItem = ArrayList<EyeDataModel.Category>()
    private val categoryAdapter by lazy { EyeCategoryAdapter(this, categoryItem) }
    private val groupList = ArrayList<EyeDataModel.Group>()
    private val groupAdapter by lazy { AddInGroupDeviceAdapter(this, groupList) }
    private val checkedArray = ArrayList<EyeDataModel.Device>()
    private val db by lazy { GroupDataBase.getGroupInstance(this).groupRepository() }

    private val deviceListViewModel by viewModel<GetEyeDeviceListViewModel>()
    private val listLiveData by lazy {deviceListViewModel.fetchData()}

    private lateinit var indicators: Array<ImageView>

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        TimberUtil().d("appLinkTest", "onNewIntent ${intent?.data}")
    }

    override fun onStart() {
        super.onStart()
        loadDeviceList()
        if (!SharedPreferenceManager(this).getBoolean("eye_tutorial_skip", false)) {
            createTutorial()
        }
    }

    override fun onRestart() {
        super.onRestart()
        loadDeviceList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        applyDeviceList()

        binding.aeListDeviceRv.adapter = deviceListAdapter
        binding.aeListCategoryRv.adapter = categoryAdapter

        addCategoryFirstItem()

        categoryAdapter.setOnItemClickListener(object : OnAdapterItemClick.OnAdapterItemClick {
            override fun onItemClick(v: View, position: Int) {
                deviceListItem.clear()
                categoryAdapter.changeSelected(position)
                if (position == 0) {
                    loadDeviceList()
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        @SuppressLint("SuspiciousIndentation")
                        val group = db.findByCategoryName(categoryItem[position].name)
                        allDevicesList.forEachIndexed { index, device ->
                            if (group.device.contains(device.serial)) {
                                deviceListItem.add(device)
                                deviceListAdapter.notifyItemInserted(index)
                            }
                        }
                    }
                }
            }
        })

        readCategoryItems()

        deviceListAdapter.setOnItemClickListener(object : OnAdapterItemClick.OnAdapterItemClick {
            override fun onItemClick(v: View, position: Int) {
                val mSerial = deviceListItem[position].serial
                if (mSerial != "") {
                    val intent = Intent(this@EyeListActivity, EyeDetailActivity::class.java)
                    intent.apply {
                        putExtra("alias", deviceListItem[position].alias)
                        putExtra("serial", mSerial)
                        deviceListItem[position].detail?.let { pDetail ->
                            putExtra("ssid", pDetail.ssid)
                        }
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

    private fun addCategoryFirstItem() {
        addCategoryItem(ENTIRE_GROUP, deviceListItem.map {it.serial}.toMutableList())
    }

    private fun readCategoryItems() {
        categoryItem.clear()
        addCategoryFirstItem()

        CoroutineScope(Dispatchers.IO).launch {
            val allGroups = getAllGroup()
            withContext(Dispatchers.Main) {
                allGroups.forEach {
                    addCategoryItem(it.name, it.device)
                }
            }
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
                showCategoryPb()
                CoroutineScope(Dispatchers.IO).launch {
                    EyeGroupRepository(this@EyeListActivity)
                        .update(categoryItem[categoryAdapter.selectedPosition].name, aliasEt.text.toString())
                    withContext(Dispatchers.Main) {
                        dialog.dismiss()
                        delay(1000)
                        readCategoryItems()
                        hideCategoryPb()
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
        span.setSpan(ForegroundColorSpan(getColor(R.color.main_blue_color)),0,itemName.length+1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val make = dialog.make(span, "삭제","취소", android.R.color.holo_red_light)

        make.first.setOnClickListener {
            showCategoryPb()
            CoroutineScope(Dispatchers.IO).launch {
                db.deleteFromSerialWithCoroutine(itemName)
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    delay(1500)
                    returnPrevCategory()
                    hideCategoryPb()
                }
            }
        }
    }

    private fun showCategoryPb() {
        binding.aeListLoading.bringToFront()
        binding.aeListLoading.visibility = View.VISIBLE
        binding.aeListLoading.playAnimation()
    }

    private fun hideCategoryPb() {
        binding.aeListLoading.visibility = View.GONE
    }

    private fun returnPrevCategory() {
        categoryAdapter.changeSelected(categoryAdapter.selectedPosition - 1)
        readCategoryItems()
        loadDeviceList()
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
                addGroupIntoDB(EyeDataModel.Category(aliasEt.text.toString(), checkedArray.map {it.serial}.toMutableList()))
                addCategoryItem(aliasEt.text.toString(), checkedArray.map {it.serial}.toMutableList())
                dialog.dismiss()
            }
        }

        addBtn.animation =
            AnimationUtils.loadAnimation(this, R.anim.trans_bottom_to_top_add_group)
        rv.animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_group_add)
        groupList.clear()
        allDevicesList.forEachIndexed { index, d ->
            addGroupList(EyeDataModel.Group(false, d))
            groupAdapter.notifyItemInserted(index)
        }

        dialog.show(groupView, true, null)
    }

    private fun addGroupList(item: EyeDataModel.Group) {
        groupList.add(item)
    }

    private fun addGroupIntoDB(item: EyeDataModel.Category) {
        CoroutineScope(Dispatchers.IO).launch {
            db.insertGroupWithCoroutine(EyeGroupEntity(item.name, item.device))
        }
    }

    private suspend fun getAllGroup(): List<EyeGroupEntity> { return db.findAll() }

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
                    isMaster, sort, SharedPreferenceManager(this@EyeListActivity)
                        .getString(SpDao.userEmail), alias, serial, detail)

            deviceListItem.add(item)
        }
    }

    private fun addCategoryItem(name: String, device: MutableList<String?>?) {
        device?.let { devices ->
            val item = EyeDataModel.Category(name, devices)
            categoryItem.add(item)
            categoryAdapter.notifyDataSetChanged()
        }
    }

    private fun addLastAddItem() {
        addListItem(false, "", "", "",
            EyeDataModel.DeviceDetail(null, report = false, power = true))
    }

    private fun loadDeviceList() {
        if (listLiveData.hasActiveObservers()) {
            deviceListViewModel.loadDataResult()
        }
        else {
            applyDeviceList()
            deviceListViewModel.loadDataResult()
        }
    }

    private fun applyDeviceList() {
        try {
            if (!listLiveData.hasObservers()) {
                listLiveData.observe(this) { result ->
                    result?.let { list ->
                        when (list) {
                            // 통신 성공
                            is BaseRepository.ApiState.Success -> {
                                deviceListItem.clear()
                                allDevicesList.clear()

                                list.data?.let { pList ->
                                    pList.forEach{ device ->
                                        val detail = device.detail?.let { pDetail ->
                                            EyeDataModel.DeviceDetail(pDetail.ssid, pDetail.report, pDetail.power)
                                        } ?:  EyeDataModel.DeviceDetail("", report = false, power = false)

                                        addListItem(device.isMaster, device.sort, device.alias, device.serial, detail)
                                        allDevicesList.add(device)
                                    }
                                }

                                addLastAddItem()
                                deviceListAdapter.notifyItemRangeChanged(0, deviceListItem.size)
                            }

                            // 통신 실패
                            is BaseRepository.ApiState.Error -> {
                                TimberUtil().e("eyetest", result.toString())
                                ToastUtils(this).showMessage("장치를 불러오는데 실패했습니다")
                            }

                            // 통신 중
                            is BaseRepository.ApiState.Loading -> {}


                            else -> {}
                        }
                    }
                }
            }
        } catch(e: IOException) {
            TimberUtil().e("eyetest", e.stackTraceToString())
            ToastUtils(this).showMessage("장치를 불러오는데 실패했습니다")
        }
    }

    private fun createTutorial() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_eye_tutorial, binding.aeListRoot)
        val dialog = ShowDialogClass(this)
        val cancel = view.findViewById<TextView>(R.id.eyeTutorialCancel)
        val viewPager = view.findViewById<ViewPager2>(R.id.eyeTutorialViewPager)
        val indicatorContainer = view.findViewById<LinearLayout>(R.id.eyeTutorialIndicator)

        cancel.bringToFront()

        cancel.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                SharedPreferenceManager(this@EyeListActivity).setBoolean("eye_tutorial_skip",true)

                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    recreate()
                }
            }
        }

        dialog.show(view,false, ShowDialogClass.DialogTransition.BOTTOM_TO_TOP)

        val viewPagerList = ArrayList<Int>()
        viewPagerList.run {
            add(R.drawable.test_vp_img)
            add(R.drawable.test_vp_img2)
            add(R.drawable.test_vp_img3)
            add(R.drawable.test_vp_img4)
        }

        createViewPager(viewPager, indicatorContainer, viewPagerList)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (viewPagerList.isNotEmpty() && viewPagerList.size > 1) {
                    updateIndicators(position)
                    viewPager.requestLayout()
                }

                if (position == viewPagerList.lastIndex)
                    cancel.text = "닫기" else cancel.text = "SKIP"
            }
        })
    }

    private fun createIndicator(size: Int, container: LinearLayout, viewPager: ViewPager2) {
        indicators = Array(size) {
            val indicatorView = ImageView(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(10, 0, 10, 0)
            indicatorView.layoutParams = params
            indicatorView.setImageResource(R.drawable.indicator_empty) // 선택되지 않은 원 이미지
            indicatorView.scaleType = ImageView.ScaleType.FIT_XY
            container.addView(indicatorView)
            indicatorView
        }
        updateIndicators(viewPager.currentItem)
    }

    private fun updateIndicators(position: Int) {
        for (i in indicators.indices) {
            val animator = ValueAnimator.ofInt(
                indicators[i].layoutParams.width,
                if (i == position) 120 else 35
            )
            animator.addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Int
                val params = indicators[i].layoutParams
                params.width = value
                indicators[i].layoutParams = params
            }

            // 애니메이션 기간을 설정합니다. (밀리초 단위)
            animator.duration = 300

            // 애니메이션을 시작합니다.
            animator.start()
        }
    }

    private fun createViewPager(vp: ViewPager2,
                                indicatorContainer: LinearLayout,
                                vpList: ArrayList<Int>) {
        val vpAdapter = TutorialViewPagerAdapter(this, vpList, vp)
        vp.apply {
            adapter = vpAdapter
            isClickable = false
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 4
        }

        if (vpList.isNotEmpty() && vpList.size > 1) {
            indicatorContainer.removeAllViews()
            createIndicator(vpList.size,indicatorContainer,vp)
        }
    }
}
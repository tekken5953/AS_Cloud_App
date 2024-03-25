package app.airsignal.weather.as_eye.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
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
import app.airsignal.weather.as_eye.adapter.*
import app.airsignal.weather.as_eye.customview.EyeGroupSelectorView
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.databinding.ActivityEyeListBinding
import app.airsignal.weather.db.SharedPreferenceManager
import app.airsignal.weather.db.room.database.GroupDataBase
import app.airsignal.weather.db.room.model.EyeGroupEntity
import app.airsignal.weather.db.room.repository.EyeGroupRepository
import app.airsignal.weather.db.sp.SpDao
import app.airsignal.weather.db.sp.SpDao.TUTORIAL_SKIP
import app.airsignal.weather.firebase.fcm.SubFCM
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.util.OnSingleClickListener
import app.airsignal.weather.util.ToastUtils
import app.airsignal.weather.view.custom_view.MakeDoubleDialog
import app.airsignal.weather.view.custom_view.ShowDialogClass
import app.airsignal.weather.view.dialog.IndicatorView
import app.airsignal.weather.viewmodel.GetEyeDeviceListViewModel
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EyeListActivity : BaseEyeActivity<ActivityEyeListBinding>() {
    override val resID: Int get() = R.layout.activity_eye_list

    companion object { const val ENTIRE_GROUP = "전체" }

    private val allDeviceList = ArrayList<EyeDataModel.Device>()
    private val groupDeviceList = ArrayList<EyeDataModel.Device>()
    private val deviceListAdapter by lazy { EyeDeviceAdapter(this, groupDeviceList) }
    private val categoryItem = ArrayList<EyeDataModel.Category>()
    private val categoryAdapter by lazy { EyeCategoryAdapter(this, categoryItem) }
    private val groupList = ArrayList<EyeDataModel.Group>()
    private val groupAdapter by lazy { AddInGroupDeviceAdapter(this, groupList) }
    private val checkedArray = ArrayList<EyeDataModel.Device>()
    private val db by lazy { GroupDataBase.getGroupInstance(this).groupRepository() }

    private val deviceListViewModel by viewModel<GetEyeDeviceListViewModel>()
    private val listLiveData by lazy {deviceListViewModel.fetchData()}


    private val viewPagerList = ArrayList<Int>()
    private val vpAdapter by lazy {TutorialViewPagerAdapter(this, viewPagerList)}
    private val indicatorView by lazy { IndicatorView(this,viewPagerList.size) }

    private val sp by lazy {SharedPreferenceManager(this)}

    private var isLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()

        binding.aeListDeviceRv.adapter = deviceListAdapter
        binding.aeListCategoryRv.adapter = categoryAdapter

        initViewModel()

        categoryAdapter.setOnItemClickListener(object : OnAdapterItemSingleClick() {
            override fun onSingleClick(v: View?, position: Int) {
                groupDeviceList.clear()
                categoryAdapter.changeSelected(position)
                if (position == 0) {
                    if (allDeviceList.isNotEmpty()) {
                        groupDeviceList.addAll(allDeviceList)
                        deviceListAdapter.notifyItemRangeChanged(0, allDeviceList.size)
                        addLastAddItem()
                    } else { loadDeviceList() }
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        @SuppressLint("SuspiciousIndentation")
                        val group = db.findByCategoryName(categoryItem[position].name)
                        allDeviceList.forEachIndexed { index, device ->
                            if (group.device.contains(device.serial)) {
                                groupDeviceList.add(device)
                                deviceListAdapter.notifyItemInserted(index)
                            }
                        }
                    }
                }
            }
        })

        readCategoryItems()

        deviceListAdapter.setOnItemClickListener(object : OnAdapterItemSingleClick() {
            override fun onSingleClick(v: View?, position: Int) {
                val mSerial = groupDeviceList[position].serial
                if (mSerial != "") {
                    moveToDetail(position)
                } else {
                    val intent = Intent(this@EyeListActivity, AddEyeDeviceActivity::class.java)
                    startActivity(intent)
                }
            }
        })

        binding.aeListBack.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) { finish() } })

        binding.aeListCategorySelector.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
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
        })

        loadDeviceList()
    }

    private fun moveToDetail(position: Int) {
        val mSerial = groupDeviceList[position].serial
        if (mSerial != "") {
            val intent = Intent(this@EyeListActivity, EyeDetailActivity::class.java)
            intent.apply {
                putExtra("alias", groupDeviceList[position].alias)
                putExtra("serial", mSerial)
                putExtra("is_master", groupDeviceList[position].isMaster)
                putExtra("model_name", groupDeviceList[position].sort)
                putExtra(
                    "create_at", groupDeviceList[position].created_at?.format(
                        DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm")
                    )
                )
                groupDeviceList[position].detail?.let { pDetail ->
                    putExtra("ssid", pDetail.ssid)
                }
            }
            startActivity(intent)
            finish()
        }
    }

    private fun addCategoryFirstItem() {
        addCategoryItem(ENTIRE_GROUP, groupDeviceList.map {it.serial}.toMutableList())
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

    private fun destroyObserver() {
        deviceListViewModel.cancelJob()
        listLiveData?.removeObservers(this)
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
        val dialog = ShowDialogClass(this, true)
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
        groupDeviceList.forEachIndexed { index, d ->
            if (d.serial != "") {
                addGroupList(EyeDataModel.Group(false, d))
                groupAdapter.notifyItemInserted(index)
            }
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
        createdAt: LocalDateTime?,
        isMaster: Boolean,
        sort: String?,
        alias: String?,
        serial: String?,
        detail: EyeDataModel.DeviceDetail?
    ) {
        serial?.let {
            val item =
                EyeDataModel.Device(
                    createdAt, isMaster, sort, sp.getString(SpDao.userEmail), alias, serial, detail)

            groupDeviceList.add(item)
        }
    }

    private fun addCategoryItem(name: String, device: MutableList<String?>?) {
        device?.let { devices ->
            val item = EyeDataModel.Category(name, devices)
            categoryItem.add(item)
            categoryAdapter.notifyItemRangeChanged(0, device.size)
        }
    }

    private fun addLastAddItem() {
        addListItem(null,false, "", "", "",
            EyeDataModel.DeviceDetail(null, report = false, power = true))

        deviceListAdapter.notifyItemInserted(groupDeviceList.lastIndex)
    }

    private fun initViewModel() {
        if (listLiveData?.hasActiveObservers() == true) { destroyObserver() }
        applyDeviceList()
    }

    private fun loadDeviceList() {
        deviceListViewModel.loadDataResult()
    }

    private fun applyDeviceList() {
        try {
            if (!isLoaded) {
                isLoaded = true
                listLiveData?.observe(this) { result ->
                    result?.let { list ->
                        when (list) {
                            // 통신 성공
                            is BaseRepository.ApiState.Success -> {
                                hideCategoryPb()
                                list.data?.let { pList ->
                                    binding.aeListDeviceRv.removeAllViews()
                                    groupDeviceList.clear()
                                    allDeviceList.clear()
                                    pList.forEachIndexed { index,  device ->
                                        groupDeviceList.add(device)
                                        allDeviceList.add(device)

                                        if (index == pList.lastIndex) {
                                            addLastAddItem()
                                            deviceListAdapter.notifyItemRangeChanged(0, groupDeviceList.size)

                                            if (!sp.getBoolean(TUTORIAL_SKIP, false)) createTutorial()
                                        }

                                        isLoaded = false
                                    }
                                }
                            }

                            // 통신 실패
                            is BaseRepository.ApiState.Error -> {
                                hideCategoryPb()
                                ToastUtils(this@EyeListActivity).showMessage("장치를 불러오는데 실패했습니다")
                                isLoaded = false
                            }

                            // 통신 중
                            is BaseRepository.ApiState.Loading -> { showCategoryPb() }

                            else -> {}
                        }
                    }
                }
            }
        } catch(e: IOException) {
            e.stackTraceToString()
            ToastUtils(this).showMessage("장치를 불러오는데 실패했습니다")
        }
    }

    private fun createTutorial() {
        val view = LayoutInflater.from(this).inflate(R.layout.tutorial_dialog_eye, binding.aeListRoot)
        val dialog = ShowDialogClass(this,true)
        val cancel = view.findViewById<TextView>(R.id.eyeTutorialCancel)
        val viewPager = view.findViewById<ViewPager2>(R.id.eyeTutorialViewPager)
        val indicatorContainer = view.findViewById<LinearLayout>(R.id.eyeTutorialIndicator)
        val contents = view.findViewById<TextView>(R.id.eyeTutorialContents)

        contents.text = getGuideMsg(0)

        cancel.bringToFront()

        cancel.setOnClickListener {
            if (viewPager.currentItem == viewPagerList.lastIndex) {

                CoroutineScope(Dispatchers.IO).launch {
                    sp.setBoolean(TUTORIAL_SKIP, true)

                    //TODO 베타 테스트 끝나면 삭제
                    SubFCM().subTopic("AOA00000053638").subTopic("AOA0000002F479")

                    withContext(Dispatchers.Main) {
                        dialog.dismiss()
                        recreate()
                    }
                }
            } else {
                viewPager.currentItem = viewPagerList.lastIndex
            }
        }

        dialog.show(view,false, ShowDialogClass.DialogTransition.BOTTOM_TO_TOP)

        viewPagerList.addAll(arrayListOf(R.raw.ani_tuto_plus,R.raw.ani_tuto_group,R.raw.ani_tuto_alarm,R.raw.ani_tuto_danger,R.raw.ani_etc))

        createViewPager(viewPager, indicatorContainer)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (viewPagerList.isNotEmpty() && viewPagerList.size > 1) {
                    indicatorView.updateIndicators(position)
                    vpAdapter.pausePreviousLottie(position)
                    vpAdapter.playCurrentLottie(position)
                }

                if (position == viewPagerList.lastIndex)
                    cancel.text = getString(R.string.close) else cancel.text = "SKIP"

                contents.text = getGuideMsg(position)
            }
        })
    }

    private fun createViewPager(vp: ViewPager2,
                                indicatorContainer: LinearLayout) {
        vp.apply {
            adapter = vpAdapter
            isClickable = false
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 4
        }

        if (viewPagerList.isNotEmpty() && viewPagerList.size > 1) {
            indicatorContainer.removeAllViews()
            indicatorView.createIndicators(indicatorContainer,vp, ColorStateList.valueOf(getColor(R.color.main_black)))
        }
    }

    private fun getGuideMsg(index: Int): String {
        return when(index) {
            0 -> {"+ 버튼을 눌러 새로운 기기를 쉽게 등록할 수 있어요"}
            1 -> {"내가 보고싶은 기기의 그룹을 만들 수 있어요"}
            2 -> {"위험 데이터를 알림받고 싶으시다면\n설정 페이지에서 알림을 허용해주세요"}
            3 -> {"현재 위험 단계의 데이터가 있는 기기를 알려드려요"}
            4 -> {"그 밖의 AS-Eye의 다양한 데이터와\n통계 서비스를 경험해보세요"}
            else -> ""
        }
    }
}
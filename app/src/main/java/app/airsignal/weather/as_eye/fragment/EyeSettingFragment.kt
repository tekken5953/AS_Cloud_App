package app.airsignal.weather.as_eye.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.EyeDetailActivity
import app.airsignal.weather.as_eye.activity.EyeListActivity
import app.airsignal.weather.as_eye.adapter.EyeMembersAdapter
import app.airsignal.weather.as_eye.customview.EyeSettingView
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.dao.AdapterModel
import app.airsignal.weather.databinding.EyeSettingFragmentBinding
import app.airsignal.weather.db.SharedPreferenceManager
import app.airsignal.weather.db.room.repository.EyeGroupRepository
import app.airsignal.weather.db.sp.SpDao.userEmail
import app.airsignal.weather.firebase.fcm.EyeNotiBuilder
import app.airsignal.weather.network.retrofit.ApiModel
import app.airsignal.weather.network.retrofit.HttpClient
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.util.RefreshUtils
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.util.ToastUtils
import app.airsignal.weather.view.custom_view.MakeDoubleDialog
import app.airsignal.weather.view.custom_view.MakeSingleDialog
import app.airsignal.weather.view.custom_view.ShowDialogClass
import app.airsignal.weather.view.custom_view.SnackBarUtils
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import app.airsignal.weather.viewmodel.SetEyeDeviceAliasViewModel
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class EyeSettingFragment : Fragment() {
    private lateinit var mActivity: EyeDetailActivity
    private lateinit var binding: EyeSettingFragmentBinding

    private var isCanApi = false

    private val deviceAliasViewModel by viewModel<SetEyeDeviceAliasViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EyeDetailActivity) mActivity = context
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.eye_setting_fragment, container, false)

        binding.aeSettingName.setOnClickListener {
            val changeDeviceNameDialog = ShowDialogClass(mActivity,true)
            val changeDeviceNameView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_eye_change_device_name,binding.aeSettingViewParent,false)
            val backPress = changeDeviceNameView.findViewById<ImageView>(R.id.dialogChangeEyeNameBack)
            val changeDeviceEt = changeDeviceNameView.findViewById<EditText>(R.id.dialogChangeEyeNameEt)
            val changeDeviceBtn = changeDeviceNameView.findViewById<AppCompatButton>(R.id.dialogChangeEyeNameBtn)

            changeDeviceEt.setText(mActivity.aliasExtra)

            changeDeviceEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let {
                        if (it.isNotBlank()) {
                            if (changeDeviceEt.text.toString() != mActivity.aliasExtra) {
                                changeDeviceBtn.isEnabled = true
                                changeDeviceBtn.setTextColor(requireContext().getColor(R.color.white))
                            } else {
                                changeDeviceBtn.isEnabled = false
                                changeDeviceBtn.setTextColor(requireContext().getColor(R.color.eye_btn_disable_color))
                            }
                        } else {
                            changeDeviceBtn.isEnabled = false
                            changeDeviceBtn.setTextColor(requireContext().getColor(R.color.eye_btn_disable_color))
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            changeDeviceEt.setOnTouchListener { _, motionEvent ->
                try {
                    if (motionEvent.action == MotionEvent.ACTION_UP &&
                        motionEvent.rawX >= changeDeviceEt.right
                        - changeDeviceEt.compoundDrawablesRelative[2].bounds.width()
                    ) {
                        changeDeviceEt.text.clear()
                        changeDeviceEt.requestFocus()
                        return@setOnTouchListener true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                false
            }

            changeDeviceBtn.setOnClickListener {
                if (changeDeviceBtn.isEnabled) {
                    mActivity.serialExtra?.let { serial ->
                        if (isBeta(serial)) {
                            ToastUtils(requireContext()).showMessage("베타 테스트 기기는 수정이 불가능합니다!")
                        } else {
                            if (!isCanApi) {
                                isCanApi = true
                                callChangeAliasApi(changeDeviceNameDialog,serial,changeDeviceEt.text.toString())
                            }
                        }
                    }
                }
            }

            changeDeviceNameDialog.setBackPressed(backPress)
                .show(changeDeviceNameView,true, ShowDialogClass.DialogTransition.BOTTOM_TO_TOP)
        }

        binding.aeSettingSerial.setOnClickListener { }
        binding.aeSettingWifi.setOnClickListener { }
        binding.aeSettingNotification.setOnClickListener {
            val builder = ShowDialogClass(mActivity,true)
            val settingView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_eye_noti_setting,null)
            val settingBack = settingView.findViewById<ImageView>(R.id.dialogEyeSettingBack)
            val noiseDetail = settingView.findViewById<EyeSettingView>(R.id.dialogEyeSettingNoise)
            val gyroDetail = settingView.findViewById<EyeSettingView>(R.id.dialogEyeSettingGyro)
            val testNoti = settingView.findViewById<EyeSettingView>(R.id.dialogEyeSettingBetaNoti)

            builder.setBackPressed(settingBack)
            builder.show(settingView, true, ShowDialogClass.DialogTransition.BOTTOM_TO_TOP)

//            noiseDetail.isEnabled = !isBeta(mActivity.serialExtra.toString())
            noiseDetail.fetchEnable(!isBeta(mActivity.serialExtra.toString()))
//            gyroDetail.isEnabled = !isBeta(mActivity.serialExtra.toString())
            gyroDetail.fetchEnable(!isBeta(mActivity.serialExtra.toString()))

            noiseDetail.setOnClickListener {
                if (isBeta(mActivity.serialExtra.toString())) {
                    SnackBarUtils(settingView,"베타 기기는 설정이 불가능합니다",
                    ResourcesCompat.getDrawable(resources,R.drawable.caution_test,null)!!).show()
                }
            }
            gyroDetail.setOnClickListener {
                if (isBeta(mActivity.serialExtra.toString())) {
                    SnackBarUtils(settingView,"베타 기기는 설정이 불가능합니다",
                        ResourcesCompat.getDrawable(resources,R.drawable.caution_test,null)!!).show()
                }
            }

            testNoti.setOnClickListener {
                val perm = RequestPermissionsUtil(requireContext())
                if (perm.isNotificationPermitted()) {
                    if (SharedPreferenceManager(requireContext()).getBoolean(mActivity.serialExtra.toString(), false)) {
                        val bundle = Bundle()
                        bundle.putString("sort","noise")
                        bundle.putString("payload","${Random.nextInt(60, 120)}")
                        bundle.putString("device",mActivity.serialExtra.toString())
                        val message = RemoteMessage(bundle)
                        EyeNotiBuilder(requireContext()).sendNotification(message.data)
                    } else {
                        Toast.makeText(requireContext(), "먼저 알림을 활성화 해주세요", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "알림 권한이 거부되어있습니다", Toast.LENGTH_SHORT).show()
                    RequestPermissionsUtil(requireContext()).requestNotification()
                }
            }

            val settingSwitch = settingView.findViewById<EyeSettingView>(R.id.dialogEyeSettingToggle)
            mActivity.serialExtra?.let { serial ->
                val notiChecked = SharedPreferenceManager(requireContext()).getBoolean(serial, false)
                settingSwitch.fetchToggle(notiChecked).setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        if (RequestPermissionsUtil(requireContext()).isNotificationPermitted()) {
                            RequestPermissionsUtil(requireContext()).requestNotification()
                        }
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        SharedPreferenceManager(requireContext()).setBoolean(serial, isChecked)
                        withContext(Dispatchers.Main) {
                            SnackBarUtils(settingView,
                                "알림을 ${if(isChecked) "허용" else "거부"}하였습니다",
                                if (isChecked) ResourcesCompat.getDrawable(resources,R.drawable.alert_on,null)!!
                                else ResourcesCompat.getDrawable(resources,R.drawable.alert_off,null)!!).show()
                        }
                    }
                }
            }
        }

        binding.aeSettingMembers.fetchEnable(mActivity.isMaster)

        binding.aeSettingMembers.setOnClickListener {
            if (mActivity.isMaster) {
                val dialog = ShowDialogClass(mActivity, true)
                val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_eye_setting_members, null)
                val rv = view.findViewById<RecyclerView>(R.id.dialogMembersRv)
                val failMsg = view.findViewById<TextView>(R.id.dialogMembersFail)
                val delete = view.findViewById<ImageView>(R.id.listItemMembersDelete)
                val list = ArrayList<EyeDataModel.Members>()
                val adapter = EyeMembersAdapter(requireContext(),list)
                rv.adapter = adapter

                HttpClient.getInstance(false).setClientBuilder().getOwner(mActivity.serialExtra.toString())
                    .enqueue(object : Callback<List<ApiModel.Owner>>{
                        override fun onResponse(
                            call: Call<List<ApiModel.Owner>>,
                            response: Response<List<ApiModel.Owner>>
                        ) {
                            if (response.isSuccessful) {
                                val body = response.body()!!
                                if (body.isNotEmpty()) {
                                    list.clear()
                                    rv.visibility = View.VISIBLE
                                    failMsg.visibility = View.GONE

                                    body.forEachIndexed { index, item ->
                                        list.add(EyeDataModel.Members(item.id,item.master))
                                        adapter.notifyItemInserted(index)
                                    }
                                } else {
                                    rv.visibility = View.GONE
                                    failMsg.visibility = View.VISIBLE
                                }
                            } else {
                                rv.visibility = View.GONE
                                failMsg.visibility = View.VISIBLE
                            }
                        }

                        override fun onFailure(call: Call<List<ApiModel.Owner>>, t: Throwable) {
                            rv.visibility = View.GONE
                            failMsg.visibility = View.VISIBLE
                        }
                    })


                dialog.setBackPressed(view.findViewById(R.id.dialogMembersBack))
                dialog.show(view, true, ShowDialogClass.DialogTransition.BOTTOM_TO_TOP)
            } else {
                SnackBarUtils(requireView(),"소유자 전용 기능입니다",
                ResourcesCompat.getDrawable(resources,R.drawable.caution_test,null)!!).show()
            }
        }

        binding.aeSettingDeleteDevice.fetchTitleColor(R.color.red)
        binding.aeSettingDeleteDevice.setOnClickListener {
            showDeleteDialog()
        }

        binding.aeSettingModelName.fetchData(mActivity.modelName.toString())
        binding.aeSettingModelName.setOnClickListener {  }

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyData()
    }

    private fun isBeta(serial: String) : Boolean {
        return serial == "AOA00000053638" || serial == "AOA0000002F479"
    }

    private fun callChangeAliasApi(dialog: ShowDialogClass, serial: String, alias: String) {
        applyPostAlias(dialog,serial,alias)
        TimberUtil().d("eyetest","serial is $serial alias is $alias")
        deviceAliasViewModel.loadDataResult(serial,alias)
    }

    private fun applyPostAlias(dialog: ShowDialogClass, serial: String, alias: String) {
        if (!deviceAliasViewModel.fetchData().hasObservers()) {
            deviceAliasViewModel.fetchData().observe(mActivity) { result ->
                result?.let { res ->
                    when (res) {
                        is BaseRepository.ApiState.Success -> {
                            mActivity.hidePb()
                            val alert = MakeSingleDialog(requireContext())
                            val maker = alert.makeDialog(
                                getString(R.string.eye_success_change),
                                R.color.main_blue_color, getString(R.string.ok), true
                            )

                            EyeGroupRepository(requireContext()).update(serial, alias)

                            isCanApi = false

                            maker.setOnClickListener {
                                dialog.dismiss()
                                alert.dismiss()
                                val intent = Intent(mActivity, EyeListActivity::class.java)
                                startActivity(intent)
                                mActivity.finish()
                            }
                        }

                        is BaseRepository.ApiState.Error -> {
                            mActivity.hidePb()
                            dialog.dismiss()
                            ToastUtils(requireContext()).showMessage(getString(R.string.eye_fail_change))
                        }

                        is BaseRepository.ApiState.Loading -> mActivity.showPb()
                    }
                }
            }
        }
    }

    private fun applyData() {
        try {
            binding.aeSettingSerial.fetchData(mActivity.serialExtra ?: "")
            binding.aeSettingName.fetchData(mActivity.aliasExtra ?: "")
            binding.aeSettingWifi.fetchData(mActivity.ssidExtra ?: "")
            binding.aeSettingSetupDate.fetchData(mActivity.createExtra ?: "")
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
    }

    private fun showDeleteDialog() {
        val alias = mActivity.aliasExtra
        val serial = mActivity.serialExtra
        serial?.let {
            if (isBeta(serial)) {
                ToastUtils(requireContext()).showMessage("베타 테스트 기기는 삭제가 불가능합니다!")
            } else {
                val dialog = MakeDoubleDialog(requireContext())

                val show = dialog.make(
                    "${alias}(${serial})를\n삭제하시겠습니까?",
                    "예", "아니오", android.R.color.holo_red_light
                )

                show.first.setOnClickListener {
                    dialog.dismiss()
                    deleteDevice(serial, SharedPreferenceManager(requireContext()).getString(userEmail))
                }
                show.second.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }
    }

    private fun deleteDevice(sn: String, email: String) {
        HttpClient.getInstance(false).setClientBuilder().deleteDevice(
            sn, email
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                ToastUtils(requireContext()).showMessage(requireContext().getString(R.string.success_to_delete))
                RefreshUtils(requireContext()).refreshActivity()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                ToastUtils(requireContext()).showMessage(requireContext().getString(R.string.fail_to_delete))
            }
        })
    }
}
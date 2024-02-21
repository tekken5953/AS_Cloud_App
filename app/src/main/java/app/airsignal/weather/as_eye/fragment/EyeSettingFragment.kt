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
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.EyeDetailActivity
import app.airsignal.weather.as_eye.activity.EyeListActivity
import app.airsignal.weather.databinding.EyeSettingFragmentBinding
import app.airsignal.weather.db.SharedPreferenceManager
import app.airsignal.weather.db.room.repository.EyeGroupRepository
import app.airsignal.weather.repository.BaseRepository
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.util.ToastUtils
import app.airsignal.weather.view.custom_view.MakeSingleDialog
import app.airsignal.weather.view.custom_view.ShowDialogClass
import app.airsignal.weather.viewmodel.SetEyeDeviceAliasViewModel
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel

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
            val changeDeviceNameDialog = ShowDialogClass(mActivity)
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
//                        KeyboardController.onKeyboardUp(requireContext(),changeDeviceEt)
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
                        if (!isCanApi) {
                            isCanApi = true
                            callApi(changeDeviceNameDialog,serial,changeDeviceEt.text.toString())
                        }
                    }
                }
            }

            changeDeviceNameDialog.setBackPressed(backPress)
                .show(changeDeviceNameView,true, ShowDialogClass.DialogTransition.BOTTOM_TO_TOP)

        }

        binding.aeSettingSerial.setOnClickListener { }
        binding.aeSettingWifi.setOnClickListener { }
        val settingSwitch = binding.aeSettingNotification.findViewById<SwitchCompat>(R.id.customEyeSettingSwitch)
        mActivity.serialExtra?.let {
            settingSwitch.isChecked = SharedPreferenceManager(requireContext()).getBoolean(it, true)
            settingSwitch.setOnCheckedChangeListener { _, isChecked ->
                SharedPreferenceManager(requireContext()).setBoolean(it, isChecked)
            }
        }

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyData()
    }

    private fun callApi(dialog: ShowDialogClass, serial: String, alias: String) {
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
            val extras = mActivity.intent.extras
            binding.aeSettingSerial.fetchData(extras?.getString("serial") ?: "")
            binding.aeSettingName.fetchData(extras?.getString("alias") ?: "")
            binding.aeSettingWifi.fetchData(extras?.getString("ssid") ?: "")
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
    }
}
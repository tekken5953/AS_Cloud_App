package app.airsignal.weather.as_eye.fragment

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.airsignal.weather.R
import app.airsignal.weather.as_eye.activity.EyeDetailActivity
import app.airsignal.weather.as_eye.dao.EyeDataModel
import app.airsignal.weather.databinding.EyeSettingFragmentBinding
import app.airsignal.weather.util.KeyboardController
import app.airsignal.weather.view.custom_view.MakeSingleDialog
import app.airsignal.weather.view.custom_view.ShowDialogClass

class EyeSettingFragment : Fragment() {
    private lateinit var mActivity: EyeDetailActivity
    private lateinit var binding: EyeSettingFragmentBinding

    private lateinit var entireData: EyeDataModel.Setting

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

            //TODO 현재 기기명으로 넣기
            changeDeviceEt.setText("현재 기기명")

            changeDeviceEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let {
                        // TODO 현재 기기명과 다른지 검사
                        if (it.isNotBlank()) {
                            changeDeviceBtn.isEnabled = true
                            changeDeviceBtn.setTextColor(requireContext().getColor(R.color.white))
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
                    //TODO 기기 Alias 변경 요청
                    val alert = MakeSingleDialog(requireContext())
                    val maker = alert.makeDialog("변경에 성공했습니다", R.color.main_blue_color,"확인",false)
                    maker.setOnClickListener {
                        changeDeviceNameDialog.dismiss()
                    }
                }
            }

            changeDeviceNameDialog.setBackPressed(backPress)
                .show(changeDeviceNameView,true, ShowDialogClass.DialogTransition.BOTTOM_TO_TOP)

        }
        binding.aeSettingSerial.setOnClickListener { }
        binding.aeSettingWifi.setOnClickListener { }
        binding.aeSettingNotification.setOnClickListener { }
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyData()
    }

    private fun applyData() {
        try {
            entireData.let {
                binding.aeSettingSerial.fetchData(it.deviceSerial ?: "")
                binding.aeSettingName.fetchData(it.deviceName ?: "")
                binding.aeSettingWifi.fetchData(it.wifiSSID ?: "")
            }
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
    }
    fun onDataTransfer(data: EyeDataModel.Setting?) {
        data?.let {
            entireData = it
        }
    }
}
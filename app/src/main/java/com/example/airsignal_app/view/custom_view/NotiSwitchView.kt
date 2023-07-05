package com.example.airsignal_app.view.custom_view

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.airsignal_app.R
import com.example.airsignal_app.databinding.CustomViewSettingNotiBinding
import com.example.airsignal_app.firebase.fcm.SubFCM
import com.example.airsignal_app.util.RequestPermissionsUtil
import com.example.airsignal_app.util.`object`.SetAppInfo

/**
 * @author : Lee Jae Young
 * @since : 2023-07-03 오후 3:35
 **/
class NotiSwitchView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private var notiBinding: CustomViewSettingNotiBinding
    private var isInit = true

    init {
        val inflater = LayoutInflater.from(context)
        notiBinding = CustomViewSettingNotiBinding.inflate(inflater, this, true)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.NotiSwitchView)
            val customTitle = typedArray.getString(R.styleable.NotiSwitchView_notiTitle)!!
            val customTopic = typedArray.getString(R.styleable.NotiSwitchView_topicTitle)!!
            val basicTopic = typedArray.getString(R.styleable.NotiSwitchView_basicTitle)!!
            val customEnableSubTitle =
                typedArray.getBoolean(R.styleable.NotiSwitchView_enableSubTitle, false)
            typedArray.recycle()

            notiBinding.customNotiTitle.text = customTitle
            if (customEnableSubTitle) {
                setNightAlertsSpan(notiBinding.customNotiTitle)
            }

            notiBinding.customNotiSwitch.setOnCheckedChangeListener { _, isChecked ->
                val permission = RequestPermissionsUtil(context)
                if (!permission.isNotificationPermitted()) {
                    permission.requestNotification()
                    showSnackBar(isChecked, basicTopic)
                    SetAppInfo.setUserNoti(context, customTopic, isChecked)
                } else {
                    showSnackBar(isChecked, basicTopic)
                    SetAppInfo.setUserNoti(context, customTopic, isChecked)
                }

                if (isChecked) {
                    SubFCM().subTopic(customTopic)
                } else {
                    SubFCM().unSubTopic(customTopic)
                }
            }
        }
    }

    /** 알림 커스텀 스낵바 세팅 **/
    private fun showSnackBar(isAllow: Boolean, title: String) {
        val alertOn = ContextCompat.getDrawable(context, R.drawable.alert_on)!!
        val alertOff = ContextCompat.getDrawable(context, R.drawable.alert_off)!!
        alertOn.setTint(context.getColor(R.color.mode_color_view))
        alertOff.setTint(context.getColor(R.color.mode_color_view))
        if (isAllow) {
            if (!isInit) {
                SnackBarUtils.make(
                    notiBinding.root,
                    "$title ${context.getString(R.string.allowed_noti)}", alertOn
                ).show()
            }
        } else {
            if (!isInit) {
                SnackBarUtils.make(
                    notiBinding.root,
                    "$title ${context.getString(R.string.denied_noti)}", alertOff
                ).show()
            }
        }
    }

    /** 야간 알림 허용 텍스트 설정 **/
    private fun setNightAlertsSpan(textView: TextView) {
        val span = SpannableStringBuilder(textView.text)
        val formatText = textView.text.split(System.lineSeparator())
        // 색상변경
        span.setSpan(
            ForegroundColorSpan(context.getColor(R.color.main_gray_color)),
            formatText[0].length, span.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        // 크기변경
        span.setSpan(
            RelativeSizeSpan(0.8f),
            formatText[0].length, span.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.text = span
    }

    fun fetchData(isChecked: Boolean): NotiSwitchView {
        notiBinding.customNotiSwitch.isChecked = isChecked
        isInit = false
        return this
    }
}
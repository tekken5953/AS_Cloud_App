package com.example.airsignal_app.login

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import com.example.airsignal_app.R
import com.example.airsignal_app.dao.IgnoredKeyFile.userEmail
import com.example.airsignal_app.dao.StaticDataObject
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.util.EnterPage
import com.example.airsignal_app.util.RefreshUtils
import com.example.airsignal_app.util.ShowDialogClass
import com.example.airsignal_app.util.ToastUtils
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.orhanobut.logger.Logger

/**
 * @author : Lee Jae Young
 * @since : 2023-04-05 오전 9:50
 **/
class EmailLogin(private val activity: Activity) {
    private lateinit var auth: FirebaseAuth
    private lateinit var tst: ToastUtils

    fun initialize(): EmailLogin {
        auth = Firebase.auth
        tst = ToastUtils(activity)
        return this
    }

    private fun createEmailUser(email: String, password: String) {

        val actionCodeSettings = actionCodeSettings {
            url = "https://airsignalapp.firebaseapp.com/__/auth/action"
            handleCodeInApp = true
            setAndroidPackageName(
                activity.packageName,
                true,        // installIfNotAvailable
                "12"               // minimumVersionQX
            )
        }
        Logger.t(StaticDataObject.TAG_LOGIN).d("sendEmailAuth to ${actionCodeSettings.url}")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    tst.shortMessage("계정 생성 완료")
                    loginEmail(email, password)
                } else {
                    Logger.t(StaticDataObject.TAG_LOGIN)
                        .e("계정 생성 실패 ${task.exception!!.localizedMessage}")
                }
            }
    }

    fun logoutEmail() {
        auth.signOut()
        RefreshUtils(activity).refreshActivityAfterSecond(sec = 1)
    }

    fun loginEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        tst.shortMessage("로그인에 성공 하였습니다.")
                        EnterPage(activity).toMain("email")
                        SharedPreferenceManager(activity).setString(userEmail, email)
                    } else {
                        try {
                            tst.shortMessage("로그인에 실패 하였습니다.")
                        } catch (e: Exception) {
                            if (e is RuntimeExecutionException) {
                                tst.shortMessage("존재하지 않는 회원입니다.\n회원가입을 진행합니다.")
                                val dialog = ShowDialogClass().getInstance(activity)
                                val viewSignUp: View = LayoutInflater.from(activity)
                                    .inflate(R.layout.dialog_signup_email, null)
                                val signUpCreate: AppCompatButton =
                                    viewSignUp.findViewById(R.id.sign_up_ok)

                                dialog.setBackPressed(viewSignUp.findViewById(R.id.sign_up_cancel))
                                    .show(viewSignUp, true)

                                signUpCreate.setOnClickListener {
                                    dialog.dismiss()
                                    createEmailUser(email, password)
                                }
                            }
                        }
                    }
                }
        }
    }
}
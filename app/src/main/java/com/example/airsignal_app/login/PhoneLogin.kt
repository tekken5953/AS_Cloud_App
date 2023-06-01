package com.example.airsignal_app.login

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.airsignal_app.dao.IgnoredKeyFile
import com.example.airsignal_app.db.SharedPreferenceManager
import com.example.airsignal_app.util.RefreshUtils
import com.firebase.ui.auth.AuthUI
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


/**
 * @author : Lee Jae Young
 * @since : 2023-04-18 오후 4:44
 **/
@SuppressLint("LogNotTimber")
class PhoneLogin(
    private val activity: Activity,
    private val btn: Button?,
    private val msg: TextView?,
) {

    private val auth = FirebaseAuth.getInstance()
    private val sp by lazy { SharedPreferenceManager(activity) }

    /** 로그인 **/
    fun login(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(120L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()

        btnDisable()

        PhoneAuthProvider.verifyPhoneNumber(options)
        auth.useAppLanguage()
    }

    /** 로그아웃 **/
    fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            AuthUI.getInstance()
                .signOut(activity)
                .addOnCompleteListener {
                    RefreshUtils(activity)
                        .refreshActivityAfterSecond(sec = 1,
                            pbLayout = null)
                }
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        /** 검증 성공 **/
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d("phone_tag", "onVerificationCompleted:${credential.signInMethod}")

            sp.setString("verificationCode", credential.smsCode.toString())
                .setString(IgnoredKeyFile.lastLoginPlatform, "phone")
            signInWithPhoneAuthCredential(credential)
            btnEnable()
        }

        /** 검증 실패 **/
        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w("phone_tag", "onVerificationFailed", e)
            btnEnable()
            msgEnable()

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                msg!!.text = "올바르지 않은 요청입니다"
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                msg!!.text = "입력시간이 초과되었습니다"
            }

            // Show a message and update the UI
        }

        /** 코드 메시지 발송 **/
        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d("phone_tag", "onCodeSent:$verificationId")
            btnDisable()
            msgDisable()

            PhoneAuthProvider.getCredential(verificationId, sp.getString("verificationCode"))
            // Save verification ID and resending token so we can use them later
//            SharedPreferenceManager(activity).setString("verificationId",verificationId)
//            SharedPreferenceManager(activity).setString("phoneLoginToken",token.toString())
        }
    }

    /** 세션 확인 후 로그인 **/
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("phone_tag", "signInWithCredential:complete")
                    Log.d("phone_tag", "complete : ${task.result.user?.phoneNumber}")
//                    val user = task.result?.user
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w("phone_tag", "signInWithCredential:failure", task.exception)
//                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
//                        // The verification code entered was invalid
//                    }
                    // Update UI
                }
            }

            .addOnSuccessListener { result ->
                Log.d("phone_tag", "signInWithCredential:success")
                Log.d("phone_tag", "success : ${result.user?.phoneNumber}")
                sp.setString(IgnoredKeyFile.userEmail, result.user?.phoneNumber.toString())
                    .setString(IgnoredKeyFile.userProfile, result.user?.photoUrl.toString())
                Toast.makeText(activity, "$result", Toast.LENGTH_SHORT).show()
            }
    }

    private fun btnDisable() {
        if (btn!!.isEnabled)
            btn.isEnabled = false
    }

    private fun btnEnable() {
        if (!btn!!.isEnabled)
            btn.isEnabled = true
    }

    private fun msgDisable() {
        if (msg!!.isEnabled)
            msg.isEnabled = false
    }

    private fun msgEnable() {
        if (!msg!!.isEnabled)
            msg.isEnabled = true
    }
}
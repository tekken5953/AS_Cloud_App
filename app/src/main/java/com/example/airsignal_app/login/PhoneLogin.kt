package com.example.airsignal_app.login

import android.app.Activity
import com.example.airsignal_app.dao.StaticDataObject.TAG_L
import com.example.airsignal_app.db.SharedPreferenceManager
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.orhanobut.logger.Logger
import java.util.concurrent.TimeUnit

/**
 * @author : Lee Jae Young
 * @since : 2023-04-18 오후 4:44
 **/
class PhoneLogin(private val activity: Activity, phoneNumber: String) {

    private val auth = FirebaseAuth.getInstance()

    fun login() {
        PhoneAuthProvider.verifyPhoneNumber(options)
        auth.setLanguageCode("kr")
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Logger.t(TAG_L).d("onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Logger.t(TAG_L).w( "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Logger.t(TAG_L).d( "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            SharedPreferenceManager(activity).setString("verificationId",verificationId)
            SharedPreferenceManager(activity).setString("phoneLoginToken",token.toString())
        }
    }

    private val options = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber(phoneNumber)       // Phone number to verify
        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
        .setActivity(activity)                 // Activity (for callback binding)
        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
        .build()

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Logger.t(TAG_L).d( "signInWithCredential:success")

                    val user = task.result?.user
                } else {
                    // Sign in failed, display a message and update the UI
                    Logger.t(TAG_L).w( "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

}
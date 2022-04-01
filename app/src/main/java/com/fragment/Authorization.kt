package com.fragment

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.connectionlesson_3firebase_auth.R
import com.example.connectionlesson_3firebase_auth.databinding.FragmentAuthorizationBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class Authorization : Fragment(R.layout.fragment_authorization) {
    private val binding: FragmentAuthorizationBinding by viewBinding()
    lateinit var handler: Handler
    var startTime = 60
    private lateinit var auth: FirebaseAuth
    private lateinit var phoneNumber: String
    private val TAG = "Authorization"
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("en")
        phoneNumber = arguments?.getString("phoneNumber")!!
        val num = phoneNumber.toCharArray()
        binding.advice.text =
            "One-time code is sent to (+998 9${num[5]}) ${num[6]}${num[7]}${num[8]} - ** - **"
        handler = Handler(Looper.getMainLooper())
        handler.post(runnable)

        sentVerificationCode(phoneNumber)
        binding.smsCode.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyCode()
                val view = requireActivity().currentFocus
                if (view != null) {
                    val imm: InputMethodManager =
                        requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
            true
        }
        binding.smsCode.addTextChangedListener {
            if (it.toString().length == 6) {
                verifyCode()
            }
        }
        binding.resendLayout.setOnClickListener {
            if (::resendToken.isInitialized) {
                if (startTime <= 55) {
                    resendCode(phoneNumber)
                    startTime = 60
                    handler.removeCallbacks(runnable)
                    Toast.makeText(requireContext(), "Clicked", Toast.LENGTH_SHORT).show()
                    binding.timeLimit.text = "00:59"
                    handler.post(runnable)
                }
            }


        }

    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(runnable)
    }

    val runnable = object : Runnable {
        override fun run() {
            startTime -= 1
            if (startTime < 10) {
                binding.timeLimit.text = "00:0$startTime"

            } else {
                binding.timeLimit.text = "00:$startTime"

            }
            handler.postDelayed(this, 1000)
            if (startTime == 0) {
                handler.removeCallbacks(this)
            }
        }
    }


    private fun resendCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private fun verifyCode() {
        val code = binding.smsCode.text.toString()
        if (::storedVerificationId.isInitialized) {
            if (code.length == 6) {
                val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun sentVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user?.phoneNumber
                    val bundleOf = bundleOf("phoneNumber" to user)
                    findNavController().navigate(R.id.registrated, bundleOf)
                    Toast.makeText(requireContext(), "$user", Toast.LENGTH_SHORT).show()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }
}
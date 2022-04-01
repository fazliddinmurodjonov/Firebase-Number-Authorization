package com.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.connectionlesson_3firebase_auth.R
import com.example.connectionlesson_3firebase_auth.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.util.Empty


class SignUp : Fragment(R.layout.fragment_sign_up) {
    private val binding: FragmentSignUpBinding by viewBinding()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.entranceButton.setOnClickListener {
            val number = binding.phoneNumber.text.toString()
            val phoneNumber = "+$number"
            val numBol = Empty.empty(number)
            val numSpace = Empty.space(number)
            if (numBol && numSpace && phoneNumber.length == 13) {
                val bundle = bundleOf("phoneNumber" to phoneNumber)
                findNavController().navigate(R.id.authorization, bundle)
            }
        }
    }


}
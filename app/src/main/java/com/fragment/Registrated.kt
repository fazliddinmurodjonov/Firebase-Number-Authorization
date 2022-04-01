package com.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.connectionlesson_3firebase_auth.R
import com.example.connectionlesson_3firebase_auth.databinding.FragmentRegistratedBinding

class Registrated : Fragment(R.layout.fragment_registrated) {
    private val binding: FragmentRegistratedBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val phoneNumber = arguments?.getString("phoneNumber")

        binding.phoneNumber.text = phoneNumber

    }


}
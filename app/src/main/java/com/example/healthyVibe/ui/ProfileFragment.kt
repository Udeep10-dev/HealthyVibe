package com.example.healthyVibe.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.healthyVibe.R
import com.example.healthyVibe.data.Storage
import com.google.android.material.textfield.TextInputEditText

class ProfileFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_profile, container, false)
        val storage = Storage(requireContext())

        val etName = v.findViewById<TextInputEditText>(R.id.etName)
        val etBio = v.findViewById<TextInputEditText>(R.id.etBio)
        val tvAvatar = v.findViewById<TextView>(R.id.tvAvatar)
        val btnSave = v.findViewById<View>(R.id.btnSave)

        // load
        etName.setText(storage.getProfileName())
        etBio.setText(storage.getProfileBio())
        tvAvatar.text = storage.getProfileName().trim().takeIf { it.isNotEmpty() }?.first()?.uppercase() ?: "H"

        btnSave.setOnClickListener {
            val name = etName.text?.toString()?.trim().orEmpty()
            val bio = etBio.text?.toString()?.trim().orEmpty()
            storage.setProfileName(name)
            storage.setProfileBio(bio)
            tvAvatar.text = name.takeIf { it.isNotEmpty() }?.first()?.uppercase() ?: "H"
            Toast.makeText(requireContext(), "Profile saved", Toast.LENGTH_SHORT).show()
        }

        return v
    }
}

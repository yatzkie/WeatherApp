package com.rpc.weatherapp.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rpc.weatherapp.databinding.ActivitySignUpBinding

class SignUpActivity: AppCompatActivity() {

    private var binding: ActivitySignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)
    }
}
package com.rpc.weatherapp.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rpc.weatherapp.databinding.ActivityLoginBinding

class LoginActivity: AppCompatActivity() {

    private var binding: ActivityLoginBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)
    }
}
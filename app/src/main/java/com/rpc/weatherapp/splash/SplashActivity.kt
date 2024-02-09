package com.rpc.weatherapp.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.rpc.weatherapp.databinding.ActivitySplashBinding
import com.rpc.weatherapp.user.LoginActivity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SplashActivity: AppCompatActivity() {

    private var binding: ActivitySplashBinding? = null
    private val viewModel: SplashViewModel by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getStates().collect { state ->
                    handleState(state)
                }
            }
        }
        lifecycle.addObserver(viewModel)
    }

    private fun handleState(state: SplashState) {
        when(state) {
            SplashState.Loading -> binding?.loadingIndicator?.isVisible = true
            SplashState.Authenticated -> {}
            SplashState.Unauthorized -> {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}
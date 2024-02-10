package com.rpc.weatherapp.weather.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.rpc.weatherapp.databinding.FragmentWeatherHistoryBinding
import com.rpc.weatherapp.splash.SplashActivity
import com.rpc.weatherapp.weather.current.CurrentWeatherEvent
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class WeatherHistoryFragment : Fragment() {

    private var binding: FragmentWeatherHistoryBinding? = null
    private val viewModel: WeatherHistoryViewModel by inject()
    private val controller = WeatherDataHistoryController()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentWeatherHistoryBinding.inflate(inflater, container, false)
        this.binding = binding
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getState().collect { state -> handleState(state) }
            }
        }
        return binding.root
    }

    private fun handleState(state: WeatherHistoryState) {
        when(state) {
            WeatherHistoryState.Idle -> {
                binding?.listWeatherHistory?.isVisible = false
                binding?.loadingIndicator?.isVisible = false
                binding?.notificationBanner?.isVisible = false
                binding?.bannerHint?.isVisible = false
            }
            WeatherHistoryState.Loading -> {
                binding?.listWeatherHistory?.isVisible = false
                binding?.loadingIndicator?.isVisible = true
                binding?.notificationBanner?.isVisible = false
                binding?.bannerHint?.isVisible = false
            }
            is WeatherHistoryState.Error -> {
                binding?.listWeatherHistory?.isVisible = false
                binding?.loadingIndicator?.isVisible = true
                binding?.notificationBanner?.isVisible = true
                binding?.bannerHint?.isVisible = true
                binding?.notificationBanner?.text = state.message
            }
            WeatherHistoryState.UnauthorizedUser -> {
                showForcedSignOutPrompt()
            }
            is WeatherHistoryState.WeatherHistoryFetched -> {
                binding?.loadingIndicator?.isVisible = false
                binding?.notificationBanner?.isVisible = false
                binding?.bannerHint?.isVisible = false
                binding?.listWeatherHistory?.isVisible = state.data.isNotEmpty()
                if (state.data.isNotEmpty()) {
                    binding?.listWeatherHistory?.adapter = controller.adapter
                    controller.setData(state.data)
                } else {
                    binding?.notificationBanner?.text = "No history found yet."
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.sendEvent(WeatherHistoryEvent.FetchWeatherHistory)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun showForcedSignOutPrompt() {
        val context = this.context ?: return
        val builder = AlertDialog.Builder(context)
            .setTitle("Sign Out")
            .setMessage("Unable to find existing current session. Forcing sign out.")
            .setPositiveButton("ok") { _, _ -> }
            .setOnDismissListener { goToSplash() }
        builder.show()
    }

    private fun goToSplash() {
        activity?.let { act ->
            act.startActivity(Intent(act, SplashActivity::class.java))
            act.finish()
        }
    }
}
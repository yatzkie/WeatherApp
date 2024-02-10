package com.rpc.weatherapp.weather.current

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rpc.weatherapp.R
import com.rpc.weatherapp.databinding.FragmentCurrentWeatherBinding
import com.rpc.weatherapp.splash.SplashActivity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class CurrentWeatherFragment : Fragment() {

    private var binding: FragmentCurrentWeatherBinding? = null
    private val viewModel: CurrentWeatherViewModel by inject()

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
       val coarsePermissionGranted = granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
       val finePermissionGranted = granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if(coarsePermissionGranted && finePermissionGranted) {
            viewModel.sendEvent(CurrentWeatherEvent.FetchCurrentWeather)
        } else {
            showLocationPermissionRationale()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCurrentWeatherBinding.inflate(inflater, container, false)
        this.binding = binding

        bindingUserEvents()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getState().collect {state -> handleState(state) }
            }
        }
        return binding.root
    }

    private fun handleState(state: CurrentWeatherState) {
        val binding = this.binding ?: return
        when(state) {
            is CurrentWeatherState.Error -> {
                binding.weatherDetails.isVisible = false
                binding.weatherIcon.isVisible = false
                binding.loadingIndicator.isVisible = false
                binding.notificationBanner.isVisible = true
                binding.bannerHint.isVisible = true
                binding.notificationBanner.text = state.message
            }
            CurrentWeatherState.Idle -> {
                binding.weatherIcon.setImageResource(R.drawable.app_icon_white)
                binding.loadingIndicator.isVisible = false
                binding.weatherDetails.isVisible = true
                binding.notificationBanner.isVisible = false
                binding.bannerHint.isVisible = false
            }
            is CurrentWeatherState.LoadWeatherData -> {
                binding.weatherDetails.isVisible = true
                binding.temperature.text = "${state.data.temperature}"
                binding.description.text = state.data.description
                binding.cityName.text = state.data.location
                Glide
                    .with(this)
                    .load(state.data.icon)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.app_icon_white)
                            .error(R.drawable.app_icon_white)
                    )
                    .into(binding.weatherIcon)

                binding.weatherIcon.isVisible = true
                binding.loadingIndicator.isVisible = false
            }
            CurrentWeatherState.Loading -> {
                binding.loadingIndicator.isVisible = true
                binding.weatherDetails.isVisible = false
                binding.weatherIcon.isVisible = false
            }
            is CurrentWeatherState.WelcomeUser -> {
                binding.welcomeBanner.text = "Welcome, ${state.displayName}!"
                checkPermissions()
            }
            CurrentWeatherState.UnauthorizedUser -> showForcedSignOutPrompt()
            CurrentWeatherState.SignOut -> goToSplash()
        }
    }

    private fun goToSplash() {
        activity?.let { act ->
            act.startActivity(Intent(act, SplashActivity::class.java))
            act.finish()
        }
    }

    private fun bindingUserEvents() {
        val binding = this.binding ?: return
        binding.signOutButton.setOnClickListener {
            viewModel.sendEvent(CurrentWeatherEvent.SignOut)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.sendEvent(CurrentWeatherEvent.FetchCurrentUser)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun checkPermissions() {
        val activity = this.activity ?: return
        context?.apply {
            val coarsePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val finePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasPermission = coarsePermission && finePermission
            if (!hasPermission) {
                requestPermission(activity)
            } else {
                viewModel.sendEvent(CurrentWeatherEvent.FetchCurrentWeather)
            }
        }
    }

    private fun requestPermission(activity: Activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission_group.LOCATION)) {
            showLocationPermissionRationale()
        } else {
            permissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ))
        }
    }

    private fun showLocationPermissionRationale() {
        val context = this.context ?: return
        val builder = AlertDialog.Builder(context)
            .setTitle("Requesting location permission")
            .setMessage("In order to use this feature you have to allow location permission")
            .setPositiveButton("open settings") { _, _ -> goToSettings() }
            .setNegativeButton("cancel") { _, _ ->  }
        builder.show()
    }

    private fun showForcedSignOutPrompt() {
        val context = this.context ?: return
        val builder = AlertDialog.Builder(context)
            .setTitle("Sign Out")
            .setMessage("Unable to find existing current session. Forcing sign out.")
            .setPositiveButton("ok") { _, _ -> }
            .setOnDismissListener { viewModel.sendEvent(CurrentWeatherEvent.SignOut) }
        builder.show()
    }


    private fun goToSettings() {
        val context = this.context ?: return
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + context.packageName)
        startActivity(intent)
    }

}
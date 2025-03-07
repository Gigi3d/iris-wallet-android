package org.iriswallet.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import androidx.navigation.fragment.findNavController
import org.iriswallet.R
import org.iriswallet.data.SharedPreferencesManager
import org.iriswallet.data.SharedPreferencesManager.PREFS_PIN_LOGIN_CONFIGURED
import org.iriswallet.databinding.FragmentRoutingBinding
import org.iriswallet.utils.AppAuthenticationService
import org.iriswallet.utils.AppAuthenticationServiceListener
import org.iriswallet.utils.AppContainer

class RoutingFragment :
    MainBaseFragment<FragmentRoutingBinding>(FragmentRoutingBinding::inflate),
    AppAuthenticationServiceListener {

    private lateinit var appAuthenticationService: AppAuthenticationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appAuthenticationService = AppAuthenticationService(this)
        AppContainer.storedMnemonic = SharedPreferencesManager.mnemonic
        if (AppContainer.storedMnemonic.isBlank())
            findNavController().navigate(R.id.action_routingFragment_to_firstRunFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity.supportActionBar!!.hide()
    }

    override fun onResume() {
        super.onResume()
        if (!mActivity.loggedIn && !mActivity.loggingIn) {
            if (SharedPreferencesManager.pinLoginConfigured) {
                mActivity.loggingIn = true
                appAuthenticationService.auth(PREFS_PIN_LOGIN_CONFIGURED)
            } else {
                authenticated(PREFS_PIN_LOGIN_CONFIGURED)
            }
        }
    }

    override fun authenticated(requestCode: String) {
        mActivity.loggedIn = true
        viewModel.refreshAssets(allowFailures = true)
        if (SharedPreferencesManager.pinLoginConfigured && !AppContainer.canUseBiometric)
            mActivity.hideSplashScreen = true
        viewModel.assets.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                findNavController().navigate(R.id.action_routingFragment_to_mainFragment)
            }
        }
    }

    override fun handleAuthError(requestCode: String, errorExtraInfo: String?, errCode: Int?) {
        when (errCode) {
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> requireActivity().finish()
            BiometricPrompt.ERROR_LOCKOUT -> {
                mActivity.hideSplashScreen = true
                AlertDialog.Builder(requireContext())
                    .setMessage(getString(R.string.err_too_many_attempts))
                    .setPositiveButton(getString(R.string.exit)) { _, _ -> mActivity.finish() }
                    .setCancelable(false)
                    .create()
                    .show()
            }
            AppAuthenticationService.USER_DISABLED_AUTH -> {
                SharedPreferencesManager.pinLoginConfigured = false
                SharedPreferencesManager.pinActionsConfigured = false
                authenticated()
            }
            else -> appAuthenticationService.auth(PREFS_PIN_LOGIN_CONFIGURED)
        }
    }
}

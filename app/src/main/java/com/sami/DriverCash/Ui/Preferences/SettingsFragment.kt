package com.sami.DriverCash.Ui.Preferences

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.sami.DriverCash.Auth.AuthManager
import com.sami.DriverCash.MainActivity
import com.sami.DriverCash.R
import com.sami.DriverCash.ViewModel.VehicleViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val vehicleViewModel: VehicleViewModel by activityViewModels()
    private var vehiclePreference: Preference? = null
    private var themePreference: ListPreference? = null
    private var signOutPreference: Preference? = null

    @Inject
    lateinit var authManager: AuthManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        vehiclePreference = findPreference("pref_vehicle")
        themePreference = findPreference("pref_theme")
        signOutPreference = findPreference("pref_sign_out")

        setupThemePreferenceListener()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vehiclePreference?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_misVehiculosRegistradosFragment)
            true
        }

        setupSignOutPreference()
        observeVehiculoPredeterminado()
    }

    private fun setupThemePreferenceListener() {
        themePreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            val themeValue = newValue as String
            applyTheme(themeValue)
            true // Importante para que el valor de la preferencia se guarde
        }
    }

    private fun setupSignOutPreference() {
        signOutPreference?.isVisible = authManager.currentUser != null
        signOutPreference?.setOnPreferenceClickListener {
            authManager.signOut {
                // Esta lambda se llama cuando Google Sign-Out también se completa
                // Reiniciar MainActivity para limpiar el estado y forzar la verificación de autenticación
                activity?.let { currentActivity ->
                    val intent = Intent(currentActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    currentActivity.finish()
                }
            }
            true
        }
    }

    private fun applyTheme(themeValue: String) {
        when (themeValue) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "auto" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            // Considera añadir un 'else' o un valor por defecto si tus theme_values son diferentes
        }
        // No es necesario recrear el fragmento aquí, AppCompatDelegate se encarga de la actividad.
    }

    private fun observeVehiculoPredeterminado() {
        vehicleViewModel.vehiculoPredeterminado.observe(viewLifecycleOwner) { vehiculo ->
            if (vehiculo != null) {
                vehiclePreference?.summary = "Vehículo actual: ${vehiculo.marca} ${vehiculo.modelo} (${vehiculo.placa})"
            } else {
                vehiclePreference?.summary = "Ningún vehículo seleccionado como predeterminado"
            }
        }
    }
}
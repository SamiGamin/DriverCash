package com.sami.DriverCash.Ui.Preferences

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.sami.DriverCash.R
import com.sami.DriverCash.ViewModel.VehicleViewModel

class SettingsFragment : PreferenceFragmentCompat() {

    private val vehicleViewModel: VehicleViewModel by activityViewModels()
    private var vehiclePreference: Preference? = null
    private var themePreference: ListPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        vehiclePreference = findPreference("pref_vehicle")
        themePreference = findPreference("pref_theme")

        setupThemePreferenceListener()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vehiclePreference?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_misVehiculosRegistradosFragment)
            true
        }

        observeVehiculoPredeterminado()
    }

    private fun setupThemePreferenceListener() {
        themePreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            val themeValue = newValue as String
            applyTheme(themeValue)
            true // Importante para que el valor de la preferencia se guarde
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
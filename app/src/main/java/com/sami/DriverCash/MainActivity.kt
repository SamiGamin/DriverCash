package com.sami.DriverCash

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
// import com.sami.DriverCash.Auth.AuthManager // Eliminado
import com.sami.DriverCash.ViewModel.VehicleViewModel
import com.sami.DriverCash.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
// import javax.inject.Inject // Eliminado si AuthManager era el único @Inject aquí

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val vehicleViewModel: VehicleViewModel by viewModels()

    // @Inject // Eliminado
    // lateinit var authManager: AuthManager // Eliminado

    private fun applyUserSelectedTheme() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themeValue = sharedPreferences.getString("pref_theme", "auto") // "auto" es el defaultValue
        when (themeValue) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "auto" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyUserSelectedTheme() // Aplicar tema ANTES de super.onCreate y de inflar la vista
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigation()
        observeVehicleViewModel()
    }

    private fun setupNavigation() {
        setSupportActionBar(binding.mainToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNav.setOnItemSelectedListener { itemId ->
            // Navegar al destino si no es el actual para evitar recargar la misma pantalla
            if (navController.currentDestination?.id != itemId) {
                navController.navigate(itemId)
            }
        }

        // Sincronizar el ChipNavigationBar con el NavController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.setItemSelected(destination.id)
            // supportActionBar?.title = destination.label // Ya estaba comentado
        }
    }

    private fun observeVehicleViewModel() {
        vehicleViewModel.vehiculoPredeterminado.observe(this) { vehiculo ->
            if (vehiculo != null) {
                binding.tvToolbarVehicleModel.text = "${vehiculo.marca} ${vehiculo.modelo}"
                binding.tvToolbarVehiclePlate.text = vehiculo.placa
                binding.ivToolbarVehicleIcon.setImageResource(R.drawable.ic_directions_car) // Reemplaza con tu drawable
                binding.tvToolbarVehicleModel.visibility = View.VISIBLE
                binding.tvToolbarVehiclePlate.visibility = View.VISIBLE
                binding.ivToolbarVehicleIcon.visibility = View.VISIBLE
            } else {
                binding.tvToolbarVehicleModel.text = "Sin vehículo"
                binding.tvToolbarVehiclePlate.text = "Seleccione uno"
                binding.ivToolbarVehicleIcon.setImageResource(R.drawable.ic_privacy) // Reemplaza con tu drawable
                binding.tvToolbarVehicleModel.visibility = View.VISIBLE
                binding.tvToolbarVehiclePlate.visibility = View.VISIBLE
                binding.ivToolbarVehicleIcon.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        // val signOutItem = menu.findItem(R.id.action_sign_out) // Eliminado
        // signOutItem?.isVisible = authManager.currentUser != null // Eliminado
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_preferences -> {
                if (::navController.isInitialized) {
                    if (navController.currentDestination?.id != R.id.nav_settings &&
                        navController.currentDestination?.id != R.id.nav_mis_vehiculos_registrados &&
                        navController.currentDestination?.id != R.id.nav_add_vehicle
                    ) {
                        navController.navigate(R.id.nav_settings)
                    }
                }
                true
            }
            // R.id.action_sign_out -> { // Bloque eliminado
            //     authManager.signOut {
            //         val intent = Intent(this, MainActivity::class.java)
            //         intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            //         startActivity(intent)
            //         finish()
            //     }
            //     true
            // }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
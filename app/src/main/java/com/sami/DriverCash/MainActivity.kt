package com.sami.DriverCash

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
import com.sami.DriverCash.ViewModel.VehicleViewModel
import com.sami.DriverCash.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val vehicleViewModel: VehicleViewModel by viewModels()

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
        // Esto es útil si la navegación puede ocurrir desde otras fuentes además del ChipNavigationBar
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Actualiza el ítem seleccionado en ChipNavigationBar.
            // El método exacto puede variar según la versión de la biblioteca,
            // pero setItemSelected(id) o setItemSelected(id, true) es común.
            // El segundo parámetro 'true' usualmente indica que el cambio es programático
            // y no debería re-disparar el listener OnItemSelectedListener.
            // Consulta la documentación de ChipNavigationBar si esto no funciona como se espera.
            binding.bottomNav.setItemSelected(destination.id) // O binding.bottomNav.setItemSelected(destination.id, true)

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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_preferences -> {
                if (::navController.isInitialized) {
                    // Evitar navegar a settings si ya estamos en settings o en una subpantalla de settings
                    if (navController.currentDestination?.id != R.id.nav_settings &&
                        navController.currentDestination?.id != R.id.nav_mis_vehiculos_registrados && // Ejemplo si MisVehiculos es sub-settings
                        navController.currentDestination?.id != R.id.nav_add_vehicle
                    ) { // Ejemplo
                        navController.navigate(R.id.nav_settings)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
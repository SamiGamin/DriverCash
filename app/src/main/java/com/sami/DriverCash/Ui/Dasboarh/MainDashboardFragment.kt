package com.sami.DriverCash.Ui.Dasboarh

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.sami.DriverCash.Auth.AuthManager
import com.sami.DriverCash.Auth.LoginDialogFragment
import com.sami.DriverCash.Model.Local.Gasto
import com.sami.DriverCash.Model.Local.HorasTrabajadas
import com.sami.DriverCash.Model.Local.Ingreso
import com.sami.DriverCash.Model.Local.KilometrajeRecorrido
import com.sami.DriverCash.R
import com.sami.DriverCash.Ui.Adapter.DashboardAdapter
import com.sami.DriverCash.Ui.Model.DashboardCardItem
import com.sami.DriverCash.Ui.Model.DashboardCardType
import com.sami.DriverCash.Utils.CurrencyUtils
import com.sami.DriverCash.ViewModel.GastoViewModel
import com.sami.DriverCash.ViewModel.HorasTrabajadasViewModel
import com.sami.DriverCash.ViewModel.IngresoViewModel
import com.sami.DriverCash.ViewModel.KilometrajeRecorridoViewModel
import com.sami.DriverCash.ViewModel.VehicleViewModel
import com.sami.DriverCash.databinding.FragmentMainDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainDashboardFragment : Fragment() {
    private val TAG_DASHBOARD = "MainDashboardDebug"

    private var _binding: FragmentMainDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var dashboardAdapter: DashboardAdapter
    private val dashboardItemsList = mutableListOf<DashboardCardItem>()

    // ViewModels
    private val ingresoViewModel: IngresoViewModel by viewModels()
    private val vehicleViewModel: VehicleViewModel by viewModels()
    private val gastoViewModel: GastoViewModel by viewModels()
    private val horasTrabajadasViewModel: HorasTrabajadasViewModel by viewModels()
    private val kilometrajeViewModel: KilometrajeRecorridoViewModel by viewModels()

    @Inject
    lateinit var authManager: AuthManager

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cardViewTotalesHistoricos.isVisible = false
        setupRecyclerView()
        setupDashboardItems()
        observeViewModels()
        observeVehicleViewModel()
        setupBackupPromptCard()
        observeAuthStateAndSync()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    private fun setupBackupPromptCard() {
        binding.cardViewBackupPrompt.setOnClickListener {
            LoginDialogFragment().show(childFragmentManager, LoginDialogFragment.TAG)
        }
    }

    private fun observeAuthStateAndSync() {
        // Usa viewLifecycleOwner.lifecycleScope y repeatOnLifecycle para manejar el ciclo de vida
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authManager.getAuthStateFlow().collectLatest { firebaseUser ->
                    // El código dentro de collectLatest solo se ejecutará cuando la vista esté al menos en estado STARTED
                    // y se cancelará automáticamente cuando esté en STOPPED.
                    // Por lo tanto, el acceso a binding aquí es seguro.
                    val isLoggedIn = firebaseUser != null
                    binding.cardViewBackupPrompt.isVisible = !isLoggedIn
                    if (isLoggedIn) {
                        Log.d(TAG_DASHBOARD, "Usuario ha iniciado sesión: ${firebaseUser?.displayName}")
                        if (isNetworkAvailable()) {
                            Log.d(TAG_DASHBOARD, "Conexión disponible, sincronización automática DESACTIVADA TEMPORALMENTE PARA PRUEBAS.")
                            // Considera mostrar un indicador de carga aquí si la sincronización puede tardar
                            // binding.progressBarSync.isVisible = true // Ejemplo
                            // val backupResult = authManager.performFullUserBackup(firebaseUser!!)
                            // // binding.progressBarSync.isVisible = false // Ocultar después de la operación
                            // backupResult.fold(
                            //     onSuccess = {
                            //         Log.d(TAG_DASHBOARD, "Sincronización automática completada.")
                            //         // Toast.makeText(context, getString(R.string.sync_successful), Toast.LENGTH_SHORT).show()
                            //     },
                            //     onFailure = { exception ->
                            //         Log.e(TAG_DASHBOARD, "Error en sincronización automática.", exception)
                            //         // Toast.makeText(context, getString(R.string.sync_failed), Toast.LENGTH_LONG).show()
                            //     }
                            // )
                        } else {
                            Log.d(TAG_DASHBOARD, "No hay conexión de red para sincronización automática.")
                        }
                    } else {
                        Log.d(TAG_DASHBOARD, "Usuario no ha iniciado sesión. No se requiere sincronización automática.")
                    }
                }
            }
        }
    }

    private fun observeVehicleViewModel() {
        vehicleViewModel.vehiculoPredeterminado.observe(viewLifecycleOwner) { vehiculo ->
            if (vehiculo != null) {
                binding.tvTituloTotalesHistoricos.text = "Resumen Histórico (${vehiculo.modelo} ${vehiculo.placa})"
            } else {
                binding.tvTituloTotalesHistoricos.text = "Sin vehículo predeterminado"
            }
        }
    }

    private fun setupRecyclerView() {
        dashboardAdapter = DashboardAdapter(
            onAddItemClick = { item ->
                findNavController().navigate(item.addActionNavId)
            },
            onCardClick = { item ->
                item.detailsActionNavId?.let {
                    findNavController().navigate(it)
                }
            }
        )
        binding.rvDashboardItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = dashboardAdapter
        }
    }

    private fun setupDashboardItems() {
        dashboardItemsList.clear()
        dashboardItemsList.add(
            DashboardCardItem(
                DashboardCardType.INGRESOS,
                "Ingresos",
                "No hay ingresos recientes",
                R.id.nav_add_ingreso,
                R.id.nav_details
            )
        )
        dashboardItemsList.add(
            DashboardCardItem(
                DashboardCardType.GASTOS,
                "Gastos",
                "No hay gastos recientes",
                R.id.nav_add_gasto,
                R.id.nav_details
            )
        )
        dashboardItemsList.add(
            DashboardCardItem(
                DashboardCardType.HORAS_TRABAJADAS,
                "Horas Trabajadas",
                "Sin horas recientes",
                R.id.nav_add_horas_trabajadas,
                R.id.nav_details
            )
        )
        dashboardItemsList.add(
            DashboardCardItem(
                DashboardCardType.KILOMETRAJE,
                "Kilometraje",
                "Sin kilometraje reciente",
                R.id.nav_add_kilometraje,
                R.id.nav_details
            )
        )
        dashboardAdapter.submitList(dashboardItemsList.toList())
    }

    private fun observeViewModels() {
        ingresoViewModel.ultimoIngreso.observe(viewLifecycleOwner) { ultimoIngreso ->
            updateDashboardItemSummary(DashboardCardType.INGRESOS, formatIngresoSummary(ultimoIngreso))
        }
        gastoViewModel.ultimoGasto.observe(viewLifecycleOwner) { ultimoGasto ->
            updateDashboardItemSummary(DashboardCardType.GASTOS, formatGastoSummary(ultimoGasto))
        }
        horasTrabajadasViewModel.ultimasHorasTrabajadas.observe(viewLifecycleOwner) { ultimasHoras ->
            updateDashboardItemSummary(DashboardCardType.HORAS_TRABAJADAS, formatHorasTrabajadasSummary(ultimasHoras))
        }
        kilometrajeViewModel.ultimoKilometrajeRecorrido.observe(viewLifecycleOwner) { ultimoKm ->
            updateDashboardItemSummary(DashboardCardType.KILOMETRAJE, formatKilometrajeSummary(ultimoKm))
        }

        ingresoViewModel.totalIngresosHistoricos.observe(viewLifecycleOwner) { totalIngresos ->
            Log.d(TAG_DASHBOARD, "Total Ingresos Históricos observado: $totalIngresos")
            val ingresosStr = totalIngresos?.let { CurrencyUtils.formatCurrency(requireContext(), it) } ?: getText(R.string.not_available_short)
            binding.tvIngresosHistoricosValor.text = ingresosStr
            actualizarBalanceHistorico()
            actualizarVisibilidadCardViewTotales()
        }
        gastoViewModel.totalGastosHistoricos.observe(viewLifecycleOwner) { totalGastos ->
            Log.d(TAG_DASHBOARD, "Total Gastos Históricos observado: $totalGastos")
            val gastosStr = totalGastos?.let { CurrencyUtils.formatCurrency(requireContext(), it) } ?: getText(R.string.not_available_short)
            binding.tvGastosHistoricosValor.text = gastosStr
            actualizarBalanceHistorico()
            actualizarVisibilidadCardViewTotales()
        }
    }

    private fun actualizarBalanceHistorico() {
        val totalIngresos = ingresoViewModel.totalIngresosHistoricos.value ?: 0.0
        val totalGastos = gastoViewModel.totalGastosHistoricos.value ?: 0.0
        val balance = totalIngresos - totalGastos

        val balanceStr = CurrencyUtils.formatCurrency(requireContext(), balance)
        binding.tvNetoHistoricoValor.text = balanceStr
        Log.d(TAG_DASHBOARD, "Balance histórico actualizado: $balanceStr")
    }

    private fun actualizarVisibilidadCardViewTotales() {
        val ingresosCargados = ingresoViewModel.totalIngresosHistoricos.value != null
        val gastosCargados = gastoViewModel.totalGastosHistoricos.value != null
        val mostrarCardView = (ingresosCargados && (ingresoViewModel.totalIngresosHistoricos.value != null)) ||
                (gastosCargados && (gastoViewModel.totalGastosHistoricos.value != null)) ||
                (ingresoViewModel.totalIngresosHistoricos.value == 0.0 && gastoViewModel.totalGastosHistoricos.value == 0.0)

        Log.d(TAG_DASHBOARD, "actualizarVisibilidadCardViewTotales: ingresosCargados=$ingresosCargados, gastosCargados=$gastosCargados, mostrar=$mostrarCardView")
        binding.cardViewTotalesHistoricos.isVisible = mostrarCardView
    }

    private fun updateDashboardItemSummary(type: DashboardCardType, summary: String) {
        Log.d(TAG_DASHBOARD, "updateDashboardItemSummary para tipo: $type, resumen: $summary")
        val itemIndex = dashboardItemsList.indexOfFirst { it.type == type }
        if (itemIndex != -1) {
            val oldItem = dashboardItemsList[itemIndex]
            val newItem = oldItem.copy(lastEntrySummary = summary)
            dashboardItemsList[itemIndex] = newItem
            Log.d(TAG_DASHBOARD, "Enviando nueva lista al adapter. Tarjeta '$type' actualizada a '$summary'")
            dashboardAdapter.submitList(dashboardItemsList.toList())
        } else {
            Log.w(TAG_DASHBOARD, "No se encontró el tipo de tarjeta: $type para actualizar resumen.")
        }
    }

    private fun formatDate(timestamp: Long): String {
        return dateFormatter.format(Date(timestamp))
    }

    private fun formatIngresoSummary(ingreso: Ingreso?): String {
        return ingreso?.let {
            val formattedAmount = CurrencyUtils.formatCurrency(requireContext(), it.monto)
            "Último: $formattedAmount el ${formatDate(it.fecha)}"
        } ?: "No hay ingresos recientes"
    }

    private fun formatGastoSummary(gasto: Gasto?): String {
        return gasto?.let {
            val formattedAmount = CurrencyUtils.formatCurrency(requireContext(), it.monto)
            val categoriaFormatted = it.categoria.toString().replace("_", " ").lowercase()
                .replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString() }
            "Último: $formattedAmount ($categoriaFormatted) el ${formatDate(it.fecha)}"
        } ?: "No hay gastos recientes"
    }

    private fun formatHorasTrabajadasSummary(horas: HorasTrabajadas?): String {
        return horas?.let {
            val horasFormateadas = CurrencyUtils.formatDecimalHoursToHHMM(it.horas)
            "Últimas: $horasFormateadas hrs el ${formatDate(it.fecha)}"
        } ?: "Sin horas recientes"
    }

    private fun formatKilometrajeSummary(kilometraje: KilometrajeRecorrido?): String {
        return kilometraje?.let {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val distanceUnit = sharedPreferences.getString("pref_distance_unit", "km") ?: "km"
            val unitLabel = if (distanceUnit == "miles") "mi" else "km"
            val kmFormateados = if (it.kilometros % 1 == 0.0) String.format(Locale.US, "%.0f", it.kilometros)
            else String.format(Locale.US, "%.2f", it.kilometros)
            "Último: $kmFormateados $unitLabel el ${formatDate(it.fecha)}"
        } ?: "Sin kilometraje reciente"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

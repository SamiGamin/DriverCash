package com.sami.DriverCash.Ui.Detalles

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController // DESCOMENTADO
import androidx.recyclerview.widget.LinearLayoutManager
import com.sami.DriverCash.R
import com.sami.DriverCash.databinding.FragmentDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class DetailsFragment : Fragment() {

    private val TAG = "DetailsFragment"

    private val viewModel: DetailsViewModel by viewModels()
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var detallesAdapter: DetallesPrincipalAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView llamado")
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated llamado")

        setupRecyclerView()
        setupFilterButtons()
        setupPeriodNavigationPanel()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView llamado")
        detallesAdapter = DetallesPrincipalAdapter { item ->
            viewModel.gestionarClicEnItemRv(item)
        }

        binding.rvDetallesPrincipal.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = detallesAdapter
        }
    }

    private fun setupFilterButtons() {
        Log.d(TAG, "setupFilterButtons llamado")
        binding.btnFilterDay.setOnClickListener {
            Log.d(TAG, "Botón Filtro DIA presionado")
            viewModel.seleccionarFiltroPrincipal(PeriodoFiltro.DIA)
        }
        binding.btnFilterWeek.setOnClickListener {
            Log.d(TAG, "Botón Filtro SEMANA presionado")
            viewModel.seleccionarFiltroPrincipal(PeriodoFiltro.SEMANA)
        }
        binding.btnFilterMonth.setOnClickListener {
            Log.d(TAG, "Botón Filtro MES presionado")
            viewModel.seleccionarFiltroPrincipal(PeriodoFiltro.MES)
        }
        binding.btnFilterYear.setOnClickListener {
            Log.d(TAG, "Botón Filtro AÑO presionado")
            viewModel.seleccionarFiltroPrincipal(PeriodoFiltro.ANO)
        }
    }

    private fun setupPeriodNavigationPanel() {
        Log.d(TAG, "setupPeriodNavigationPanel llamado")
        binding.btnPreviousPeriod.setOnClickListener {
            Log.d(TAG, "Botón Periodo Anterior presionado")
            viewModel.navegarAPeriodoAnterior()
        }
        binding.btnNextPeriod.setOnClickListener {
            Log.d(TAG, "Botón Periodo Siguiente presionado")
            viewModel.navegarAPeriodoPosterior()
        }
        binding.tvCurrentPeriod.setOnClickListener {
            Log.d(TAG, "TextView Periodo Actual presionado, mostrando DatePicker")
            mostrarDatePickerDialog()
        }
    }

    private fun mostrarDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                Log.d(TAG, "Fecha seleccionada: $selectedDayOfMonth/${selectedMonth + 1}/$selectedYear")
                viewModel.seleccionarFechaEspecifica(selectedYear, selectedMonth, selectedDayOfMonth)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun updateFilterButtonStates(selectedFiltro: PeriodoFiltro) {
        Log.d(TAG, "Actualizando estado de botones de filtro, seleccionado: $selectedFiltro")

        val activeBackgroundColor = ContextCompat.getColor(requireContext(), R.color.md_theme_onSecondaryFixedVariant)
        val activeTextColor = ContextCompat.getColor(requireContext(), R.color.md_theme_tertiaryContainer_mediumContrast)
        val inactiveBackgroundColor = ContextCompat.getColor(requireContext(), R.color.md_theme_surfaceVariant)
        val inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.md_theme_inversePrimary)

        val filterButtons = listOf(
            binding.btnFilterDay,
            binding.btnFilterWeek,
            binding.btnFilterMonth,
            binding.btnFilterYear
        )

        filterButtons.forEach { button ->
            button.backgroundTintList = ColorStateList.valueOf(inactiveBackgroundColor)
            button.setTextColor(inactiveTextColor)
        }

        when (selectedFiltro) {
            PeriodoFiltro.DIA -> {
                binding.btnFilterDay.backgroundTintList = ColorStateList.valueOf(activeBackgroundColor)
                binding.btnFilterDay.setTextColor(activeTextColor)
            }
            PeriodoFiltro.SEMANA -> {
                binding.btnFilterWeek.backgroundTintList = ColorStateList.valueOf(activeBackgroundColor)
                binding.btnFilterWeek.setTextColor(activeTextColor)
            }
            PeriodoFiltro.MES -> {
                binding.btnFilterMonth.backgroundTintList = ColorStateList.valueOf(activeBackgroundColor)
                binding.btnFilterMonth.setTextColor(activeTextColor)
            }
            PeriodoFiltro.ANO -> {
                binding.btnFilterYear.backgroundTintList = ColorStateList.valueOf(activeBackgroundColor)
                binding.btnFilterYear.setTextColor(activeTextColor)
            }
        }
    }

    private fun observeViewModel() {
        Log.d(TAG, "observeViewModel llamado")

        viewModel.listaResumenesRv.observe(viewLifecycleOwner) { lista ->
            Log.d(TAG, "Observador listaResumenesRv: ${lista.size} items")
            detallesAdapter.submitList(lista)
            binding.tvNoDataDetails.isVisible = lista.isEmpty()
            binding.rvDetallesPrincipal.isVisible = lista.isNotEmpty()
        }

        viewModel.textoPeriodoSeleccionado.observe(viewLifecycleOwner) { textoPeriodo ->
            Log.d(TAG, "Observador textoPeriodoSeleccionado: $textoPeriodo")
            binding.tvCurrentPeriod.text = textoPeriodo
        }

        viewModel.filtroPrincipalActual.observe(viewLifecycleOwner) { filtro ->
            Log.d(TAG, "Observador filtroPrincipalActual: $filtro")
            if (filtro != null) {
                updateFilterButtonStates(filtro)
            }
        }

        viewModel.navegarADetalleDia.observe(viewLifecycleOwner) { timestamp ->
            timestamp?.let { // Solo actuar si el timestamp no es null
                Log.d(TAG, "Navegando a detalle del día con timestamp: $it")
                // Navegación real usando Safe Args
                val action = DetailsFragmentDirections.actionDetailsFragmentToHistorialDiaFragment(it)
                findNavController().navigate(action)

                // Notificamos al ViewModel que la navegación se ha gestionado.
                viewModel.onNavegacionADetalleDiaCompletada()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView llamado")
        _binding = null
    }

    companion object {
        // No es necesario si Hilt lo maneja y no hay argumentos
    }
}

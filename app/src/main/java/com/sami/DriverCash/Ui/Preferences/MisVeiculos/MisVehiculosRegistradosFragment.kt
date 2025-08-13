package com.sami.DriverCash.Ui.Preferences.MisVeiculos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sami.DriverCash.Model.Local.Vehicle // Asegúrate de que Vehicle esté importado
import com.sami.DriverCash.R
import com.sami.DriverCash.ViewModel.VehicleViewModel
import com.sami.DriverCash.databinding.FragmentMisVehiculosRegistradosBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MisVehiculosRegistradosFragment : Fragment() {

    private var _binding: FragmentMisVehiculosRegistradosBinding? = null
    private val binding get() = _binding!!

    private val vehicleViewModel: VehicleViewModel by viewModels()
    private lateinit var vehiculoAdapter: VehiculoRegistradoAdapter
    private var navigatedDueToEmptyList = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMisVehiculosRegistradosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigatedDueToEmptyList = false // Reset flag

        setupRecyclerView()

        binding.fabAddVehiculo.setOnClickListener {
             // Navegar a AddVehicleFragment sin argumentos (para añadir nuevo)
            findNavController().navigate(R.id.action_misVehiculosRegistradosFragment_to_addVehicleFragment)
        }

        vehicleViewModel.allVehicles.observe(viewLifecycleOwner) { vehicles ->
            if (vehicles.isNullOrEmpty()) {
                binding.tvNoVehiculos.visibility = View.VISIBLE
                binding.rvVehiculosRegistrados.visibility = View.GONE
                if (!navigatedDueToEmptyList && isAdded) {
                    navigatedDueToEmptyList = true
                    findNavController().navigate(R.id.action_misVehiculosRegistradosFragment_to_addVehicleFragment)
                }
            } else {
                binding.tvNoVehiculos.visibility = View.GONE
                binding.rvVehiculosRegistrados.visibility = View.VISIBLE
                vehiculoAdapter.submitList(vehicles)
            }
        }
    }

    private fun setupRecyclerView() {
        vehiculoAdapter = VehiculoRegistradoAdapter(
            onItemClicked = { vehicle ->
                vehicleViewModel.establecerComoPredeterminado(vehicle.id)
                Toast.makeText(requireContext(), "${vehicle.marca} ${vehicle.modelo} (${vehicle.placa}) establecido como predeterminado", Toast.LENGTH_SHORT).show()
                 findNavController().popBackStack()
            },
            onItemLongClicked = { vehicle ->
                showEditDeleteDialog(vehicle)
                true // Indica que el evento de clic largo ha sido consumido
            }
        )
        binding.rvVehiculosRegistrados.apply {
            adapter = vehiculoAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun showEditDeleteDialog(vehicle: Vehicle) {
        val options = arrayOf("Editar", "Eliminar")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Vehículo: ${vehicle.marca} ${vehicle.modelo}")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        // Acción para Editar: Navegar a AddVehicleFragment con el ID del vehículo
                        val bundle = bundleOf("vehicleId" to vehicle.id)
                        findNavController().navigate(R.id.action_misVehiculosRegistradosFragment_to_addVehicleFragment, bundle)
                    }
                    1 -> {
                        // Acción para Eliminar - Mostrar diálogo de confirmación
                        showConfirmDeleteDialog(vehicle)
                    }
                }
            }
            .show()
    }

    private fun showConfirmDeleteDialog(vehicle: Vehicle) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar el vehículo ${vehicle.marca} ${vehicle.modelo} (${vehicle.placa})? Esta acción no se puede deshacer.")
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Sí, eliminar") { dialog, _ ->
                vehicleViewModel.delete(vehicle)
                Toast.makeText(requireContext(), "Vehículo eliminado: ${vehicle.placa}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.sami.DriverCash.Ui.Preferences.MisVeiculos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sami.DriverCash.Model.Local.Vehicle
import com.sami.DriverCash.R
import com.sami.DriverCash.ViewModel.VehicleViewModel
import com.sami.DriverCash.databinding.FragmentVehicleBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class AddVehicleFragment : Fragment() {
    private var _binding: FragmentVehicleBinding? = null
    private val binding get() = _binding!!

    private lateinit var vehicleViewModel: VehicleViewModel
    private val args: AddVehicleFragmentArgs by navArgs()
    private var currentVehicleId: Long = -1L
    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVehicleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vehicleViewModel = ViewModelProvider(this)[VehicleViewModel::class.java]
        currentVehicleId = args.vehicleId
        isEditMode = currentVehicleId != -1L

        setupFuelTypeSpinner()

        observeVehicleToEdit()
        if (isEditMode) {
            setupEditMode()
        } else {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Añadir Vehículo"
            binding.btnSave.text = "Guardar Vehículo"
        }

        binding.btnSave.setOnClickListener {
            saveOrUpdateVehicle()
        }
    }
    private fun observeVehicleToEdit() {
        vehicleViewModel.vehiculoSeleccionadoParaEditar.observe(viewLifecycleOwner) { vehicle ->
            // El LiveData ha emitido un valor. Si no es nulo, rellenamos el formulario.
            vehicle?.let {
                populateForm(it)
            }
        }
    }


    private fun setupFuelTypeSpinner() {
        val fuelTypes = resources.getStringArray(R.array.fuel_type_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, fuelTypes)
        binding.etFuelType.setAdapter(adapter)
    }

    private fun setupEditMode() {
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Editar Vehículo"
        binding.btnSave.text = "Actualizar Vehículo"
        lifecycleScope.launch {
            vehicleViewModel.cargarVehiculoParaEditar(currentVehicleId)
        }
    }

    private fun populateForm(vehicle: Vehicle) {
        binding.etPlate.setText(vehicle.placa)
        binding.etBrand.setText(vehicle.marca)
        binding.etModel.setText(vehicle.modelo)
        binding.etYear.setText(vehicle.anio.toString())
        binding.etColor.setText(vehicle.color)
        binding.etFuelType.setText(vehicle.tipoCombustible, false) // false para no filtrar
        // Asegurarse de que esPredeterminado se maneje si es necesario en este formulario
    }

    private fun saveOrUpdateVehicle() {
        val placa = binding.etPlate.text.toString().trim()
        val marca = binding.etBrand.text.toString().trim()
        val modelo = binding.etModel.text.toString().trim()
        val yearString = binding.etYear.text.toString().trim()
        val color = binding.etColor.text.toString().trim()
        val tipoCombustible = binding.etFuelType.text.toString().trim()

        if (placa.isEmpty() || marca.isEmpty() || modelo.isEmpty() || yearString.isEmpty() || tipoCombustible.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val year = yearString.toIntOrNull()
        if (year == null) {
            Toast.makeText(requireContext(), "El año debe ser un número válido", Toast.LENGTH_SHORT).show()
            return
        }

        val vehicle = Vehicle(
            id = if (isEditMode) currentVehicleId else 0, // Usar el ID actual si es modo edición
            placa = placa,
            marca = marca,
            modelo = modelo,
            anio = year,
            color = color,
            tipoCombustible = tipoCombustible,
            esPredeterminado = if(isEditMode) {
                                   // Si estamos editando, necesitamos saber si el vehículo que se está editando ERA el predeterminado.
                                   // Esta lógica puede necesitar un ajuste si el campo 'esPredeterminado' se puede editar aquí.
                                   // Por ahora, asumimos que no se cambia aquí, o necesitamos cargar el estado 'esPredeterminado' original.
                                   vehicleViewModel.vehiculoPredeterminado.value?.id == currentVehicleId
                               } else false // Los nuevos vehículos no son predeterminados por defecto
        )

        if (isEditMode) {
            vehicleViewModel.update(vehicle)
            Toast.makeText(requireContext(), "Vehículo actualizado", Toast.LENGTH_SHORT).show()
        } else {
            vehicleViewModel.insert(vehicle)
            Toast.makeText(requireContext(), "Vehículo guardado", Toast.LENGTH_SHORT).show()
        }
        // Navegar de vuelta a la lista de vehículos (MisVehiculosRegistradosFragment)
        // La acción y el popUpTo ya están configurados en nav_graph.xml
        findNavController().navigate(R.id.action_nav_add_vehicle_to_nav_mis_vehiculos_registrados)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

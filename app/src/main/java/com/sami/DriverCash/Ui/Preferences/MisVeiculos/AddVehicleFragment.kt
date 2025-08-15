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
import com.sami.DriverCash.Model.Local.TipoCombustible // Importa tu enum
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
            vehicle?.let {
                populateForm(it)
            }
        }
    }

    private fun setupFuelTypeSpinner() {
        // Asume que R.array.fuel_type_options en strings.xml contiene los nombres de los enums
        // ej: <string-array name="fuel_type_options">
        //         <item>GASOLINA</item>
        //         <item>DIESEL</item>
        //         ...
        //     </string-array>
        // O si quieres mostrar nombres más amigables, necesitarás un mapeo.
        // Por ahora, asumimos que los valores en el array son los nombres de los enums.
        val fuelTypeNames = TipoCombustible.values().map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, fuelTypeNames)
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
        // Convertir el enum a String para mostrarlo. Si es null, poner texto vacío.
        binding.etFuelType.setText(vehicle.tipoCombustible?.name ?: "", false)
        // Asegurarse de que esPredeterminado se maneje si es necesario en este formulario
        // binding.switchPredeterminado.isChecked = vehicle.esPredeterminado // Ejemplo si tuvieras un switch
    }

    private fun saveOrUpdateVehicle() {
        val placa = binding.etPlate.text.toString().trim()
        val marca = binding.etBrand.text.toString().trim()
        val modelo = binding.etModel.text.toString().trim()
        val yearString = binding.etYear.text.toString().trim()
        val color = binding.etColor.text.toString().trim()
        val tipoCombustibleString = binding.etFuelType.text.toString().trim()

        if (placa.isEmpty() || marca.isEmpty() || modelo.isEmpty() || yearString.isEmpty() || tipoCombustibleString.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val year = yearString.toIntOrNull()
        if (year == null) {
            Toast.makeText(requireContext(), "El año debe ser un número válido", Toast.LENGTH_SHORT).show()
            return
        }

        // Convertir el String del AutoCompleteTextView de nuevo al enum TipoCombustible
        val tipoCombustibleEnum: TipoCombustible? = try {
            if (tipoCombustibleString.isNotBlank()) TipoCombustible.valueOf(tipoCombustibleString.uppercase()) else null
        } catch (e: IllegalArgumentException) {
            null // O manejar el error, por ejemplo, mostrando un Toast y retornando
        }

        if (tipoCombustibleString.isNotBlank() && tipoCombustibleEnum == null) {
            Toast.makeText(requireContext(), "Tipo de combustible no válido", Toast.LENGTH_SHORT).show()
            return // No continuar si el tipo de combustible es inválido pero no estaba vacío
        }
        
        // Aquí necesitarías también obtener los valores para apodo y numeroEconomico si los tienes en el layout
        // val apodo = binding.etApodo.text.toString().trim() // Ejemplo
        // val numeroEconomico = binding.etNumeroEconomico.text.toString().trim() // Ejemplo

        val vehicle = Vehicle(
            id = if (isEditMode) currentVehicleId else 0,
            placa = placa,
            marca = marca,
            modelo = modelo,
            anio = year,
            color = color,
            tipoCombustible = tipoCombustibleEnum, // Usar el enum convertido
            esPredeterminado = if (isEditMode) {
                vehicleViewModel.vehiculoPredeterminado.value?.id == currentVehicleId
            } else false,
            apodo = "", // Recuperar de tu UI si tienes un campo para apodo
            numeroEconomico = "" // Recuperar de tu UI si tienes un campo para numeroEconomico
            // Asegúrate de pasar todos los campos que tu Vehicle necesite
        )

        if (isEditMode) {
            vehicleViewModel.update(vehicle)
            Toast.makeText(requireContext(), "Vehículo actualizado", Toast.LENGTH_SHORT).show()
        } else {
            vehicleViewModel.insert(vehicle)
            Toast.makeText(requireContext(), "Vehículo guardado", Toast.LENGTH_SHORT).show()
        }
        findNavController().navigate(R.id.action_nav_add_vehicle_to_nav_mis_vehiculos_registrados)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

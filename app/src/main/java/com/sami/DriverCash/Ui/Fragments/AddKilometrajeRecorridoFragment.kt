package com.sami.DriverCash.Ui.Fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.sami.DriverCash.Model.Local.KilometrajeRecorrido
import com.sami.DriverCash.ViewModel.KilometrajeRecorridoViewModel
import com.sami.DriverCash.ViewModel.VehicleViewModel
import com.sami.DriverCash.databinding.FragmentAddKilometrajeRecorridoBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class AddKilometrajeRecorridoFragment : Fragment() {

    private val TAG = "AddKilometrajeFragment" // Tag para logs
    private var _binding: FragmentAddKilometrajeRecorridoBinding? = null
    private val binding get() = _binding!!

    private val kilometrajeViewModel: KilometrajeRecorridoViewModel by viewModels()
    private val vehicleViewModel: VehicleViewModel by viewModels()
    private val args: AddKilometrajeRecorridoFragmentArgs by navArgs()

    private val calendar = Calendar.getInstance()
    private var kilometrajeActual: KilometrajeRecorrido? = null
    private var esModoEdicion = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddKilometrajeRecorridoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val kilometrajeId = args.kilometrajeId
        esModoEdicion = kilometrajeId != -1L
        Log.d(TAG, "onViewCreated - kilometrajeId: $kilometrajeId, esModoEdicion: $esModoEdicion")

        loadDistanceUnitPreference()
        setupDatePicker()

        if (esModoEdicion) {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Editar Kilometraje"
            binding.btnGuardarKilometraje.text = "Actualizar Kilometraje"
            binding.btnGuardarKilometraje.isEnabled = true // Habilitado por defecto en modo edición
            cargarDatosKilometraje(kilometrajeId)
        } else {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Registrar Kilometraje"
            binding.btnGuardarKilometraje.text = "Guardar Kilometraje"
            binding.btnGuardarKilometraje.isEnabled = false // Deshabilitado hasta que se confirme vehículo
            vehicleViewModel.vehiculoPredeterminado.observe(viewLifecycleOwner) { vehiculo ->
                if (vehiculo != null) {
                    binding.btnGuardarKilometraje.isEnabled = true
                } else {
                    binding.btnGuardarKilometraje.isEnabled = false
                    if(isAdded){
                        Toast.makeText(requireContext(), "Configure un vehículo predeterminado para guardar.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            updateDateInView() // Fecha actual para nuevo registro
        }

        binding.btnGuardarKilometraje.setOnClickListener {
            saveKilometrajeRecorrido()
        }
    }

    private fun cargarDatosKilometraje(id: Long) {
        kilometrajeViewModel.getKilometrajeRecorridoById(id).observe(viewLifecycleOwner) { kilometraje ->
            if (kilometraje != null) {
                kilometrajeActual = kilometraje
                Log.d(TAG, "Datos cargados para edición: $kilometraje")
                poblarUiConKilometraje(kilometraje)
            } else {
                Log.w(TAG, "No se encontró KilometrajeRecorrido con ID: $id")
                Toast.makeText(requireContext(), "Error al cargar los datos.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun poblarUiConKilometraje(kilometraje: KilometrajeRecorrido) {
        val kmValue = kilometraje.kilometros
        val formattedKm = if (kmValue % 1.0 == 0.0) { // Comprueba si es un número entero
            String.format(Locale.US, "%.0f", kmValue) // Formato sin decimales
        } else {
            String.format(Locale.US, "%.2f", kmValue).replace(",", ".") // Formato con hasta 2 decimales
        }
        binding.etCantidadKilometros.setText(formattedKm)
        binding.etDescripcionKilometraje.setText(kilometraje.descripcion ?: "")
        calendar.timeInMillis = kilometraje.fecha
        updateDateInView()
        Log.d(TAG, "UI poblada con: $kilometraje, km formateados: $formattedKm")
    }

    private fun loadDistanceUnitPreference() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val distanceUnit = sharedPreferences.getString("pref_distance_unit", "km") // "km" es el defaultValue

        if (distanceUnit == "miles") {
            binding.tilCantidadKilometros.hint = "Cantidad de Millas"
        } else {
            binding.tilCantidadKilometros.hint = "Cantidad de Kilómetros"
        }
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        binding.etFechaKilometraje.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        // Solo llama a updateDateInView aquí si no es modo edición, 
        // en modo edición la fecha se establece en poblarUiConKilometraje
        if (!esModoEdicion) {
            updateDateInView()
        }
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etFechaKilometraje.setText(sdf.format(calendar.time))
    }

    private fun saveKilometrajeRecorrido() {
        val kilometrosString = binding.etCantidadKilometros.text.toString().trim().replace(",", ".")
        val descripcion = binding.etDescripcionKilometraje.text.toString().trim()
        val fechaTimestamp = calendar.timeInMillis

        if (kilometrosString.isEmpty()) {
            val currentHint = binding.tilCantidadKilometros.hint
            Toast.makeText(requireContext(), "La $currentHint no puede estar vacía", Toast.LENGTH_SHORT).show()
            return
        }

        val kilometros = kilometrosString.toDoubleOrNull()
        if (kilometros == null || kilometros <= 0) {
            val currentHint = binding.tilCantidadKilometros.hint
            Toast.makeText(requireContext(), "Ingresa una $currentHint válida", Toast.LENGTH_SHORT).show()
            return
        }

        if (esModoEdicion) {
            if (kilometrajeActual != null) {
                val kilometrajeActualizado = kilometrajeActual!!.copy(
                    fecha = fechaTimestamp,
                    kilometros = kilometros,
                    descripcion = descripcion.ifEmpty { null }
                    // vehiculoId e id se mantienen del kilometrajeActual
                )
                Log.d(TAG, "Actualizando KilometrajeRecorrido: $kilometrajeActualizado")
                kilometrajeViewModel.update(kilometrajeActualizado)
                Toast.makeText(requireContext(), "Kilometraje actualizado correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Log.e(TAG, "Error en modo edición: kilometrajeActual es null")
                Toast.makeText(requireContext(), "Error al actualizar los datos.", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            val vehiculoIdPredeterminado = vehicleViewModel.vehiculoPredeterminado.value?.id
            if (vehiculoIdPredeterminado == null) {
                Toast.makeText(requireContext(), "No hay vehículo predeterminado. Configúralo en Preferencias.", Toast.LENGTH_LONG).show()
                return
            }
            val nuevoKilometraje = KilometrajeRecorrido(
                vehiculoId = vehiculoIdPredeterminado,
                fecha = fechaTimestamp,
                kilometros = kilometros,
                descripcion = descripcion.ifEmpty { null }
            )
            Log.d(TAG, "Insertando nuevo KilometrajeRecorrido: $nuevoKilometraje")
            kilometrajeViewModel.insert(nuevoKilometraje)
            Toast.makeText(requireContext(), "Registro de kilometraje guardado", Toast.LENGTH_SHORT).show()
        }
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

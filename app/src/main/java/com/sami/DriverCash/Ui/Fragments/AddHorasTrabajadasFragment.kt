package com.sami.DriverCash.Ui.Fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.sami.DriverCash.Model.Local.HorasTrabajadas
import com.sami.DriverCash.Utils.CurrencyUtils // Asegurar esta importación
import com.sami.DriverCash.ViewModel.HorasTrabajadasViewModel
import com.sami.DriverCash.ViewModel.VehicleViewModel
import com.sami.DriverCash.databinding.FragmentAddHorasTrabajadasBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@AndroidEntryPoint
class AddHorasTrabajadasFragment : Fragment() {

    private val TAG = "AddHorasFragment"
    private var _binding: FragmentAddHorasTrabajadasBinding? = null
    private val binding get() = _binding!!

    private val horasTrabajadasViewModel: HorasTrabajadasViewModel by viewModels()
    private val vehicleViewModel: VehicleViewModel by viewModels()
    private val args: AddHorasTrabajadasFragmentArgs by navArgs()

    private val mainCalendar = Calendar.getInstance()
    private val horaInicioCalendar = Calendar.getInstance()
    private val horaFinCalendar = Calendar.getInstance()
    private var horaInicioSet = false
    private var horaFinSet = false

    private var is24HourFormat = true
    private lateinit var timeFormatPattern: String

    private var horasTrabajadasActual: HorasTrabajadas? = null
    private var esModoEdicion = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddHorasTrabajadasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val horasId = args.horasTrabajadasId
        esModoEdicion = horasId != -1L
        Log.d(TAG, "onViewCreated - horasTrabajadasId: $horasId, esModoEdicion: $esModoEdicion")

        loadTimeFormatPreference()
        setupDatePicker()
        setupTimePickers()

        if (!esModoEdicion) {
            binding.tvDuracionCalculada.text = ""
        } else {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Editar Horas"
            binding.btnGuardarHoras.text = "Actualizar Horas"
            binding.btnGuardarHoras.isEnabled = true
            cargarDatosHorasTrabajadas(horasId)
        }

        if (!esModoEdicion) {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Registrar Horas"
            binding.btnGuardarHoras.text = "Guardar Horas"
            binding.btnGuardarHoras.isEnabled = false
            vehicleViewModel.vehiculoPredeterminado.observe(viewLifecycleOwner) { vehiculo ->
                if (vehiculo != null) {
                    binding.btnGuardarHoras.isEnabled = true
                } else {
                    binding.btnGuardarHoras.isEnabled = false
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Configure un vehículo predeterminado para guardar.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            updateDateInView()
        }

        binding.btnGuardarHoras.setOnClickListener {
            saveHorasTrabajadas()
        }
    }

    private fun cargarDatosHorasTrabajadas(id: Long) {
        horasTrabajadasViewModel.getHorasTrabajadasById(id).observe(viewLifecycleOwner) { horas ->
            if (horas != null) {
                horasTrabajadasActual = horas
                Log.d(TAG, "Datos cargados para edición: $horas")
                poblarUiConHorasTrabajadas(horas)
            } else {
                Log.w(TAG, "No se encontraron HorasTrabajadas con ID: $id")
                Toast.makeText(requireContext(), "Error al cargar los datos.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun poblarUiConHorasTrabajadas(horas: HorasTrabajadas) {
        mainCalendar.timeInMillis = horas.fecha
        updateDateInView()

        binding.etDescripcionHoras.setText(horas.descripcion ?: "")

        binding.etCantidadHoras.setText(CurrencyUtils.formatDecimalHoursToHHMM(horas.horas))
        binding.etCantidadHoras.hint = "HH:MM o N.N"

        binding.etHoraInicio.text = null
        binding.etHoraFin.text = null
        binding.tvDuracionCalculada.text = ""
        horaInicioSet = false
        horaFinSet = false
        Log.d(TAG, "UI poblada con: ${horas}. etCantidadHoras seteado a: ${binding.etCantidadHoras.text}")
    }

    private fun loadTimeFormatPreference() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val timeFormatValue = sharedPreferences.getString("pref_time_format", "24")
        is24HourFormat = timeFormatValue == "24"
        timeFormatPattern = if (is24HourFormat) "HH:mm" else "hh:mm a"
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            mainCalendar.set(Calendar.YEAR, year)
            mainCalendar.set(Calendar.MONTH, monthOfYear)
            mainCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
            if (horaInicioSet && horaFinSet) {
                calculateAndDisplayDuration()
            }
        }
        binding.etFechaHoras.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                mainCalendar.get(Calendar.YEAR),
                mainCalendar.get(Calendar.MONTH),
                mainCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        if (!esModoEdicion) updateDateInView()
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etFechaHoras.setText(sdf.format(mainCalendar.time))
    }

    private fun setupTimePickers() {
        val timeFormatter = SimpleDateFormat(timeFormatPattern, Locale.getDefault())

        binding.etHoraInicio.setOnClickListener {
            val currentHour = if (horaInicioSet) horaInicioCalendar.get(Calendar.HOUR_OF_DAY) else mainCalendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = if (horaInicioSet) horaInicioCalendar.get(Calendar.MINUTE) else mainCalendar.get(Calendar.MINUTE)
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                horaInicioCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                horaInicioCalendar.set(Calendar.MINUTE, minute)
                binding.etHoraInicio.setText(timeFormatter.format(horaInicioCalendar.time))
                horaInicioSet = true
                if (horaFinSet) calculateAndDisplayDuration()
            }
            TimePickerDialog(requireContext(), timeSetListener, currentHour, currentMinute, is24HourFormat).show()
        }

        binding.etHoraFin.setOnClickListener {
            val currentHour = if (horaFinSet) horaFinCalendar.get(Calendar.HOUR_OF_DAY) else mainCalendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = if (horaFinSet) horaFinCalendar.get(Calendar.MINUTE) else mainCalendar.get(Calendar.MINUTE)
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                horaFinCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                horaFinCalendar.set(Calendar.MINUTE, minute)
                binding.etHoraFin.setText(timeFormatter.format(horaFinCalendar.time))
                horaFinSet = true
                if (horaInicioSet) calculateAndDisplayDuration()
            }
            TimePickerDialog(requireContext(), timeSetListener, currentHour, currentMinute, is24HourFormat).show()
        }
    }

    private fun calculateAndDisplayDuration(): Double? {
        if (!horaInicioSet || !horaFinSet) {
            binding.tvDuracionCalculada.text = ""
            Log.d(TAG, "calculateAndDisplayDuration: Hora inicio o fin no establecida.")
            return null
        }

        val startCal = mainCalendar.clone() as Calendar
        startCal.set(Calendar.HOUR_OF_DAY, horaInicioCalendar.get(Calendar.HOUR_OF_DAY))
        startCal.set(Calendar.MINUTE, horaInicioCalendar.get(Calendar.MINUTE))
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)

        val endCal = mainCalendar.clone() as Calendar
        endCal.set(Calendar.HOUR_OF_DAY, horaFinCalendar.get(Calendar.HOUR_OF_DAY))
        endCal.set(Calendar.MINUTE, horaFinCalendar.get(Calendar.MINUTE))
        endCal.set(Calendar.SECOND, 0)
        endCal.set(Calendar.MILLISECOND, 0)

        Log.d(TAG, "Fecha base (mainCalendar): ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(mainCalendar.time)}")
        Log.d(TAG, "Hora inicio seleccionada: ${SimpleDateFormat(timeFormatPattern, Locale.getDefault()).format(horaInicioCalendar.time)}")
        Log.d(TAG, "Hora fin seleccionada: ${SimpleDateFormat(timeFormatPattern, Locale.getDefault()).format(horaFinCalendar.time)}")
        Log.d(TAG, "Calculando con Start: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(startCal.time)}")
        Log.d(TAG, "Calculando con End: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(endCal.time)}")

        if (endCal.timeInMillis <= startCal.timeInMillis) {
            Log.d(TAG, "Hora de fin es <= hora de inicio. Asumiendo día siguiente para la hora de fin.")
            endCal.add(Calendar.DAY_OF_YEAR, 1)
            Log.d(TAG, "Nueva hora de fin ajustada: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(endCal.time)}")
        }

        val diffInMillis = endCal.timeInMillis - startCal.timeInMillis
        Log.d(TAG, "Diferencia en milisegundos: $diffInMillis")

        if (diffInMillis <= 0) {
            binding.tvDuracionCalculada.text = "Error: Duración inválida"
            binding.etCantidadHoras.setText("")
            Log.w(TAG, "Error en cálculo: diffInMillis es ${diffInMillis} después de ajustes.")
            return null
        }

        val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis) % 60
        val durationDecimal = diffInMillis / (1000.0 * 60.0 * 60.0)

        Log.d(TAG, "Duración calculada: $hours horas, $minutes minutos ($durationDecimal decimal)")

        binding.tvDuracionCalculada.text = String.format(Locale.getDefault(), "Duración: %d h %d min", hours, minutes)
        binding.etCantidadHoras.setText(CurrencyUtils.formatDecimalHoursToHHMM(durationDecimal))

        return durationDecimal
    }

    private fun parseHorasStringToDecimalOrNull(input: String): Double? {
        val trimmedInput = input.trim()

        val timeRegex = """(\d{1,2})\s*[:.]\s*(\d{1,2})""".toRegex()
        val matchResult = timeRegex.matchEntire(trimmedInput)

        if (matchResult != null) {
            return try {
                val hours = matchResult.groupValues[1].toInt()
                val minutes = matchResult.groupValues[2].toInt()
                if (hours >= 0 && minutes in 0..59) {
                    hours.toDouble() + (minutes.toDouble() / 60.0)
                } else {
                    Log.w(TAG, "Formato HH:MM/HH.MM inválido: Horas=$hours, Minutos=$minutes")
                    null
                }
            } catch (e: NumberFormatException) {
                Log.e(TAG, "Error al parsear HH:MM/HH.MM: $trimmedInput", e)
                null
            }
        }
        return try {
            val decimalValue = trimmedInput.replace(',', '.').toDouble()
            if (decimalValue >= 0) {
                decimalValue
            } else {
                Log.w(TAG, "Formato decimal inválido (negativo): $trimmedInput")
                null
            }
        } catch (e: NumberFormatException) {
            Log.w(TAG, "No es un formato de hora reconocible: $trimmedInput")
            null
        }
    }

    private fun saveHorasTrabajadas() {
        val descripcion = binding.etDescripcionHoras.text.toString().trim()
        val fechaTimestamp = mainCalendar.timeInMillis
        var horasDecimalesParaGuardar: Double? = null
        val cantidadHorasInput = binding.etCantidadHoras.text.toString().trim()

        if (horaInicioSet && horaFinSet) {
            horasDecimalesParaGuardar = calculateAndDisplayDuration()
            if (horasDecimalesParaGuardar == null) {
                Toast.makeText(requireContext(), "Error en la duración calculada. Verifique las horas de inicio y fin.", Toast.LENGTH_LONG).show()
                return
            }
        } else if (cantidadHorasInput.isNotEmpty()) {
            horasDecimalesParaGuardar = parseHorasStringToDecimalOrNull(cantidadHorasInput)
            if (horasDecimalesParaGuardar == null) {
                Toast.makeText(requireContext(), "Formato de horas inválido en 'Cantidad'. Use HH:MM, H.M (ej. 8.17 para 8h 17m) o un número decimal (ej. 2.5 para 2h 30m).", Toast.LENGTH_LONG).show()
                return
            }
        } else {
            Toast.makeText(requireContext(), "Ingrese las horas trabajadas, ya sea con Hora Inicio/Fin o en 'Cantidad de Horas'.", Toast.LENGTH_LONG).show()
            return
        }

        if (horasDecimalesParaGuardar <= 0.0) {
            Toast.makeText(requireContext(), "La cantidad de horas debe ser mayor a cero.", Toast.LENGTH_SHORT).show()
            return
        }

        val vehiculoIdPredeterminadoActual = vehicleViewModel.vehiculoPredeterminado.value?.id

        if (esModoEdicion) {
            if (horasTrabajadasActual != null) {
                val horasActualizadas = horasTrabajadasActual!!.copy(
                    fecha = fechaTimestamp,
                    horas = horasDecimalesParaGuardar,
                    descripcion = descripcion.ifEmpty { null }
                )
                Log.d(TAG, "Modo EDICIÓN. Actualizando horas: $horasActualizadas")
                horasTrabajadasViewModel.update(horasActualizadas)
                Toast.makeText(requireContext(), "Horas actualizadas correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Log.e(TAG, "Error en modo edición: horasTrabajadasActual es null.")
                Toast.makeText(requireContext(), "Error al actualizar los datos.", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
                return
            }
        } else {
            if (vehiculoIdPredeterminadoActual == null) {
                Toast.makeText(requireContext(), "Configure un vehículo predeterminado para guardar.", Toast.LENGTH_LONG).show()
                return
            }
            val nuevasHoras = HorasTrabajadas(
                vehiculoId = vehiculoIdPredeterminadoActual,
                fecha = fechaTimestamp,
                horas = horasDecimalesParaGuardar,
                descripcion = descripcion.ifEmpty { null }
            )
            Log.d(TAG, "Modo AÑADIR. Insertando nuevas horas: $nuevasHoras")
            horasTrabajadasViewModel.insert(nuevasHoras)
            Toast.makeText(requireContext(), "Horas guardadas correctamente", Toast.LENGTH_SHORT).show()
        }
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "onDestroyView")
    }
}

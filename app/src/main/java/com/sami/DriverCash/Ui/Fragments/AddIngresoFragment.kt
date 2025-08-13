package com.sami.DriverCash.Ui.Fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// import androidx.core.view.isVisible // Descomentar si añade un TextView para el mensaje
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sami.DriverCash.Model.Local.Ingreso
import com.sami.DriverCash.Utils.CurrencyUtils
import com.sami.DriverCash.ViewModel.IngresoViewModel
import com.sami.DriverCash.ViewModel.VehicleViewModel
import com.sami.DriverCash.databinding.FragmentAddIngresoBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class AddIngresoFragment : Fragment() {

    private val TAG = "AddIngresoFragment"
    private var _binding: FragmentAddIngresoBinding? = null
    private val binding get() = _binding!!

    private val ingresoViewModel: IngresoViewModel by viewModels()
    private val vehicleViewModel: VehicleViewModel by viewModels()

    private val args: AddIngresoFragmentArgs by navArgs()
    private var ingresoActual: Ingreso? = null
    private var esModoEdicion = false

    private val calendar = Calendar.getInstance()
    private val currencyFormatter = NumberFormat.getNumberInstance(Locale.GERMAN).apply {
        maximumFractionDigits = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddIngresoBinding.inflate(inflater, container, false)
        // Ejemplo si tuviera un TextView para el mensaje:
        // binding.tvMensajeNoVehiculo.isVisible = false 
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ingresoId = args.ingresoId
        Log.d(TAG, "onViewCreated - ingresoId recibido: $ingresoId")
        esModoEdicion = ingresoId != -1L
        Log.d(TAG, "onViewCreated - esModoEdicion: $esModoEdicion")

        setupDatePicker()
        setupMontoIngresoInputFormatting()

        if (esModoEdicion) {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Editar Ingreso"
            binding.btnGuardarIngreso.text = "Actualizar Ingreso"
            binding.btnGuardarIngreso.isEnabled = true // Habilitado por defecto en modo edición
            // if (::binding.isInitialized && binding.tvMensajeNoVehiculo != null) { // Ejemplo
            //     binding.tvMensajeNoVehiculo.isVisible = false
            // }
            cargarDatosIngreso(ingresoId)
        } else {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Registrar Ingreso"
            binding.btnGuardarIngreso.text = "Guardar Ingreso"
            binding.btnGuardarIngreso.isEnabled = false // Deshabilitado inicialmente en modo añadir
            Log.d(TAG, "Modo AÑADIR: Botón Guardar deshabilitado inicialmente. Observando vehículo...")

            vehicleViewModel.vehiculoPredeterminado.observe(viewLifecycleOwner) { vehiculo ->
                if (vehiculo != null) {
                    binding.btnGuardarIngreso.isEnabled = true
                    //  if (::binding.isInitialized && binding.tvMensajeNoVehiculo != null) { // Ejemplo
                    //      binding.tvMensajeNoVehiculo.isVisible = false
                    //  }
                    Log.d(TAG, "Vehículo predeterminado (${vehiculo.placa}) cargado. Botón Guardar HABILITADO.")
                } else {
                    binding.btnGuardarIngreso.isEnabled = false
                    //  if (::binding.isInitialized && binding.tvMensajeNoVehiculo != null) { // Ejemplo
                    //      binding.tvMensajeNoVehiculo.text = "Configure un vehículo predeterminado en Preferencias para guardar."
                    //      binding.tvMensajeNoVehiculo.isVisible = true
                    //  }
                    Log.w(TAG, "No hay vehículo predeterminado. Botón Guardar DESHABILITADO.")
                    if (isAdded && !esModoEdicion) { // Mostrar Toast solo si el fragmento está añadido y en modo añadir
                        Toast.makeText(requireContext(), "Configure un vehículo predeterminado en Preferencias para guardar.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            updateDateInView()
        }

        binding.btnGuardarIngreso.setOnClickListener {
            Log.d(TAG, "Boton Guardar/Actualizar presionado")
            saveIngreso()
        }
    }

    private fun cargarDatosIngreso(id: Long) {
        Log.d(TAG, "cargarDatosIngreso - Cargando datos para ingresoId: $id")
        ingresoViewModel.getIngresoById(id).observe(viewLifecycleOwner) { ingreso ->
            if (ingreso != null) {
                Log.d(TAG, "cargarDatosIngreso - Ingreso cargado: $ingreso")
                ingresoActual = ingreso
                poblarUiConIngreso(ingreso)
            } else {
                Log.w(TAG, "cargarDatosIngreso - No se encontró ingreso con ID: $id. Volviendo atrás.")
                Toast.makeText(requireContext(), "Error al cargar el ingreso.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun poblarUiConIngreso(ingreso: Ingreso) {
        val montoFormateado = currencyFormatter.format(ingreso.monto)
        binding.etMontoIngreso.setText(montoFormateado)
        binding.etDescripcionIngreso.setText(ingreso.descripcion ?: "")
        calendar.timeInMillis = ingreso.fecha
        updateDateInView()
        Log.d(TAG, "poblarUiConIngreso - UI poblada con datos de ingresoActual: $ingresoActual")
    }

    private fun setupMontoIngresoInputFormatting() { 
        binding.etMontoIngreso.addTextChangedListener(object : TextWatcher {
            private var currentText = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val inputText = s.toString()
                if (inputText == currentText) return
                binding.etMontoIngreso.removeTextChangedListener(this)
                val formattedText = CurrencyUtils.formatNumberStringWithThousands(inputText)
                currentText = formattedText
                binding.etMontoIngreso.setText(formattedText)
                binding.etMontoIngreso.setSelection(formattedText.length)
                binding.etMontoIngreso.addTextChangedListener(this)
            }
        })
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        binding.etFechaIngreso.setOnClickListener {
            DatePickerDialog(
                requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etFechaIngreso.setText(sdf.format(calendar.time))
    }
    
    private fun saveIngreso() {
        Log.d(TAG, "saveIngreso: Iniciando... esModoEdicion: $esModoEdicion, ingresoActual ID: ${ingresoActual?.id}, vehiculoId: ${ingresoActual?.vehiculoId}")

        val montoStringConPuntos = binding.etMontoIngreso.text.toString().trim()
        val montoStringSinFormato = montoStringConPuntos.replace(".", "")
        val descripcion = binding.etDescripcionIngreso.text.toString().trim()
        val fechaTimestamp = calendar.timeInMillis

        if (montoStringSinFormato.isEmpty()) {
            Toast.makeText(requireContext(), "El monto no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }
        val monto = montoStringSinFormato.toDoubleOrNull()
        if (monto == null || monto <= 0) {
            Toast.makeText(requireContext(), "Ingresa un monto válido", Toast.LENGTH_SHORT).show()
            return
        }

        val vehiculoIdPredeterminadoActual = vehicleViewModel.vehiculoPredeterminado.value?.id
        Log.d(TAG, "saveIngreso: vehiculoIdPredeterminadoActual obtenido de ViewModel: $vehiculoIdPredeterminadoActual")

        val vehiculoIdParaGuardar = ingresoActual?.vehiculoId ?: vehiculoIdPredeterminadoActual
        Log.d(TAG, "saveIngreso: vehiculoIdParaGuardar determinado: $vehiculoIdParaGuardar")

        if (vehiculoIdParaGuardar == null) {
            Log.e(TAG, "saveIngreso: vehiculoIdParaGuardar es NULL. No se puede guardar.")
            Toast.makeText(requireContext(), "No hay vehículo predeterminado. Configúralo en Preferencias.", Toast.LENGTH_LONG).show()
            return
        }

        if (esModoEdicion) {
            Log.d(TAG, "saveIngreso: Entrando en bloque esModoEdicion.")
            if (ingresoActual != null) {
                val ingresoActualizado = ingresoActual!!.copy(
                    vehiculoId = vehiculoIdParaGuardar,
                    fecha = fechaTimestamp,
                    monto = monto,
                    descripcion = descripcion.ifEmpty { null }
                )
                Log.d(TAG, "saveIngreso: Modo EDICIÓN. Llamando a UPDATE para ingresoActualizado: $ingresoActualizado")
                ingresoViewModel.update(ingresoActualizado)
                Toast.makeText(requireContext(), "Ingreso actualizado correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Log.e(TAG, "saveIngreso: Modo EDICIÓN pero ingresoActual es NULL. No se puede actualizar.")
                Toast.makeText(requireContext(), "Error al actualizar: no se cargó el ingreso original.", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
                return
            }
        } else {
            Log.d(TAG, "saveIngreso: Entrando en bloque ELSE (Modo AÑADIR).")
            val nuevoIngreso = Ingreso(
                vehiculoId = vehiculoIdParaGuardar,
                fecha = fechaTimestamp,
                monto = monto,
                descripcion = descripcion.ifEmpty { null }
            )
            Log.d(TAG, "saveIngreso: Modo AÑADIR. Llamando a INSERT para nuevoIngreso: $nuevoIngreso")
            ingresoViewModel.insert(nuevoIngreso)
            Toast.makeText(requireContext(), "Ingreso guardado correctamente", Toast.LENGTH_SHORT).show()
        }
        Log.d(TAG, "saveIngreso: Llamando a popBackStack.")
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        _binding = null
    }
}

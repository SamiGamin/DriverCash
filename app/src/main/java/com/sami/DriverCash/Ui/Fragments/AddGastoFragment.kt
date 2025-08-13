package com.sami.DriverCash.Ui.Fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sami.DriverCash.Model.Local.Gasto
import com.sami.DriverCash.Model.Local.TipoGasto
import com.sami.DriverCash.Utils.CurrencyUtils
import com.sami.DriverCash.ViewModel.GastoViewModel
import com.sami.DriverCash.ViewModel.VehicleViewModel
import com.sami.DriverCash.databinding.FragmentAddGastoBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class AddGastoFragment : Fragment() {

    private val TAG = "AddGastoFragment"
    private var _binding: FragmentAddGastoBinding? = null
    private val binding get() = _binding!!

    private val gastoViewModel: GastoViewModel by viewModels()
    private val vehicleViewModel: VehicleViewModel by viewModels()
    private val args: AddGastoFragmentArgs by navArgs()

    private val calendar = Calendar.getInstance()
    private var selectedTipoGasto: TipoGasto? = null
    private var gastoActual: Gasto? = null
    private var esModoEdicion = false
    
    // Formateador para mostrar el monto en el EditText
    private val currencyDisplayFormatter = NumberFormat.getNumberInstance(Locale.GERMAN).apply {
        maximumFractionDigits = 0 // Sin decimales para la visualización con puntos
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddGastoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gastoId = args.gastoId
        Log.d(TAG, "onViewCreated - gastoId recibido: $gastoId")
        esModoEdicion = gastoId != -1L
        Log.d(TAG, "onViewCreated - esModoEdicion: $esModoEdicion")

        setupDatePicker()
        setupCategorySpinner() // Configura el adapter primero
        setupMontoGastoInputFormatting()

        if (esModoEdicion) {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Editar Gasto"
            binding.btnGuardarGasto.text = "Actualizar Gasto"
            binding.btnGuardarGasto.isEnabled = true // Habilitado por defecto en modo edición
            cargarDatosGasto(gastoId)
        } else {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Registrar Gasto"
            binding.btnGuardarGasto.text = "Guardar Gasto"
            binding.btnGuardarGasto.isEnabled = false 
            vehicleViewModel.vehiculoPredeterminado.observe(viewLifecycleOwner) { vehiculo ->
                if (vehiculo != null) {
                    binding.btnGuardarGasto.isEnabled = true
                } else {
                    binding.btnGuardarGasto.isEnabled = false
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Configure un vehículo predeterminado para guardar gastos.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            updateDateInView() // Establecer fecha actual para nuevo gasto
        }

        binding.btnGuardarGasto.setOnClickListener {
            saveGasto()
        }
    }

    private fun cargarDatosGasto(id: Long) {
        Log.d(TAG, "Cargando datos para gastoId: $id")
        gastoViewModel.getGastoById(id).observe(viewLifecycleOwner) { gasto ->
            if (gasto != null) {
                gastoActual = gasto
                Log.d(TAG, "Gasto cargado: $gasto")
                poblarUiConGasto(gasto) // Puebla la UI después de que el spinner está configurado
            } else {
                Log.w(TAG, "No se encontró gasto con ID: $id. Volviendo atrás.")
                Toast.makeText(requireContext(), "Error al cargar el gasto.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun poblarUiConGasto(gasto: Gasto) {
        binding.etMontoGasto.setText(currencyDisplayFormatter.format(gasto.monto))
        binding.etDescripcionGasto.setText(gasto.descripcion ?: "")
        calendar.timeInMillis = gasto.fecha
        updateDateInView()

        selectedTipoGasto = gasto.categoria
        // Obtener el nombre formateado de la categoría específica del gasto
        val nombreCategoriaGasto = gasto.categoria.name.replace("_", " ").lowercase()
            .replaceFirstChar { char: Char -> // Especificar el tipo Char explícitamente
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }
        binding.actvCategoriaGasto.setText(nombreCategoriaGasto, false) // El 'false' evita que se filtre la lista desplegable

        Log.d(TAG, "UI poblada con datos de gastoActual: $gastoActual")
    }

    private fun setupMontoGastoInputFormatting() {
        binding.etMontoGasto.addTextChangedListener(object : TextWatcher {
            private var currentText = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val inputText = s.toString()
                if (inputText == currentText) return
                binding.etMontoGasto.removeTextChangedListener(this)
                val formattedText = CurrencyUtils.formatNumberStringWithThousands(inputText)
                currentText = formattedText
                binding.etMontoGasto.setText(formattedText)
                binding.etMontoGasto.setSelection(formattedText.length)
                binding.etMontoGasto.addTextChangedListener(this)
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
        binding.etFechaGasto.setOnClickListener {
            DatePickerDialog(requireContext(), dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etFechaGasto.setText(sdf.format(calendar.time))
    }

    private fun setupCategorySpinner() {
        val categoriasNombres = TipoGasto.values().map { 
            it.name.replace("_", " ").lowercase().replaceFirstChar { char: Char -> // Tipo Char explícito
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString() 
            }
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoriasNombres)
        binding.actvCategoriaGasto.setAdapter(adapter)

        binding.actvCategoriaGasto.setOnItemClickListener { _, _, position, _ ->
            selectedTipoGasto = TipoGasto.values()[position]
            Log.d(TAG, "Categoría seleccionada: $selectedTipoGasto")
        }
    }

    private fun saveGasto() {
        val montoStringConPuntos = binding.etMontoGasto.text.toString().trim()
        val montoStringSinFormato = montoStringConPuntos.replace(".", "")
        val descripcion = binding.etDescripcionGasto.text.toString().trim()
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
        // En modo edición, selectedTipoGasto ya está seteado desde poblarUiConGasto.
        // En modo añadir, debe ser seleccionado por el usuario.
        if (selectedTipoGasto == null && !esModoEdicion) { // Solo requerir si es un nuevo gasto y no se ha seleccionado
            Toast.makeText(requireContext(), "Selecciona una categoría de gasto", Toast.LENGTH_SHORT).show()
            return
        }
        // Si estamos en modo edición y selectedTipoGasto es null (lo cual no debería pasar si poblarUiConGasto funcionó),
        // podríamos usar gastoActual.categoria. Pero si el usuario borra la categoría e intenta guardar, este check es bueno.
        if (selectedTipoGasto == null && esModoEdicion && gastoActual != null) {
            selectedTipoGasto = gastoActual!!.categoria //Fallback, pero el usuario debería poder cambiarla
        } else if (selectedTipoGasto == null){
             Toast.makeText(requireContext(), "Selecciona una categoría de gasto", Toast.LENGTH_SHORT).show()
            return
        }

        val vehiculoIdPredeterminadoActual = vehicleViewModel.vehiculoPredeterminado.value?.id

        if (esModoEdicion) {
            if (gastoActual != null) {
                val gastoActualizado = gastoActual!!.copy(
                    fecha = fechaTimestamp,
                    monto = monto,
                    categoria = selectedTipoGasto!!, // Usamos la categoría seleccionada (puede haber cambiado)
                    descripcion = descripcion.ifEmpty { null }
                )
                Log.d(TAG, "Modo EDICIÓN. Actualizando gasto: $gastoActualizado")
                gastoViewModel.update(gastoActualizado)
                Toast.makeText(requireContext(), "Gasto actualizado correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Log.e(TAG, "Error en modo edición: gastoActual es null.")
                Toast.makeText(requireContext(), "Error al actualizar el gasto.", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
                return
            }
        } else {
            if (vehiculoIdPredeterminadoActual == null) {
                Log.e(TAG, "No hay vehículo predeterminado para un nuevo gasto.")
                Toast.makeText(requireContext(), "No hay vehículo predeterminado. Configúralo en Preferencias.", Toast.LENGTH_LONG).show()
                return
            }
            val nuevoGasto = Gasto(
                vehiculoId = vehiculoIdPredeterminadoActual,
                fecha = fechaTimestamp,
                monto = monto,
                categoria = selectedTipoGasto!!,
                descripcion = descripcion.ifEmpty { null }
            )
            Log.d(TAG, "Modo AÑADIR. Insertando nuevo gasto: $nuevoGasto")
            gastoViewModel.insert(nuevoGasto)
            Toast.makeText(requireContext(), "Gasto guardado correctamente", Toast.LENGTH_SHORT).show()
        }
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        _binding = null
    }
}

// Extensión de ejemplo para nombres legibles de TipoGasto, si decides usarla:
// (Asegúrate que tu enum TipoGasto esté en el alcance correcto o importa com.sami.DriverCash.Model.Local.TipoGasto)
/*
fun TipoGasto.displayName(context: Context): String {
    return when (this) {
        TipoGasto.COMBUSTIBLE -> context.getString(R.string.tipo_gasto_combustible) // Debes crear estas strings
        TipoGasto.MANTENIMIENTO -> context.getString(R.string.tipo_gasto_mantenimiento)
        TipoGasto.PEAJES -> context.getString(R.string.tipo_gasto_peajes)
        TipoGasto.SEGUROS_IMPUESTOS -> context.getString(R.string.tipo_gasto_seguros_impuestos)
        TipoGasto.ACCESORIOS_REPUESTOS -> context.getString(R.string.tipo_gasto_accesorios_repuestos)
        TipoGasto.COMIDAS_BEBIDAS -> context.getString(R.string.tipo_gasto_comidas_bebidas)
        TipoGasto.OTRO_GASTO -> context.getString(R.string.tipo_gasto_otro_gasto)
        // Añade más casos si tienes más tipos
    }
}
*/

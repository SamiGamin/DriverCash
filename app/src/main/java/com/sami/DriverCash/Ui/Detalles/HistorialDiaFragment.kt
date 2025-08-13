package com.sami.DriverCash.Ui.Detalles

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder // IMPORTANTE
import com.sami.DriverCash.Model.Local.Gasto
import com.sami.DriverCash.Model.Local.HorasTrabajadas
import com.sami.DriverCash.Model.Local.Ingreso
import com.sami.DriverCash.Model.Local.KilometrajeRecorrido
import com.sami.DriverCash.Ui.Adapter.HistorialDiaAdapter
import com.sami.DriverCash.databinding.FragmentHistorialDiaBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

@AndroidEntryPoint
class HistorialDiaFragment : Fragment() {

    private val TAG = "HistorialDiaFragment"

    private var _binding: FragmentHistorialDiaBinding? = null
    private val binding get() = _binding!!

    private val args: HistorialDiaFragmentArgs by navArgs()
    private val viewModel: HistorialDiaViewModel by viewModels()
    private lateinit var transaccionesAdapter: HistorialDiaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView llamado")
        _binding = FragmentHistorialDiaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated llamado")

        val fechaTimestamp = args.fechaTimestamp
        Log.d(TAG, "Timestamp recibido: $fechaTimestamp")

        configurarTituloFecha(fechaTimestamp)
        setupRecyclerView()
        observeViewModel()

        viewModel.cargarTransaccionesDelDia(fechaTimestamp)
    }

    private fun configurarTituloFecha(timestamp: Long) {
        val calendar = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        val sdf = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale.getDefault())
        binding.tvHistorialDiaTituloFecha.text = sdf.format(calendar.time).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }

    private fun setupRecyclerView() {
        transaccionesAdapter = HistorialDiaAdapter(requireContext()) { transaccion ->
            Log.d(TAG, "Item clickeado: $transaccion de tipo ${transaccion::class.simpleName}")
            mostrarDialogoOpciones(transaccion) // CAMBIO AQUÍ
        }
        binding.rvHistorialDiaTransacciones.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transaccionesAdapter
        }
        Log.d(TAG, "RecyclerView configurado con lógica de diálogo de opciones")
    }

    private fun mostrarDialogoOpciones(transaccion: Any) {
        val opciones = arrayOf("Editar", "Eliminar")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Seleccionar Acción")
            .setItems(opciones) { dialog, which ->
                when (which) {
                    0 -> { // Editar
                        Log.d(TAG, "Opción seleccionada: Editar para $transaccion")
                        procederAEditar(transaccion)
                    }
                    1 -> { // Eliminar
                        Log.d(TAG, "Opción seleccionada: Eliminar para $transaccion")
                        confirmarYEliminar(transaccion)
                    }
                }
            }
            .show()
    }

    private fun confirmarYEliminar(transaccion: Any) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Está seguro de que desea eliminar este registro?")
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Eliminar") { dialog, _ ->
                Log.d(TAG, "Confirmada eliminación para: $transaccion")
                viewModel.eliminarTransaccion(transaccion)
                // El ViewModel debería recargar los datos, y el observer actualizará la UI.
                // Opcionalmente, puedes mostrar un Toast aquí.
                dialog.dismiss()
            }
            .show()
    }

    private fun procederAEditar(transaccion: Any) {
        when (transaccion) {
            is Ingreso -> {
                Log.d(TAG, "procederAEditar: Intentando editar Ingreso. ID que se pasará: ${transaccion.id}")
                val action = HistorialDiaFragmentDirections
                    .actionHistorialDiaFragmentToAddIngresoFragment(transaccion.id)
                findNavController().navigate(action)
            }
            is Gasto -> {
                Log.d(TAG, "procederAEditar: Intentando editar Gasto. ID que se pasará: ${transaccion.id}")
                val action = HistorialDiaFragmentDirections
                    .actionHistorialDiaFragmentToAddGastoFragment(transaccion.id)
                findNavController().navigate(action)
            }
            is HorasTrabajadas -> {
                Log.d(TAG, "procederAEditar: Intentando editar HorasTrabajadas. ID que se pasará: ${transaccion.id}")
                val action = HistorialDiaFragmentDirections
                    .actionHistorialDiaFragmentToAddHorasTrabajadasFragment(transaccion.id)
                findNavController().navigate(action)
            }
            is KilometrajeRecorrido -> {
                Log.d(TAG, "procederAEditar: Intentando editar KilometrajeRecorrido. ID que se pasará: ${transaccion.id}")
                val action = HistorialDiaFragmentDirections
                    .actionHistorialDiaFragmentToAddKilometrajeFragment(transaccion.id)
                findNavController().navigate(action)
            }
            else -> {
                Log.w(TAG, "Tipo de ítem desconocido, no se puede editar: $transaccion")
            }
        }
    }

    private fun observeViewModel() {
        viewModel.transaccionesDelDia.observe(viewLifecycleOwner) { listaTransacciones ->
            Log.d(TAG, "Nuevos datos recibidos para el adapter. submitList LxB: ${listaTransacciones.size} items")
            // Código de log detallado omitido para brevedad, pero estaba aquí antes.
            transaccionesAdapter.submitList(listaTransacciones)
            Log.d(TAG, "submitList ha sido llamado.")
        }

        viewModel.mostrarMensajeNoDatos.observe(viewLifecycleOwner) { mostrar ->
            Log.d(TAG, "Visibilidad de 'No hay datos' cambiada a: $mostrar")
            binding.tvHistorialDiaNoData.visibility = if (mostrar) View.VISIBLE else View.GONE
            binding.rvHistorialDiaTransacciones.visibility = if (mostrar) View.GONE else View.VISIBLE
        }
        
        // Observador para mensajes/eventos desde el ViewModel (ej. "Eliminación exitosa")
        viewModel.eventoMensaje.observe(viewLifecycleOwner) { evento ->
            evento?.let { mensaje ->
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                viewModel.onEventoMensajeMostrado() // Notificar al ViewModel que el mensaje fue mostrado
            }
        }
        Log.d(TAG, "Observadores del ViewModel configurados")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView llamado")
        binding.rvHistorialDiaTransacciones.adapter = null
        _binding = null
    }
}

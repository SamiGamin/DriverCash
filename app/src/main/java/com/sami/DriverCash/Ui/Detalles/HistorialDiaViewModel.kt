package com.sami.DriverCash.Ui.Detalles

import android.content.Context // Asegúrate de tener este import
import android.util.Log // Asegúrate de tener este import
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sami.DriverCash.Model.Local.Gasto
import com.sami.DriverCash.Model.Local.HorasTrabajadas
import com.sami.DriverCash.Model.Local.Ingreso
import com.sami.DriverCash.Model.Local.KilometrajeRecorrido
import com.sami.DriverCash.Model.Local.VehicleDao
import com.sami.DriverCash.Model.Local.IngresoDao
import com.sami.DriverCash.Model.Local.GastoDao
import com.sami.DriverCash.Model.Local.HorasTrabajadasDao
import com.sami.DriverCash.Model.Local.KilometrajeRecorridoDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date // Para el log del rango de fechas
import javax.inject.Inject

@HiltViewModel
class HistorialDiaViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context, 
    private val vehicleDao: VehicleDao,
    private val ingresoDao: IngresoDao,
    private val gastoDao: GastoDao,
    private val horasTrabajadasDao: HorasTrabajadasDao,
    private val kilometrajeRecorridoDao: KilometrajeRecorridoDao
) : ViewModel() {

    private val TAG = "HistorialDiaVM" // Tag más corto para filtrar mejor

    private var vehiculoIdPredeterminado: Long? = null
    private var ultimoTimestampCargado: Long? = null // NUEVO

    private val _transaccionesDelDia = MutableLiveData<List<Any>>()
    val transaccionesDelDia: LiveData<List<Any>> = _transaccionesDelDia

    private val _mostrarMensajeNoDatos = MutableLiveData<Boolean>(false)
    val mostrarMensajeNoDatos: LiveData<Boolean> = _mostrarMensajeNoDatos

    private val _eventoMensaje = MutableLiveData<String?>() // NUEVO
    val eventoMensaje: LiveData<String?> = _eventoMensaje // NUEVO

    init {
        viewModelScope.launch {
            val vehiculo = vehicleDao.getVehiculoPredeterminadoSuspend()
            if (vehiculo == null) {
                Log.w(TAG, "INIT: No hay vehículo predeterminado.")
            } else {
                vehiculoIdPredeterminado = vehiculo.id
                Log.i(TAG, "INIT: Vehículo predeterminado ID: $vehiculoIdPredeterminado")
            }
        }
    }

    fun cargarTransaccionesDelDia(timestampDia: Long) {
        Log.d(TAG, "cargarTransaccionesDelDia - Timestamp recibido: $timestampDia")
        ultimoTimestampCargado = timestampDia // GUARDAR TIMESTAMP

        if (vehiculoIdPredeterminado == null) {
             viewModelScope.launch { 
                val vehiculo = vehicleDao.getVehiculoPredeterminadoSuspend()
                if (vehiculo == null) {
                    Log.e(TAG, "cargarTransaccionesDelDia - Fallo al obtener vehiculoIdPredeterminado. Es NULL.")
                    _transaccionesDelDia.postValue(emptyList())
                    _mostrarMensajeNoDatos.postValue(true)
                    return@launch
                } else {
                    vehiculoIdPredeterminado = vehiculo.id
                    Log.i(TAG, "cargarTransaccionesDelDia - Vehículo predeterminado ID obtenido tardíamente: $vehiculoIdPredeterminado")
                    cargarTransaccionesDelDiaConId(timestampDia, vehiculoIdPredeterminado!!)
                }
            }
            return 
        }
        cargarTransaccionesDelDiaConId(timestampDia, vehiculoIdPredeterminado!!)
    }

    private fun cargarTransaccionesDelDiaConId(timestampDia: Long, vehiculoId: Long) {
        Log.i(TAG, "cargarTransaccionesDelDiaConId - Usando vehiculoId: $vehiculoId para timestamp: $timestampDia")
        ultimoTimestampCargado = timestampDia // GUARDAR TIMESTAMP TAMBIÉN AQUÍ POR SI SE LLAMA DIRECTAMENTE

        viewModelScope.launch {
            val (inicioDiaMillis, finDiaMillis) = getInicioFinDia(timestampDia)
            Log.d(TAG, "cargarTransaccionesDelDiaConId - Rango de fechas: $inicioDiaMillis (${Date(inicioDiaMillis)}) a $finDiaMillis (${Date(finDiaMillis)})")

            val listaCombinada = mutableListOf<Any>()

            try {
                val ingresos = ingresoDao.getIngresosEntreFechasParaVehiculo(vehiculoId, inicioDiaMillis, finDiaMillis)
                Log.d(TAG, "DAO Ingresos: QueryFinalizada. Size=${ingresos.size}. Content=${ingresos.joinToString { it.toString() }}")
                if (ingresos.isNotEmpty()) listaCombinada.addAll(ingresos)

                val gastos = gastoDao.getGastosEntreFechasParaVehiculo(vehiculoId, inicioDiaMillis, finDiaMillis)
                Log.d(TAG, "DAO Gastos: QueryFinalizada. Size=${gastos.size}. Content=${gastos.joinToString { it.toString() }}")
                if (gastos.isNotEmpty()) listaCombinada.addAll(gastos)

                val horas = horasTrabajadasDao.getHorasTrabajadasEntreFechasParaVehiculo(vehiculoId, inicioDiaMillis, finDiaMillis)
                Log.d(TAG, "DAO HorasTrabajadas: QueryFinalizada. Size=${horas.size}. Content=${horas.joinToString { it.toString() }}")
                if (horas.isNotEmpty()) listaCombinada.addAll(horas)

                val kilometrajes = kilometrajeRecorridoDao.getKilometrajeEntreFechasParaVehiculo(vehiculoId, inicioDiaMillis, finDiaMillis)
                Log.d(TAG, "DAO KilometrajeRecorrido: QueryFinalizada. Size=${kilometrajes.size}. Content=${kilometrajes.joinToString { it.toString() }}")
                if (kilometrajes.isNotEmpty()) listaCombinada.addAll(kilometrajes)

            } catch (e: Exception) {
                Log.e(TAG, "cargarTransaccionesDelDiaConId - EXCEPCIÓN al obtener datos de DAOs: ${e.message}", e)
                _transaccionesDelDia.postValue(emptyList())
                _mostrarMensajeNoDatos.postValue(true)
                return@launch
            }
            
            Log.d(TAG, "Antes de ordenar: listaCombinada.size=${listaCombinada.size}, content=${listaCombinada.joinToString { it.toString() }}")
            listaCombinada.sortBy { item -> // Añadido 'item ->' para claridad
                when (item) {
                    is Ingreso -> item.fecha
                    is Gasto -> item.fecha
                    is HorasTrabajadas -> item.fecha
                    is KilometrajeRecorrido -> item.fecha
                    else -> Long.MAX_VALUE 
                }
            }
            Log.d(TAG, "Después de ordenar: listaCombinada.size=${listaCombinada.size}, content=${listaCombinada.joinToString { it.toString() }}")
            _transaccionesDelDia.postValue(listaCombinada)
            _mostrarMensajeNoDatos.postValue(listaCombinada.isEmpty())
        }
    }

    fun eliminarTransaccion(transaccion: Any) {
        viewModelScope.launch {
            try {
                when (transaccion) {
                    is Ingreso -> ingresoDao.delete(transaccion)
                    is Gasto -> gastoDao.delete(transaccion)
                    is HorasTrabajadas -> horasTrabajadasDao.delete(transaccion)
                    is KilometrajeRecorrido -> kilometrajeRecorridoDao.delete(transaccion)
                    else -> {
                        Log.w(TAG, "eliminarTransaccion: Tipo de transacción desconocido: $transaccion")
                        _eventoMensaje.postValue("Error: Tipo de registro desconocido")
                        return@launch
                    }
                }
                _eventoMensaje.postValue("Registro eliminado correctamente")
                // Recargar datos para el día actual
                if (ultimoTimestampCargado != null && vehiculoIdPredeterminado != null) {
                    cargarTransaccionesDelDiaConId(ultimoTimestampCargado!!, vehiculoIdPredeterminado!!)
                } else {
                    Log.w(TAG, "eliminarTransaccion: No se pudo recargar la lista, ultimoTimestampCargado o vehiculoIdPredeterminado es null.")
                    // Opcionalmente, podría forzar una recarga completa o mostrar un error más específico.
                    _transaccionesDelDia.postValue(emptyList()) // Limpia la lista si no se puede recargar
                     _mostrarMensajeNoDatos.postValue(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "eliminarTransaccion: Error al eliminar la transacción: ${e.message}", e)
                _eventoMensaje.postValue("Error al eliminar el registro")
            }
        }
    }

    fun onEventoMensajeMostrado() {
        _eventoMensaje.value = null
    }

    private fun getInicioFinDia(timestamp: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val inicio = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val fin = calendar.timeInMillis
        return Pair(inicio, fin)
    }
}

package com.sami.DriverCash.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sami.DriverCash.Model.Local.AppDatabase
import com.sami.DriverCash.Model.Local.Repository.VehicleRepository
import com.sami.DriverCash.Model.Local.Vehicle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val repository: VehicleRepository // ¡Hilt inyecta el Repositorio!
) : ViewModel() {

    private val TAG = "VehicleViewModel"

    // Ahora el ViewModel simplemente expone los LiveData del repositorio.
    val vehiculoPredeterminado: LiveData<Vehicle?> = repository.vehiculoPredeterminado
    val allVehicles: LiveData<List<Vehicle>> = repository.allVehicles
    private val _vehiculoSeleccionadoParaEditar = MutableLiveData<Vehicle?>()
    val vehiculoSeleccionadoParaEditar: LiveData<Vehicle?> = _vehiculoSeleccionadoParaEditar
    init {
        // La lógica para auto-seleccionar el predeterminado se queda aquí.
        verificarYEstablecerPredeterminado()
    }

    private fun verificarYEstablecerPredeterminado() {
        viewModelScope.launch {
            val predeterminadoActual = repository.getVehiculoPredeterminadoSuspend()

            if (predeterminadoActual == null) {
                Log.w(TAG, "No se encontró vehículo predeterminado. Buscando alternativa...")
                val todosLosVehiculos = repository.getAllSuspend()

                if (todosLosVehiculos.isNotEmpty()) {
                    val primerVehiculo = todosLosVehiculos.first()
                    Log.i(TAG, "Estableciendo '${primerVehiculo.modelo}' como predeterminado.")
                    repository.establecerVehiculoPredeterminado(primerVehiculo.id)
                } else {
                    Log.w(TAG, "No hay vehículos en la base de datos.")
                }
            }
        }
    }
    fun cargarVehiculoParaEditar(vehicleId: Long) {
        viewModelScope.launch {
            // Obtenemos el vehículo desde el repositorio (que se ejecuta en el hilo IO).
            val vehicle = repository.getVehicleById(vehicleId)
            // Publicamos el resultado en el LiveData.
            _vehiculoSeleccionadoParaEditar.postValue(vehicle)
        }
    }
    fun limpiarSeleccionParaEditar() {
        _vehiculoSeleccionadoParaEditar.value = null
    }

    fun establecerComoPredeterminado(vehicleId: Long) {
        viewModelScope.launch {
            repository.establecerVehiculoPredeterminado(vehicleId)
        }
    }

    // Otras funciones (insert, update, delete) que simplemente llaman al repositorio
    fun insert(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.insert(vehicle)
        }
    }
    fun update(vehicle: Vehicle) { // Nueva función para actualizar
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(vehicle)
        }
    }
    fun delete(vehicle: Vehicle) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(vehicle)
        }
    }

    suspend fun getVehicleById(id: Long): Vehicle? { // Nueva función para obtener por ID
        return withContext(Dispatchers.IO) {
            repository.getVehicleById(id)
        }
    }

    suspend fun getAllVehiclesSuspend(): List<Vehicle> {
        return repository.getAllSuspend()
    }
}
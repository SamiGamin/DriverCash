package com.sami.DriverCash.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
// import androidx.lifecycle.Transformations // Este import ya no es necesario para totalIngresosHistoricos
import androidx.lifecycle.viewModelScope
import com.sami.DriverCash.Model.Local.AppDatabase
import com.sami.DriverCash.Model.Local.Ingreso
import com.sami.DriverCash.Model.Local.Repository.IngresoRepository
import com.sami.DriverCash.Model.Local.VehicleDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext // Asegúrate de que este import esté si lo usas directamente aquí, aunque es mejor en el Repositorio
import javax.inject.Inject

@HiltViewModel
class IngresoViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val repository: IngresoRepository
    private val vehicleDao: VehicleDao

    val allIngresos: LiveData<List<Ingreso>>
    val ultimoIngreso: LiveData<Ingreso?>
    val totalIngresosHistoricos: MediatorLiveData<Double?> // Cambiado a MediatorLiveData explícitamente

    init {
        val db = AppDatabase.getDatabase(application)
        val ingresoDao = db.ingresoDao()
        vehicleDao = db.vehicleDao()
        repository = IngresoRepository(ingresoDao)

        allIngresos = repository.getAllIngresos()
        ultimoIngreso = repository.getUltimoIngreso()

        val liveDataVehiculoIngresos = vehicleDao.getVehiculoPredeterminado()
        totalIngresosHistoricos = MediatorLiveData<Double?>().apply {
            var currentSource: LiveData<Double?>? = null

            addSource(liveDataVehiculoIngresos) { vehiculo ->
                currentSource?.let {
                    removeSource(it)
                    currentSource = null
                }

                if (vehiculo != null) {
                    val nuevaFuente = repository.getTotalIngresosByVehiculoId(vehiculo.id)
                    currentSource = nuevaFuente
                    addSource(nuevaFuente) { total ->
                        value = total
                    }
                } else {
                    value = null
                }
            }
        }
    }

    fun insert(ingreso: Ingreso) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(ingreso)
    }

    fun update(ingreso: Ingreso) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(ingreso)
    }

    fun delete(ingreso: Ingreso) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(ingreso)
    }

    fun getIngresosByVehiculoId(vehiculoId: Long): LiveData<List<Ingreso>> {
        return repository.getIngresosByVehiculoId(vehiculoId)
    }

    // Función suspendida para obtener la lista directamente desde el repositorio
    suspend fun getIngresosByVehiculoIdSuspend(vehiculoId: Long): List<Ingreso> {
        // DEBERÁS ASEGURARTE de que tu IngresoRepository tenga una función suspendida como esta:
        // suspend fun getIngresosListByVehiculoId(vehiculoId: Long): List<Ingreso>
        // Esta función en el repositorio debería obtener los datos del DAO (también con suspend o withContext).
        return repository.getIngresosListByVehiculoId(vehiculoId) 
    }

    fun getTotalIngresosByVehiculoId(vehiculoId: Long): LiveData<Double?> {
        return repository.getTotalIngresosByVehiculoId(vehiculoId)
    }

    fun getIngresoById(ingresoId: Long): LiveData<Ingreso?> {
        return repository.getIngresoById(ingresoId)
    }
}
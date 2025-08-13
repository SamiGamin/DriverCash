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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IngresoViewModel(application: Application) : AndroidViewModel(application) {

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

        // NUEVO: Calcular el total histórico de ingresos para el vehículo predeterminado (Alternativa con MediatorLiveData)
        val liveDataVehiculoIngresos = vehicleDao.getVehiculoPredeterminado()
        totalIngresosHistoricos = MediatorLiveData<Double?>().apply {
            var currentSource: LiveData<Double?>? = null

            addSource(liveDataVehiculoIngresos) { vehiculo ->
                // Si ya hay una fuente observada del repositorio, la removemos
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
                    value = null // O puedes poner 0.0 si prefieres
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

    fun getTotalIngresosByVehiculoId(vehiculoId: Long): LiveData<Double?> {
        return repository.getTotalIngresosByVehiculoId(vehiculoId)
    }

    fun getIngresoById(ingresoId: Long): LiveData<Ingreso?> {
        return repository.getIngresoById(ingresoId)
    }
}
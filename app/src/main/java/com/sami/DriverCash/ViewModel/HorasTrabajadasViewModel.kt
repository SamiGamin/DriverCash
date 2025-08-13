package com.sami.DriverCash.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.sami.DriverCash.Model.Local.AppDatabase
import com.sami.DriverCash.Model.Local.HorasTrabajadas
import com.sami.DriverCash.Model.Local.Repository.HorasTrabajadasRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HorasTrabajadasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HorasTrabajadasRepository

    val allHorasTrabajadas: LiveData<List<HorasTrabajadas>>
    val ultimasHorasTrabajadas: LiveData<HorasTrabajadas?> // Nuevo LiveData

    init {
        val horasTrabajadasDao = AppDatabase.getDatabase(application).horasTrabajadasDao()
        repository = HorasTrabajadasRepository(horasTrabajadasDao)
        allHorasTrabajadas = repository.getAllHorasTrabajadas()
        ultimasHorasTrabajadas = repository.getUltimasHorasTrabajadas() // Inicializar
    }

    fun insert(horasTrabajadas: HorasTrabajadas) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(horasTrabajadas)
    }

    fun update(horasTrabajadas: HorasTrabajadas) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(horasTrabajadas)
    }

    fun delete(horasTrabajadas: HorasTrabajadas) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(horasTrabajadas)
    }

    fun getHorasTrabajadasByVehiculoId(vehiculoId: Long): LiveData<List<HorasTrabajadas>> {
        return repository.getHorasTrabajadasByVehiculoId(vehiculoId)
    }

    fun getTotalHorasTrabajadasByVehiculoId(vehiculoId: Long): LiveData<Double?> {
        return repository.getTotalHorasTrabajadasByVehiculoId(vehiculoId)
    }

    fun getHorasTrabajadasById(id: Long): LiveData<HorasTrabajadas?> {
        return repository.getHorasTrabajadasById(id)
    }
}
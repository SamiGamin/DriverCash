package com.sami.DriverCash.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.sami.DriverCash.Model.Local.AppDatabase
import com.sami.DriverCash.Model.Local.KilometrajeRecorrido
import com.sami.DriverCash.Model.Local.Repository.KilometrajeRecorridoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KilometrajeRecorridoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KilometrajeRecorridoRepository

    val allKilometrajeRecorrido: LiveData<List<KilometrajeRecorrido>>
    val ultimoKilometrajeRecorrido: LiveData<KilometrajeRecorrido?> // Nuevo LiveData

    init {
        val kilometrajeRecorridoDao = AppDatabase.getDatabase(application).kilometrajeRecorridoDao()
        repository = KilometrajeRecorridoRepository(kilometrajeRecorridoDao)
        allKilometrajeRecorrido = repository.getAllKilometrajeRecorrido()
        ultimoKilometrajeRecorrido = repository.getUltimoKilometrajeRecorrido() // Inicializar
    }

    fun insert(kilometrajeRecorrido: KilometrajeRecorrido) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(kilometrajeRecorrido)
    }

    fun update(kilometrajeRecorrido: KilometrajeRecorrido) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(kilometrajeRecorrido)
    }

    fun delete(kilometrajeRecorrido: KilometrajeRecorrido) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(kilometrajeRecorrido)
    }

    fun getKilometrajeRecorridoByVehiculoId(vehiculoId: Long): LiveData<List<KilometrajeRecorrido>> {
        return repository.getKilometrajeRecorridoByVehiculoId(vehiculoId)
    }

    fun getTotalKilometrosByVehiculoId(vehiculoId: Long): LiveData<Double?> {
        return repository.getTotalKilometrosByVehiculoId(vehiculoId)
    }

    fun getKilometrajeRecorridoById(id: Long): LiveData<KilometrajeRecorrido?> {
        return repository.getKilometrajeRecorridoById(id)
    }
}
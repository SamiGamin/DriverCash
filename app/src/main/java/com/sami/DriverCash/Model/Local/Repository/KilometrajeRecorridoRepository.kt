package com.sami.DriverCash.Model.Local.Repository

import androidx.lifecycle.LiveData
import com.sami.DriverCash.Model.Local.KilometrajeRecorrido
import com.sami.DriverCash.Model.Local.KilometrajeRecorridoDao

class KilometrajeRecorridoRepository(private val kilometrajeRecorridoDao: KilometrajeRecorridoDao) {

    suspend fun insert(kilometrajeRecorrido: KilometrajeRecorrido): Long {
        return kilometrajeRecorridoDao.insert(kilometrajeRecorrido)
    }

    suspend fun update(kilometrajeRecorrido: KilometrajeRecorrido) {
        kilometrajeRecorridoDao.update(kilometrajeRecorrido)
    }

    suspend fun delete(kilometrajeRecorrido: KilometrajeRecorrido) {
        kilometrajeRecorridoDao.delete(kilometrajeRecorrido)
    }

    fun getKilometrajeRecorridoById(id: Long): LiveData<KilometrajeRecorrido?> {
        return kilometrajeRecorridoDao.getKilometrajeRecorridoById(id)
    }

    fun getAllKilometrajeRecorrido(): LiveData<List<KilometrajeRecorrido>> {
        return kilometrajeRecorridoDao.getAllKilometrajeRecorrido()
    }

    fun getKilometrajeRecorridoByVehiculoId(vehiculoId: Long): LiveData<List<KilometrajeRecorrido>> {
        return kilometrajeRecorridoDao.getKilometrajeRecorridoByVehiculoId(vehiculoId)
    }

    fun getTotalKilometrosByVehiculoId(vehiculoId: Long): LiveData<Double?> {
        return kilometrajeRecorridoDao.getTotalKilometrosByVehiculoId(vehiculoId)
    }

    // Nueva función para obtener el último registro de kilometraje desde el DAO
    fun getUltimoKilometrajeRecorrido(): LiveData<KilometrajeRecorrido?> {
        return kilometrajeRecorridoDao.getUltimoKilometrajeRecorrido()
    }
}
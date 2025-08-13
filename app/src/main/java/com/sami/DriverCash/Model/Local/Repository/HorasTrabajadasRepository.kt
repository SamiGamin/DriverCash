package com.sami.DriverCash.Model.Local.Repository

import androidx.lifecycle.LiveData
import com.sami.DriverCash.Model.Local.HorasTrabajadas
import com.sami.DriverCash.Model.Local.HorasTrabajadasDao

class HorasTrabajadasRepository(private val horasTrabajadasDao: HorasTrabajadasDao) {

    suspend fun insert(horasTrabajadas: HorasTrabajadas): Long {
        return horasTrabajadasDao.insert(horasTrabajadas)
    }

    suspend fun update(horasTrabajadas: HorasTrabajadas) {
        horasTrabajadasDao.update(horasTrabajadas)
    }

    suspend fun delete(horasTrabajadas: HorasTrabajadas) {
        horasTrabajadasDao.delete(horasTrabajadas)
    }

    fun getHorasTrabajadasById(id: Long): LiveData<HorasTrabajadas?> {
        return horasTrabajadasDao.getHorasTrabajadasById(id)
    }

    fun getAllHorasTrabajadas(): LiveData<List<HorasTrabajadas>> {
        return horasTrabajadasDao.getAllHorasTrabajadas()
    }

    fun getHorasTrabajadasByVehiculoId(vehiculoId: Long): LiveData<List<HorasTrabajadas>> {
        return horasTrabajadasDao.getHorasTrabajadasByVehiculoId(vehiculoId)
    }

    fun getTotalHorasTrabajadasByVehiculoId(vehiculoId: Long): LiveData<Double?> {
        return horasTrabajadasDao.getTotalHorasTrabajadasByVehiculoId(vehiculoId)
    }

    // Nueva función para obtener el último registro de horas trabajadas desde el DAO
    fun getUltimasHorasTrabajadas(): LiveData<HorasTrabajadas?> {
        return horasTrabajadasDao.getUltimasHorasTrabajadas()
    }
}
package com.sami.DriverCash.Model.Local.Repository

import androidx.lifecycle.LiveData
import com.sami.DriverCash.Model.Local.Ingreso
import com.sami.DriverCash.Model.Local.IngresoDao

class IngresoRepository(private val ingresoDao: IngresoDao) {

    suspend fun insert(ingreso: Ingreso): Long {
        return ingresoDao.insert(ingreso)
    }

    suspend fun update(ingreso: Ingreso) {
        ingresoDao.update(ingreso)
    }

    suspend fun delete(ingreso: Ingreso) {
        ingresoDao.delete(ingreso)
    }

    fun getIngresoById(ingresoId: Long): LiveData<Ingreso?> {
        return ingresoDao.getIngresoById(ingresoId)
    }

    fun getAllIngresos(): LiveData<List<Ingreso>> {
        return ingresoDao.getAllIngresos()
    }

    fun getIngresosByVehiculoId(vehiculoId: Long): LiveData<List<Ingreso>> {
        return ingresoDao.getIngresosByVehiculoId(vehiculoId)
    }

    fun getTotalIngresosByVehiculoId(vehiculoId: Long): LiveData<Double?> {
        return ingresoDao.getTotalIngresosByVehiculoId(vehiculoId)
    }

    // Nueva función para obtener el último ingreso desde el DAO
    fun getUltimoIngreso(): LiveData<Ingreso?> {
        return ingresoDao.getUltimoIngreso()
    }

    // Aquí podrías añadir más lógica de negocio o combinar fuentes de datos si fuera necesario
}
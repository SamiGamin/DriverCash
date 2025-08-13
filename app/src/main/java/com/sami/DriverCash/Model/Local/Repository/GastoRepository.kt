package com.sami.DriverCash.Model.Local.Repository

import androidx.lifecycle.LiveData
import com.sami.DriverCash.Model.Local.Gasto
import com.sami.DriverCash.Model.Local.GastoDao
import com.sami.DriverCash.Model.Local.TipoGasto

class GastoRepository(private val gastoDao: GastoDao) {

    suspend fun insert(gasto: Gasto): Long {
        return gastoDao.insert(gasto)
    }

    suspend fun update(gasto: Gasto) {
        gastoDao.update(gasto)
    }

    suspend fun delete(gasto: Gasto) {
        gastoDao.delete(gasto)
    }

    fun getGastoById(id: Long): LiveData<Gasto?> {
        return gastoDao.getGastoById(id)
    }

    fun getAllGastos(): LiveData<List<Gasto>> {
        return gastoDao.getAllGastos()
    }

    fun getGastosByVehiculoId(vehiculoId: Long): LiveData<List<Gasto>> {
        return gastoDao.getGastosByVehiculoId(vehiculoId)
    }

    fun getGastosByVehiculoIdAndCategoria(vehiculoId: Long, categoria: TipoGasto): LiveData<List<Gasto>> {
        return gastoDao.getGastosByVehiculoIdAndCategoria(vehiculoId, categoria)
    }

    fun getTotalGastosByVehiculoId(vehiculoId: Long): LiveData<Double?> {
        return gastoDao.getTotalGastosByVehiculoId(vehiculoId)
    }

    fun getTotalGastosByVehiculoIdAndCategoria(vehiculoId: Long, categoria: TipoGasto): LiveData<Double?> {
        return gastoDao.getTotalGastosByVehiculoIdAndCategoria(vehiculoId, categoria)
    }

    // Nueva función para obtener el último gasto desde el DAO
    fun getUltimoGasto(): LiveData<Gasto?> {
        return gastoDao.getUltimoGasto()
    }
}
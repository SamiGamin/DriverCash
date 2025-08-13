package com.sami.DriverCash.Model.Local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GastoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gasto: Gasto): Long

    @Update
    suspend fun update(gasto: Gasto)

    @Delete
    suspend fun delete(gasto: Gasto)

    @Query("SELECT * FROM gastos WHERE id = :id LIMIT 1")
    fun getGastoById(id: Long): LiveData<Gasto?>

    @Query("SELECT * FROM gastos ORDER BY fecha DESC")
    fun getAllGastos(): LiveData<List<Gasto>>

    @Query("SELECT * FROM gastos WHERE vehiculoId = :vehiculoId ORDER BY fecha DESC")
    fun getGastosByVehiculoId(vehiculoId: Long): LiveData<List<Gasto>>

    @Query("SELECT * FROM gastos WHERE vehiculoId = :vehiculoId AND categoria = :categoria ORDER BY fecha DESC")
    fun getGastosByVehiculoIdAndCategoria(vehiculoId: Long, categoria: TipoGasto): LiveData<List<Gasto>>

    @Query("SELECT SUM(monto) FROM gastos WHERE vehiculoId = :vehiculoId")
    fun getTotalGastosByVehiculoId(vehiculoId: Long): LiveData<Double?>

    @Query("SELECT SUM(monto) FROM gastos WHERE vehiculoId = :vehiculoId AND categoria = :categoria")
    fun getTotalGastosByVehiculoIdAndCategoria(vehiculoId: Long, categoria: TipoGasto): LiveData<Double?>

    // Nueva función para obtener el último gasto registrado
    @Query("SELECT * FROM gastos ORDER BY fecha DESC, id DESC LIMIT 1")
    fun getUltimoGasto(): LiveData<Gasto?>
    @Query("SELECT SUM(monto) FROM gastos WHERE vehiculoId = :vehiculoId AND fecha BETWEEN :fechaInicio AND :fechaFin")
    suspend fun getSumaGastosEntreFechas(vehiculoId: Long, fechaInicio: Long, fechaFin: Long): Double?
    // CORRECCIÓN AQUÍ: Cambiado "tabla_ingresos" a "ingresos"
    @Query("SELECT * FROM gastos WHERE vehiculoId = :vehiculoId AND fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY fecha ASC")
    suspend fun getGastosEntreFechasParaVehiculo(vehiculoId: Long, fechaInicio: Long, fechaFin: Long): List<Gasto>

}
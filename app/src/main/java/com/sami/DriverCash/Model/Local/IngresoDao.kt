package com.sami.DriverCash.Model.Local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface IngresoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ingreso: Ingreso): Long // Devuelve el ID del ingreso insertado

    @Update
    suspend fun update(ingreso: Ingreso)

    @Delete
    suspend fun delete(ingreso: Ingreso)
    @Query("DELETE FROM ingresos") // "ingresos" es el nombre de tu tabla de Ingreso
    suspend fun deleteAll() // O un nombre como deleteAllIngresos() si prefieres

    @Query("SELECT * FROM ingresos WHERE id = :ingresoId LIMIT 1")
    fun getIngresoById(ingresoId: Long): LiveData<Ingreso?>

    @Query("SELECT * FROM ingresos ORDER BY fecha DESC")
    fun getAllIngresos(): LiveData<List<Ingreso>>

    @Query("SELECT * FROM ingresos WHERE vehiculoId = :vehiculoId ORDER BY fecha DESC")
    fun getIngresosByVehiculoId(vehiculoId: Long): LiveData<List<Ingreso>>

    // Nueva función suspendida para obtener la lista de ingresos por ID de vehículo para el backup
    @Query("SELECT * FROM ingresos WHERE vehiculoId = :vehiculoId ORDER BY fecha DESC")
    suspend fun getIngresosListForVehiculo(vehiculoId: Long): List<Ingreso>

    @Query("SELECT SUM(monto) FROM ingresos WHERE vehiculoId = :vehiculoId")
    fun getTotalIngresosByVehiculoId(vehiculoId: Long): LiveData<Double?>

    // Nueva función para obtener el último ingreso registrado
    @Query("SELECT * FROM ingresos ORDER BY fecha DESC, id DESC LIMIT 1")
    fun getUltimoIngreso(): LiveData<Ingreso?>

    @Query("SELECT SUM(monto) FROM ingresos WHERE vehiculoId = :vehiculoId AND fecha BETWEEN :fechaInicio AND :fechaFin")
    suspend fun getSumaIngresosEntreFechas(vehiculoId: Long, fechaInicio: Long, fechaFin: Long): Double?
    
    @Query("SELECT * FROM ingresos WHERE vehiculoId = :vehiculoId AND fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY fecha ASC")
    suspend fun getIngresosEntreFechasParaVehiculo(vehiculoId: Long, fechaInicio: Long, fechaFin: Long): List<Ingreso>


}
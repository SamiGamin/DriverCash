package com.sami.DriverCash.Model.Local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface HorasTrabajadasDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(horasTrabajadas: HorasTrabajadas): Long

    @Update
    suspend fun update(horasTrabajadas: HorasTrabajadas)

    @Delete
    suspend fun delete(horasTrabajadas: HorasTrabajadas)

    @Query("SELECT * FROM horas_trabajadas WHERE id = :id LIMIT 1")
    fun getHorasTrabajadasById(id: Long): LiveData<HorasTrabajadas?>

    @Query("SELECT * FROM horas_trabajadas ORDER BY fecha DESC")
    fun getAllHorasTrabajadas(): LiveData<List<HorasTrabajadas>>

    @Query("SELECT * FROM horas_trabajadas WHERE vehiculoId = :vehiculoId ORDER BY fecha DESC")
    fun getHorasTrabajadasByVehiculoId(vehiculoId: Long): LiveData<List<HorasTrabajadas>>

    @Query("SELECT SUM(horas) FROM horas_trabajadas WHERE vehiculoId = :vehiculoId")
    fun getTotalHorasTrabajadasByVehiculoId(vehiculoId: Long): LiveData<Double?>

    @Query("SELECT * FROM horas_trabajadas ORDER BY fecha DESC, id DESC LIMIT 1")
    fun getUltimasHorasTrabajadas(): LiveData<HorasTrabajadas?>

    @Query("SELECT SUM(horas) FROM horas_trabajadas WHERE vehiculoId = :vehiculoId AND fecha BETWEEN :fechaInicio AND :fechaFin")
    suspend fun getSumaHorasEntreFechas(vehiculoId: Long, fechaInicio: Long, fechaFin: Long): Double?

    // CORREGIDO: Apunta a 'horas_trabajadas' y devuelve List<HorasTrabajadas>
    @Query("SELECT * FROM horas_trabajadas WHERE vehiculoId = :vehiculoId AND fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY fecha ASC")
    suspend fun getHorasTrabajadasEntreFechasParaVehiculo(vehiculoId: Long, fechaInicio: Long, fechaFin: Long): List<HorasTrabajadas>
}

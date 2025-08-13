package com.sami.DriverCash.Model.Local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
// import java.util.Date // O podrías usar Long para timestamp. Decidimos usar Long.

@Entity(
    tableName = "ingresos",
    foreignKeys = [ForeignKey(
        entity = Vehicle::class,
        parentColumns = ["id"],
        childColumns = ["vehiculoId"],
        onDelete = ForeignKey.CASCADE 
    )],
    indices = [Index(value = ["vehiculoId"])]
)
data class Ingreso(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehiculoId: Long,          // ID del vehículo asociado
    val fecha: Long,               // Timestamp (milisegundos desde epoch)
    val monto: Double,
    val descripcion: String? = null // Opcional
)
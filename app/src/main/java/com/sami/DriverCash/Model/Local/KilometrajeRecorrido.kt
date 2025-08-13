package com.sami.DriverCash.Model.Local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
// import java.util.Date // Decidimos usar Long para fecha.

@Entity(
    tableName = "kilometraje_recorrido",
    foreignKeys = [ForeignKey(
        entity = Vehicle::class,
        parentColumns = ["id"],
        childColumns = ["vehiculoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["vehiculoId"])]
)
data class KilometrajeRecorrido(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehiculoId: Long,             // ID del vehículo asociado
    val fecha: Long,                  // Timestamp
    val kilometros: Double,            // Kilómetros recorridos en esta entrada
    val descripcion: String? = null    // Opcional, ej: "Viaje a ciudad X", "Ruta de reparto"
)
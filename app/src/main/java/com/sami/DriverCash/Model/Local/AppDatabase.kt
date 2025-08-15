package com.sami.DriverCash.Model.Local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Vehicle::class,
        Ingreso::class,
        HorasTrabajadas::class,
        KilometrajeRecorrido::class,
        Gasto::class
    ],
    version = 4, // Versión incrementada a 3
    exportSchema = false
)
@TypeConverters(Converters::class) // Añadido para los TypeConverters
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun ingresoDao(): IngresoDao // Nuevo DAO
    abstract fun horasTrabajadasDao(): HorasTrabajadasDao // Nuevo DAO
    abstract fun kilometrajeRecorridoDao(): KilometrajeRecorridoDao // Nuevo DAO
    abstract fun gastoDao(): GastoDao // Nuevo DAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "DriverCashDatabase"
                )
                .fallbackToDestructiveMigration() // Corregido y mantenido para desarrollo
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
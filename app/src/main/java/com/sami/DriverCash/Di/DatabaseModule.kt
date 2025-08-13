package com.sami.DriverCash.Di

import android.content.Context
import androidx.room.Room
import com.sami.DriverCash.Model.Local.AppDatabase
import com.sami.DriverCash.Model.Local.GastoDao
import com.sami.DriverCash.Model.Local.HorasTrabajadasDao
import com.sami.DriverCash.Model.Local.IngresoDao
import com.sami.DriverCash.Model.Local.KilometrajeRecorridoDao
import com.sami.DriverCash.Model.Local.VehicleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Le dice a Hilt que estas dependencias vivirán mientras la app esté viva (son singletons).
object DatabaseModule {

    /**
     * Provee la instancia de la base de datos AppDatabase.
     * Será un singleton, por lo que Hilt solo creará una instancia.
     * Hilt provee automáticamente el @ApplicationContext.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "DriverCashDatabase" // Puedes cambiar el nombre del archivo de la BD si quieres
        ).build()
    }

    /**
     * Provee el DAO para Ingresos.
     * Hilt ve que necesita una AppDatabase para llamar a este método,
     * y usa el método de arriba (provideAppDatabase) para obtenerla.
     */
    @Provides
    fun provideIngresoDao(appDatabase: AppDatabase): IngresoDao {
        return appDatabase.ingresoDao() // Asume que AppDatabase tiene un método .ingresoDao()
    }

    /**
     * Provee el DAO para Gastos.
     */
    @Provides
    fun provideGastoDao(appDatabase: AppDatabase): GastoDao {
        return appDatabase.gastoDao() // Asume que AppDatabase tiene un método .gastoDao()
    }

    /**
     * Provee el DAO para Horas Trabajadas.
     */
    @Provides
    fun provideHorasTrabajadasDao(appDatabase: AppDatabase): HorasTrabajadasDao {
        return appDatabase.horasTrabajadasDao() // Asume que AppDatabase tiene un método .horasTrabajadasDao()
    }

    /**
     * Provee el DAO para Kilometraje Recorrido.
     */
    @Provides
    fun provideKilometrajeRecorridoDao(appDatabase: AppDatabase): KilometrajeRecorridoDao {
        return appDatabase.kilometrajeRecorridoDao() // Asume que AppDatabase tiene un método .kilometrajeRecorridoDao()
    }

    /**
     * Provee el DAO para Vehiculos.
     */
    @Provides
    fun provideVehicleDao(appDatabase: AppDatabase): VehicleDao {
        return appDatabase.vehicleDao()
    }
}

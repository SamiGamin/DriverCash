package com.sami.DriverCash.Auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sami.DriverCash.Model.Local.Gasto
import com.sami.DriverCash.Model.Local.HorasTrabajadas
import com.sami.DriverCash.Model.Local.Ingreso
import com.sami.DriverCash.Model.Local.KilometrajeRecorrido
import com.sami.DriverCash.Model.Local.Vehicle // Asegúrate que es tu Vehicle local
import com.sami.DriverCash.Model.Local.Repository.GastoRepository
import com.sami.DriverCash.Model.Local.Repository.HorasTrabajadasRepository
import com.sami.DriverCash.Model.Local.Repository.IngresoRepository
import com.sami.DriverCash.Model.Local.Repository.KilometrajeRecorridoRepository
import com.sami.DriverCash.Model.Local.Repository.VehicleRepository
import com.sami.DriverCash.Model.Local.TipoCombustible
import com.sami.DriverCash.Model.Local.TipoGasto
import com.sami.DriverCash.Model.Local.toMap

import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val vehicleRepository: VehicleRepository,
    private val ingresoRepository: IngresoRepository,
    private val gastoRepository: GastoRepository,
    private val horasTrabajadasRepository: HorasTrabajadasRepository,
    private val kilometrajeRecorridoRepository: KilometrajeRecorridoRepository
) {
    private val firebaseAuth: FirebaseAuth = Firebase.auth
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    private val googleSignInWebClientId: String by lazy {
        try {
            val resourceId = appContext.resources.getIdentifier("default_web_client_id", "string", appContext.packageName)
            if (resourceId == 0) {
                throw IllegalStateException(
                    "String resource 'default_web_client_id' not found. " +
                            "Ensure google-services.json is in your app module and correctly processed."
                )
            }
            appContext.getString(resourceId)
        } catch (e: Exception) {
            throw IllegalStateException(
                "Failed to get 'default_web_client_id'. " +
                        "Ensure google-services.json is correctly configured. Error: ${e.message}", e
            )
        }
    }

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    fun getAuthStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(googleSignInWebClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(appContext, gso)
    }

    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Log.w("AuthManager", "signInWithGoogle:failure", e)
            Result.failure(e)
        }
    }

    suspend fun checkForExistingBackup(firebaseUser: FirebaseUser): Result<Map<String, Any?>?> =
        suspendCancellableCoroutine { continuation ->
            val userId = firebaseUser.uid
            val databaseRef = firebaseDatabase.reference.child("DriverCash").child("users").child(userId)
            Log.d("AuthManager", "[checkForExistingBackup] Verificando ref: ${databaseRef.toString()}") // LOG NUEVO

            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("AuthManager", "[checkForExistingBackup] onDataChange. Snapshot existe? ${snapshot.exists()}. Snapshot value: ${snapshot.value}") // LOG NUEVO
                    if (snapshot.exists()) {
                        @Suppress("UNCHECKED_CAST")
                        val backupData = snapshot.value as? Map<String, Any?>
                        Log.d("AuthManager", "[checkForExistingBackup] backupData obtenido del snapshot: $backupData") // LOG NUEVO

                        val vehiclesNode = backupData?.get("vehicles")
                        Log.d("AuthManager", "[checkForExistingBackup] vehiclesNode obtenido de backupData: $vehiclesNode") // LOG NUEVO

                        if (vehiclesNode != null) {
                            var hasActualVehicleData = false
                            if (vehiclesNode is Map<*, *>) {
                                Log.d("AuthManager", "[checkForExistingBackup] vehiclesNode es un Map. Está vacío? ${vehiclesNode.isEmpty()}") // LOG NUEVO
                                if (vehiclesNode.isNotEmpty()) {
                                    hasActualVehicleData = true
                                }
                            } else {
                                 Log.d("AuthManager", "[checkForExistingBackup] vehiclesNode NO es un Map. Tipo: ${vehiclesNode::class.simpleName}") // LOG NUEVO
                            }

                            if (hasActualVehicleData) {
                                Log.d("AuthManager", "Copia de seguridad válida encontrada para $userId con datos de vehículos.")
                                continuation.resume(Result.success(backupData))
                            } else {
                                Log.d("AuthManager", "Clave 'vehicles' encontrada para $userId, pero no contiene datos de vehículos válidos (vacía o formato inesperado después de obtenerla).")
                                continuation.resume(Result.success(null))
                            }
                        } else {
                            Log.d("AuthManager", "Nodo de usuario encontrado para $userId, pero falta la clave 'vehicles' (o es nula) - indica no hay backup de vehículos.")
                            continuation.resume(Result.success(null))
                        }
                    } else {
                        Log.d("AuthManager", "No se encontró copia de seguridad (nodo de usuario no existe) para $userId")
                        continuation.resume(Result.success(null))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AuthManager", "[checkForExistingBackup] onCancelled. Error: ${error.message}", error.toException()) // LOG NUEVO
                    continuation.resume(Result.failure(error.toException()))
                }
            })
            continuation.invokeOnCancellation { /* Buena práctica */ }
        }

    fun handleGoogleSignInResultIntent(
        data: Intent?,
        onSuccess: (GoogleSignInAccount) -> Unit,
        onFailure: (ApiException) -> Unit
    ) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)!!
            onSuccess(account)
        } catch (e: ApiException) {
            Log.w("AuthManager", "handleGoogleSignInResultIntent:failure", e)
            onFailure(e)
        }
    }

    fun signOut(onComplete: () -> Unit = {}) {
        firebaseAuth.signOut()
        getGoogleSignInClient().signOut().addOnCompleteListener {
            onComplete()
        }
    }

    private suspend fun prepareUserDataForBackup(): Map<String, Any?> {
        val vehiclesMapForFirebase = mutableMapOf<String, Any?>()
        val localVehicles = vehicleRepository.getAllSuspend()
        Log.d("AuthManager", "Preparando backup para ${localVehicles.size} vehículos.")

        for (vehicle in localVehicles) {
            val vehicleData = mutableMapOf<String, Any?>()
            vehicleData["id"] = vehicle.id
            vehicleData["marca"] = vehicle.marca
            vehicleData["modelo"] = vehicle.modelo
            vehicleData["anio"] = vehicle.anio
            vehicleData["color"] = vehicle.color
            vehicleData["placa"] = vehicle.placa
            vehicleData["esPredeterminado"] = vehicle.esPredeterminado
            vehicleData["apodo"] = vehicle.apodo
            vehicleData["numeroEconomico"] = vehicle.numeroEconomico
            vehicleData["tipoCombustible"] = vehicle.tipoCombustible?.name

            vehicleData["ingresos"] = ingresoRepository.getIngresosListByVehiculoId(vehicle.id).map { it.toMap() }
            vehicleData["gastos"] = gastoRepository.getGastosListByVehiculoId(vehicle.id).map { it.toMap() }
            vehicleData["horasTrabajadas"] = horasTrabajadasRepository.getHorasListByVehiculoId(vehicle.id).map { it.toMap() }
            vehicleData["kilometrajes"] = kilometrajeRecorridoRepository.getKilometrajeListByVehiculoId(vehicle.id).map { it.toMap() }

            // Usar prefijo para la clave del vehículo para asegurar que Firebase lo trate como mapa
            vehiclesMapForFirebase["v_${vehicle.id}"] = vehicleData
        }
        Log.d("AuthManager", "Datos de vehículos preparados para Firebase: $vehiclesMapForFirebase")
        return vehiclesMapForFirebase
    }

    suspend fun performFullUserBackup(firebaseUser: FirebaseUser): Result<Unit> {
        return try {
            val userId = firebaseUser.uid
            val databaseRef = firebaseDatabase.reference.child("DriverCash").child("users").child(userId)
            val userVehiclesData = prepareUserDataForBackup()

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val timestamp = sdf.format(Date())

            val finalDataToWrite = mutableMapOf<String, Any?>()
            
            // Añadir datos del perfil del usuario de Firebase
            finalDataToWrite["email"] = firebaseUser.email
            finalDataToWrite["displayName"] = firebaseUser.displayName
            finalDataToWrite["photoURL"] = firebaseUser.photoUrl?.toString() // Manejar nulabilidad

            // Solo añadir "vehicles" si hay datos de vehículos
            if (userVehiclesData.isNotEmpty()) {
                finalDataToWrite["vehicles"] = userVehiclesData
            }
            finalDataToWrite["lastBackupTimestamp"] = timestamp

            databaseRef.setValue(finalDataToWrite).await()
            if (userVehiclesData.isEmpty()){
                 Log.d("AuthManager", "Timestamp de backup y perfil de usuario actualizado para ${firebaseUser.uid}. No había vehículos locales para respaldar.")
            } else {
                 Log.d("AuthManager", "Backup de vehículos y perfil de usuario exitoso para ${firebaseUser.uid} con datos: $finalDataToWrite")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthManager", "Error en backup para ${firebaseUser.uid}", e)
            Result.failure(e)
        }
    }

    suspend fun restoreDataFromBackup(backupDataContainer: Map<String, Any?>): Result<Unit> {
        Log.d("AuthManager", "Iniciando restauración desde backup: $backupDataContainer")
        try {
            vehicleRepository.deleteAllVehicles()
            ingresoRepository.deleteAllIngresos()
            gastoRepository.deleteAllGastos()
            horasTrabajadasRepository.deleteAllhorasTrabajadas()
            kilometrajeRecorridoRepository.deleteAllkilometrajeRecorrido()

            // "vehicles" ahora siempre debería ser un Map si existe y tiene contenido
            @Suppress("UNCHECKED_CAST")
            val vehiclesMapFromBackup = backupDataContainer["vehicles"] as? Map<String, Map<String, Any?>>

            if (vehiclesMapFromBackup == null || vehiclesMapFromBackup.isEmpty()) {
                Log.w("AuthManager", "Restauración: No se encontraron datos de vehículos en el backup para restaurar, o la clave 'vehicles' es nula/vacía.")
                // Esto se considera un éxito si no hay nada que restaurar.
                return Result.success(Unit)
            }
            
            Log.d("AuthManager", "Restaurando ${vehiclesMapFromBackup.size} vehículos desde el MAPA de Firebase.")

            // Iterar sobre los valores del mapa de vehículos (que son los vehicleData individuales)
            for (vehicleMapFirebase in vehiclesMapFromBackup.values) {
                if (vehicleMapFirebase.isNotEmpty()) { // Doble chequeo, aunque el filter en checkForExistingBackup debería cubrirlo
                    val vehicle = mapToVehicle(vehicleMapFirebase)
                    vehicleRepository.insert(vehicle)
                    val currentVehicleOriginalId = vehicle.id
                    Log.d("AuthManager", "Restaurado vehículo con ID original $currentVehicleOriginalId, marca: ${vehicle.marca}")

                    (vehicleMapFirebase["ingresos"] as? List<Map<String, Any?>>)?.forEach { ingresoMap ->
                        ingresoRepository.insert(mapToIngreso(ingresoMap, currentVehicleOriginalId))
                    }
                    (vehicleMapFirebase["gastos"] as? List<Map<String, Any?>>)?.forEach { gastoMap ->
                        gastoRepository.insert(mapToGasto(gastoMap, currentVehicleOriginalId))
                    }
                    (vehicleMapFirebase["horasTrabajadas"] as? List<Map<String, Any?>>)?.forEach { horasMap ->
                        horasTrabajadasRepository.insert(mapToHorasTrabajadas(horasMap, currentVehicleOriginalId))
                    }
                    (vehicleMapFirebase["kilometrajes"] as? List<Map<String, Any?>>)?.forEach { kmMap ->
                        kilometrajeRecorridoRepository.insert(mapToKilometraje(kmMap, currentVehicleOriginalId))
                    }
                } else {
                    Log.w("AuthManager", "Se encontró un mapa de vehículo vacío dentro del nodo 'vehicles' durante la restauración. Se omitió.")
                }
            }

            Log.d("AuthManager", "Restauración completada exitosamente.")
            return Result.success(Unit)

        } catch (e: Exception) {
            Log.e("AuthManager", "Error durante la restauración de datos", e)
            return Result.failure(e)
        }
    }

    private fun mapToVehicle(data: Map<String, Any?>): Vehicle {
        val tipoCombustibleString = data["tipoCombustible"] as? String
        val tipoCombustibleEnum = tipoCombustibleString?.let {
            try {
                TipoCombustible.valueOf(it.uppercase())
            } catch (e: IllegalArgumentException) {
                Log.w("AuthManager", "Tipo de combustible desconocido '$it' en mapToVehicle. Usando null.")
                null
            }
        }
        return Vehicle(
            id = (data["id"] as? Long) ?: (data["id"] as? Double)?.toLong() ?: 0L,
            marca = data["marca"] as? String ?: "",
            modelo = data["modelo"] as? String ?: "",
            anio = (data["anio"] as? Long)?.toInt() ?: (data["anio"] as? Double)?.toInt() ?: 0,
            color = data["color"] as? String ?: "",
            placa = data["placa"] as? String ?: "",
            esPredeterminado = data["esPredeterminado"] as? Boolean ?: false,
            apodo = data["apodo"] as? String ?: "",
            numeroEconomico = data["numeroEconomico"] as? String ?: "",
            tipoCombustible = tipoCombustibleEnum
        )
    }

    private fun mapToIngreso(data: Map<String, Any?>, vehiculoIdOwner: Long): Ingreso {
        return Ingreso(
            id = (data["id"] as? Long) ?: (data["id"] as? Double)?.toLong() ?: 0L,
            vehiculoId = (data["vehiculoId"] as? Long) ?: (data["vehiculoId"] as? Double)?.toLong() ?: vehiculoIdOwner,
            monto = (data["monto"] as? Double) ?: (data["monto"] as? Long)?.toDouble() ?: 0.0,
            fecha = (data["fecha"] as? Long) ?: (data["fecha"] as? Double)?.toLong() ?: System.currentTimeMillis(),
            descripcion = data["descripcion"] as? String ?: "",
            categoria = data["categoria"] as? String ?: "General"
        )
    }

    private fun mapToGasto(data: Map<String, Any?>, vehiculoIdOwner: Long): Gasto {
        val tipoGastoString = data["tipoGasto"] as? String ?: data["categoria"] as? String ?: TipoGasto.OTROS.name
        val tipoGasto = try {
            TipoGasto.valueOf(tipoGastoString.uppercase())
        } catch (e: IllegalArgumentException) {
            Log.w("AuthManager", "Tipo de gasto desconocido '$tipoGastoString' en mapToGasto. Usando OTROS.")
            TipoGasto.OTROS
        }
        return Gasto(
            id = (data["id"] as? Long) ?: (data["id"] as? Double)?.toLong() ?: 0L,
            vehiculoId = (data["vehiculoId"] as? Long) ?: (data["vehiculoId"] as? Double)?.toLong() ?: vehiculoIdOwner,
            monto = (data["monto"] as? Double) ?: (data["monto"] as? Long)?.toDouble() ?: 0.0,
            fecha = (data["fecha"] as? Long) ?: (data["fecha"] as? Double)?.toLong() ?: System.currentTimeMillis(),
            descripcion = data["descripcion"] as? String ?: "",
            categoria = tipoGasto,
            pathFoto = data["pathFoto"] as? String
        )
    }

    private fun mapToHorasTrabajadas(data: Map<String, Any?>, vehiculoIdOwner: Long): HorasTrabajadas {
        return HorasTrabajadas(
            id = (data["id"] as? Long) ?: (data["id"] as? Double)?.toLong() ?: 0L,
            vehiculoId = (data["vehiculoId"] as? Long) ?: (data["vehiculoId"] as? Double)?.toLong() ?: vehiculoIdOwner,
            fecha = (data["fecha"] as? Long) ?: (data["fecha"] as? Double)?.toLong() ?: System.currentTimeMillis(),
            horas = (data["horas"] as? Double) ?: (data["horas"] as? Long)?.toDouble() ?: 0.0,
            descripcion = data["descripcion"] as? String ?: ""
        )
    }

    private fun mapToKilometraje(data: Map<String, Any?>, vehiculoIdOwner: Long): KilometrajeRecorrido {
        return KilometrajeRecorrido(
            id = (data["id"] as? Long) ?: (data["id"] as? Double)?.toLong() ?: 0L,
            vehiculoId = (data["vehiculoId"] as? Long) ?: (data["vehiculoId"] as? Double)?.toLong() ?: vehiculoIdOwner,
            fecha = (data["fecha"] as? Long) ?: (data["fecha"] as? Double)?.toLong() ?: System.currentTimeMillis(),
            kilometros = (data["kilometros"] as? Double) ?: (data["kilometros"] as? Long)?.toDouble() ?: 0.0,
            descripcion = data["descripcion"] as? String ?: ""
        )
    }
}

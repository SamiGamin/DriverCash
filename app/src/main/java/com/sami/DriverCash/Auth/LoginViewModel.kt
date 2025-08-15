package com.sami.DriverCash.Auth

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// Clase de datos para pasar información al diálogo de restauración
data class RestoreDialogData(val firebaseUser: FirebaseUser, val backupMap: Map<String, Any?>)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _launchGoogleSignIn = MutableLiveData<Event<Intent>>()
    val launchGoogleSignIn: LiveData<Event<Intent>> = _launchGoogleSignIn

    private val _signInSuccess = MutableLiveData<Event<FirebaseUser>>()
    val signInSuccess: LiveData<Event<FirebaseUser>> = _signInSuccess

    private val _signInError = MutableLiveData<Event<String>>()
    val signInError: LiveData<Event<String>> = _signInError

    // Para mostrar el diálogo de confirmación de restauración
    private val _showRestoreConfirmationDialog = MutableLiveData<Event<RestoreDialogData>>()
    val showRestoreConfirmationDialog: LiveData<Event<RestoreDialogData>> = _showRestoreConfirmationDialog

    // Para el estado de la restauración en sí
    private val _restoreStatus = MutableLiveData<Event<String>>()
    val restoreStatus: LiveData<Event<String>> = _restoreStatus

    // Para el estado del backup (puede ser usado también para el de restauración si se desea simplificar)
    private val _backupStatus = MutableLiveData<Event<String>>()
    val backupStatus: LiveData<Event<String>> = _backupStatus

    val currentUser: FirebaseUser?
        get() = authManager.currentUser

    fun startGoogleSignIn() {
        _isLoading.value = true
        try {
            val signInIntent = authManager.getGoogleSignInClient().signInIntent
            _launchGoogleSignIn.value = Event(signInIntent)
        } catch (e: Exception) {
            _isLoading.value = false
            _signInError.value = Event("Error al iniciar Google Sign-In: ${e.message}")
        }
    }

    fun processGoogleSignInResult(data: Intent?) {
        authManager.handleGoogleSignInResultIntent(
            data = data,
            onSuccess = { googleAccount ->
                viewModelScope.launch {
                    _isLoading.value = true
                    val firebaseUserResult = authManager.signInWithGoogle(googleAccount)
                    firebaseUserResult.fold(
                        onSuccess = { firebaseUser ->
                            _signInSuccess.value = Event(firebaseUser) // Notificar éxito de login primero
                            Log.d("LoginViewModel", "Usuario autenticado: ${firebaseUser.uid}. Verificando backup...") // LOG AÑADIDO
                            // Ahora, verificar si hay backup existente
                            val checkBackupResult = authManager.checkForExistingBackup(firebaseUser)
                            checkBackupResult.fold(
                                onSuccess = { backupDataMap -> // backupDataMap es Map<String, Any?>?
                                    Log.d("LoginViewModel", "Resultado de checkForExistingBackup. backupDataMap es null? ${backupDataMap == null}") // LOG AÑADIDO
                                    if (backupDataMap != null) {
                                        Log.d("LoginViewModel", "Backup encontrado. Mostrando diálogo de restauración. Datos: $backupDataMap") // LOG AÑADIDO
                                        // Backup encontrado, mostrar diálogo de confirmación
                                        _showRestoreConfirmationDialog.value = Event(RestoreDialogData(firebaseUser, backupDataMap))
                                        _isLoading.value = false // Permitir interacción con el diálogo
                                    } else {
                                        // No se encontró backup en la nube, proceder con backup de datos locales
                                        Log.d("LoginViewModel", "No se encontró copia de seguridad en la nube para ${firebaseUser.uid} o es inválida. Se procederá a hacer backup local si hay datos.") // LOG MODIFICADO
                                        initiateLocalDataBackup(firebaseUser)
                                    }
                                },
                                onFailure = { exception ->
                                    _isLoading.value = false
                                    _signInError.value = Event("Error al verificar copia de seguridad: ${exception.message}")
                                    Log.e("LoginViewModel", "Error al verificar copia de seguridad: ${exception.message}", exception) // LOG AÑADIDO
                                    // Aquí podrías decidir si igual intentas un backup local o simplemente muestras error.
                                    // Por ahora, solo error.
                                }
                            )
                        },
                        onFailure = { exception ->
                            _isLoading.value = false
                            _signInError.value = Event("Error de autenticación con Firebase: ${exception.message}")
                        }
                    )
                }
            },
            onFailure = { apiException ->
                _isLoading.value = false
                _signInError.value = Event("Error de Google Sign-In: ${apiException.message} (Código: ${apiException.statusCode})")
            }
        )
    }

    // Se llama si el usuario elige restaurar
    fun userChoseToRestore(restoreData: RestoreDialogData) {
        _isLoading.value = true
        viewModelScope.launch {
            // restoreData.backupMap es Map<String, Any?> que es lo que espera restoreDataFromBackup
            val restoreResult = authManager.restoreDataFromBackup(restoreData.backupMap)
            restoreResult.fold(
                onSuccess = {
                    _isLoading.value = false
                    _restoreStatus.value = Event("Datos restaurados con éxito.")
                    // También podrías usar _backupStatus para notificar el cierre del diálogo genéricamente
                     _backupStatus.value = Event("Datos restaurados con éxito.") 
                },
                onFailure = { exception ->
                    _isLoading.value = false
                    _restoreStatus.value = Event("Error al restaurar datos: ${exception.message}")
                     _backupStatus.value = Event("Error al restaurar datos: ${exception.message}")
                }
            )
        }
    }

    // Se llama si el usuario elige NO restaurar (o si no había backup y se procede a backup local)
    fun userChoseNotToRestore(firebaseUser: FirebaseUser) {
        Log.d("LoginViewModel", "Usuario eligió no restaurar o no había backup. Iniciando backup local.")
        initiateLocalDataBackup(firebaseUser)
    }

    // Inicia el proceso de backup de datos locales a Firebase
    private fun initiateLocalDataBackup(firebaseUser: FirebaseUser) {
        _isLoading.value = true
        viewModelScope.launch {
            val backupResult = authManager.performFullUserBackup(firebaseUser)
            backupResult.fold(
                onSuccess = {
                    _isLoading.value = false
                    _backupStatus.value = Event("Copia de seguridad local completada con éxito.")
                },
                onFailure = { exception ->
                    _isLoading.value = false
                    _backupStatus.value = Event("Error en la copia de seguridad local: ${exception.message}")
                }
            )
        }
    }

    fun googleSignInFlowConcluded() {
        // Esta función podría necesitar ajustarse si _isLoading se maneja de forma diferente
        // por el diálogo de restauración.
        if (_isLoading.value == false) { // Solo si no hay una operación en curso
             // No hacemos nada aquí, el _isLoading se maneja por operación.
        }
    }
}

// Event class (sin cambios)
open class Event<out T>(private val content: T) {
    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}

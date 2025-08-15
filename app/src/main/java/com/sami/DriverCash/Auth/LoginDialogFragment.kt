package com.sami.DriverCash.Auth

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.sami.DriverCash.R
import com.sami.DriverCash.databinding.DialogLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginDialogFragment : DialogFragment() {

    private var _binding: DialogLoginBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by viewModels()

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    companion object {
        const val TAG = "LoginDialogFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data: Intent? = result.data
            loginViewModel.processGoogleSignInResult(data)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.btnGoogleSignIn.setOnClickListener {
            loginViewModel.startGoogleSignIn()
        }
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false) // Inicialmente no cancelable mientras carga
    }

    private fun observeViewModel() {
        loginViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbLoginLoading.isVisible = isLoading
            binding.btnGoogleSignIn.isEnabled = !isLoading
            dialog?.setCancelable(!isLoading) // Permitir cancelar solo si no está cargando
            if (!isLoading) {
                binding.tvLoginError.isVisible = false // Ocultar error si ya no está cargando
            }
        }

        loginViewModel.launchGoogleSignIn.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { signInIntent ->
                try {
                    googleSignInLauncher.launch(signInIntent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error al lanzar Google Sign-In Intent", e)
                    Toast.makeText(context, "Error al iniciar Google Sign-In: ${e.message}", Toast.LENGTH_LONG).show()
                    loginViewModel.googleSignInFlowConcluded() 
                }
            }
        }

        loginViewModel.signInSuccess.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { firebaseUser ->
                // El Toast de "Inicio de sesión exitoso" se maneja implícitamente por el flujo 
                // que sigue (verificación de backup o backup/restore status).
                // El ViewModel ya está manejando el siguiente paso (checkForExistingBackup).
                Log.d(TAG, "Sign in successful for ${firebaseUser.displayName}, ViewModel will check for backup.")
            }
        }

        loginViewModel.signInError.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { errorMessage ->
                binding.tvLoginError.text = errorMessage
                binding.tvLoginError.isVisible = true
                dialog?.setCancelable(true) // Permitir cerrar si hay error de login
            }
        }

        loginViewModel.showRestoreConfirmationDialog.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { restoreData ->
                // Asegurarse de que el diálogo no esté cancelable mientras se muestra esta confirmación
                dialog?.setCancelable(false)
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.restore_backup_title)) // Necesitarás añadir este string
                    .setMessage(getString(R.string.restore_backup_message)) // Necesitarás añadir este string
                    .setPositiveButton(getString(R.string.restore_yes)) { _, _ -> // Necesitarás añadir este string
                        loginViewModel.userChoseToRestore(restoreData)
                    }
                    .setNegativeButton(getString(R.string.restore_no_start_new)) { _, _ -> // Necesitarás añadir este string
                        loginViewModel.userChoseNotToRestore(restoreData.firebaseUser)
                    }
                    .setCancelable(false) // El diálogo de confirmación en sí no es cancelable
                    .show()
            }
        }

        // Este observer ahora maneja mensajes de backup y de restauración.
        loginViewModel.backupStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { statusMessage ->
                Toast.makeText(context, statusMessage, Toast.LENGTH_LONG).show()
                // Condiciones para cerrar el diálogo principal
                if (statusMessage.contains("éxito") || 
                    statusMessage.contains("Error") || 
                    statusMessage.contains("No hay datos locales para respaldar")) {
                    dismiss()
                }
            }
        }

        // Si tenías un observador separado para restoreStatus, puedes fusionarlo o asegurarte
        // de que backupStatus cubre todos los casos para cerrar el diálogo.
        // loginViewModel.restoreStatus.observe(viewLifecycleOwner) { event -> ... }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

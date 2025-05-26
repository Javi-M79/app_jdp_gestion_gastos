package com.example.app_jdp_gestion_gastos.ui.fragments

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.app_jdp_gestion_gastos.R
import com.example.app_jdp_gestion_gastos.data.repository.UserRepository
import com.example.app_jdp_gestion_gastos.databinding.FragmentLoginBinding
import com.example.app_jdp_gestion_gastos.ui.dialog.PasswordResetDialog
import com.example.app_jdp_gestion_gastos.ui.viewmodel.UserModelFactory
import com.example.app_jdp_gestion_gastos.ui.viewmodel.UserViewModel
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    // ViewBinding para acceder a las vistas del layout
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    //Inicio del UserRepository
    private val userRepository by lazy {
        UserRepository(
            FirebaseAuth.getInstance(),
            FirebaseFirestore.getInstance())
    }

    //Inicio del UserViewModel que recibe el reposotory a traves de ViewModdel factory
    private val userViewModel: UserViewModel by viewModels {
        UserModelFactory(userRepository)
    }

    // Variables para google y Facebook
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleLauncher: ActivityResultLauncher<Intent>
    private lateinit var callbackManager: CallbackManager
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Fondo animado
        val background = binding.root.background
        if (background is AnimationDrawable) {
            background.setEnterFadeDuration(1000)
            background.setExitFadeDuration(1000)
            background.start()
        }

        // Animación del titulo
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        binding.tvTitulo.startAnimation(fadeIn)

        // Aplicar la animación a otros elementos
        binding.etMail.startAnimation(fadeIn)
        binding.etPassword.startAnimation(fadeIn)
        binding.btnLogin.startAnimation(fadeIn)
        binding.ivFondo.startAnimation(fadeIn)
        binding.tvCrearCuenta.startAnimation(fadeIn)

        applyFocusAnimations()
        applyButtonPressAnimation()

        //LOGIN EN FIREBASE
        binding.btnLogin.setOnClickListener {
            val mail = binding.etMail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            //Metodo en el userViewModel
            userViewModel.loginUser(mail, password) { userId, error ->
                if (userId != null) {
                    Toast.makeText(requireContext(), "Bienvenido, $mail", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_statsFragment)
                } else {
                    Toast.makeText(requireContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Navegación a la pantalla de registro
        binding.tvCrearCuenta.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registroFragment)
        }

        // Google Sign-In
        googleSignInClient = GoogleSignIn.getClient(
            requireActivity(),
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful) {
                val account = task.result
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Sesión iniciada con Google", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_statsFragment)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error con Google: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivGmail.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleLauncher.launch(signInIntent)
        }

        // Facebook Sign-In
        callbackManager = CallbackManager.Factory.create()

        binding.ivMeta.setOnClickListener {
            FacebookSdk.sdkInitialize(requireContext().applicationContext)

            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        val token = result.accessToken
                        val credential = FacebookAuthProvider.getCredential(token.token)
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Sesión iniciada con Facebook", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_loginFragment_to_statsFragment)
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Error con Facebook: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }

                    override fun onCancel() {
                        Toast.makeText(requireContext(), "Inicio cancelado", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(error: FacebookException) {
                        Toast.makeText(requireContext(), "Error Facebook: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // Apple Sign-In no implementado
        binding.ivApple.setOnClickListener {
            Toast.makeText(requireContext(), "Apple Sign-In no implementado", Toast.LENGTH_SHORT).show()
        }

        // Recuperar contraseña
        binding.resetpassword.setOnClickListener {
            PasswordResetDialog().show(parentFragmentManager, "passwordResetDialog")
        }
    }

    // TODO: Resultado del Facebook Login
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    // TODO: función para aplicar animaciones de foco a los EditText
    private fun applyFocusAnimations() {
        val fondo = binding.ivFondo

        // Aplicamos animación escalado
        binding.etMail.setOnFocusChangeListener { _, hasFocus ->
            fondo.startAnimation(createScaleAnimation(if (hasFocus) 1.0f else 1.2f, if (hasFocus) 1.2f else 1.0f))
        }

        // Aplicamos animación escalado
        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            fondo.startAnimation(createScaleAnimation(if (hasFocus) 1.0f else 1.2f, if (hasFocus) 1.2f else 1.0f))
        }
    }

    // TODO: Animación de presión del botón
    private fun applyButtonPressAnimation() {
        binding.btnLogin.setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            false
        }
    }

    // TODO: función para crear una animación de escalado
    private fun createScaleAnimation(from: Float, to: Float): ScaleAnimation {
        return ScaleAnimation(from, to, from, to, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f).apply {
            duration = 300
            fillAfter = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
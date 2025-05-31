package com.example.login

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.compose.*
import com.example.login.screens.*
import com.example.login.ui.theme.LoginTheme
import com.google.android.gms.auth.api.identity.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId("970799186064-geqt4q58nl77j9hq1ugnldm6c390qvrj.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            ).build()

        setContent {
            LoginTheme {
                val navController = rememberNavController()
                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                    try {
                        val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                        val idToken = credential.googleIdToken
                        val email = credential.id

                        if (idToken != null) {
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        navController.navigate("welcome/$email")
                                    } else {
                                        Log.e("FirebaseAuth", "Error al autenticar", it.exception)
                                    }
                                }
                        } else {
                            Log.e("GoogleSignIn", "ID Token nulo")
                        }
                    } catch (e: Exception) {
                        Log.e("GoogleSignIn", "Error: ${e.message}", e)
                    }
                }

                NavHost(navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            navController = navController,
                            auth = auth,
                            onGoogleLogin = {
                                oneTapClient.beginSignIn(signInRequest)
                                    .addOnSuccessListener { result ->
                                        launcher.launch(
                                            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                                        )
                                    }
                                    .addOnFailureListener {
                                        Log.e("GoogleSignIn", "Error al iniciar sesión con Google", it)
                                    }
                            }
                        )
                    }
                    composable("welcome/{email}") {
                        val email = it.arguments?.getString("email") ?: ""
                        WelcomeScreen(email, navController)
                    }
                    composable("crear_evento") {
                        CrearEventoScreen(navController, auth)
                    }
                    composable("ver_eventos") {
                        VerEventosScreen(navController, auth)
                    }
                    composable("otros_eventos") {
                        OtrosEventosScreen(navController, auth)
                    }
                    composable("detalle_evento/{id}") {
                        val id = it.arguments?.getString("id") ?: ""
                        DetalleEventoScreen(navController, id, auth)
                    }
                    composable("historial") {
                        HistorialEventosScreen(navController, auth)
                    }
                    composable("comentar/{eventoId}") { backStackEntry ->
                        val eventoId = backStackEntry.arguments?.getString("eventoId") ?: ""
                        ComentarScreen(navController, eventoId, auth)
                    }


                }
            }
        }
    }
}

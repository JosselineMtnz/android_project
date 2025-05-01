package com.example.login

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import com.example.login.ui.theme.LoginTheme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
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
            )
            .build()

        setContent {
            LoginTheme {
                val navController = rememberNavController()
                val context = LocalContext.current

                val launcher =
                    rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
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
                                            Log.e("FirebaseAuth", "Error al autenticar con Firebase", it.exception)
                                        }
                                    }
                                    .addOnFailureListener {
                                        Log.e("FirebaseAuth", "Excepción durante el login con Firebase", it)
                                    }
                            } else {
                                Log.e("GoogleSignIn", "ID Token nulo")
                            }
                        } catch (e: Exception) {
                            Log.e("GoogleSignIn", "Error al obtener credenciales: ${e.message}", e)
                        }
                    }

                NavHost(navController = navController, startDestination = "login") {
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
                                        // manejar error
                                    }
                            }
                        )
                    }
                    composable("welcome/{email}") { backStackEntry ->
                        val email = backStackEntry.arguments?.getString("email") ?: ""
                        WelcomeScreen(email, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    navController: NavHostController,
    auth: FirebaseAuth,
    onGoogleLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Inicio de Sesión", fontSize = 24.sp)

        Spacer(Modifier.height(24.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Campos vacíos."
                    showErrorDialog = true
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    errorMessage = "Correo inválido."
                    showErrorDialog = true
                } else {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                navController.navigate("welcome/${email}")
                            } else {
                                errorMessage = it.exception?.message ?: "Error desconocido."
                                showErrorDialog = true
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Iniciar sesión con correo", color = MaterialTheme.colorScheme.onPrimary)
        }

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(modifier = Modifier.weight(1f), color = Color.Gray)
            Text(
                text = "  o  ",
                modifier = Modifier.padding(horizontal = 8.dp),
                color = Color.Gray
            )
            Divider(modifier = Modifier.weight(1f), color = Color.Gray)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onGoogleLogin,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            ),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
        ) {
            Text("Iniciar sesión con Google")
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
fun WelcomeScreen(email: String, navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("¡Bienvenido!", fontSize = 24.sp)
        Text("Sesión iniciada como: $email")

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            FirebaseAuth.getInstance().signOut()
            navController.navigate("login") {
                popUpTo("welcome/{$email}") { inclusive = true }
            }
        }) {
            Text("Cerrar sesión")
        }
    }
}
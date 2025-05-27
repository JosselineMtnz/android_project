// screens/WelcomeScreen.kt
package com.example.login.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

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
            navController.navigate("ver_eventos")
        }) {
            Text("Ver Eventos")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            navController.navigate("crear_evento")
        }) {
            Text("Crear Evento")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            FirebaseAuth.getInstance().signOut()
            navController.navigate("login") {
                popUpTo(0)
            }
        }) {
            Text("Cerrar sesión")
        }
    }
}
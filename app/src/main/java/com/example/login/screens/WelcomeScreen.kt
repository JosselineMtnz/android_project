// screens/WelcomeScreen.kt
package com.example.login.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.login.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun WelcomeScreen(email: String, navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo2),
            contentDescription = "Logo PLANA",
            modifier = Modifier
                .size(120.dp)
        )

        Spacer(Modifier.height(32.dp))

        Text(
            text = "¡Bienvenido, $email!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedButton(
            onClick = { navController.navigate("ver_eventos") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text("Ver mis eventos")
        }

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedButton(
            onClick = { navController.navigate("crear_evento") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text("Crear evento")
        }

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedButton(
            onClick = { navController.navigate("historial") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text("Mi Historial")
        }

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedButton(
            onClick = { navController.navigate("otros_eventos") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text("Otros eventos")
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo(0)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.error),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Cerrar sesión")
        }
    }
}

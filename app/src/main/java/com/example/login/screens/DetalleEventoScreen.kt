package com.example.login.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.login.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun DetalleEventoScreen(
    navController: NavHostController,
    eventoId: String,
    auth: FirebaseAuth
) {
    val db = FirebaseFirestore.getInstance()
    val userEmail = auth.currentUser?.email ?: return
    var evento by remember { mutableStateOf<Map<String, String>?>(null) }
    var mensaje by remember { mutableStateOf("") }

    LaunchedEffect(eventoId) {
        db.collection("eventos").document(eventoId).get()
            .addOnSuccessListener { doc ->
                evento = doc.data?.mapValues { it.value.toString() }
            }
    }

    evento?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo2),
                contentDescription = "Logo App",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("🎫 ${it["titulo"]}", fontSize = 24.sp, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📝 ${it["descripcion"]}")
                    Text("📍 ${it["ubicacion"]}")
                    Text("📅 ${it["fecha"]} | 🕒 ${it["hora"]}")
                }
            }


        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

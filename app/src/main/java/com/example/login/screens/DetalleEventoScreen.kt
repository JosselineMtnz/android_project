//screens/DetalleEventoScreen.kt
package com.example.login.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun DetalleEventoScreen(
    navController: NavHostController,
    eventoId: String,
    auth: FirebaseAuth
) {
    val db = FirebaseFirestore.getInstance()
    val userEmail = auth.currentUser?.email ?: ""
    var evento by remember { mutableStateOf<Map<String, String>?>(null) }
    var mensaje by remember { mutableStateOf("") }

    LaunchedEffect(eventoId) {
        db.collection("eventos").document(eventoId).get()
            .addOnSuccessListener { doc ->
                evento = doc.data?.mapValues { it.value.toString() }
            }
    }

    evento?.let {
        Column(Modifier.padding(16.dp)) {
            Text("Título: ${it["titulo"]}", style = MaterialTheme.typography.headlineMedium)
            Text("Descripción: ${it["descripcion"]}")
            Text("Ubicación: ${it["ubicacion"]}")
            Text("Fecha: ${it["fecha"]}")
            Text("Hora: ${it["hora"]}")

            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                db.collection("eventos").document(eventoId)
                    .collection("asistentes")
                    .document(userEmail)
                    .set(mapOf("email" to userEmail))
                    .addOnSuccessListener {
                        mensaje = "Asistencia confirmada"
                    }
                    .addOnFailureListener {
                        mensaje = "Error al confirmar asistencia"
                    }
            }) {
                Text("Confirmar Asistencia")
            }

            Spacer(Modifier.height(8.dp))

            Button(onClick = {
                navController.navigate("comentario/$eventoId")
            }) {
                Text("Dejar Comentario")
            }

            if (mensaje.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(mensaje, color = MaterialTheme.colorScheme.primary)
            }
        }
    } ?: Text("Cargando evento...")
}

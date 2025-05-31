package com.example.login.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ComentarScreen(
    navController: NavHostController,
    eventoId: String,
    auth: FirebaseAuth
) {
    val db = FirebaseFirestore.getInstance()
    val userEmail = auth.currentUser?.email ?: return
    var comentario by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dejar Comentario", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = comentario,
            onValueChange = { comentario = it },
            label = { Text("Comentario") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Tu calificación")
        Row {
            for (i in 1..5) {
                IconToggleButton(
                    checked = i <= rating,
                    onCheckedChange = { rating = i }
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i <= rating) Color(0xFFFFC107) else Color.LightGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (comentario.isNotBlank() && rating > 0) {
                val data = hashMapOf(
                    "eventoId" to eventoId,
                    "correo" to userEmail,
                    "comentario" to comentario,
                    "rating" to rating,
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("comentarios")
                    .add(data)
                    .addOnSuccessListener {
                        mensaje = "✅ Comentario enviado"
                        navController.popBackStack()
                    }
                    .addOnFailureListener {
                        mensaje = "❌ Error al enviar comentario"
                    }
            } else {
                mensaje = "⚠️ Comentario y calificación requeridos"
            }
        }) {
            Text("Enviar")
        }

        if (mensaje.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(mensaje)
        }
    }
}

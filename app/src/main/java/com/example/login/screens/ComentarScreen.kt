package com.example.login.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    val eventoIdNum = eventoId.toIntOrNull() ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text("Dejar Comentario", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = comentario,
            onValueChange = { comentario = it },
            label = { Text("Comentario") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("¡Califica el evento, tu opinión es importante!")
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
                // Obtener fecha y hora actuales
                val ahora = Date()
                val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
                val fecha = formatoFecha.format(ahora)
                val hora = formatoHora.format(ahora)

                // Obtener último id_comentario
                db.collection("comentarios")
                    .orderBy("id_comentario", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { result ->
                        val ultimoId = result.documents.firstOrNull()?.getLong("id_comentario") ?: 0L
                        val nuevoId = ultimoId + 1

                        val data = hashMapOf(
                            "id_comentario" to nuevoId,
                            "id_evento" to eventoIdNum,
                            "creado_por" to userEmail,
                            "texto" to comentario,
                            "calificacion" to rating,
                            "fecha" to fecha,
                            "hora" to hora
                        )

                        db.collection("comentarios")
                            .add(data)
                            .addOnSuccessListener {
                                mensaje = "✅ Comentario enviado"
                                navController.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                mensaje = "❌ Error al enviar el comentario: ${e.message}"
                            }
                    }
            } else {
                mensaje = "⚠️ Comentario y calificación requeridos"
            }
        }) {
            Text("Enviar Comentario")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Obtener el último id_asistencia y luego guardar
                db.collection("asistencia")
                    .orderBy("id_asistencia", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { result ->
                        val ultimoId = result.documents.firstOrNull()?.getLong("id_asistencia") ?: 0L
                        val nuevoId = ultimoId + 1

                        val asistencia = hashMapOf(
                            "asistencia" to 1,
                            "id_evento" to eventoIdNum,
                            "correo" to userEmail,
                            "id_asistencia" to nuevoId,
                        )

                        db.collection("asistencia")
                            .add(asistencia)
                            .addOnSuccessListener {
                                mensaje = "✅ Asistencia confirmada"
                            }
                            .addOnFailureListener { e ->
                                mensaje = "❌ Error al confirmar asistencia: ${e.message}"
                            }
                    }
                    .addOnFailureListener { e ->
                        mensaje = "❌ Error al obtener el último ID: ${e.message}"
                    }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Confirmar Asistencia", color = Color.White)
        }

        if (mensaje.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(mensaje)
        }
    }
}
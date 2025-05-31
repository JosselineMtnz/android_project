package com.example.login.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.login.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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
    val comentarios = remember { mutableStateListOf<Map<String, Any>>() }

    // Cargar evento y comentarios
    LaunchedEffect(eventoId) {
        db.collection("eventos").document(eventoId).get()
            .addOnSuccessListener { doc ->
                evento = doc.data?.mapValues { it.value.toString() }
            }

        db.collection("comentarios")
            .whereEqualTo("eventoId", eventoId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                comentarios.clear()
                for (doc in snapshot.documents) {
                    comentarios.add(doc.data ?: emptyMap())
                }
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


//aca
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val asistencia = hashMapOf(
                        "correo" to userEmail,
                        "id_evento" to (it["id"]?.toLongOrNull() ?: -1L)
                    )

                    db.collection("asistencia").add(asistencia)
                        .addOnSuccessListener { mensaje = "✅ Asistencia confirmada" }
                        .addOnFailureListener { mensaje = "❌ Error al confirmar asistencia" }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar Asistencia")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    navController.navigate("comentar/${eventoId}")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dejar Comentario")
            }

            if (mensaje.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(mensaje)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Comentarios y Calificaciones", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            LazyColumn {
                items(comentarios) { comentario ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("👤 ${comentario["correo"] ?: "Anónimo"}", fontWeight = FontWeight.SemiBold)
                            Text("📝 ${comentario["comentario"] ?: ""}")
                            Row {
                                val rating = (comentario["rating"] as? Long)?.toInt() ?: 0
                                repeat(rating) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                                }
                                repeat(5 - rating) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }

        //esto va aca 
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

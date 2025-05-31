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
import android.util.Log

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
        val idNum = eventoId.toLongOrNull() ?: -1L

        db.collection("eventos")
            .whereEqualTo("id", idNum)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val doc = snapshot.documents.firstOrNull()
                evento = doc?.data?.mapValues { it.value.toString() }
                Log.d("MiApp", "Evento cargado: $evento")
            }

        db.collection("comentarios")
            .whereEqualTo("id_evento", idNum)
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d("MiApp", "Comentarios encontrados: ${snapshot.size()}")
                comentarios.clear()
                for (doc in snapshot.documents) {
                    comentarios.add(doc.data ?: emptyMap())
                }
                Log.d("MiApp", "Comentarios cargados: ${comentarios.size}")
            }
            .addOnFailureListener {
                Log.e("MiApp", "Error al cargar comentarios: ${it.message}")
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
                    Text(
                        "🎫 ${it["titulo"]}",
                        fontSize = 24.sp,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📝 ${it["descripcion"]}")
                    Text("📍 ${it["ubicacion"]}")
                    Text("📅 ${it["fecha"]} | 🕒 ${it["hora"]}")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección de comentarios
            Text(
                text = "Comentarios",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (comentarios.isEmpty()) {
                Text("Aún no hay comentarios.", modifier = Modifier.padding(top = 8.dp))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(comentarios) { comentario ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Usuario y fecha/hora
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = comentario["creado_por"]?.toString() ?: "Anónimo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    val fecha = comentario["fecha"]?.toString() ?: ""
                                    val hora = comentario["hora"]?.toString() ?: ""
                                    Text(
                                        text = "$fecha $hora",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Texto del comentario
                                Text(
                                    text = comentario["texto"]?.toString() ?: "",
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Calificación con estrellas y label
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Calificación: ",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    val rating = (comentario["calificacion"] as? Long)?.toInt() ?: 0
                                    repeat(rating) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Estrella",
                                            tint = Color(0xFFFFC107),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

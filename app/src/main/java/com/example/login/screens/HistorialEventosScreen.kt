package com.example.login.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.tasks.await

@Composable
fun HistorialEventosScreen(navController: NavHostController, auth: FirebaseAuth) {
    val db = FirebaseFirestore.getInstance()
    val userEmail = auth.currentUser?.email?.lowercase()?.trim() ?: ""
    val eventosAsistidos = remember { mutableStateListOf<Map<String, Any>>() }
    val errorMessage = remember { mutableStateOf("") }

    LaunchedEffect(userEmail) {
        eventosAsistidos.clear()
        errorMessage.value = ""

        try {
            val asistenciaSnapshot = db.collection("asistencia")
                .whereEqualTo("correo", userEmail)
                .get()
                .await()

            println("Asistencia total: ${asistenciaSnapshot.size()}")

            asistenciaSnapshot.documents.forEach { docAsistencia ->
                val idEventoAny = docAsistencia.get("id_evento")
                println("id_evento encontrado: $idEventoAny (tipo: ${idEventoAny?.javaClass})")

                if (idEventoAny != null) {
                    val querySnapshot = db.collection("eventos")
                        .whereEqualTo("id", idEventoAny)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val eventoDoc = querySnapshot.documents[0]
                        val data = eventoDoc.data?.toMutableMap() ?: mutableMapOf()
                        println("Evento encontrado: ${data["titulo"]}")
                        eventosAsistidos.add(data)
                    } else {
                        println("No existe evento con id = $idEventoAny")
                    }
                }
            }
        } catch (e: Exception) {
            errorMessage.value = "Error al cargar historial: ${e.message}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo2),
                contentDescription = "Logo PLANA",
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = "Eventos asistidos",
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage.value.isNotEmpty()) {
            Text(errorMessage.value, color = MaterialTheme.colorScheme.error)
        } else if (eventosAsistidos.isEmpty()) {
            Text("No has asistido a ningún evento.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn {
                items(eventosAsistidos) { evento ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                // Ejemplo navegación:
                                // val docId = evento["docId"] as? String ?: ""
                                // navController.navigate("detalle_evento/$docId")
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "🎫 ${evento["titulo"] ?: "Sin título"}",
                                fontSize = 30.sp,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("📅 ${evento["fecha"] ?: "Sin fecha"}")
                            Text("📍 ${evento["ubicacion"] ?: "Sin ubicación"}")
                        }
                    }
                }
            }
        }
    }
}

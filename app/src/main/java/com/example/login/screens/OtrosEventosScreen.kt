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

@Composable
fun OtrosEventosScreen(navController: NavHostController, auth: FirebaseAuth) {
    val eventos = remember { mutableStateListOf<Map<String, String>>() }
    val db = FirebaseFirestore.getInstance()
    val userEmail = auth.currentUser?.email?.lowercase()?.trim() ?: ""

    var showDialog by remember { mutableStateOf(false) }
    var eventoSeleccionado by remember { mutableStateOf<Map<String, String>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }
    var mensajeExito by remember { mutableStateOf("") }

    LaunchedEffect(userEmail) {
        db.collection("eventos")
            .whereNotEqualTo("creadoPor", userEmail)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Error en consulta: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    println("Snapshot es null")
                    return@addSnapshotListener
                }

                println("Eventos recibidos: ${snapshot.size()}")
                eventos.clear()
                for (doc in snapshot.documents) {
                    println("Evento doc id: ${doc.id}, creadoPor: ${doc.getString("creadoPor")}")
                    val data = doc.data?.mapValues { it.value.toString() }?.toMutableMap() ?: mutableMapOf()
                    data["docId"] = doc.id
                    eventos.add(data)
                }
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
                text = "Otros Eventos",
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn {
            items(eventos) { evento ->
                val id = evento["id"] ?: return@items
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            val eventoId = evento["id"] ?: return@clickable
                            navController.navigate("comentar/$eventoId")
                            /*eventoSeleccionado = evento
                            showDialog = true
                            mensajeError = ""
                            mensajeExito = ""*/
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🎫 ${evento["titulo"]}", fontSize = 30.sp, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("📝 ${evento["descripcion"]}")
                        Text("📍 ${evento["ubicacion"]}")
                        Text("📅 ${evento["fecha"]} | 🕒 ${evento["hora"]}")
                    }
                }
            }
        }

        if (showDialog && eventoSeleccionado != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("HOLAAAAA!") },
                text = { Text("¿Quieres asistir a este evento?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            isLoading = true
                            mensajeError = ""
                            mensajeExito = ""

                            db.collection("asistencia")
                                .orderBy("id_asistencia", com.google.firebase.firestore.Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    val maxIdAsistencia = if (!querySnapshot.isEmpty) {
                                        val doc = querySnapshot.documents[0]
                                        (doc.getLong("id_asistencia") ?: 1L).toInt()
                                    } else {
                                        1
                                    }

                                    val nuevoIdAsistencia = maxIdAsistencia + 1

                                    val nuevoRegistro = hashMapOf(
                                        "correo" to userEmail,
                                        "id_evento" to (eventoSeleccionado!!["id"] ?: ""),
                                        "asistencia" to 1,
                                        "id_asistencia" to nuevoIdAsistencia
                                    )

                                    db.collection("asistencia")
                                        .add(nuevoRegistro)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            mensajeExito = "¡Te esperamos!"
                                            showDialog = false
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            mensajeError = "Error al registrar asistencia: ${e.message}"
                                        }
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    mensajeError = "Error al obtener último id_asistencia: ${e.message}"
                                }
                        }
                    ) {
                        Text("Sí")
                    }
                }
            )
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        if (mensajeError.isNotEmpty()) {
            Text(text = mensajeError, color = MaterialTheme.colorScheme.error)
        }

        if (mensajeExito.isNotEmpty()) {
            Text(text = mensajeExito, color = MaterialTheme.colorScheme.primary)
        }
    }
}

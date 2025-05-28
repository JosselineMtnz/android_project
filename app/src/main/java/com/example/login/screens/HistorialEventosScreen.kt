// screens/HistorialEventosScreen.kt
package com.example.login.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HistorialEventosScreen(navController: NavHostController, auth: FirebaseAuth) {
    val db = FirebaseFirestore.getInstance()
    val userEmail = auth.currentUser?.email ?: ""
    val eventosAsistidos = remember { mutableStateListOf<Map<String, Any>>() }

    LaunchedEffect(userEmail) {
        db.collection("eventos").get().addOnSuccessListener { snapshot ->
            eventosAsistidos.clear()
            for (doc in snapshot) {
                doc.reference.collection("asistentes").document(userEmail).get()
                    .addOnSuccessListener { asistente ->
                        if (asistente.exists()) {
                            eventosAsistidos.add(doc.data)
                        }
                    }
            }
        }
    }

    Column(Modifier.padding(16.dp)) {
        Text("Historial de Asistencia", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(eventosAsistidos) { evento ->
                Card(modifier = Modifier.padding(8.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Título: ${evento["titulo"]}")
                        Text("Fecha: ${evento["fecha"]}")
                        Text("Ubicación: ${evento["ubicacion"]}")
                    }
                }
            }
        }
    }
}

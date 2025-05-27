// screens/VerEventosScreen.kt
package com.example.login.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun VerEventosScreen(navController: NavHostController, auth: FirebaseAuth) {
    val eventos = remember { mutableStateListOf<Map<String, String>>() }
    val db = FirebaseFirestore.getInstance()
    val userEmail = auth.currentUser?.email ?: ""

    LaunchedEffect(userEmail) {
        db.collection("eventos")
            .whereEqualTo("creadoPor", userEmail)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                eventos.clear()
                for (doc in snapshot.documents) {
                    val data = doc.data?.mapValues { it.value.toString() } ?: emptyMap()
                    eventos.add(data)
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Eventos", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(eventos) { evento ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Título: ${evento["titulo"]}", fontSize = 18.sp)
                        Text("Descripción: ${evento["descripcion"]}")
                        Text("Ubicación: ${evento["ubicacion"]}")
                        Text("Fecha: ${evento["fecha"]}")
                        Text("Hora: ${evento["hora"]}")
                        Text("Creado Por: ${evento["creadoPor"]}")
                    }
                }
            }
        }
    }
}

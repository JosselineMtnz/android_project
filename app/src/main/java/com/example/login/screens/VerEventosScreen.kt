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
fun VerEventosScreen(navController: NavHostController, auth: FirebaseAuth) {
    val eventos = remember { mutableStateListOf<Map<String, String>>() }
    val db = FirebaseFirestore.getInstance()
    val userEmail = auth.currentUser?.email ?: ""

    LaunchedEffect(userEmail) {
        db.collection("eventos")
            .whereEqualTo("creadoPor", userEmail)
            .orderBy("id", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                eventos.clear()
                for (doc in snapshot.documents) {
                    val data = doc.data?.mapValues { it.value.toString() }?.toMutableMap() ?: mutableMapOf()
                    data["id"] = doc.id
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
                text = "Mis Eventos",
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
                            navController.navigate("detalle_evento/$id")
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
                        //Text("👤 ${evento["creadoPor"]}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

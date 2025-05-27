// screens/CrearEventoScreen.kt
package com.example.login.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CrearEventoScreen(navController: NavHostController, auth: FirebaseAuth) {
    val db = FirebaseFirestore.getInstance()
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear Evento", fontSize = 24.sp)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = ubicacion, onValueChange = { ubicacion = it }, label = { Text("Ubicación") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = fecha, onValueChange = { fecha = it }, label = { Text("Fecha") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = hora, onValueChange = { hora = it }, label = { Text("Hora") })

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            val userEmail = auth.currentUser?.email ?: ""
            if (titulo.isNotBlank() && descripcion.isNotBlank() && ubicacion.isNotBlank()) {
                val evento = hashMapOf(
                    "titulo" to titulo,
                    "descripcion" to descripcion,
                    "ubicacion" to ubicacion,
                    "fecha" to fecha,
                    "hora" to hora,
                    "creadoPor" to userEmail
                )
                db.collection("eventos")
                    .add(evento)
                    .addOnSuccessListener {
                        mensaje = "Evento guardado correctamente."
                        navController.navigate("ver_eventos")
                    }
                    .addOnFailureListener {
                        mensaje = "Error al guardar evento."
                        Log.e("CrearEvento", it.message.toString())
                    }
            } else {
                mensaje = "Todos los campos son obligatorios."
            }
        }) {
            Text("Guardar Evento")
        }

        if (mensaje.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(mensaje, color = MaterialTheme.colorScheme.error)
        }
    }
}
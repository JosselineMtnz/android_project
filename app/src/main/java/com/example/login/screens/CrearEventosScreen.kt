package com.example.login.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.login.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo2),
            contentDescription = "Logo",
            modifier = Modifier.size(100.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text("Crear Evento", fontSize = 26.sp)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.Black))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.Black))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = ubicacion, onValueChange = { ubicacion = it }, label = { Text("Ubicación") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.Black))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = fecha, onValueChange = { fecha = it }, label = { Text("Fecha") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.Black))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = hora, onValueChange = { hora = it }, label = { Text("Hora") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = Color.Black))

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val userEmail = auth.currentUser?.email ?: ""
                if (titulo.isNotBlank() && descripcion.isNotBlank() && ubicacion.isNotBlank()) {
                    // Obtener el último ID
                    db.collection("eventos")
                        .orderBy("id", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val lastId = snapshot.documents.firstOrNull()?.getLong("id") ?: 3L
                            val newId = lastId + 1

                            val evento = hashMapOf(
                                "id" to newId,
                                "titulo" to titulo,
                                "descripcion" to descripcion,
                                "ubicacion" to ubicacion,
                                "fecha" to fecha,
                                "hora" to hora,
                                "creadoPor" to userEmail
                            )

                            db.collection("eventos")
                                .document(newId.toString())
                                .set(evento)
                                .addOnSuccessListener {
                                    mensaje = "✅ Evento guardado correctamente"
                                    navController.navigate("ver_eventos")
                                }
                                .addOnFailureListener {
                                    mensaje = "❌ Error al guardar evento"
                                }
                        }
                        .addOnFailureListener {
                            mensaje = "❌ Error al generar ID"
                            //Log.e("CrearEvento", it.message.toString())
                        }
                } else {
                    mensaje = "⚠️ Todos los campos son obligatorios"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Evento")
        }

        if (mensaje.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(mensaje, color = MaterialTheme.colorScheme.primary)
        }
    }
}

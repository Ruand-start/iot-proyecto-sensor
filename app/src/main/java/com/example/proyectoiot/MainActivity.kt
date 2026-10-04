package com.example.proyectoiot

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectoiot.ui.theme.ProyectoIoTTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = FirebaseAuth.getInstance()

        setContent {
            ProyectoIoTTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var isUserLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (!isUserLoggedIn) {
                            LoginScreen(
                                auth = auth,
                                onLoginSuccess = { isUserLoggedIn = true }
                            )
                        } else {
                            DashboardScreen(
                                onLogout = {
                                    auth.signOut()
                                    isUserLoggedIn = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(auth: FirebaseAuth, onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Monitor IoT - Acceso", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Color.Red, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    auth.signInWithEmailAndPassword(email.trim(), password.trim())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onLoginSuccess()
                            } else {
                                errorMessage = task.exception?.localizedMessage ?: "Error al autenticar"
                            }
                        }
                } else {
                    errorMessage = "Por favor ingrese correo y contraseña"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar Sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } else {
                                errorMessage = task.exception?.localizedMessage ?: "Error al registrar"
                            }
                        }
                } else {
                    errorMessage = "Ingrese correo y contraseña para registrarse"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear Cuenta")
        }
    }
}

@Composable
fun DashboardScreen(onLogout: () -> Unit) {
    var temperatura by remember { mutableStateOf(0.0) }
    var alertaActiva by remember { mutableStateOf(false) }

    // Conexión a Realtime Database en tiempo real
    val database = FirebaseDatabase.getInstance().getReference("sensores")

    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempVal = snapshot.child("temperatura").getValue(Double::class.java)
                val alertaVal = snapshot.child("alerta").getValue(Boolean::class.java)

                if (tempVal != null) temperatura = tempVal
                if (alertaVal != null) alertaActiva = alertaVal
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        database.addValueEventListener(listener)

        onDispose {
            database.removeEventListener(listener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Panel de Monitoreo IoT", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (temperatura > 25.0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Temperatura Actual", fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = String.format("%.1f °C", temperatura),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (temperatura > 25.0) Color.Red else Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (temperatura > 25.0) "¡ALERTA: SOBRETEMPERATURA!" else "Estado: Normal",
                    fontWeight = FontWeight.SemiBold,
                    color = if (temperatura > 25.0) Color.Red else Color(0xFF2E7D32)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de control interactivo bidireccional
        Button(
            onClick = {
                val nuevoEstado = !alertaActiva
                database.child("alerta").setValue(nuevoEstado)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (alertaActiva) Color.Red else MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (alertaActiva) "Desactivar Alerta Manual" else "Activar Alerta Manual")
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onLogout) {
            Text("Cerrar Sesión", color = Color.Gray)
        }
    }
}
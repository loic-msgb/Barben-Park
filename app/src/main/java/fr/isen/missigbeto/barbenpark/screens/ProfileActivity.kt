package fr.isen.missigbeto.barbenpark.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.missigbeto.barbenpark.components.BottomNavBar
import fr.isen.missigbeto.barbenpark.models.User
import fr.isen.missigbeto.barbenpark.ui.theme.BarbenParkTheme
import fr.isen.missigbeto.barbenpark.utils.AuthHelper
import kotlinx.coroutines.launch

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarbenParkTheme {
                ProfileScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var isLoading by remember { mutableStateOf(false) }
    var isLoggedIn by remember { mutableStateOf(AuthHelper.isUserLoggedIn()) }
    var user by remember { mutableStateOf<User?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Charger les données utilisateur si connecté
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            isLoading = true
            coroutineScope.launch {
                AuthHelper.getUserData()
                    .onSuccess { userData ->
                        user = userData
                        isLoading = false
                    }
                    .onFailure { e ->
                        errorMessage = e.message
                        isLoading = false
                    }
            }
        }
    }
    
    Scaffold(
        bottomBar = { BottomNavBar(currentRoute = "profile") }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (isLoggedIn && user != null) {
                // Affichage du profil utilisateur
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Icône profil
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profil",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Nom et prénom
                    Text(
                        text = "${user!!.prenom} ${user!!.nom}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Email
                    Text(
                        text = user!!.email,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Age
                    Text(
                        text = "Age: ${user!!.age} ans",
                        fontSize = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Bouton de déconnexion
                    Button(
                        onClick = {
                            AuthHelper.signOut()
                            isLoggedIn = false
                            user = null
                        }
                    ) {
                        Text(text = "Déconnexion")
                    }
                }
            } else {
                // Écran non connecté
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Vous n'êtes pas connecté",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            val intent = Intent(context, LoginActivity::class.java)
                            context.startActivity(intent)
                        }
                    ) {
                        Text(text = "Se connecter")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TextButton(
                        onClick = {
                            val intent = Intent(context, RegisterActivity::class.java)
                            context.startActivity(intent)
                        }
                    ) {
                        Text(text = "Créer un compte")
                    }
                }
            }
            
            // Afficher les erreurs éventuelles
            errorMessage?.let {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Text(text = it)
                }
            }
        }
    }
}
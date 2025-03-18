package fr.isen.missigbeto.barbenpark.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import fr.isen.missigbeto.barbenpark.models.Animal
import fr.isen.missigbeto.barbenpark.ui.theme.BarbenParkTheme
import kotlinx.coroutines.tasks.await

class AnimalsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Récupérer les paramètres passés à l'activité
        val zoneId = intent.getStringExtra("ZONE_ID") ?: ""
        val enclosureId = intent.getStringExtra("ENCLOSURE_ID") ?: ""
        val zoneColor = intent.getStringExtra("ZONE_COLOR") ?: "#CCCCCC"
        
        setContent {
            BarbenParkTheme {
                AnimalsScreen(zoneId, enclosureId, zoneColor)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalsScreen(zoneId: String, enclosureId: String, zoneColor: String) {
    var animals by remember { mutableStateOf<List<Animal>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Récupérer la couleur de la zone
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(zoneColor))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    // Récupérer le contexte
    val context = LocalContext.current

    // Effet pour charger les données au démarrage
    LaunchedEffect(key1 = "$zoneId-$enclosureId") {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val animalsSnapshot = firestore.collection("zones")
                .document(zoneId)
                .collection("enclosures")
                .document(enclosureId)
                .collection("animals")
                .get()
                .await()
            
            val animalsList = mutableListOf<Animal>()
            for (document in animalsSnapshot.documents) {
                val id = document.id
                val name = document.getString("name") ?: ""
                val id_animal = document.getString("id_animal") ?: ""
                
                animalsList.add(Animal(id = id, name = name, id_animal = id_animal))
            }
            
            animals = animalsList
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Animaux de l'enclos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = "Erreur: $error",
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                animals.isEmpty() -> {
                    Text(
                        text = "Aucun animal trouvé dans cet enclos",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(animals) { animal ->
                            AnimalCard(animal = animal, zoneColor = zoneColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimalCard(animal: Animal, zoneColor: String) {
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(zoneColor)).copy(alpha = 0.7f)
    } catch (e: Exception) {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = animal.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "ID: ${animal.id_animal}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
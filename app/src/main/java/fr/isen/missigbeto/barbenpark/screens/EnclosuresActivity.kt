package fr.isen.missigbeto.barbenpark.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
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
import fr.isen.missigbeto.barbenpark.models.Enclosure
import fr.isen.missigbeto.barbenpark.ui.theme.BarbenParkTheme
import kotlinx.coroutines.tasks.await

class EnclosuresActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Récupérer les paramètres passés à l'activité
        val zoneId = intent.getStringExtra("ZONE_ID") ?: ""
        val zoneName = intent.getStringExtra("ZONE_NAME") ?: "Enclos"
        val zoneColor = intent.getStringExtra("ZONE_COLOR") ?: "#CCCCCC"
        
        setContent {
            BarbenParkTheme {
                EnclosuresScreen(zoneId, zoneName, zoneColor)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnclosuresScreen(zoneId: String, zoneName: String, zoneColor: String) {
    var enclosures by remember { mutableStateOf<List<Enclosure>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Récupérer la couleur de la zone
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(zoneColor))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    // Récupérer le contexte ici, dans le contexte @Composable
    val context = LocalContext.current

    // Effet pour charger les données au démarrage
    LaunchedEffect(key1 = zoneId) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val enclosuresSnapshot = firestore.collection("zones")
                .document(zoneId)
                .collection("enclosures")
                .get()
                .await()
            
            val enclosuresList = mutableListOf<Enclosure>()
            for (document in enclosuresSnapshot.documents) {
                val id = document.id
                val id_biomes = document.getString("id_biomes") ?: ""
                val meal = document.getString("meal") ?: ""
                val etat = document.getString("etat") ?: "ouvert"
                // Utilisez toDouble() qui est plus sûr que getDouble qui pourrait retourner null
                val note = document.get("note")?.toString()?.toDoubleOrNull() ?: 0.0
                
                enclosuresList.add(Enclosure(
                    id = id, 
                    id_biomes = id_biomes, 
                    meal = meal,
                    etat = etat,
                    note = note
                ))
            }
            
            enclosures = enclosuresList
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
                        text = zoneName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Utiliser le contexte récupéré plus haut
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
                enclosures.isEmpty() -> {
                    Text(
                        text = "Aucun enclos trouvé dans cette zone",
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
                        items(enclosures) { enclosure ->
                            EnclosureTile(enclosure = enclosure, zoneColor = zoneColor)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnclosureTile(enclosure: Enclosure, zoneColor: String) {
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(zoneColor)).copy(alpha = 0.7f)
    } catch (e: Exception) {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    // Récupérer le contexte pour la navigation
    val context = LocalContext.current
    
    // Récupérer l'ID de zone depuis les arguments d'intent de l'activité actuelle
    val zoneId = (context as? EnclosuresActivity)?.intent?.getStringExtra("ZONE_ID") ?: ""
    
    // Couleur pour l'état de l'enclos
    val etatColor = if (enclosure.etat == "ouvert") Color.Green else Color.Red
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        onClick = {
            // Navigation vers AnimalsActivity
            val intent = Intent(context, AnimalsActivity::class.java).apply {
                putExtra("ZONE_ID", zoneId)
                putExtra("ENCLOSURE_ID", enclosure.id)
                putExtra("ZONE_COLOR", zoneColor)
            }
            context.startActivity(intent)
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enclos ${enclosure.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                
                // Affichage de l'état (ouvert/fermé)
                Text(
                    text = enclosure.etat.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = etatColor,
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Biome: ${enclosure.id_biomes}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            if (enclosure.meal.isNotEmpty()) {
                Text(
                    text = "Nourriture: ${enclosure.meal}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            
            // Affichage de la note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Note: ",
                    fontSize = 14.sp,
                    color = Color.White
                )
                
                // Affichage des étoiles pour la note
                RatingDisplay(rating = enclosure.note)
                
                // Affichage de la note en chiffre
                Text(
                    text = " (${String.format("%.1f", enclosure.note)})",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

// Composant pour afficher les étoiles de notation
@Composable
fun RatingDisplay(rating: Double) {
    Row {
        for (i in 1..5) {
            val starFill = when {
                i <= rating -> 1f
                i - rating < 1 -> i - rating
                else -> 0f
            }
            
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Étoile $i",
                tint = Color.Yellow.copy(alpha = starFill.toFloat()),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
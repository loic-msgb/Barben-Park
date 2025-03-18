package fr.isen.missigbeto.barbenpark.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import fr.isen.missigbeto.barbenpark.components.BottomNavBar
import fr.isen.missigbeto.barbenpark.models.Enclosure
import fr.isen.missigbeto.barbenpark.ui.theme.BarbenParkTheme
import fr.isen.missigbeto.barbenpark.utils.AuthHelper
import fr.isen.missigbeto.barbenpark.utils.FirestoreHelper
import kotlinx.coroutines.launch
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
                title = { Text(zoneName) },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        bottomBar = { BottomNavBar(currentRoute = "zones") }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (enclosures.isEmpty()) {
                Text(
                    text = "Aucun enclos trouvé dans cette zone.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(enclosures) { enclosure ->
                        EnclosureTile(enclosure = enclosure, zoneColor = zoneColor, zoneId = zoneId)
                    }
                }
            }
            
            // Afficher les erreurs éventuelles
            error?.let {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Text(text = it)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnclosureTile(enclosure: Enclosure, zoneColor: String, zoneId: String) {
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(zoneColor)).copy(alpha = 0.7f)
    } catch (e: Exception) {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // État pour suivre la note donnée par l'utilisateur courant
    var userRating by remember { mutableStateOf(0.0) }
    var isRatingLoading by remember { mutableStateOf(true) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // Ajouter cette ligne pour gérer la note temporaire dans la boîte de dialogue
    var dialogTempRating by remember { mutableStateOf(0) }
    
    // Charger la note de l'utilisateur
    LaunchedEffect(enclosure.id) {
        if (AuthHelper.isUserLoggedIn()) {
            FirestoreHelper.getUserRating(zoneId, enclosure.id)
                .onSuccess { rating ->
                    userRating = rating
                    isRatingLoading = false
                }
                .onFailure {
                    isRatingLoading = false
                }
        } else {
            isRatingLoading = false
        }
    }
    
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
            
            // Affichage de la note moyenne
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Note moyenne: ",
                    fontSize = 14.sp,
                    color = Color.White
                )
                
                // Affichage des étoiles pour la note moyenne
                RatingDisplay(rating = enclosure.note)
                
                // Affichage de la note en chiffre
                Text(
                    text = " (${String.format("%.1f", enclosure.note)})",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
            
            // Section pour noter l'enclos (uniquement pour les utilisateurs connectés)
            if (AuthHelper.isUserLoggedIn()) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.White.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (userRating > 0) "Votre note: " else "Noter cet enclos: ",
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Affichage des étoiles cliquables pour noter
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showRatingDialog = true }
                    ) {
                        for (i in 1..5) {
                            if (i <= userRating.toInt() || (i == userRating.toInt() + 1 && userRating % 1 > 0)) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Étoile $i",
                                    tint = Color.Yellow,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(2.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Étoile vide $i",
                                    tint = Color.Yellow.copy(alpha = 0.3f), // Réduire l'opacité pour les étoiles vides
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Boîte de dialogue pour noter l'enclos
    if (showRatingDialog) {
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text("Noter cet enclos") },
            text = {
                Column {
                    Text("Sélectionnez une note:")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Étoile $i",
                                tint = if (i <= dialogTempRating) Color.Yellow else Color.Yellow.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(4.dp)
                                    .clickable { dialogTempRating = i }
                            )
                        }
                    }
                    
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            FirestoreHelper.rateEnclosure(zoneId, enclosure.id, dialogTempRating.toDouble())
                                .onSuccess {
                                    userRating = dialogTempRating.toDouble()
                                    showRatingDialog = false
                                    // Rafraîchir les données après la notation
                                    (context as? ComponentActivity)?.recreate()
                                }
                                .onFailure { e ->
                                    errorMessage = e.message
                                }
                        }
                    }
                ) {
                    Text("Noter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) {
                    Text("Annuler")
                }
            }
        )
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
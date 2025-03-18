package fr.isen.missigbeto.barbenpark.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import fr.isen.missigbeto.barbenpark.models.Zone
import fr.isen.missigbeto.barbenpark.ui.theme.BarbenParkTheme
import kotlinx.coroutines.tasks.await
import fr.isen.missigbeto.barbenpark.screens.EnclosuresActivity
import fr.isen.missigbeto.barbenpark.screens.AnimalsActivity
import fr.isen.missigbeto.barbenpark.components.BottomNavBar

class ZonesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarbenParkTheme {
                ZonesScreenWithNavBar()
            }
        }
    }
}

@Composable
fun ZonesScreenWithNavBar() {
    Scaffold(
        bottomBar = { BottomNavBar(currentRoute = "zones") }
    ) { innerPadding ->
        ZonesScreen(modifier = Modifier.padding(innerPadding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZonesScreen(modifier: Modifier = Modifier) {
    var zones by remember { mutableStateOf<List<Zone>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Effet pour charger les données au démarrage
    LaunchedEffect(key1 = "loadZones") {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val zonesSnapshot = firestore.collection("zones").get().await()
            
            val zonesList = mutableListOf<Zone>()
            for (document in zonesSnapshot.documents) {
                val id = document.id
                val name = document.getString("name") ?: ""
                val color = document.getString("color") ?: "#CCCCCC"
                
                zonesList.add(Zone(id = id, name = name, color = color))
            }
            
            zones = zonesList
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Les zones du parc",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
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
                zones.isEmpty() -> {
                    Text(
                        text = "Aucune zone trouvée",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(zones) { zone ->
                            ZoneTile(zone = zone)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneTile(zone: Zone) {
    val context = LocalContext.current
    
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(zone.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        onClick = {
            // Navigation vers EnclosuresActivity
            val intent = Intent(context, EnclosuresActivity::class.java).apply {
                putExtra("ZONE_ID", zone.id)
                putExtra("ZONE_NAME", zone.name)
                putExtra("ZONE_COLOR", zone.color)
            }
            context.startActivity(intent)
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = zone.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
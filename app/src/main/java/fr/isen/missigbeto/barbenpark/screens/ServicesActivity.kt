package fr.isen.missigbeto.barbenpark.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.missigbeto.barbenpark.components.BottomNavBar
import fr.isen.missigbeto.barbenpark.ui.theme.BarbenParkTheme
import kotlin.math.sqrt

class ServicesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarbenParkTheme {
                ServicesScreenWithNavBar()
            }
        }
    }
}

data class Service(
    val name: String,
    val emoji: String,
    val x: Int,
    val y: Int
)

@Composable
fun ServicesScreenWithNavBar() {
    Scaffold(
        bottomBar = { BottomNavBar(currentRoute = "services") }
    ) { innerPadding ->
        ServicesScreen(modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun ServicesScreen(modifier: Modifier = Modifier) {
    val services = listOf(
        Service("Toilettes", "🚽", 10, 90),
        Service("Point d'eau", "🚰", 15, 85),
        Service("Boutique", "🛍️", 20, 20),
        Service("Gare", "🚂", 5, 5),
        Service("Lodge", "🏠", 80, 80),
        Service("Tente pédagogique", "🎪", 40, 75),
        Service("Café", "☕", 25, 25),
        Service("Espace pique-nique", "🧺", 50, 50),
        Service("Trajet train", "🚆", 0, 0),
        Service("Paillote", "🏖️", 70, 20),
        Service("Petit café", "🍵", 30, 70),
        Service("Plateau des jeux", "🎲", 60, 60),
        Service("Point de vues", "🔭", 90, 10)
    )

    var selectedStart by remember { mutableStateOf<Service?>(null) }
    var selectedEnd by remember { mutableStateOf<Service?>(null) }
    var resultText by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Services disponibles",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            DropdownSelector("Départ", services, selectedStart) { selectedStart = it }
            DropdownSelector("Arrivée", services, selectedEnd) { selectedEnd = it }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (selectedStart != null && selectedEnd != null) {
                        val dx = (selectedStart!!.x - selectedEnd!!.x).toDouble()
                        val dy = (selectedStart!!.y - selectedEnd!!.y).toDouble()
                        val distanceMeters = sqrt(dx * dx + dy * dy) * 10 // 1 unité = 10 m
                        val walkingSpeed = 5000.0 / 3600.0 // 5km/h = ~1.39 m/s
                        val timeSeconds = distanceMeters / walkingSpeed
                        val minutes = timeSeconds.toInt() / 60
                        val seconds = (timeSeconds % 60).toInt()
                        resultText = "Distance : ${distanceMeters.toInt()} m\nTemps estimé : ${minutes} min ${seconds} sec"
                    } else {
                        resultText = "Veuillez sélectionner un départ et une arrivée."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculer le trajet")
            }

            if (resultText.isNotEmpty()) {
                Text(
                    text = resultText,
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(services) { service ->
                    ServiceCard(service = service)
                }
            }
        }
    }
}

@Composable
fun DropdownSelector(
    label: String,
    options: List<Service>,
    selected: Service?,
    onSelect: (Service) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = selected?.let { "${it.emoji} ${it.name}" } ?: "Sélectionner un service")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { service ->
                    DropdownMenuItem(
                        text = { Text("${service.emoji} ${service.name}") },
                        onClick = {
                            onSelect(service)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceCard(service: Service) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = service.emoji,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = service.name,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "(${service.x}, ${service.y})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

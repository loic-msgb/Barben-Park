package fr.isen.missigbeto.barbenpark

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.isen.missigbeto.barbenpark.screens.ZonesActivity
import fr.isen.missigbeto.barbenpark.ui.theme.BarbenParkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarbenParkTheme {
                WelcomeContent()
            }
        }
    }
}

@Composable
fun WelcomeContent() {
    // Récupérer le contexte actuel pour lancer l'activité
    val context = LocalContext.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Image de fond floutée
        Image(
            painter = painterResource(id = R.drawable.welcome),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 3.dp),
            contentScale = ContentScale.Crop
        )

        // Bouton central avec navigation vers ZonesActivity
        Button(
            onClick = { 
                // Lancer l'activité ZonesActivity
                val intent = Intent(context, ZonesActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        ) {
            Text(text = stringResource(R.string.welcomeButton))
        }
    }
}
package fr.isen.missigbeto.barbenpark

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.gson.Gson
import fr.isen.missigbeto.barbenpark.models.Zone
import fr.isen.missigbeto.barbenpark.screens.ZonesActivity
import fr.isen.missigbeto.barbenpark.ui.theme.BarbenParkTheme
import fr.isen.missigbeto.barbenpark.utils.FirestoreHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Décommentez la ligne suivante pour réimporter les données dans Firestore
        //importDataToFirestore()
        
        // Redirection vers ZonesActivity
        val intent = Intent(this, ZonesActivity::class.java)
        startActivity(intent)
        finish() // Ferme MainActivity pour qu'elle ne reste pas dans le back stack
    }
    
    private fun importDataToFirestore() {
        try {
            // Lire le fichier JSON depuis les assets
            val jsonString = assets.open("zoo.json").bufferedReader().use { it.readText() }
            
            // Convertir le JSON en liste d'objets Zone
            val zonesList = Gson().fromJson(jsonString, Array<Zone>::class.java).toList()
            
            // Lancer l'upload vers Firestore
            CoroutineScope(Dispatchers.Main).launch {
                FirestoreHelper.uploadZooData(zonesList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
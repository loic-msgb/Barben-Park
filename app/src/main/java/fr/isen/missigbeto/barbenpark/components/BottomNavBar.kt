package fr.isen.missigbeto.barbenpark.components

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import fr.isen.missigbeto.barbenpark.screens.ServicesActivity
import fr.isen.missigbeto.barbenpark.screens.ZonesActivity

@Composable
fun BottomNavBar(currentRoute: String) {
    val context = LocalContext.current
    
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Place, contentDescription = "Zones") },
            label = { Text("Zones") },
            selected = currentRoute == "zones",
            onClick = {
                if (currentRoute != "zones") {
                    val intent = Intent(context, ZonesActivity::class.java)
                    context.startActivity(intent)
                }
            }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Info, contentDescription = "Services") },
            label = { Text("Services") },
            selected = currentRoute == "services",
            onClick = {
                if (currentRoute != "services") {
                    val intent = Intent(context, ServicesActivity::class.java)
                    context.startActivity(intent)
                }
            }
        )
    }
}
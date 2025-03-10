package fr.isen.missigbeto.barbenpark.utils

import com.google.firebase.firestore.FirebaseFirestore
import fr.isen.missigbeto.barbenpark.models.Zone
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FirestoreHelper {

    suspend fun uploadZooData(zones: List<Zone>) {
        withContext(Dispatchers.IO) {
            try {
                val firestore = FirebaseFirestore.getInstance() // ✅ Initialisation ici

                for (zone in zones) {
                    val zoneRef = firestore.collection("zones").document(zone.id)
                    zoneRef.set(mapOf("name" to zone.name, "color" to zone.color)).await()

                    for (enclosure in zone.enclosures) {
                        val enclosureRef = zoneRef.collection("enclosures").document(enclosure.id)
                        enclosureRef.set(mapOf("id_biomes" to enclosure.id_biomes, "meal" to enclosure.meal)).await()

                        for (animal in enclosure.animals) {
                            val animalRef = enclosureRef.collection("animals").document(animal.id)
                            animalRef.set(mapOf("name" to animal.name, "id_animal" to animal.id_animal)).await()
                        }
                    }
                }
                println("🔥 Importation Firestore réussie !")
            } catch (e: Exception) {
                println("❌ Erreur lors de l'importation Firestore : ${e.message}")
            }
        }
    }
}


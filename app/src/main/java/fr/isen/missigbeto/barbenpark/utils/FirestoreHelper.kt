package fr.isen.missigbeto.barbenpark.utils

import com.google.firebase.firestore.FirebaseFirestore
import fr.isen.missigbeto.barbenpark.models.Zone
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

object FirestoreHelper {

    suspend fun uploadZooData(zones: List<Zone>) {
        withContext(Dispatchers.IO) {
            try {
                val firestore = FirebaseFirestore.getInstance()

                for (zone in zones) {
                    val zoneRef = firestore.collection("zones").document(zone.id)
                    zoneRef.set(mapOf("name" to zone.name, "color" to zone.color)).await()

                    for (enclosure in zone.enclosures) {
                        val enclosureRef = zoneRef.collection("enclosures").document(enclosure.id)
                        
                        // Générer un état aléatoire pour la démonstration
                        val etat = if (Random.nextBoolean()) "ouvert" else "fermé"
                        
                        // Générer une note aléatoire pour la démonstration (entre 1.0 et 5.0)
                        val note = (Random.nextDouble() * 4 + 1).round(1)
                        
                        enclosureRef.set(mapOf(
                            "id_biomes" to enclosure.id_biomes, 
                            "meal" to enclosure.meal,
                            "etat" to etat,
                            "note" to note
                        )).await()

                        for (animal in enclosure.animals) {
                            val animalRef = enclosureRef.collection("animals").document(animal.id)
                            animalRef.set(mapOf(
                                "name" to animal.name, 
                                "id_animal" to animal.id_animal
                            )).await()
                        }
                    }
                }
                println("🔥 Importation Firestore réussie !")
            } catch (e: Exception) {
                println("❌ Erreur lors de l'importation Firestore : ${e.message}")
            }
        }
    }
    
    // Extension pour arrondir les nombres à décimale
    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }
}


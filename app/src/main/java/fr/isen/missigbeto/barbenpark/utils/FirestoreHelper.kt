package fr.isen.missigbeto.barbenpark.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import fr.isen.missigbeto.barbenpark.models.Rating
import fr.isen.missigbeto.barbenpark.models.Zone
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
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
    
    // Soumettre une note pour un enclos
    suspend fun rateEnclosure(zoneId: String, enclosureId: String, rating: Double): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    return@withContext Result.failure(Exception("Utilisateur non connecté"))
                }
                
                val firestore = FirebaseFirestore.getInstance()
                val ratingId = UUID.randomUUID().toString()
                
                // Créer l'objet Rating
                val ratingObj = Rating(
                    id = ratingId,
                    userId = currentUser.uid,
                    zoneId = zoneId,
                    enclosureId = enclosureId,
                    rating = rating
                )
                
                // Vérifier si l'utilisateur a déjà noté cet enclos
                val existingRatings = firestore.collection("ratings")
                    .whereEqualTo("userId", currentUser.uid)
                    .whereEqualTo("zoneId", zoneId)
                    .whereEqualTo("enclosureId", enclosureId)
                    .get()
                    .await()
                
                // Si l'utilisateur a déjà noté, mettre à jour sa note
                if (!existingRatings.isEmpty) {
                    val existingRatingDoc = existingRatings.documents[0]
                    firestore.collection("ratings")
                        .document(existingRatingDoc.id)
                        .update("rating", rating, "timestamp", System.currentTimeMillis())
                        .await()
                } else {
                    // Sinon, ajouter une nouvelle note
                    firestore.collection("ratings")
                        .document(ratingId)
                        .set(ratingObj)
                        .await()
                }
                
                // Calculer la moyenne des notes pour cet enclos
                updateEnclosureAverageRating(zoneId, enclosureId)
                
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Récupérer la note donnée par l'utilisateur à un enclos spécifique
    suspend fun getUserRating(zoneId: String, enclosureId: String): Result<Double> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    return@withContext Result.success(0.0) // Aucune note si non connecté
                }
                
                val firestore = FirebaseFirestore.getInstance()
                val ratingsSnapshot = firestore.collection("ratings")
                    .whereEqualTo("userId", currentUser.uid)
                    .whereEqualTo("zoneId", zoneId)
                    .whereEqualTo("enclosureId", enclosureId)
                    .get()
                    .await()
                
                if (ratingsSnapshot.isEmpty) {
                    return@withContext Result.success(0.0) // Aucune note trouvée
                }
                
                val rating = ratingsSnapshot.documents[0].getDouble("rating") ?: 0.0
                Result.success(rating)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Calculer et mettre à jour la note moyenne d'un enclos
    private suspend fun updateEnclosureAverageRating(zoneId: String, enclosureId: String) {
        withContext(Dispatchers.IO) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                
                // Récupérer toutes les notes pour cet enclos
                val ratingsSnapshot = firestore.collection("ratings")
                    .whereEqualTo("zoneId", zoneId)
                    .whereEqualTo("enclosureId", enclosureId)
                    .get()
                    .await()
                
                if (ratingsSnapshot.isEmpty) return@withContext
                
                // Calculer la moyenne
                var totalRating = 0.0
                var count = 0
                
                for (doc in ratingsSnapshot.documents) {
                    val rating = doc.getDouble("rating")
                    if (rating != null) {
                        totalRating += rating
                        count++
                    }
                }
                
                val averageRating = if (count > 0) (totalRating / count).round(1) else 0.0
                
                // Mettre à jour la note moyenne dans l'enclos
                firestore.collection("zones")
                    .document(zoneId)
                    .collection("enclosures")
                    .document(enclosureId)
                    .update("note", averageRating)
                    .await()
                
            } catch (e: Exception) {
                println("❌ Erreur lors de la mise à jour de la note moyenne : ${e.message}")
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


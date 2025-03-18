package fr.isen.missigbeto.barbenpark.models

data class Enclosure(
    val id: String = "",
    val id_biomes: String = "",
    val meal: String = "",
    val animals: List<Animal> = emptyList(),
    val etat: String = "ouvert", // Valeur par défaut "ouvert"
    val note: Double = 0.0 // Valeur par défaut 0.0
)
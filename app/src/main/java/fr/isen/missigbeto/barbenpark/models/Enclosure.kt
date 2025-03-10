package fr.isen.missigbeto.barbenpark.models

data class Enclosure(
    val id: String = "",
    val id_biomes: String = "",
    val meal: String = "",
    val animals: List<Animal> = emptyList()
)
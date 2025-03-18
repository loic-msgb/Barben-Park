package fr.isen.missigbeto.barbenpark.models

data class Rating(
    val id: String = "",
    val userId: String = "",
    val zoneId: String = "",
    val enclosureId: String = "",
    val rating: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
package fr.isen.missigbeto.barbenpark.models

data class Zone(
    val id: String = "",
    val name: String = "",
    val color: String = "",
    val enclosures: List<Enclosure> = emptyList()
)
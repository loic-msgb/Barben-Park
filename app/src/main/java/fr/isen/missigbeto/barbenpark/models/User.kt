package fr.isen.missigbeto.barbenpark.models

data class User(
    val id: String = "",
    val nom: String = "",
    val prenom: String = "",
    val age: Int = 0,
    val email: String = "",
    val role: String = "visiteur" // Valeur par défaut "visiteur"
)
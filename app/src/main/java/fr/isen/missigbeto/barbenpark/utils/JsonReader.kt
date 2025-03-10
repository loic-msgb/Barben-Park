package fr.isen.missigbeto.barbenpark.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.isen.missigbeto.barbenpark.models.Zone
import java.io.BufferedReader
import java.io.InputStreamReader

object JsonReader {

    fun loadZooData(context: Context): List<Zone> {
        try {
            // Lire le fichier JSON dans les assets
            val inputStream = context.assets.open("zoo.json")
            val reader = BufferedReader(InputStreamReader(inputStream))

            // Définir le type des données attendues
            val zoneListType = object : TypeToken<List<Zone>>() {}.type

            // Convertir le JSON en objets Kotlin
            return Gson().fromJson(reader, zoneListType)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Retourner une liste vide en cas d'erreur
        return emptyList()
    }
}

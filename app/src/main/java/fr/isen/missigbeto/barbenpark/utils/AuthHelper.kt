package fr.isen.missigbeto.barbenpark.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import fr.isen.missigbeto.barbenpark.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object AuthHelper {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    // Vérifier si un utilisateur est connecté
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    
    // Récupérer l'utilisateur Firebase actuel
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    // Inscription avec email et mot de passe
    suspend fun signUp(email: String, password: String, nom: String, prenom: String, age: Int): Result<FirebaseUser> {
        return withContext(Dispatchers.IO) {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                
                if (firebaseUser != null) {
                    // Créer le profil utilisateur dans Firestore
                    val user = User(
                        id = firebaseUser.uid,
                        nom = nom,
                        prenom = prenom,
                        age = age,
                        email = email
                    )
                    
                    // Stocker les données utilisateur dans Firestore
                    firestore.collection("users")
                        .document(firebaseUser.uid)
                        .set(user)
                        .await()
                    
                    Result.success(firebaseUser)
                } else {
                    Result.failure(Exception("Impossible de créer l'utilisateur"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Connexion avec email et mot de passe
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return withContext(Dispatchers.IO) {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                
                if (firebaseUser != null) {
                    Result.success(firebaseUser)
                } else {
                    Result.failure(Exception("Connexion échouée"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Déconnexion
    fun signOut() {
        auth.signOut()
    }
    
    // Récupérer les données de l'utilisateur depuis Firestore
    suspend fun getUserData(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    return@withContext Result.failure(Exception("Aucun utilisateur connecté"))
                }

                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                if (!userDoc.exists()) {
                    return@withContext Result.failure(Exception("Données utilisateur introuvables"))
                }

                val user = User(
                    id = currentUser.uid,
                    nom = userDoc.getString("nom") ?: "",
                    prenom = userDoc.getString("prenom") ?: "",
                    age = userDoc.getLong("age")?.toInt() ?: 0,
                    email = userDoc.getString("email") ?: "",
                    role = userDoc.getString("role") ?: "visiteur"
                )

                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
package org.example.aapanam.data.remote

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.aapanam.data.model.Item
import org.example.aapanam.data.model.Sale

class FirebaseService {

    private val firestore: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    // Auth functions
    suspend fun signIn(email: String, password: String) = auth.signInWithEmailAndPassword(email, password)
    suspend fun signUp(email: String, password: String) = auth.createUserWithEmailAndPassword(email, password)
    suspend fun signOut() = auth.signOut()
    fun getCurrentUser() = auth.currentUser

    // Item functions
    suspend fun getItems(userId: String): List<Item> {
        val snapshot = firestore.collection("users").document(userId).collection("item").get()
        return snapshot.documents.map { it.data() }
    }

    suspend fun addItem(userId: String, item: Item) {
        firestore.collection("users").document(userId).collection("item").document(item.id.toString()).set(item)
    }

    suspend fun deleteItem(userId: String, itemId: String) {
        firestore.collection("users").document(userId).collection("item").document(itemId).delete()
    }

    fun getItemsFlow(userId: String): Flow<List<Item>> {
        return firestore.collection("users").document(userId).collection("item").snapshots.map { snapshot ->
            snapshot.documents.map { it.data() }
        }
    }

    // Sale functions
    suspend fun getSales(userId: String): List<Sale> {
        val snapshot = firestore.collection("users").document(userId).collection("sale").get()
        return snapshot.documents.map { it.data() }
    }

    suspend fun addSale(userId: String, sale: Sale) {
        firestore.collection("users").document(userId).collection("sale").document(sale.id.toString()).set(sale)
    }

    fun getSalesFlow(userId: String): Flow<List<Sale>> {
        return firestore.collection("users").document(userId).collection("sale").snapshots.map { snapshot ->
            snapshot.documents.map { it.data() }
        }
    }

}

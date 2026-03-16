package org.example.aapanam.data.remote

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.aapanam.data.model.CreditPayment
import org.example.aapanam.data.model.Customer
import org.example.aapanam.data.model.Item
import org.example.aapanam.data.model.Sale
import org.example.aapanam.util.Logger

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
        return try {
            val snapshot = firestore.collection("users").document(userId).collection("item").get()
            snapshot.documents.map { it.data() }
        } catch (e: Exception) {
            Logger.e("Failed to get items: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun addItem(userId: String, item: Item) {
        try {
            firestore.collection("users").document(userId).collection("item").document(item.id.toString()).set(item)
        } catch (e: Exception) {
            Logger.e("Failed to add item ${item.id}: ${e.message}", e)
        }
    }

    suspend fun addItems(userId: String, items: List<Item>) {
        if (items.isEmpty()) return
        try {
            val batch = firestore.batch()
            items.forEach { item ->
                val docRef = firestore.collection("users").document(userId).collection("item").document(item.id.toString())
                batch.set(docRef, item)
            }
            batch.commit()
            Logger.i("Successfully synced ${items.size} items")
        } catch (e: Exception) {
            Logger.e("Failed to batch add items: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteItem(userId: String, itemId: String) {
        try {
            firestore.collection("users").document(userId).collection("item").document(itemId).delete()
        } catch (e: Exception) {
            Logger.e("Failed to delete item $itemId: ${e.message}", e)
        }
    }

    fun getItemsFlow(userId: String): Flow<List<Item>> {
        return firestore.collection("users").document(userId).collection("item").snapshots.map { snapshot ->
            snapshot.documents.map { it.data() }
        }
    }

    // Sale functions
    suspend fun getSales(userId: String): List<Sale> {
        return try {
            val snapshot = firestore.collection("users").document(userId).collection("sale").get()
            snapshot.documents.map { it.data() }
        } catch (e: Exception) {
            Logger.e("Failed to get sales: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun addSale(userId: String, sale: Sale) {
        try {
            firestore.collection("users").document(userId).collection("sale").document(sale.id.toString()).set(sale)
        } catch (e: Exception) {
            Logger.e("Failed to add sale ${sale.id}: ${e.message}", e)
        }
    }

    suspend fun addSales(userId: String, sales: List<Sale>) {
        if (sales.isEmpty()) return
        try {
            val batch = firestore.batch()
            sales.forEach { sale ->
                val docRef = firestore.collection("users").document(userId).collection("sale").document(sale.id.toString())
                batch.set(docRef, sale)
            }
            batch.commit()
            Logger.i("Successfully synced ${sales.size} sales")
        } catch (e: Exception) {
            Logger.e("Failed to batch add sales: ${e.message}", e)
            throw e
        }
    }

    fun getSalesFlow(userId: String): Flow<List<Sale>> {
        return firestore.collection("users").document(userId).collection("sale").snapshots.map { snapshot ->
            snapshot.documents.map { it.data() }
        }
    }

    // Customer functions
    suspend fun addCustomer(userId: String, customer: Customer) {
        try {
            firestore.collection("users").document(userId).collection("customer").document(customer.id.toString()).set(customer)
        } catch (e: Exception) {
            Logger.e("Failed to add customer ${customer.id}: ${e.message}", e)
        }
    }

    suspend fun addCustomers(userId: String, customers: List<Customer>) {
        if (customers.isEmpty()) return
        try {
            val batch = firestore.batch()
            customers.forEach { customer ->
                val docRef = firestore.collection("users").document(userId).collection("customer").document(customer.id.toString())
                batch.set(docRef, customer)
            }
            batch.commit()
            Logger.i("Successfully synced ${customers.size} customers")
        } catch (e: Exception) {
            Logger.e("Failed to batch add customers: ${e.message}", e)
            throw e
        }
    }

    // CreditPayment functions
    suspend fun addCreditPayment(userId: String, payment: CreditPayment) {
        try {
            firestore.collection("users").document(userId).collection("credit_payment").document(payment.id.toString()).set(payment)
        } catch (e: Exception) {
            Logger.e("Failed to add credit payment ${payment.id}: ${e.message}", e)
        }
    }

    suspend fun addCreditPayments(userId: String, payments: List<CreditPayment>) {
        if (payments.isEmpty()) return
        try {
            val batch = firestore.batch()
            payments.forEach { payment ->
                val docRef = firestore.collection("users").document(userId).collection("credit_payment").document(payment.id.toString())
                batch.set(docRef, payment)
            }
            batch.commit()
            Logger.i("Successfully synced ${payments.size} credit payments")
        } catch (e: Exception) {
            Logger.e("Failed to batch add credit payments: ${e.message}", e)
            throw e
        }
    }

}

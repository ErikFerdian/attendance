package com.azhar.absensi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.azhar.absensi.model.ModelFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _dataLaporan = MutableLiveData<List<ModelFirestore>>()
    val dataLaporan: LiveData<List<ModelFirestore>> get() = _dataLaporan

    init {
        loadHistoryData()
    }

    private fun loadHistoryData() {
        val userId = auth.currentUser?.uid // Mendapatkan UID pengguna saat ini
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .collection("absensi")
                .get()
                .addOnSuccessListener { result ->
                    val historyList = result.documents.mapNotNull { document ->
                        document.toObject(ModelFirestore::class.java)?.apply { id = document.id }
                    }
                    _dataLaporan.value = historyList // Hasil sudah berupa List<ModelFirestore>
                }
                .addOnFailureListener {
                    _dataLaporan.value = emptyList() // Mengatur data kosong jika gagal
                }
        } else {
            _dataLaporan.value = emptyList() // Mengatur data kosong jika pengguna tidak terautentikasi
        }
    }

    // Fungsi untuk memperbarui dataLaporan
    fun updateDataLaporan(dataList: List<ModelFirestore>) {
        _dataLaporan.value = dataList
    }

    fun deleteDataById(id: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection("absensi")
            .document(id)
            .delete()
            .addOnSuccessListener {
                loadHistoryData() // Refresh data setelah penghapusan
            }
    }
}

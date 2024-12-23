package com.azhar.absensi.view.history

import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityHistoryBinding
import com.azhar.absensi.model.ModelFirestore
import com.azhar.absensi.view.history.HistoryAdapter.HistoryAdapterCallback
import com.azhar.absensi.viewmodel.HistoryViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HistoryActivity : AppCompatActivity(), HistoryAdapterCallback {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setInitLayout()
        setViewModel()
        loadDataHistory() // Real-time listener untuk data history
    }

    private fun setInitLayout() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        binding.tvNotFound.visibility = View.GONE
        binding.progressBar.visibility = View.GONE // Sembunyikan loading saat inisialisasi

        // Inisialisasi Adapter dan RecyclerView
        historyAdapter = HistoryAdapter(this, mutableListOf(), this)
        binding.rvHistory.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }
    }

    private fun setViewModel() {
        historyViewModel = ViewModelProvider(this)[HistoryViewModel::class.java]

        // Observasi data dari ViewModel
        historyViewModel.dataLaporan.observe(this) { modelFirestoreList ->
            binding.progressBar.visibility = View.GONE // Sembunyikan loading saat data sudah ada
            if (modelFirestoreList.isEmpty()) {
                binding.tvNotFound.visibility = View.VISIBLE
                binding.rvHistory.visibility = View.GONE
            } else {
                binding.tvNotFound.visibility = View.GONE
                binding.rvHistory.visibility = View.VISIBLE
            }
            historyAdapter.setDataAdapter(modelFirestoreList)
        }
    }

    private fun loadDataHistory() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            binding.progressBar.visibility = View.VISIBLE // Tampilkan loading saat memuat data
            firestore.collection("users")
                .document(userId)
                .collection("absensi")
                .orderBy("tanggal", Query.Direction.DESCENDING) // Urutkan data berdasarkan tanggal
                .addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (querySnapshot != null) {
                        val dataList = querySnapshot.documents.mapNotNull { document ->
                            document.toObject(ModelFirestore::class.java)?.apply {
                                id = document.id
                            }
                        }
                        // Mengupdate LiveData di ViewModel
                        historyViewModel.updateDataLaporan(dataList)
                    }
                }
        } else {
            Toast.makeText(this, "Pengguna tidak ditemukan. Harap login.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDelete(modelFirestore: ModelFirestore?) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("Hapus riwayat ini?")
        alertDialogBuilder.setPositiveButton("Ya, Hapus") { _, _ ->
            val id = modelFirestore?.id ?: return@setPositiveButton
            val userId = auth.currentUser?.uid ?: return@setPositiveButton
            firestore.collection("users")
                .document(userId)
                .collection("absensi")
                .document(id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Data berhasil dihapus.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menghapus data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        alertDialogBuilder.setNegativeButton("Batal") { dialogInterface: DialogInterface, _ ->
            dialogInterface.cancel()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Fungsi untuk mendekode Base64 menjadi Bitmap
     */
    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}

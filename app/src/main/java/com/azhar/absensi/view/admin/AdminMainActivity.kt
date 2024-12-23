package com.azhar.absensi.view.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.azhar.absensi.R
import com.azhar.absensi.adapter.AbsenAdapter
import com.azhar.absensi.model.Absensi
import com.azhar.absensi.view.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminMainActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var absenAdapter: AbsenAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        // Inisialisasi Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Aktifkan tombol kembali di Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Admin Dashboard"

        // Inisialisasi Firestore
        db = FirebaseFirestore.getInstance()

        // Inisialisasi RecyclerView
        absenAdapter = AbsenAdapter()
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewAbsensi)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = absenAdapter

        // Memuat data absensi dari Firestore
        loadAbsens()

        // Menambahkan listener untuk tombol logout
        val logoutButton: ImageView = findViewById(R.id.imageLogout)
        logoutButton.setOnClickListener {
            logoutAndReturnToLogin() // Panggil fungsi logout
        }
    }

    private fun loadAbsens() {
        db.collection("absensi")
            .get()
            .addOnSuccessListener { documents ->
                val absenList = documents.map { document ->
                    document.toObject(Absensi::class.java)
                }
                absenAdapter.submitList(absenList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal memuat data absen: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                logoutAndReturnToLogin() // Logout dan kembali ke LoginActivity
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logoutAndReturnToLogin() {
        // Logout dari Firebase Authentication
        FirebaseAuth.getInstance().signOut()

        // Pindah ke LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)

        // Tutup aktivitas saat ini
        finish()
    }
}

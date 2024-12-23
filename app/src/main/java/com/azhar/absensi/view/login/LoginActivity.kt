package com.azhar.absensi.view.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.azhar.absensi.databinding.ActivityLoginBinding
import com.azhar.absensi.utils.SessionLogin
import com.azhar.absensi.view.main.AdminMainActivity
import com.azhar.absensi.view.main.MainActivity
import com.azhar.absensi.view.register.RegisterActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var session: SessionLogin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        session = SessionLogin(applicationContext)

        if (session.isLoggedIn()) {
            val email = session.getEmail()
            if (!email.isNullOrEmpty()) {
                navigateToDashboard(email)
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.inputNama.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            when {
                email.isEmpty() -> {
                    binding.inputNama.error = "Nama atau Email tidak boleh kosong"
                    binding.inputNama.requestFocus()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    binding.inputNama.error = "Format email tidak valid"
                    binding.inputNama.requestFocus()
                }
                password.isEmpty() -> {
                    binding.inputPassword.error = "Password tidak boleh kosong"
                    binding.inputPassword.requestFocus()
                }
                else -> loginUser(email, password)
            }
        }

        // Perbaikan: menggunakan safe call untuk btnRegister yang nullable
        binding.btnRegister?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Fitur lupa password belum diimplementasikan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val isAdmin = email == "admin@gmail.com"
                session.createLoginSession(email, email) // Menyimpan email sebagai nama untuk sementara
                navigateToDashboard(email, isAdmin)
            } else {
                val errorMessage = when {
                    task.exception?.message?.contains("There is no user record") == true -> "Email tidak terdaftar"
                    task.exception?.message?.contains("The password is invalid") == true -> "Password salah"
                    task.exception?.message?.contains("network error") == true -> "Kesalahan jaringan, periksa koneksi Anda"
                    else -> "Login gagal: ${task.exception?.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun navigateToDashboard(email: String, isAdmin: Boolean = false) {
        val intent = if (isAdmin) {
            Intent(this, AdminMainActivity::class.java) // Admin dashboard
        } else {
            Intent(this, MainActivity::class.java) // Siswa dashboard
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

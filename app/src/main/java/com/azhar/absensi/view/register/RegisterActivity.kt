package com.azhar.absensi.view.register

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.azhar.absensi.databinding.ActivityRegisterBinding
import com.azhar.absensi.view.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            when {
                email.isEmpty() -> {
                    binding.inputEmail.error = "Email tidak boleh kosong"
                    binding.inputEmail.requestFocus()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    binding.inputEmail.error = "Format email tidak valid"
                    binding.inputEmail.requestFocus()
                }
                password.isEmpty() -> {
                    binding.inputPassword.error = "Password tidak boleh kosong"
                    binding.inputPassword.requestFocus()
                }
                password.length < 6 -> {
                    binding.inputPassword.error = "Password minimal 6 karakter"
                    binding.inputPassword.requestFocus()
                }
                else -> registerUser(email, password)
            }
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Menutup aktivitas ini agar tidak kembali ke register.
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                val errorMessage = when {
                    task.exception?.message?.contains("email address is already in use") == true ->
                        "Email sudah terdaftar"
                    task.exception?.message?.contains("network error") == true ->
                        "Kesalahan jaringan, periksa koneksi Anda"
                    else -> "Registrasi gagal: ${task.exception?.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

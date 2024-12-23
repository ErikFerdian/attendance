package com.azhar.absensi.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.azhar.absensi.view.login.LoginActivity

class SessionLogin(var context: Context) {
    private var pref: SharedPreferences
    private var editor: SharedPreferences.Editor
    private val PRIVATE_MODE = 0

    init {
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }

    // Menambahkan email sebagai parameter dalam sesi login
    fun createLoginSession(nama: String, email: String) {
        editor.putBoolean(IS_LOGIN, true)
        editor.putString(KEY_NAMA, nama)
        editor.putString(KEY_EMAIL, email)  // Simpan email
        editor.apply() // Menggunakan apply() yang lebih efisien
    }

    // Mengecek apakah pengguna sudah login atau belum
    fun checkLogin() {
        if (!isLoggedIn()) {
            val intent = Intent(context, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    // Logout dan membersihkan sesi
    fun logoutUser() {
        editor.clear().apply() // Menggunakan apply() untuk efisiensi
        val intent = Intent(context, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    // Mengecek apakah pengguna sudah login
    fun isLoggedIn(): Boolean = pref.getBoolean(IS_LOGIN, false)

    // Mendapatkan email yang disimpan dalam sesi login
    fun getEmail(): String? = pref.getString(KEY_EMAIL, null)

    companion object {
        private const val PREF_NAME = "AbsensiPref"
        private const val IS_LOGIN = "IsLoggedIn"
        const val KEY_NAMA = "NAMA"
        const val KEY_EMAIL = "EMAIL"  // Key untuk email
    }
}

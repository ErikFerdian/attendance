package com.azhar.absensi.model

data class Absensi(
    val nama: String = "",
    val tanggal: String = "",
    val lokasi: String = "",
    val keterangan: String = "",
    val foto: String = "", // URL atau Base64 string
    val timestamp: Long = 0L
)

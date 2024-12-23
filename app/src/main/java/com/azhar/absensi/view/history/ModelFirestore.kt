package com.azhar.absensi.model

data class ModelFirestore(
    var id: String = "",
    var nama: String = "",
    var lokasi: String = "",
    var tanggal: String = "",
    var keterangan: String = "",
    var foto: String? = "" // Menambahkan properti foto
)

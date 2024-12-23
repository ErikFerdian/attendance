package com.azhar.absensi.model

class TblAbsensi {
    // Getter dan Setter
    var uid: Int = 0
    var nama: String? = null
    var fotoSelfie: String? = null
    var tanggal: String? = null
    var lokasi: String? = null
    var keterangan: String? = null
    var newColumn: Int = 0

    constructor()

    constructor(
        uid: Int,
        nama: String?,
        fotoSelfie: String?,
        tanggal: String?,
        lokasi: String?,
        keterangan: String?,
        newColumn: Int
    ) {
        this.uid = uid
        this.nama = nama
        this.fotoSelfie = fotoSelfie
        this.tanggal = tanggal
        this.lokasi = lokasi
        this.keterangan = keterangan
        this.newColumn = newColumn
    }
}
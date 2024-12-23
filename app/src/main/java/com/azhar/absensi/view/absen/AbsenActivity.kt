
package com.azhar.absensi.view.absen

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ActivityAbsenBinding
import com.azhar.absensi.utils.BitmapManager.bitmapToBase64
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AbsenActivity : AppCompatActivity() {

    companion object {
        const val DATA_TITLE = "data_title"
    }

    private lateinit var binding: ActivityAbsenBinding
    private val REQ_CAMERA = 101
    private var strCurrentLatitude = 0.0
    private var strCurrentLongitude = 0.0
    private var strFilePath: String = ""
    private var strBase64Photo: String = ""
    private var strCurrentLocation: String = ""
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var useAutomaticLocation = true // Default menggunakan lokasi otomatis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbsenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupLocationSwitch()
        setInitLayout()
        checkAndRequestPermissions()
        loadUserEmail()
        setUploadData()

        if (useAutomaticLocation) {
            setCurrentLocation()
        } else {
            enableManualLocation()
        }
    }

    private fun setupLocationSwitch() {
        binding.switchUseLocation.setOnCheckedChangeListener { _, isChecked ->
            println("Switch changed: $isChecked")
            if (isChecked) {
                useAutomaticLocation = true
                setCurrentLocation()
            } else {
                useAutomaticLocation = false
                resetLocation()
                enableManualLocation()
            }
            println("useAutomaticLocation: $useAutomaticLocation")
        }
    }


    private fun loadUserEmail() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val email = document.getString("email") ?: "Email tidak ditemukan"
                        binding.inputNama.setText(email)
                    } else {
                        Toast.makeText(this, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Gagal memuat email: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Pengguna tidak login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUploadData() {
        binding.btnAbsen.setOnClickListener {
            val strNama = binding.inputNama.text.toString()
            val strTanggal = binding.inputTanggal.text.toString()
            val strKeterangan = binding.inputKeterangan.text.toString()
            val strLokasi = binding.inputLokasi.text.toString()

            if (strNama.isEmpty() || strTanggal.isEmpty() || strKeterangan.isEmpty() ||
                (useAutomaticLocation && strLokasi.isEmpty())) {
                Toast.makeText(this@AbsenActivity, "Data tidak boleh ada yang kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (useAutomaticLocation && strCurrentLocation.isEmpty()) {
                Toast.makeText(this@AbsenActivity, "Gagal mendapatkan lokasi otomatis. Gunakan input manual.", Toast.LENGTH_SHORT).show()
                enableManualLocation()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid
            if (userId != null) {
                // Data yang akan disimpan
                val dataAbsen = mapOf(
                    "nama" to strNama,
                    "tanggal" to strTanggal,
                    "lokasi" to strLokasi,
                    "keterangan" to strKeterangan,
                    "foto" to strBase64Photo,
                    "timestamp" to System.currentTimeMillis()
                )


                // Simpan di subkoleksi absensi pengguna
                firestore.collection("users")
                    .document(userId)
                    .collection("absensi")
                    .add(dataAbsen)
                    .addOnSuccessListener {
                        Toast.makeText(this@AbsenActivity, "Data berhasil disimpan untuk pengguna!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@AbsenActivity, "Gagal menyimpan data untuk pengguna: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                // Simpan di koleksi utama absensi untuk admin
                firestore.collection("absensi")
                    .add(dataAbsen)
                    .addOnSuccessListener {
                        Toast.makeText(this@AbsenActivity, "Data berhasil disimpan untuk admin!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@AbsenActivity, "Gagal menyimpan data untuk admin: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this@AbsenActivity, "Pengguna tidak ditemukan. Harap login terlebih dahulu.", Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun checkAndRequestPermissions() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (!report.areAllPermissionsGranted()) {
                        Toast.makeText(this@AbsenActivity, "Izin diperlukan untuk menggunakan aplikasi.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    private fun setCurrentLocation() {
        if (!useAutomaticLocation) return

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Izin lokasi tidak diberikan!", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                strCurrentLatitude = location.latitude
                strCurrentLongitude = location.longitude
                val geocoder = Geocoder(this@AbsenActivity, Locale.getDefault())
                try {
                    val addressList = geocoder.getFromLocation(strCurrentLatitude, strCurrentLongitude, 1)
                    strCurrentLocation = addressList?.get(0)?.getAddressLine(0) ?: "Lokasi tidak ditemukan"
                    binding.inputLokasi.setText(strCurrentLocation)
                    binding.inputLokasi.isEnabled = false
                } catch (e: IOException) {
                    Toast.makeText(this, "Kesalahan saat mendapatkan lokasi!", Toast.LENGTH_SHORT).show()
                    enableManualLocation()
                }
            } else {
                Toast.makeText(this@AbsenActivity, "Gagal mendapatkan lokasi!", Toast.LENGTH_SHORT).show()
                enableManualLocation()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mendapatkan lokasi. Periksa koneksi internet atau GPS!", Toast.LENGTH_SHORT).show()
            enableManualLocation()
        }
    }

    private fun enableManualLocation() {
        useAutomaticLocation = false
        binding.switchUseLocation.isChecked = false
        binding.inputLokasi.isEnabled = true
        binding.inputLokasi.text?.clear()
        println("Manual location enabled: ${binding.inputLokasi.isEnabled}")
    }



    private fun resetLocation() {
        strCurrentLatitude = 0.0
        strCurrentLongitude = 0.0
        strCurrentLocation = ""
        binding.inputLokasi.text?.clear()
        println("Location reset")
    }

    private fun setInitLayout() {
        val tanggalAbsen = Calendar.getInstance()
        binding.inputTanggal.setText(
            SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(tanggalAbsen.time)
        )

        binding.inputTanggal.setOnClickListener {
            DatePickerDialog(
                this@AbsenActivity,
                { _: DatePicker, year: Int, month: Int, day: Int ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, day)
                    binding.inputTanggal.setText(
                        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(selectedDate.time)
                    )
                },
                tanggalAbsen[Calendar.YEAR],
                tanggalAbsen[Calendar.MONTH],
                tanggalAbsen[Calendar.DAY_OF_MONTH]
            ).show()
        }

        binding.layoutImage.setOnClickListener { openCamera() }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File? = try { createImageFile() } catch (ex: IOException) {
            ex.printStackTrace()
            Toast.makeText(this, "Gagal membuat file untuk kamera!", Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.let {
            val photoURI: Uri = FileProvider.getUriForFile(this, "com.azhar.absensi.provider", it)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(cameraIntent, REQ_CAMERA)
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(storageDir, "IMG_$timeStamp.jpg").apply { strFilePath = absolutePath }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val bitmapRatio = width.toFloat() / height.toFloat()

        val newWidth: Int
        val newHeight: Int
        if (bitmapRatio > 1) {
            newWidth = maxWidth
            newHeight = (maxWidth / bitmapRatio).toInt()
        } else {
            newHeight = maxHeight
            newWidth = (maxHeight * bitmapRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun compressImageAndConvertToBase64(filePath: String): String {
        val originalBitmap = BitmapFactory.decodeFile(filePath)
        val resizedBitmap = resizeBitmap(originalBitmap, 800, 800)
        val compressedBitmap = compressBitmap(resizedBitmap)
        return bitmapToBase64(compressedBitmap)
    }

    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val outputStream = ByteArrayOutputStream()
        val maxSize = 1024 * 1024 // 1MB
        var quality = 100
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        while (outputStream.size() > maxSize && quality > 10) {
            outputStream.reset()
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }

        val compressedBytes = outputStream.toByteArray()
        return BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_CAMERA && resultCode == RESULT_OK) {
            strBase64Photo = compressImageAndConvertToBase64(strFilePath)
            Glide.with(this)
                .load(strFilePath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.imageSelfie)

        }
    }
}
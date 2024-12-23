package com.azhar.absensi.view.history

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azhar.absensi.R
import com.azhar.absensi.databinding.ListHistoryAbsenBinding
import com.azhar.absensi.model.ModelFirestore

class HistoryAdapter(
    private val mContext: Context,
    private var modelFirestore: MutableList<ModelFirestore>,
    private val mAdapterCallback: HistoryAdapterCallback
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    // Method untuk memperbarui data yang ada pada adapter
    fun setDataAdapter(items: List<ModelFirestore>) {
        modelFirestore.clear()
        modelFirestore.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListHistoryAbsenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = modelFirestore[position]
        with(holder.binding) {
            // Menampilkan data teks
            tvNomor.text = data.id // id digunakan sebagai nomor
            tvNama.text = data.nama
            tvLokasi.text = data.lokasi
            tvAbsenTime.text = data.tanggal
            tvStatusAbsen.text = data.keterangan

            // Menampilkan gambar dari Base64
            val bitmap = decodeBase64ToBitmap(data.foto) // Properti 'foto' diasumsikan Base64 string
            if (bitmap != null) {
                imageProfile.setImageBitmap(bitmap)
            } else {
                imageProfile.setImageResource(R.drawable.ic_photo_camera) // Gambar default jika Base64 null
            }

            // Mengubah warna status berdasarkan keterangan
            when (data.keterangan) {
                "Absen Masuk" -> {
                    colorStatus.setBackgroundResource(R.drawable.bg_circle_radius)
                    colorStatus.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                }
                "Absen Keluar" -> {
                    colorStatus.setBackgroundResource(R.drawable.bg_circle_radius)
                    colorStatus.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }
                "Izin" -> {
                    colorStatus.setBackgroundResource(R.drawable.bg_circle_radius)
                    colorStatus.backgroundTintList = ColorStateList.valueOf(Color.BLUE)
                }
                else -> {
                    colorStatus.setBackgroundResource(R.drawable.bg_circle_radius)
                    colorStatus.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
                }
            }

            // Aksi hapus data jika CardView diklik
            cvHistory.setOnClickListener {
                mAdapterCallback.onDelete(data)
            }
        }
    }

    override fun getItemCount(): Int {
        return modelFirestore.size
    }

    // Fungsi untuk mendekode Base64 menjadi Bitmap
    private fun decodeBase64ToBitmap(base64String: String?): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    // ViewHolder yang berisi binding untuk item layout
    inner class ViewHolder(val binding: ListHistoryAbsenBinding) : RecyclerView.ViewHolder(binding.root)

    // Callback interface untuk aksi pada item
    interface HistoryAdapterCallback {
        fun onDelete(modelFirestore: ModelFirestore?)
    }
}
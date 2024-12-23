package com.azhar.absensi.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azhar.absensi.databinding.ItemAbsensiBinding
import com.azhar.absensi.model.Absensi
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.azhar.absensi.R

class AbsenAdapter : RecyclerView.Adapter<AbsenAdapter.AbsenViewHolder>() {

    private var absenList: List<Absensi> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsenViewHolder {
        val binding = ItemAbsensiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AbsenViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AbsenViewHolder, position: Int) {
        val absen = absenList[position]
        holder.bind(absen)
    }

    override fun getItemCount(): Int = absenList.size

    fun submitList(list: List<Absensi>) {
        absenList = list
        notifyDataSetChanged()
    }

    inner class AbsenViewHolder(private val binding: ItemAbsensiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(absen: Absensi) {
            binding.tvNama.text = absen.nama
            binding.tvAbsenTime.text = absen.tanggal
            binding.tvLokasi.text = absen.lokasi
            binding.tvStatusAbsen.text = absen.keterangan

            // Menangani foto baik berupa URL atau Base64
            if (absen.foto.startsWith("http")) {
                // Jika foto adalah URL
                Glide.with(binding.root.context)
                    .load(absen.foto)
                    .into(binding.imageProfile)
            } else {
                // Jika foto adalah Base64
                val bitmap = decodeBase64ToBitmap(absen.foto)
                if (bitmap != null) {
                    binding.imageProfile.setImageBitmap(bitmap)
                } else {
                    binding.imageProfile.setImageResource(R.drawable.ic_photo_camera) // Placeholder jika gagal
                }
            }

            // Menampilkan timestamp jika perlu
            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            binding.tvNomor.text = dateFormat.format(Date(absen.timestamp))
        }

        private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
            return try {
                val decodedBytes = Base64.decode(
                    base64Str.substring(base64Str.indexOf(",") + 1), // Hilangkan header Base64
                    Base64.DEFAULT
                )
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

package com.example.entrega02.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.entrega02.R
import com.example.entrega02.data.InfoUser
import de.hdodenhof.circleimageview.CircleImageView

class ExperienciasAdapter(
    private val experiencias: List<InfoUser>
) : RecyclerView.Adapter<ExperienciasAdapter.ProfileViewHolder>() {

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profile_image)
        private val lugar: TextView = itemView.findViewById(R.id.lugar)
        private val descripcion: TextView = itemView.findViewById(R.id.descripcion)
        private val fecha: TextView = itemView.findViewById(R.id.fecha)

        fun bind(avistamiento: InfoUser) {
            profileImage.setImageResource(avistamiento.image)
            lugar.text = avistamiento.tipoAve
            descripcion.text = avistamiento.descripcion
            fecha.text = avistamiento.fecha
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.experiencias_adapter, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = experiencias[position]
        holder.bind(profile) // Llamar a la función bind para configurar el elemento
    }

    override fun getItemCount(): Int {
        return experiencias.size
    }
}

package com.example.entrega02.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.entrega02.data.InfoUser
import com.example.entrega02.R
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Locale

class SearchAdapter(
    private var infouser: List<InfoUser>,
    private val onClick: (InfoUser) -> Unit
) : RecyclerView.Adapter<SearchAdapter.ProfileViewHolder>() {

    private var filteredAvistamientos = infouser

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage1: CircleImageView = itemView.findViewById(R.id.profile_image1)
        private val titulo1: TextView = itemView.findViewById(R.id.titulo1)
        private val descripcion1: TextView = itemView.findViewById(R.id.descripcion1)
        private val fecha1: TextView = itemView.findViewById(R.id.fecha1)

        fun bind(infouser: InfoUser) {
            profileImage1.setImageResource(infouser.image)
            titulo1.text = infouser.tipoAve
            descripcion1.text = infouser.descripcion
            fecha1.text = infouser.fecha

            itemView.setOnClickListener { onClick(infouser) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_adapter, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(filteredAvistamientos[position])
    }

    override fun getItemCount(): Int {
        return filteredAvistamientos.size
    }

    fun filter(text: String) {
        if (text.isEmpty()) {
            filteredAvistamientos = infouser
        } else {
            filteredAvistamientos = infouser.filter {
                it.tipoAve.toLowerCase(Locale.getDefault()).contains(text.toLowerCase(Locale.getDefault()))
            }
        }
        notifyDataSetChanged()
    }
}

package com.example.entrega02.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.entrega02.R
import com.example.entrega02.activities.Reviews
import com.example.entrega02.data.TouristicPlace
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso

class PropietaryScreenTouristicPlaceAdapter(private var originalCardList: List<TouristicPlace>) :
    RecyclerView.Adapter<PropietaryScreenTouristicPlaceAdapter.CardViewHolder>(), Filterable {

    private var cardList: List<TouristicPlace> = originalCardList.toList()

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.PropietaryPlaceImage)
        val textView: TextView = itemView.findViewById(R.id.PropietaryPlaceName)
        val floatingButton1: FloatingActionButton = itemView.findViewById(R.id.edit)
        val floatingButton2: FloatingActionButton = itemView.findViewById(R.id.viewReviews)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.propietary_screen, parent, false)

        return CardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val currentItem = cardList[position]

        Picasso.get().load(currentItem.picture).into(holder.imageView)
        holder.textView.text = currentItem.name

        // Set the click listener for the floating button
        holder.floatingButton1.setOnClickListener {
            //too soon
        }
        holder.floatingButton2.setOnClickListener {
            val intent = Intent(holder.imageView.context, Reviews::class.java)
            intent.putExtra("object", currentItem)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return cardList.size
    }
    fun updateData(newTouristicPlaces: List<TouristicPlace>) {
        originalCardList = newTouristicPlaces
        cardList = newTouristicPlaces.toList()
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = mutableListOf<TouristicPlace>()

                if (constraint == null || constraint.isEmpty()) {
                    filteredList.addAll(originalCardList)
                } else {
                    val filterPattern = constraint.toString().toLowerCase().trim()

                    for (card in originalCardList) {
                        if (card.name.toLowerCase().contains(filterPattern)) {
                            filteredList.add(card)
                        }
                    }
                }

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                cardList = results?.values as List<TouristicPlace>
                notifyDataSetChanged()
            }
        }
    }
}

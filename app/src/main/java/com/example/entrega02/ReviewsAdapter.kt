package com.example.entrega02
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.RatingBar
import androidx.recyclerview.widget.RecyclerView
import com.example.entrega02.R

class ReviewsAdapter(private val reviews: List<Float>) : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_reviews, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ratingBar: RatingBar = itemView.findViewById(R.id.rating_bar)
        private val reviewContent: TextView = itemView.findViewById(R.id.text_review_content)

        fun bind(score: Float) {
            ratingBar.rating = score
            reviewContent.text = "Score: $score"
        }
    }
}

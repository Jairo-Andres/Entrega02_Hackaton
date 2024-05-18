package com.example.entrega02.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.entrega02.R
import com.example.entrega02.adapters.ReviewsAdapter
import com.example.entrega02.data.Review
import com.example.entrega02.data.TouristicPlace
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class Reviews : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var reviewsAdapter: ReviewsAdapter
    private var reviewListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.review_recycle_view)

        db = FirebaseFirestore.getInstance()

        val myIntent = intent
        val touristicPlace = myIntent.getSerializableExtra("object") as TouristicPlace
        val email = myIntent.getStringExtra("email") ?: ""
        val reviews = touristicPlace.reviews
        val submitReviewButton = findViewById<Button>(R.id.review_submit_button)
        val rating = findViewById<RatingBar>(R.id.review_rating)
        val reviewText = findViewById<TextView>(R.id.review_input)

        val recyclerView: RecyclerView = findViewById(R.id.ReviewsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        reviewsAdapter = ReviewsAdapter(reviews)
        recyclerView.adapter = reviewsAdapter

        submitReviewButton.setOnClickListener {
            val newRating = rating.rating
            val newReviewText = reviewText.text.toString()
            val userReview = hashMapOf(
                "email" to email,
                "placeID" to touristicPlace.ID,
                "score" to newRating,
                "review" to newReviewText
            )

            val reviewsRef = db.collection("placeReviews")
            reviewsRef.whereEqualTo("email", email).whereEqualTo("placeID", touristicPlace.ID).get().addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No existing review, add a new one
                    reviewsRef.add(userReview)
                        .addOnSuccessListener {
                            // Review added successfully
                            Toast.makeText(this,
                                getString(R.string.resena_agregada), Toast.LENGTH_SHORT).show()
                            rating.rating = 0f
                            reviewText.text = ""
                        }
                        .addOnFailureListener { e ->
                            // Error adding review
                            Toast.makeText(this, "Error agregando tu reseña: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    // Update existing review
                    for (document in documents) {
                        reviewsRef.document(document.id).update(userReview as Map<String, Any>)
                            .addOnSuccessListener {
                                // Review updated successfully
                                Toast.makeText(this,
                                    getString(R.string.resena_actualizada), Toast.LENGTH_SHORT).show()
                                rating.rating = 0f
                                reviewText.text = ""
                            }
                            .addOnFailureListener { e ->
                                // Error updating review
                                Toast.makeText(this, "Error actualizando tu reseña: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                }
            }
                .addOnFailureListener { e ->
                    // Error querying for existing review
                    Toast.makeText(this, "Failed to query reviews: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onStart() {
        super.onStart()
        val touristicPlace = intent.getSerializableExtra("object") as TouristicPlace

        val reviewsRef = db.collection("placeReviews").whereEqualTo("placeID", touristicPlace.ID)
        reviewListener = reviewsRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Toast.makeText(this, "Listen failed: ${e.message}", Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val newReviews = mutableListOf<Review>()
                for (document in snapshots) {
                    val email = document.getString("email") ?: ""
                    val score = document.getDouble("score")?.toFloat() ?: 0f
                    val reviewText = document.getString("review") ?: ""
                    newReviews.add(Review(email, score, reviewText))
                }
                reviewsAdapter.updateReviews(newReviews)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        reviewListener?.remove()
    }
}

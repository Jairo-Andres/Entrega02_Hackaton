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

class PropReviews : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var reviewsAdapter: ReviewsAdapter
    private var reviewListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prop_review_recycle_view)

        db = FirebaseFirestore.getInstance()

        val myIntent = intent
        val touristicPlace = myIntent.getSerializableExtra("object") as TouristicPlace
        val reviews = touristicPlace.reviews

        val recyclerView: RecyclerView = findViewById(R.id.ReviewsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        reviewsAdapter = ReviewsAdapter(reviews)
        recyclerView.adapter = reviewsAdapter

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

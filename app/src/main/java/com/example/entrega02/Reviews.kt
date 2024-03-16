package com.example.entrega02

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.entrega02.R

class Reviews : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.review_recycle_view)

        val myIntent = intent
        val touristicPlace = myIntent.getSerializableExtra("object") as TouristicPlace
        val scores = touristicPlace.scores // Assuming scores is the ArrayList<Float> in your TouristicPlace object

        val recyclerView: RecyclerView = findViewById(R.id.ReviewsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ReviewsAdapter(scores)
    }
}

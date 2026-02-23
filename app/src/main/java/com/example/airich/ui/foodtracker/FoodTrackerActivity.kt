package com.example.airich.ui.foodtracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.airich.R

class FoodTrackerActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_tracker)
        
        if (savedInstanceState == null) {
            showFoodLogFragment()
        }
    }
    
    fun showFoodLogFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, FoodLogFragment())
            setReorderingAllowed(true)
        }
    }
    
    fun showAddFoodFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, AddFoodFragment())
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }
}


package com.example.airich.ui.healthytasktracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.airich.R

class HealthyTaskTrackerActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_healthy_task_tracker)
        
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragmentContainer, HealthyTaskLogFragment())
                setReorderingAllowed(true)
            }
        }
    }
}


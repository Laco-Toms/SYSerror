package com.example.healthaiassistant
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_activity)

        val resultTextView = findViewById<TextView>(R.id.resultTextView)
        val backButton = findViewById<Button>(R.id.backButton)

        // Získanie výsledku analýzy odoslaného z MainActivity
        val analysisResult = intent.getStringExtra("ANALYSIS_RESULT")

        // Zobrazenie výsledku v TextView
        resultTextView.text = analysisResult

        backButton.setOnClickListener {
            finish() // Ukončí aktuálnu Activity a vráti sa na predchádzajúcu
        }
    }
}
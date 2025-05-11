package com.example.healthaiassistant

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SplashScreenActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({

            val i = Intent(this, MainActivity::class.java)
            startActivity(i)


            finish()
        }, SPLASH_TIME_OUT)
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var analyzeButton: Button
    private lateinit var editTextFeeling: EditText
    private lateinit var editTextSymptoms: EditText
    private lateinit var editTextMedications: EditText
    private lateinit var editTextAge: EditText
    private lateinit var radioGroupGender: RadioGroup

    private val database = Firebase.database
    var myRef: DatabaseReference = database.getReference("health_data")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        radioGroupGender = findViewById(R.id.radioGroupGender)
        editTextAge = findViewById(R.id.editTextAge)
        editTextMedications = findViewById(R.id.editTextMedications)
        editTextSymptoms = findViewById(R.id.editTextSymptoms)
        editTextFeeling = findViewById(R.id.editTextFeeling)
        analyzeButton = findViewById(R.id.analyzeButton)

        analyzeButton.setOnClickListener { view ->
            val selectedGenderId = radioGroupGender.checkedRadioButtonId
            val age = editTextAge.text.toString().trim()
            val medications = editTextMedications.text.toString().trim()
            val symptoms = editTextSymptoms.text.toString().trim()
            val feeling = editTextFeeling.text.toString().trim()

            if (selectedGenderId == -1) {
                Toast.makeText(this@MainActivity, "Prosím, vyberte pohlavie.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (age.isEmpty()) {
                Toast.makeText(this@MainActivity, "Prosím, zadajte vek.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (symptoms.isEmpty()) {
                Toast.makeText(this@MainActivity, "Prosím, zadajte príznaky.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (feeling.isEmpty()) {
                Toast.makeText(this@MainActivity, "Prosím, zadajte váš pocit.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val selectedGender = when (selectedGenderId) {
                R.id.radioMale -> "Male"
                R.id.radioFemale -> "Female"
                else -> "Not selected"
            }

            val userData = hashMapOf(
                "gender" to selectedGender,
                "age" to age,
                "medications" to medications,
                "symptoms" to symptoms,
                "feeling" to feeling
            )

            myRef.child(System.currentTimeMillis().toString()).setValue(userData)
                .addOnSuccessListener {
                    Log.d("Firebase", "Data saved successfully!")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Error saving data: ${e.message}")
                }

            val result = analyzeData(selectedGender, age, medications, symptoms, feeling)

            val intent = Intent(this@MainActivity, ResultActivity::class.java)
            intent.putExtra("ANALYSIS_RESULT", result)
            startActivity(intent)
        }
        // KONIEC ONCLICKLISTENER BLOKU
    }

    private fun analyzeData(gender: String, age: String, medications: String, symptoms: String, feeling: String): String {
        var result = "Based on your input:\n"
        result += "Gender: $gender\n"
        result += "Age: $age\n"
        result += "Medications: $medications\n"
        result += "Symptoms: $symptoms\n"
        result += "Feeling: $feeling\n\n"

        result += if (symptoms.contains("pain", ignoreCase = true) && feeling.contains("bad", ignoreCase = true)) {
            "It is recommended to consult a doctor if symptoms persist or worsen."
        } else {
            "Monitor your symptoms. If they don't improve, consider consulting a doctor."
        }
        return result
    }
}
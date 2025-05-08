// MainActivity.kt (Kotlin)
package com.example.zdravotnasistent

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var ageEditText: EditText
    private lateinit var medicationsEditText: EditText
    private lateinit var symptomsEditText: EditText
    private lateinit var evaluateButton: Button
    private lateinit var emergencyButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private val SMS_PERMISSION_REQUEST_CODE = 456

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        genderRadioGroup = findViewById(R.id.genderRadioGroup)
        ageEditText = findViewById(R.id.ageEditText)
        medicationsEditText = findViewById(R.id.medicationsEditText)
        symptomsEditText = findViewById(R.id.symptomsEditText)
        evaluateButton = findViewById(R.id.evaluateButton)
        emergencyButton = findViewById(R.id.emergencyButton)
        resultTextView = findViewById(R.id.resultTextView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        evaluateButton.setOnClickListener {
            val gender = when (genderRadioGroup.checkedRadioButtonId) {
                R.id.maleRadioButton -> "muž"
                R.id.femaleRadioButton -> "žena"
                R.id.otherRadioButton -> "iné"
                else -> ""
            }
            val age = ageEditText.text.toString()
            val medications = medicationsEditText.text.toString()
            val symptoms = symptomsEditText.text.toString()

            val evaluationResult = evaluateSymptoms(gender, age, medications, symptoms)
            resultTextView.text = evaluationResult
        }

        emergencyButton.setOnClickListener {
            sendEmergencySignal()
        }
    }

    private fun evaluateSymptoms(gender: String, age: String, medications: String, symptoms: String): String {
        // Tu implementuj jednoduchú logiku vyhodnotenia na základe zadaných údajov.
        // Toto je len základný príklad, mal by si pridať oveľa viac pravidiel.

        if (symptoms.contains("silná bolesť na hrudi") && symptoms.contains("dýchavičnosť")) {
            return "Je to potenciálne závažné! Okamžite vyhľadajte lekársku pomoc."
        } else if (symptoms.contains("vysoká horúčka") && symptoms.contains("stuhnutie šije")) {
            return "Je to potenciálne závažné! Okamžite vyhľadajte lekársku pomoc."
        } else if (symptoms.contains("mierna nádcha") && symptoms.contains("kašeľ") && !symptoms.contains("horúčka")) {
            return "Pravdepodobne nie je to závažné. Odpočívajte, pite veľa tekutín a sledujte svoj stav."
        } else if (age.isNotEmpty() && age.toInt() > 70 && symptoms.contains("náhla slabosť")) {
            return "Odporúča sa konzultovať lekára pre istotu."
        } else {
            return "Na základe zadaných symptómov nie je možné jednoznačne určiť závažnosť. Sledujte svoj stav a ak sa zhorší, vyhľadajte lekára."
        }
    }

    private fun sendEmergencySignal() {
        // Skontroluj povolenia pre prístup k polohe a SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                val aiMessage = evaluateSymptoms(
                    when (genderRadioGroup.checkedRadioButtonId) {
                        R.id.maleRadioButton -> "muž"
                        R.id.femaleRadioButton -> "žena"
                        R.id.otherRadioButton -> "iné"
                        else -> ""
                    },
                    ageEditText.text.toString(),
                    medicationsEditText.text.toString(),
                    symptomsEditText.text.toString()
                )
                val messageToSend = "NÚDZA! Používateľ potrebuje pomoc. Poloha: zemepisná šírka=$latitude, zemepisná dĺžka=$longitude. Správa od AI: $aiMessage"
                sendSMS("číslo_záchrannej_služby", messageToSend) // Nahraďte skutočným číslom
            } else {
                resultTextView.text = "Nepodarilo sa získať polohu."
                // Môžete pridať záložnú možnosť, napríklad len odoslať SMS bez polohy
                val aiMessage = evaluateSymptoms(
                    when (genderRadioGroup.checkedRadioButtonId) {
                        R.id.maleRadioButton -> "muž"
                        R.id.femaleRadioButton -> "žena"
                        R.id.otherRadioButton -> "iné"
                        else -> ""
                    },
                    ageEditText.text.toString(),
                    medicationsEditText.text.toString(),
                    symptomsEditText.text.toString()
                )
                sendSMS("číslo_záchrannej_služby", "NÚDZA! Používateľ potrebuje pomoc. Správa od AI: $aiMessage (nepodarilo sa získať polohu)")
            }
        }
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            resultTextView.text = "Núdzová správa bola odoslaná."
        } catch (e: Exception) {
            resultTextView.text = "Nepodarilo sa odoslať núdzovú správu: ${e.message}"
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                sendEmergencySignal()
            } else {
                resultTextView.text = "Povolenie na prístup k polohe a odosielanie SMS nebolo udelené."
            }
        }
    }
}
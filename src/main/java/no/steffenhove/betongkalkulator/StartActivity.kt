package no.steffenhove.betongkalkulator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

// Importer klassene fra samme pakke:

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val buttonCalculation = findViewById<Button>(R.id.button_start_calculation)
        val buttonHistory = findViewById<Button>(R.id.button_start_history)
        val buttonSettings = findViewById<Button>(R.id.button_start_settings)

        buttonCalculation.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        buttonHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
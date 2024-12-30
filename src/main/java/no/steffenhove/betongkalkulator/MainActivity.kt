package no.steffenhove.betongkalkulator

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.preference.PreferenceManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var defaultPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        // Referanser til knapper for formvalg
        val buttonKjerne = findViewById<Button>(R.id.button_kjerne)
        val buttonFirkant = findViewById<Button>(R.id.button_firkant)
        val buttonTrekant = findViewById<Button>(R.id.button_trekant)

        // Referanser til layout-containere for inputfelt
        val layoutKjerne = findViewById<LinearLayout>(R.id.layout_kjerne)
        val layoutFirkant = findViewById<LinearLayout>(R.id.layout_firkant)
        val layoutTrekant = findViewById<LinearLayout>(R.id.layout_trekant)

        // Referanse til inputfelt for Kjerne
        val inputDiameter = findViewById<EditText>(R.id.input_diameter)
        val inputHeight = findViewById<EditText>(R.id.input_height)
        val spinnerDiameterUnit = findViewById<Spinner>(R.id.spinner_diameter_unit)
        val spinnerHeightUnit = findViewById<Spinner>(R.id.spinner_height_unit)

        // Referanser til inputfelt for Firkant
        val inputLength = findViewById<EditText>(R.id.input_length)
        val inputWidth = findViewById<EditText>(R.id.input_width)
        val inputThickness = findViewById<EditText>(R.id.input_thickness)
        val spinnerLengthUnit = findViewById<Spinner>(R.id.spinner_length_unit)
        val spinnerWidthUnit = findViewById<Spinner>(R.id.spinner_width_unit)
        val spinnerThicknessUnit = findViewById<Spinner>(R.id.spinner_thickness_unit)

        // Referanser til inputfelt for Trekant
        val inputSideA = findViewById<EditText>(R.id.input_side_a)
        val inputSideB = findViewById<EditText>(R.id.input_side_b)
        val inputSideC = findViewById<EditText>(R.id.input_side_c)
        val inputThicknessTriangle = findViewById<EditText>(R.id.input_thickness_triangle)
        val spinnerSideAUnit = findViewById<Spinner>(R.id.spinner_side_a_unit)
        val spinnerSideBUnit = findViewById<Spinner>(R.id.spinner_side_b_unit)
        val spinnerSideCUnit = findViewById<Spinner>(R.id.spinner_side_c_unit)
        val spinnerThicknessTriangleUnit = findViewById<Spinner>(R.id.spinner_thickness_triangle_unit)

        // Referanse til beregn-knappen
        val buttonCalculate = findViewById<Button>(R.id.button_calculate)

        // Referanse til resultat-TextView
        val textResult = findViewById<TextView>(R.id.text_result)

        // Sett opp OnClickListener for hver formvalg-knapp
        buttonKjerne.setOnClickListener {
            layoutKjerne.visibility = View.VISIBLE
            layoutFirkant.visibility = View.GONE
            layoutTrekant.visibility = View.GONE
        }

        buttonFirkant.setOnClickListener {
            layoutKjerne.visibility = View.GONE
            layoutFirkant.visibility = View.VISIBLE
            layoutTrekant.visibility = View.GONE
        }

        buttonTrekant.setOnClickListener {
            layoutKjerne.visibility = View.GONE
            layoutFirkant.visibility = View.GONE
            layoutTrekant.visibility = View.VISIBLE
        }

        buttonCalculate.setOnClickListener {
            if (layoutKjerne.visibility == View.VISIBLE) {
                // Kjerne-beregning
                val diameter = inputDiameter.text.toString().toDoubleOrNull()
                val height = inputHeight.text.toString().toDoubleOrNull()

                if (diameter == null || height == null) {
                    Toast.makeText(this, "Ugyldig inndata for Kjerne", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val diameterUnit = spinnerDiameterUnit.selectedItem.toString()
                val heightUnit = spinnerHeightUnit.selectedItem.toString()

                val diameterInMeters = convertToMeters(diameter, diameterUnit)
                val heightInMeters = convertToMeters(height, heightUnit)

                val volume = calculateCylinderVolume(diameterInMeters, heightInMeters)
                val weight = calculateWeight(volume)

                val resultText = String.format(Locale.ROOT, "Volum: %.2f m³\nVekt: %.0f kg", volume, weight)
                textResult.text = resultText
                if (weight >= 1000) {
                    val weightInTons = weight / 1000
                    textResult.append(String.format(Locale.ROOT, " (%.1f tonn)", weightInTons))
                }

                // Lagre beregningen til historikk
                val dimensions = String.format("Diameter: %.0f %s, Høyde: %.0f %s", diameter, diameterUnit, height, heightUnit)
                saveCalculationToHistory(volume, weight, "Kjerne", dimensions)

            } else if (layoutFirkant.visibility == View.VISIBLE) {
                // Firkant-beregning
                val length = inputLength.text.toString().toDoubleOrNull()
                val width = inputWidth.text.toString().toDoubleOrNull()
                val thickness = inputThickness.text.toString().toDoubleOrNull()

                if (length == null || width == null || thickness == null) {
                    Toast.makeText(this, "Ugyldig inndata for Firkant", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val lengthUnit = spinnerLengthUnit.selectedItem.toString()
                val widthUnit = spinnerWidthUnit.selectedItem.toString()
                val thicknessUnit = spinnerThicknessUnit.selectedItem.toString()

                val lengthInMeters = convertToMeters(length, lengthUnit)
                val widthInMeters = convertToMeters(width, widthUnit)
                val thicknessInMeters = convertToMeters(thickness, thicknessUnit)

                val volume = calculateBoxVolume(lengthInMeters, widthInMeters, thicknessInMeters)
                val weight = calculateWeight(volume)

                val resultText = String.format(Locale.ROOT, "Volum: %.2f m³\nVekt: %.0f kg", volume, weight)
                textResult.text = resultText
                if (weight >= 1000) {
                    val weightInTons = weight / 1000
                    textResult.append(String.format(Locale.ROOT, " (%.1f tonn)", weightInTons))
                }

                // Lagre beregningen til historikk
                val dimensions = String.format("Lengde: %.0f %s, Bredde: %.0f %s, Tykkelse: %.0f %s", length, lengthUnit, width, widthUnit, thickness, thicknessUnit)
                saveCalculationToHistory(volume, weight, "Firkant", dimensions)

            } else if (layoutTrekant.visibility == View.VISIBLE) {
                // Trekant-beregning
                val sideA = inputSideA.text.toString().toDoubleOrNull()
                val sideB = inputSideB.text.toString().toDoubleOrNull()
                val sideC = inputSideC.text.toString().toDoubleOrNull()
                val thickness = inputThicknessTriangle.text.toString().toDoubleOrNull()

                if (sideA == null || sideB == null || sideC == null || thickness == null) {
                    Toast.makeText(this, "Ugyldig inndata for Trekant", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val sideAUnit = spinnerSideAUnit.selectedItem.toString()
                val sideBUnit = spinnerSideBUnit.selectedItem.toString()
                val sideCUnit = spinnerSideCUnit.selectedItem.toString()
                val thicknessUnit = spinnerThicknessTriangleUnit.selectedItem.toString()

                val sideAInMeters = convertToMeters(sideA, sideAUnit)
                val sideBInMeters = convertToMeters(sideB, sideBUnit)
                val sideCInMeters = convertToMeters(sideC, sideCUnit)
                val thicknessInMeters = convertToMeters(thickness, thicknessUnit)

                val volume = calculateTriangleVolume(sideAInMeters, sideBInMeters, sideCInMeters, thicknessInMeters)
                val weight = calculateWeight(volume)

                val resultText = String.format(Locale.ROOT, "Volum: %.2f m³\nVekt: %.0f kg", volume, weight)
                textResult.text = resultText
                if (weight >= 1000) {
                    val weightInTons = weight / 1000
                    textResult.append(String.format(Locale.ROOT, " (%.1f tonn)", weightInTons))
                }

                // Lagre beregningen til historikk
                val dimensions = String.format("Side A: %.0f %s, Side B: %.0f %s, Side C: %.0f %s, Tykkelse: %.0f %s", sideA, sideAUnit, sideB, sideBUnit, sideC, sideCUnit, thickness, thicknessUnit)
                saveCalculationToHistory(volume, weight, "Trekant", dimensions)
            }
        }
    }

    // Funksjon for å konvertere mål til meter
    private fun convertToMeters(value: Double, unit: String): Double {
        val unitSystem = defaultPrefs.getString("unit_preference", "Metrisk") // Hent som string
        if (unitSystem == "Imperial") {
            // Konverter fra tommer til meter
            return value * 0.0254
        } else {
            // Konverter fra mm, cm, eller m til meter
            return when (unit) {
                "mm" -> value / 1000
                "cm" -> value / 100
                "m" -> value
                else -> throw IllegalArgumentException("Ukjent enhet: $unit")
            }
        }
    }

    // Funksjon for å beregne volum av en sylinder (kjerne)
    private fun calculateCylinderVolume(diameter: Double, height: Double): Double {
        val radius = diameter / 2
        return Math.PI * radius * radius * height
    }

    // Funksjon for å beregne volum av en firkantet boks
    private fun calculateBoxVolume(length: Double, width: Double, thickness: Double): Double {
        return length * width * thickness
    }

    // Funksjon for å beregne volum av en trekant
    private fun calculateTriangleVolume(sideA: Double, sideB: Double, sideC: Double, thickness: Double): Double {
        // Bruk Herons formel med en modifikasjon for å håndtere ugyldige trekanter
        val s = (sideA + sideB + sideC) / 2.0
        val sMinusA = s - sideA
        val sMinusB = s - sideB
        val sMinusC = s - sideC

        // Sjekk om trekanten er ugyldig
        if (sMinusA <= 0 || sMinusB <= 0 || sMinusC <= 0) {
            // Håndter ugyldig trekant, f.eks. ved å returnere et estimat basert på gjennomsnittlig side
            val averageSide = (sideA + sideB + sideC) / 3.0
            return 0.5 * averageSide * averageSide * thickness // Forenklet arealberegning
        }

        // Fortsett med Herons formel for gyldige trekanter
        val area = kotlin.math.sqrt(s * sMinusA * sMinusB * sMinusC)
        return area * thickness
    }
    // Funksjon for å beregne vekt basert på volum og tetthet
    private fun calculateWeight(volume: Double): Double {
        val density = when (prefs.getString("density_preference", "Betong")) {
            "Lettbetong" -> 1800.0
            "Egendefinert" -> prefs.getString("custom_density", "2400.0")?.toDoubleOrNull() ?: 2400.0
            else -> 2400.0 // Standard for betong
        }
        return volume * density
    }

    private fun saveCalculationToHistory(volume: Double, weight: Double, shape: String, dimensions: String) {
        val prefs = getSharedPreferences("history", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val currentHistory = prefs.getString("calculations", "[]") // Endret til å hente en JSON-array som en streng
        val jsonArray = try {
            JSONArray(currentHistory)
        } catch (e: Exception) {
            JSONArray()
        }

        // Lag et JSON objekt for den nye beregningen
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedDateTime = currentDateTime.format(formatter)
        val newCalculation = JSONObject().apply {
            put("volume", String.format(Locale.ROOT, "%.2f", volume))
            put("weight", String.format(Locale.ROOT, "%.0f", weight))
            put("shape", shape)
            put("dimensions", dimensions)
            put("datetime", formattedDateTime)
        }

        // Legg til det nye JSON objektet til JSONArray
        jsonArray.put(newCalculation)

        // Begrens historikken til 20 beregninger
        while (jsonArray.length() > 20) {
            jsonArray.remove(0) // Fjern det eldste elementet
        }

        // Lagre den oppdaterte historikken
        editor.putString("calculations", jsonArray.toString())
        editor.apply()

        Log.d("History", "Saved calculation: $newCalculation")
    }
}
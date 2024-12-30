package no.steffenhove.betongkalkulator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat

class HistoryActivity : AppCompatActivity() {

    private lateinit var historyListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var history: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        historyListView = findViewById<ListView>(R.id.historyListView)
        history = getHistory().toMutableList()

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, history)
        historyListView.adapter = adapter
        historyListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL

        historyListView.setMultiChoiceModeListener(object : AbsListView.MultiChoiceModeListener {
            override fun onItemCheckedStateChanged(mode: ActionMode?, position: Int, id: Long, checked: Boolean) {
                // Oppdater tittelen på contextual action bar med antall valgte elementer
                mode?.title = "${historyListView.checkedItemCount} valgt"
            }

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                // Inflater menyen for contextual action bar
                menuInflater.inflate(R.menu.history_context_menu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false // Return false hvis ingenting er gjort
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                // Håndterer klikk på menyvalg
                return when (item?.itemId) {
                    R.id.action_delete -> {
                        deleteSelectedItems()
                        mode?.finish() // Avslutt contextual action bar
                        true
                    }
                    R.id.action_sum -> {
                        sumSelectedItems()
                        mode?.finish()
                        true
                    }
                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                // Her kan du gjøre nødvendige oppdateringer når contextual action bar avsluttes
            }
        })

        val buttonClearHistory = findViewById<Button>(R.id.button_clear_history)
        buttonClearHistory.setOnClickListener {
            clearHistory()
            // Oppdater listen etter sletting
            historyListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, getHistory())
        }
    }

    private fun getHistory(): List<String> {
        val prefs = getSharedPreferences("history", MODE_PRIVATE)
        val calculationsString = prefs.getString("calculations", "[]") // Henter som JSON-streng
        val jsonArray = try {
            JSONArray(calculationsString)
        } catch (e: JSONException) {
            Log.e("HistoryActivity", "Error parsing history JSON", e)
            JSONArray()
        }

        val historyList = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val volume = jsonObject.getString("volume")
            val weight = jsonObject.getString("weight")
            val shape = jsonObject.getString("shape")
            val dimensions = jsonObject.getString("dimensions")
            val datetime = jsonObject.optString("datetime", "Ukjent tidspunkt") // Håndterer manglende datetime

            historyList.add("$volume m³, $weight kg, $shape, $dimensions - $datetime")
        }

        return historyList
    }

    private fun clearHistory() {
        val prefs = getSharedPreferences("history", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove("calculations")
        editor.apply()
    }
    private fun deleteSelectedItems() {
        val checkedItemPositions = historyListView.checkedItemPositions
        val historyToRemove = mutableListOf<String>()

        // Finn elementene som skal slettes
        for (i in history.size - 1 downTo 0) {
            if (checkedItemPositions.get(i)) {
                historyToRemove.add(history[i])
            }
        }

        // Fjern elementene fra listen
        history.removeAll(historyToRemove)

        // Lagre den oppdaterte historikken
        saveHistory(history)

        // Oppdater listen i brukergrensesnittet
        adapter.notifyDataSetChanged()
    }

    private fun saveHistory(history: MutableList<String>) {
        val prefs = getSharedPreferences("history", MODE_PRIVATE)
        val editor = prefs.edit()

        // Konverter listen av strenger til en JSONArray
        val jsonArray = JSONArray()
        for (item in history) {
            val jsonObject = convertStringToJSON(item)
            if (jsonObject != null) {
                jsonArray.put(jsonObject)
            }
        }

        // Lagre JSONArray som en streng
        editor.putString("calculations", jsonArray.toString())
        editor.apply()
        Log.d("HistoryActivity", "Historikk lagret: $jsonArray")
    }

    // Hjelpefunksjon for å konvertere historikkstrengen tilbake til et JSON objekt
    private fun convertStringToJSON(historyString: String): JSONObject? {
        val regex = """([\d.]+) m³, ([\d.]+) kg, (\w+), (.*?) - (.*)""".toRegex()
        val matchResult = regex.find(historyString) ?: return null

        return try {
            val (volume, weight, shape, dimensions, datetime) = matchResult.destructured
            JSONObject().apply {
                put("volume", volume)
                put("weight", weight)
                put("shape", shape)
                put("dimensions", dimensions)
                put("datetime", datetime)
            }
        } catch (e: Exception) {
            Log.e("HistoryActivity", "Error converting history string to JSON", e)
            null
        }
    }
    private fun sumSelectedItems() {
        val checkedItemPositions = historyListView.checkedItemPositions
        var totalVolume = 0.0
        var totalWeight = 0.0

        // Gå gjennom alle elementene i listen
        for (i in 0 until history.size) {
            // Sjekk om elementet er valgt
            if (checkedItemPositions.get(i)) {
                val historyString = history[i]
                val regex = """([\d.]+) m³, ([\d.]+) kg,.*""".toRegex()
                val matchResult = regex.find(historyString)

                if (matchResult != null) {
                    try {
                        val (volumeStr, weightStr) = matchResult.destructured
                        totalVolume += volumeStr.toDouble()
                        totalWeight += weightStr.toDouble()
                    } catch (e: NumberFormatException) {
                        Log.e("HistoryActivity", "Error parsing volume or weight for sum", e)
                    }
                }
            }
        }

        // Vis summen i en Toast-melding eller et annet passende UI-element
        if (totalVolume > 0.0 || totalWeight > 0.0) {
            val df = DecimalFormat("#.##") // Format for å vise to desimaler
            Toast.makeText(
                this,
                "Total volum: ${df.format(totalVolume)} m³, Total vekt: ${df.format(totalWeight)} kg",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(this, "Ingen elementer valgt for summering", Toast.LENGTH_SHORT).show()
        }
    }
}
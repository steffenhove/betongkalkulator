package no.steffenhove.betongkalkulator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val unitPreference = findPreference<ListPreference>("unit_preference")
            val densityPreference = findPreference<ListPreference>("density_preference")
            val customDensityPreference = findPreference<EditTextPreference>("custom_density")

            // Oppdater synligheten for egendefinert tetthet basert på valg i densityPreference
            densityPreference?.setOnPreferenceChangeListener { _, newValue ->
                if (newValue == "Egendefinert") {
                    customDensityPreference?.isVisible = true
                    // Hent og sett verdien fra SharedPreferences
                    val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    val customDensity = prefs.getString("custom_density", "")
                    customDensityPreference?.text = customDensity
                } else {
                    customDensityPreference?.isVisible = false
                }
                true
            }

            // Sett initial synlighet for egendefinert tetthet
            val initialDensity = densityPreference?.value
            if (initialDensity == "Egendefinert") {
                customDensityPreference?.isVisible = true
                // Hent og sett verdien fra SharedPreferences
                val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
                val customDensity = prefs.getString("custom_density", "")
                customDensityPreference?.text = customDensity
            } else {
                customDensityPreference?.isVisible = false
            }
        }
    }
}
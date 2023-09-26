package com.rockethat.ornaassistant.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.rockethat.ornaassistant.R

class SettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preference, rootKey)

        val uiColorPreference = findPreference<ListPreference>("ui_color_preference")
        uiColorPreference?.setOnPreferenceChangeListener { preference, newValue ->
            // Handle the selected UI color change here
            // You can save the selected value to SharedPreferences and apply the theme accordingly
            // Example:
            // val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            // sharedPreferences.edit().putString("ui_color", newValue.toString()).apply()

            // Set the summary to the selected color
            val index = uiColorPreference.findIndexOfValue(newValue.toString())
            if (index >= 0) {
                uiColorPreference.summary = uiColorPreference.entries[index]
            } else {
                uiColorPreference.summary = null
            }

            true
        }

        // Initialize the summary for the initial value
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val initialValue = sharedPreferences.getString("ui_color_preference", "default")
        val initialIndex = uiColorPreference?.findIndexOfValue(initialValue)
        if (initialIndex != null && initialIndex >= 0) {
            uiColorPreference?.summary = uiColorPreference.entries[initialIndex]
        }
    }
}

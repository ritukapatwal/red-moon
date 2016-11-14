/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 *
 *  This file is free software: you may copy, redistribute and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This file is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jmstudios.redmoon.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.activity.ShadesActivity
import com.jmstudios.redmoon.preference.FilterTimePreference
import com.jmstudios.redmoon.preference.UseLocationPreference

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator

import java.util.*


class TimeToggleFragment : PreferenceFragment() {

    private val DEBUG = true
    private lateinit var mView: View
    private lateinit var mHelpSnackbar: Snackbar

    // Preferences
    private val automaticFilterPref: SwitchPreference
        get() = (preferenceScreen.findPreference
                (getString(R.string.pref_key_automatic_filter)) as SwitchPreference)

    private val useLocationPref: UseLocationPreference
        get() = (preferenceScreen.findPreference
                (getString(R.string.pref_key_use_location)) as UseLocationPreference)

    private val automaticTurnOnPref: FilterTimePreference
        get() = (preferenceScreen.findPreference
                (getString(R.string.pref_key_custom_start_time)) as FilterTimePreference)

    private val automaticTurnOffPref: FilterTimePreference
        get() = (preferenceScreen.findPreference
                (getString(R.string.pref_key_custom_end_time)) as FilterTimePreference)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.time_toggle_preferences)

        setSwitchBarTitle(automaticFilterPref.isChecked)

        automaticFilterPref.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    onAutomaticFilterPreferenceChange(newValue as Boolean)
                    true
                }

            /* requestLocationPermission() */
            /* updateFilterTimesFromSun() */
            /* locationPref.searchLocation(true); */
            /* locationPref.searchLocation(false) */
            /* int duration = Toast.LENGTH_SHORT; */
            /* Toast toast = Toast.makeText */
            /*     (mContext, mContext.getString */
            /*      (R.string.toast_warning_no_location), duration); */
            /* toast.show(); */

        useLocationPref.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    //TODO: Get location permission 
                    updateLocationPrefs(newValue as Boolean)
                    true
                }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun onAutomaticFilterPreferenceChange(auto: Boolean) {
        setSwitchBarTitle(auto)
        useLocationPref.isEnabled = auto
        updateLocationPrefs(useLocationPref.isChecked)
        if (!auto) {
            automaticTurnOnPref.isEnabled = false
            automaticTurnOffPref.isEnabled = false
        }
    }

    private fun setSwitchBarTitle(on: Boolean) {
        automaticFilterPref.setTitle(
                if (on) R.string.text_switch_on
                else R.string.text_switch_off
        )
    }

    private fun updateLocationPrefs(sun: Boolean) {
        useLocationPref.updateSummary()
        val location = useLocationPref.location
        if (!sun) {
            if (DEBUG) Log.i(TAG, "Location Disabled")
            automaticTurnOnPref.setToCustomTime()
            automaticTurnOffPref.setToCustomTime()
        } else if (location == "not set") {
            if (DEBUG) Log.i(TAG, "Location Not Set")
            automaticTurnOnPref.setToSunTime("19:30")
            automaticTurnOffPref.setToSunTime("06:30")
        } else {
            if (DEBUG) Log.i(TAG, "Location Set")
            val latitude = java.lang.Double.parseDouble(location.split(",")[0])
            val longitude = java.lang.Double.parseDouble(location.split(",")[1])

            val sunriseSunsetLocation = com.luckycatlabs.sunrisesunset.dto.Location(latitude, longitude)
            val calculator = SunriseSunsetCalculator(sunriseSunsetLocation, TimeZone.getDefault())

            val sunsetTime = calculator.getOfficialSunsetForDate(Calendar.getInstance())
            automaticTurnOnPref.setToSunTime(sunsetTime)

            val sunriseTime = calculator.getOfficialSunriseForDate(Calendar.getInstance())
            automaticTurnOffPref.setToSunTime(sunriseTime)
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission
                (activity, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        mView = v
        return v
    }

    /* private fun setPreferencesEnabled(enabled: Boolean) { */
    /*     val root = preferenceScreen */
    /*     for (i in 0..root.preferenceCount - 1) { */
    /*         root.getPreference(i).isEnabled = enabled */
    /*     } */
    /*     val auto = automaticFilterPref.isChecked */
    /*     useLocationPref.isEnabled = auto */
    /*     val sun = true */
    /*     automaticTurnOnPref.isEnabled = auto && !sun */
    /*     automaticTurnOffPref.isEnabled = auto && !sun */

    /* } */

    /* private fun setAllPreferencesEnabled(enabled: Boolean) { */
    /*     val root = preferenceScreen */
    /*     for (i in 0..root.preferenceCount - 1) { */
    /*         root.getPreference(i).isEnabled = enabled */
    /*     } */
    /* } */

    /* private fun showHelpSnackbar() { */
    /*     mHelpSnackbar = Snackbar.make(mView, activity.getString(R.string.help_snackbar_text), */
    /*             Snackbar.LENGTH_INDEFINITE) */

    /*     if (mSettingsModel.darkThemeFlag) { */
    /*         val group = mHelpSnackbar.view as ViewGroup */
    /*         group.setBackgroundColor(ContextCompat.getColor(activity, R.color.snackbar_color_dark_theme)) */

    /*         val snackbarTextId = android.support.design.R.id.snackbar_text */
    /*         val textView = group.findViewById(snackbarTextId) as TextView */
    /*         textView.setTextColor(ContextCompat.getColor(activity, R.color.text_color_dark_theme)) */
    /*     } */

    /*     mHelpSnackbar.show() */
    /* } */

    companion object {
        private val TAG = "TimeToggleFragment"
        private val DEBUG = true
    }
}// Android Fragments require an explicit public default constructor for re-creation



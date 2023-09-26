package com.rockethat.ornaassistant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils.SimpleStringSplitter
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.rockethat.ornaassistant.ui.fragment.FragmentAdapter
import com.rockethat.ornaassistant.ui.fragment.MainFragment

class MainActivity : AppCompatActivity() {

    private lateinit var tableLayout: TabLayout
    private lateinit var pager: ViewPager2
    private lateinit var adapter: FragmentAdapter
    private val TAG = "OrnaMainActivity"
    private val ACCESSIBILITY_SERVICE_NAME = "laukas service"

    // Define the permission request codes
    private val accessibilityPermissionRequestCode = 123
    private val drawOverOtherAppsPermissionRequestCode = 124

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize requestPermissionLauncher within onCreate
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                // Handle permission result here if needed
            }

        tableLayout = findViewById(R.id.tab_layout)
        pager = findViewById(R.id.pager)

        adapter = FragmentAdapter(supportFragmentManager, lifecycle)
        pager.adapter = adapter

        tableLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.text) {
                        "Main" -> {
                            pager.currentItem = 0
                            if (adapter.frags.size >= 1) {
                                (adapter.frags[0] as? MainFragment)?.drawWeeklyChart()
                            }
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tableLayout.selectTab(tableLayout.getTabAt(position))
            }
        })

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    var sharedPreferenceChangeListener =
        OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "your_key") {
                // Write your code here
            }
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.item_preference) {
            goToSettingsActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goToSettingsActivity() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        // Check and request Accessibility Service permission
        if (!isAccessibilityEnabled()) {
            requestAccessibilityPermission()
        }

        // Check and request Draw Over Other Apps permission
        if (!isDrawOverOtherAppsEnabled()) {
            requestDrawOverOtherAppsPermission()
        }

        when (tableLayout.selectedTabPosition) {
            0 -> {
                if (adapter.frags.size >= 1) {
                    (adapter.frags[0] as? MainFragment)?.drawWeeklyChart()
                }
            }
        }
    }

    private fun requestAccessibilityPermission() {
        // Check if the permission is already granted
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BIND_ACCESSIBILITY_SERVICE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted
            return
        }

        // Request the Accessibility Service permission
        requestPermissionLauncher.launch(
            android.Manifest.permission.BIND_ACCESSIBILITY_SERVICE
        )
    }

    private fun requestDrawOverOtherAppsPermission() {
        // Check if the permission is already granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)
        ) {
            // Permission is not granted, request it
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            requestPermissionLauncher.launch(intent.toString())
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        // Your existing accessibility check logic
        return false
    }

    private fun isDrawOverOtherAppsEnabled(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                Settings.canDrawOverlays(this)
    }
}

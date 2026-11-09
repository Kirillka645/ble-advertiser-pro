package io.github.kirillka645.bleadvertiserpro

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.slider.Slider
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.kirillka645.bleadvertiserpro.AppContext.AppContext
import io.github.kirillka645.bleadvertiserpro.Enums.TxPowerLevel
import io.github.kirillka645.bleadvertiserpro.Enums.toStringId
import io.github.kirillka645.bleadvertiserpro.Helpers.LogFileManager
import io.github.kirillka645.bleadvertiserpro.Helpers.QueueHandlerHelpers
import io.github.kirillka645.bleadvertiserpro.databinding.ActivityMainBinding
import io.github.kirillka645.bleadvertiserpro.ui.setupEdgeToEdge


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val navController get() =
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
    private lateinit var binding: ActivityMainBinding
    private val _logTag = "MainActivity"
    private lateinit var sharedPreferenceChangedListener: OnSharedPreferenceChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize log file
        LogFileManager.getInstance(applicationContext).initializeLogFile(this)

        // needs to be before setContentView
        enableEdgeToEdge()

        // Initialize AppContext, Activity, Advertisement Service and QueHandler
        AppContext.setContext(applicationContext)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdge(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // Listen to Preference changes
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        // On first launch, show a blocking research/educational disclaimer.
        val disclaimerKey = getString(R.string.preference_key_disclaimer_accepted)
        if (!prefs.getBoolean(disclaimerKey, false)) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.disclaimer_title)
                .setMessage(R.string.disclaimer_message)
                .setCancelable(false)
                .setPositiveButton(R.string.disclaimer_accept) { dialog, _ ->
                    prefs.edit().putBoolean(disclaimerKey, true).apply()
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.disclaimer_decline) { _, _ -> finish() }
                .show()
        }

        sharedPreferenceChangedListener =
            OnSharedPreferenceChangeListener { _, key ->
                run {
                    val app = (applicationContext as BleAdvertiserApplication)
                    if (key == getString(R.string.preference_key_use_dynamic_colors)) {
                        recreate()
                    }
                    val legacyAdvertisingKey =
                        resources.getString(R.string.preference_key_use_legacy_advertising)
                    if (key == legacyAdvertisingKey) {
                        app.setupAdvertisementService()
                    }

                    val intervalKey =
                        resources.getString(R.string.preference_key_interval_advertising_queue_handler)
                    if (key == intervalKey) {
                        val newInterval = QueueHandlerHelpers.getInterval(this)
                        Log.d(_logTag, "Setting new Interval: $newInterval")
                        app.queueHandler.setInterval(newInterval)
                    }
                }
            }

        prefs.registerOnSharedPreferenceChangeListener(sharedPreferenceChangedListener)

        setupNavController()
    }

    fun setupNavController() {

        appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf(
                R.id.nav_start,
                R.id.nav_advertisement_collection,
                R.id.nav_spam_detector,
            ),
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_start,
                R.id.nav_advertisement_collection,
                R.id.nav_spam_detector
                    -> binding.bottomNav.visibility = View.VISIBLE

                else -> binding.bottomNav.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangedListener)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.nav_preferences -> onNavDestinationSelected(item, navController)
        R.id.nav_set_tx_power -> {
            showSetTxPowerDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    fun showSetTxPowerDialog() {
        val app = applicationContext as BleAdvertiserApplication

        val dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_set_tx_power, null)

        val slider: Slider = dialogLayout.findViewById(R.id.setTxPowerDialogSeekbar)
        val label: TextView = dialogLayout.findViewById(R.id.setTxPowerDialogTxPowerTextView)
        val levels = listOf(
            TxPowerLevel.TX_POWER_ULTRA_LOW,
            TxPowerLevel.TX_POWER_LOW,
            TxPowerLevel.TX_POWER_MEDIUM,
            TxPowerLevel.TX_POWER_HIGH,
        )
        val currentLevel = app.advertisementService.getTxPowerLevel()
        slider.value = levels.indexOf(currentLevel).toFloat()
        label.text = getString(currentLevel.toStringId())
        slider.setLabelFormatter { getString(levels[it.toInt()].toStringId()) }
        slider.addOnChangeListener { _, value, fromUser ->
            val level = levels[value.toInt()]
            label.text = getString(level.toStringId())
            if (fromUser) app.advertisementService.setTxPowerLevel(level)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.power_dialog_title))
            .setView(dialogLayout)
            .setPositiveButton(getString(android.R.string.ok), null)
            .show()
    }
}

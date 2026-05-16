package io.github.kirillka645.bleadvertiserpro

import android.app.Application
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import io.github.kirillka645.bleadvertiserpro.Handlers.AdvertisementSetQueueHandler
import io.github.kirillka645.bleadvertiserpro.Helpers.BluetoothHelpers
import io.github.kirillka645.bleadvertiserpro.Helpers.ThemeManager
import io.github.kirillka645.bleadvertiserpro.Interfaces.Services.IAdvertisementService
import io.github.kirillka645.bleadvertiserpro.Interfaces.Services.IBluetoothLeScanService
import io.github.kirillka645.bleadvertiserpro.Services.BluetoothLeScanService


class BleAdvertiserApplication : Application() {

    lateinit var advertisementService: IAdvertisementService
        private set

    lateinit var queueHandler: AdvertisementSetQueueHandler
        private set

    lateinit var scanService: IBluetoothLeScanService
        private set

    override fun onCreate() {
        ThemeManager.getInstance().applyTheme(this)

        super.onCreate()

        applyDynamicColorsIfEnabled()

        setupAdvertisementService()
        scanService = BluetoothLeScanService(this)
    }

    fun setupAdvertisementService() {
        advertisementService = BluetoothHelpers.getAdvertisementService(this)
        queueHandler = AdvertisementSetQueueHandler(this, advertisementService)
    }

    private fun applyDynamicColorsIfEnabled() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val key = getString(R.string.preference_key_use_dynamic_colors)
        val enabled = prefs.getBoolean(key, true)
        if (enabled) {
            DynamicColors.applyToActivitiesIfAvailable(
                this,
                DynamicColorsOptions.Builder().build()
            )
        }
    }
}

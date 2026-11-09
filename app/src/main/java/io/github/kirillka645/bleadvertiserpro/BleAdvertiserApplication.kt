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

    private val dynamicColorsOptions = DynamicColorsOptions.Builder()
        .setPrecondition { activity, _ ->
            val key = activity.getString(R.string.preference_key_use_dynamic_colors)
            PreferenceManager.getDefaultSharedPreferences(activity)
                .getBoolean(key, true)
        }
        .build()

    override fun onCreate() {
        super.onCreate()

        ThemeManager.getInstance().applyTheme(this)
        DynamicColors.applyToActivitiesIfAvailable(this, dynamicColorsOptions)

        setupAdvertisementService()
        scanService = BluetoothLeScanService(this)
    }

    fun setupAdvertisementService() {
        advertisementService = BluetoothHelpers.getAdvertisementService(this)
        queueHandler = AdvertisementSetQueueHandler(this, advertisementService)
    }
}

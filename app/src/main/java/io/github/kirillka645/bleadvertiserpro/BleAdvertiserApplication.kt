package io.github.kirillka645.bleadvertiserpro

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Build
import android.os.Bundle
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import io.github.kirillka645.bleadvertiserpro.Handlers.AdvertisementSetQueueHandler
import io.github.kirillka645.bleadvertiserpro.Helpers.BluetoothHelpers
import io.github.kirillka645.bleadvertiserpro.Helpers.ThemeManager
import io.github.kirillka645.bleadvertiserpro.Interfaces.Services.IAdvertisementService
import io.github.kirillka645.bleadvertiserpro.Interfaces.Services.IBluetoothLeScanService
import io.github.kirillka645.bleadvertiserpro.Services.BluetoothLeScanService
import java.util.Collections
import java.util.WeakHashMap

class BleAdvertiserApplication : Application() {

    lateinit var advertisementService: IAdvertisementService
        private set

    lateinit var queueHandler: AdvertisementSetQueueHandler
        private set

    lateinit var scanService: IBluetoothLeScanService
        private set

    private val themedActivities =
        Collections.newSetFromMap(WeakHashMap<Activity, Boolean>())
    private val dynamicColorsOptions = DynamicColorsOptions.Builder().build()
    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
            applyDynamicColorsIfEnabled(activity)
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            themedActivities.add(activity)
        }

        override fun onActivityStarted(activity: Activity) = Unit

        override fun onActivityResumed(activity: Activity) = Unit

        override fun onActivityPaused(activity: Activity) = Unit

        override fun onActivityStopped(activity: Activity) = Unit

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

        override fun onActivityDestroyed(activity: Activity) {
            themedActivities.remove(activity)
        }
    }

    override fun onCreate() {
        super.onCreate()

        ThemeManager.getInstance().applyTheme(this)
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)

        setupAdvertisementService()
        scanService = BluetoothLeScanService(this)
    }

    fun setupAdvertisementService() {
        advertisementService = BluetoothHelpers.getAdvertisementService(this)
        queueHandler = AdvertisementSetQueueHandler(this, advertisementService)
    }

    fun recreateThemedActivities() {
        themedActivities.toList()
            .filterNot { it.isFinishing || it.isDestroyed }
            .forEach { it.recreate() }
    }

    private fun applyDynamicColorsIfEnabled(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        val key = getString(R.string.preference_key_use_dynamic_colors)
        val enabled = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean(key, true)
        if (enabled) {
            DynamicColors.applyToActivityIfAvailable(activity, dynamicColorsOptions)
        }
    }
}

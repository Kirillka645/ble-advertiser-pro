package io.github.kirillka645.bleadvertiserpro

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.appbar.MaterialToolbar
import io.github.kirillka645.bleadvertiserpro.Helpers.LogDirectoryPicker
import io.github.kirillka645.bleadvertiserpro.Helpers.LogFileManager
import io.github.kirillka645.bleadvertiserpro.Helpers.ThemeManager
import io.github.kirillka645.bleadvertiserpro.Helpers.ThemeManager.Companion.THEME_MODE_KEY

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_BleAdvertiserPro_NoActionBar)
        // setTheme replaces the overlay applied before onCreate.
        (application as? BleAdvertiserApplication)?.applyDynamicColorsIfEnabled(this)
        super.onCreate(savedInstanceState)

        configureEdgeToEdge()
        setContentView(R.layout.settings_activity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_root)) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, ime.bottom))
            insets
        }
        ViewCompat.requestApplyInsets(findViewById(R.id.settings_root))

        val toolbar = findViewById<MaterialToolbar>(R.id.settings_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.settings_ui_title)
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
    }

    private fun configureEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }

        val isLightTheme =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) !=
                Configuration.UI_MODE_NIGHT_YES
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = isLightTheme
            isAppearanceLightNavigationBars = isLightTheme
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private val logFileManager by lazy(LazyThreadSafetyMode.NONE) {
            LogFileManager.getInstance(requireContext())
        }
        private lateinit var logDirectoryPicker: LogDirectoryPicker
        private lateinit var directoryPickerLauncher: ActivityResultLauncher<Intent>

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            logDirectoryPicker = LogDirectoryPicker(requireActivity())
            directoryPickerLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        result.data?.data?.let(logDirectoryPicker::handleResult)
                    }
                }
            logDirectoryPicker.initialize(directoryPickerLauncher)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            findPreference<SwitchPreferenceCompat>("enable_logging")
                ?.setOnPreferenceChangeListener { _, newValue ->
                    if (newValue as Boolean) {
                        logDirectoryPicker.pickDirectory { directory ->
                            logFileManager.setCustomLogDirectory(directory, requireContext())
                            logFileManager.initializeLogFile(requireContext())
                        }
                    } else {
                        logFileManager.disableLogging(requireContext())
                    }
                    true
                }

            val themeManager = ThemeManager.getInstance()
            val themePreference = findPreference<ListPreference>(THEME_MODE_KEY)
            themePreference?.setOnPreferenceChangeListener { _, newValue ->
                themeManager.setTheme(requireContext(), newValue as String)
                themePreference.summary = themeManager.getThemeString(requireContext())
                true
            }
            themePreference?.summary = themeManager.getThemeString(requireContext())

            findPreference<SwitchPreferenceCompat>(
                getString(R.string.preference_key_use_dynamic_colors)
            )?.setOnPreferenceChangeListener { _, _ ->
                val hostActivity = requireActivity()
                hostActivity.window.decorView.post {
                    (hostActivity.application as? BleAdvertiserApplication)
                        ?.recreateThemedActivities()
                }
                true
            }
        }
    }
}

package io.github.kirillka645.bleadvertiserpro.ui.preferences

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.color.MaterialColors
import io.github.kirillka645.bleadvertiserpro.Helpers.LogDirectoryPicker
import io.github.kirillka645.bleadvertiserpro.Helpers.LogFileManager
import io.github.kirillka645.bleadvertiserpro.Helpers.ThemeManager
import io.github.kirillka645.bleadvertiserpro.Helpers.ThemeManager.Companion.THEME_MODE_KEY
import io.github.kirillka645.bleadvertiserpro.R

class PreferencesFragment : PreferenceFragmentCompat(), MenuProvider {

    private val _logTag = "PreferencesFragment"
    private lateinit var directoryPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var logDirectoryPicker: LogDirectoryPicker

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
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val loggingSwitch =
            findPreference<SwitchPreferenceCompat>(getString(R.string.preference_key_enable_logging))
        loggingSwitch?.isChecked = LogFileManager.getInstance(requireContext())
            .isLoggingEnabledAndValid()
        loggingSwitch?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                val logFileManager = LogFileManager.getInstance(requireContext())
                val accessibleDirs =
                    logFileManager.listAccessibleDirectories(requireContext())
                Log.d(_logTag, "Available accessible directories: " + accessibleDirs.size)

                logDirectoryPicker.pickDirectory { directory ->
                    logFileManager.setCustomLogDirectory(directory, requireContext())
                    logFileManager.initializeLogFile(requireContext())
                    loggingSwitch.isChecked = logFileManager.isLoggingEnabledAndValid()
                }
                false
            } else {
                LogFileManager.getInstance(requireContext()).disableLogging(requireContext())
                true
            }
        }

        val themeManager = ThemeManager.getInstance()
        val themePreference = findPreference<ListPreference>(THEME_MODE_KEY)
        themePreference?.setOnPreferenceChangeListener { _, newValue ->
            themeManager.setTheme(requireContext(), newValue as String)
            themePreference.summary = themeManager.getThemeString(requireContext())
            true
        }
        themePreference?.summary = themeManager.getThemeString(requireContext())

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(
            MaterialColors.getColor(view, com.google.android.material.R.attr.colorSurface)
        )
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) = Unit

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)

        val menuItems = listOf<MenuItem?>(
            menu.findItem(R.id.nav_preferences),
            menu.findItem(R.id.nav_set_tx_power)
        )
        val context = requireContext()

        menuItems.forEach { menuItem ->
            val actionSettingsMenuItem = menuItem
            val title = actionSettingsMenuItem?.title.toString()
            val spannable = SpannableString(title)

            var textColor = resources.getColor(R.color.text_color, context.theme)
            if (menuItem?.itemId == R.id.nav_preferences) {
                textColor = resources.getColor(R.color.text_color_light, context.theme)
                menuItem.isEnabled = false
            }

            spannable.setSpan(
                ForegroundColorSpan(textColor),
                0,
                spannable.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            actionSettingsMenuItem?.title = spannable
        }
    }
}

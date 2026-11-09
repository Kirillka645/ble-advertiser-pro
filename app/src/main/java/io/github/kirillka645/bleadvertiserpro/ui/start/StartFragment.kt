package io.github.kirillka645.bleadvertiserpro.ui.start

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.github.kirillka645.bleadvertiserpro.Database.AppDatabase
import io.github.kirillka645.bleadvertiserpro.Helpers.BluetoothHelpers.Companion.bluetoothAdapter
import io.github.kirillka645.bleadvertiserpro.Helpers.BluetoothHelpers.Companion.isBluetooth5Supported
import io.github.kirillka645.bleadvertiserpro.PermissionCheck.PermissionCheck
import io.github.kirillka645.bleadvertiserpro.R
import io.github.kirillka645.bleadvertiserpro.databinding.FragmentStartBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StartFragment : Fragment() {
    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: StartViewModel

    private val enableBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (_binding != null) refreshDeviceStatus()
    }
    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (_binding != null) refreshDeviceStatus()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[StartViewModel::class.java]
        val context = requireContext()
        val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        binding.infoCard.startFragmentTextViewAppVersion.text = getString(R.string.home_app_version, version)
        binding.infoCard.startFragmentTextViewAndroidVersion.text = getString(R.string.home_android_version, Build.VERSION.RELEASE)
        binding.infoCard.startFragmentTextViewSdkVersion.text = getString(R.string.home_sdk_version, Build.VERSION.SDK_INT.toString())

        binding.startFragmentPermissionsCardview.setOnClickListener { showPermissionDialog() }
        binding.startFragmentBluetoothCardview.setOnClickListener { promptEnableBluetooth() }
        binding.startFragmentDatabaseCardview.setOnClickListener { refreshDeviceStatus() }
        binding.homeBrowseCollections.setOnClickListener {
            findNavController().navigate(R.id.nav_advertisement_collection)
        }

        viewModel.allPermissionsGranted.observe(viewLifecycleOwner) { renderReadiness() }
        viewModel.bluetoothAdapterIsReady.observe(viewLifecycleOwner) { renderReadiness() }
        viewModel.databaseIsReady.observe(viewLifecycleOwner) { renderReadiness() }
        viewModel.isSeeding.observe(viewLifecycleOwner) { renderReadiness() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                do {
                    val (ready, seeding) = withContext(Dispatchers.IO) {
                        val database = AppDatabase.getInstance()
                        val preparing = database.isSeeding || database.inTransaction()
                        (!preparing && database.advertisementSetDao().getAll().isNotEmpty()) to preparing
                    }
                    viewModel.isSeeding.value = seeding
                    viewModel.databaseIsReady.value = ready
                    if (!ready) delay(2_000)
                } while (!ready)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshDeviceStatus()
    }

    private fun canReadBluetooth(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        PermissionCheck.checkPermission(Manifest.permission.BLUETOOTH_CONNECT, requireContext())

    private fun refreshDeviceStatus() {
        val context = requireContext()
        viewModel.allPermissionsGranted.value = PermissionCheck.getAllRelevantPermissions().all {
            PermissionCheck.checkPermission(it, context)
        }
        val adapter = context.bluetoothAdapter()
        viewModel.bluetoothAdapterIsReady.value = canReadBluetooth() && adapter?.isEnabled == true
        binding.startFragmentBluetoothStatusText.setText(when {
            adapter == null -> R.string.home_bluetooth_unavailable
            !canReadBluetooth() -> R.string.home_bluetooth_permission
            adapter.isEnabled -> R.string.home_bluetooth_ready
            else -> R.string.home_bluetooth_disabled
        })
        binding.infoCard.startFragmentTextViewBluetooth.setText(when {
            adapter == null -> R.string.home_bluetooth_unavailable
            !canReadBluetooth() -> R.string.home_bluetooth_unknown
            context.isBluetooth5Supported() -> R.string.home_bluetooth_modern
            else -> R.string.home_bluetooth_legacy
        })
    }

    private fun renderReadiness() {
        val permissionsReady = viewModel.allPermissionsGranted.value == true
        val bluetoothReady = viewModel.bluetoothAdapterIsReady.value == true
        val databaseReady = viewModel.databaseIsReady.value == true
        val seeding = viewModel.isSeeding.value == true
        binding.startFragmentRequirementsTextView.setText(
            if (permissionsReady && bluetoothReady && databaseReady) R.string.home_setup_ready
            else R.string.home_setup_pending
        )
        binding.startFragmentPermissionStatusText.setText(
            if (permissionsReady) R.string.home_permissions_ready else R.string.home_permissions_action
        )
        binding.startFragmentDatabaseStatusText.setText(when {
            databaseReady -> R.string.home_database_ready
            seeding -> R.string.home_database_loading
            else -> R.string.home_database_retry
        })
        binding.startFragmentDatabaseCardSeedingAnimation.isVisible = seeding
        binding.startFragmentDatabaseCardIcon.isVisible = !seeding
        binding.homeBrowseCollections.isEnabled = databaseReady
        styleStatus(binding.startFragmentPermissionsCardview, binding.startFragmentPermissionStatusText, permissionsReady)
        styleStatus(binding.startFragmentBluetoothCardview, binding.startFragmentBluetoothStatusText, bluetoothReady)
        styleStatus(binding.startFragmentDatabaseCardview, binding.startFragmentDatabaseStatusText, databaseReady || seeding)
    }

    private fun styleStatus(card: MaterialCardView, status: TextView, ready: Boolean) {
        val color = if (ready) com.google.android.material.R.attr.colorOnSurfaceVariant
            else com.google.android.material.R.attr.colorError
        status.setTextColor(MaterialColors.getColor(status, color))
        card.strokeWidth = if (ready) 0 else resources.displayMetrics.density.toInt().coerceAtLeast(1)
        card.setStrokeColor(MaterialColors.getColor(card, com.google.android.material.R.attr.colorOutlineVariant))
    }

    private fun showPermissionDialog() {
        if (viewModel.allPermissionsGranted.value == true) {
            Snackbar.make(binding.root, R.string.home_permissions_ready, Snackbar.LENGTH_SHORT).show()
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.missing_permissions_title)
            .setMessage(R.string.missing_permissions_text)
            .setPositiveButton(R.string.missing_permissions_grant) { _, _ -> requestRequiredPermissions() }
            .setNeutralButton(R.string.home_app_settings) { _, _ -> openAppSettings() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun requestRequiredPermissions() {
        val missing = PermissionCheck.getAllRelevantPermissions().filter {
            !PermissionCheck.checkPermission(it, requireContext())
        }
        // Android 11+ rejects a combined foreground/background location request.
        val foreground = missing.filter { it != Manifest.permission.ACCESS_BACKGROUND_LOCATION }
        when {
            foreground.isNotEmpty() -> requestPermissions.launch(foreground.toTypedArray())
            Manifest.permission.ACCESS_BACKGROUND_LOCATION in missing && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.home_background_permission)
                    .setPositiveButton(R.string.home_app_settings) { _, _ -> openAppSettings() }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
            missing.isNotEmpty() -> requestPermissions.launch(missing.toTypedArray())
        }
    }

    private fun openAppSettings() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${requireContext().packageName}")))
    }

    private fun promptEnableBluetooth() {
        val adapter = requireContext().bluetoothAdapter()
        when {
            adapter == null -> Snackbar.make(binding.root, R.string.home_bluetooth_unavailable, Snackbar.LENGTH_SHORT).show()
            !canReadBluetooth() -> showPermissionDialog()
            !adapter.isEnabled -> enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            else -> Snackbar.make(binding.root, R.string.home_bluetooth_ready, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

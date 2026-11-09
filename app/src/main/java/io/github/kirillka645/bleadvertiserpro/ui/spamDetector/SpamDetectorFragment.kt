package io.github.kirillka645.bleadvertiserpro.ui.spamDetector

import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import io.github.kirillka645.bleadvertiserpro.Adapters.FlipperDeviceScanResultListViewAdapter
import io.github.kirillka645.bleadvertiserpro.Adapters.SpamPackageScanResultListViewAdapter
import io.github.kirillka645.bleadvertiserpro.BleAdvertiserApplication
import io.github.kirillka645.bleadvertiserpro.Helpers.BluetoothHelpers.Companion.bluetoothAdapter
import io.github.kirillka645.bleadvertiserpro.Interfaces.Callbacks.IBluetoothLeScanCallback
import io.github.kirillka645.bleadvertiserpro.Models.FlipperDeviceScanResult
import io.github.kirillka645.bleadvertiserpro.Models.SpamPackageScanResult
import io.github.kirillka645.bleadvertiserpro.PermissionCheck.PermissionCheck
import io.github.kirillka645.bleadvertiserpro.R
import io.github.kirillka645.bleadvertiserpro.Services.BluetoothLeScanForegroundService
import io.github.kirillka645.bleadvertiserpro.databinding.FragmentSpamDetectorBinding

class SpamDetectorFragment : Fragment(), IBluetoothLeScanCallback {

    private var _viewModel: SpamDetectorViewModel? = null
    private val viewModel get() = _viewModel!!

    private var _binding: FragmentSpamDetectorBinding? = null
    private val binding get() = _binding!!

    private var flipperDevicesAdapter: FlipperDeviceScanResultListViewAdapter? = null
    private var spamPackagesAdapter: SpamPackageScanResultListViewAdapter? = null

    override fun onResume() {
        super.onResume()

        val context = requireContext()
        val scanService = (context.applicationContext as BleAdvertiserApplication).scanService
        scanService.addBluetoothLeScanServiceCallback(this)
        syncWithScanServices(context)
    }

    override fun onPause() {
        context?.let {
            (it.applicationContext as BleAdvertiserApplication).scanService
                .removeBluetoothLeScanServiceCallback(this)
        }
        super.onPause()
    }

    private fun syncWithScanServices(context: Context) {
        val scanService = (context.applicationContext as BleAdvertiserApplication).scanService
        viewModel.isDetecting.value = scanService.isScanning()
        updateFlipperDevicesListView(context)
        updateSpamPackageListView(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewModel = ViewModelProvider(this)[SpamDetectorViewModel::class.java]
        _binding = FragmentSpamDetectorBinding.inflate(inflater, container, false)

        setupUi()
        setupFlipperDevicesListView(requireContext())
        setupSpamPackagesListView(requireContext())
        updateEmptyStates()

        return binding.root
    }

    override fun onDestroyView() {
        _binding?.spamDetectionFlipperDevicesList?.adapter = null
        _binding?.spamDetectionSpamPackageList?.adapter = null
        flipperDevicesAdapter = null
        spamPackagesAdapter = null
        _binding = null
        super.onDestroyView()
    }

    private fun setupUi() {
        binding.spamDetectorToggleButton.setOnClickListener {
            onToggleButtonClicked(it.context)
        }

        viewModel.isDetecting.observe(viewLifecycleOwner) { isDetecting ->
            renderDetectionState(isDetecting)
        }
    }

    private fun renderDetectionState(isDetecting: Boolean) {
        val currentBinding = _binding ?: return
        if (isDetecting) {
            currentBinding.spamDetectorStatusText.setText(R.string.detector_status_running)
            currentBinding.spamDetectorStatusDescription
                .setText(R.string.detector_status_running_description)
            currentBinding.spamDetectorToggleButton.setIconResource(R.drawable.pause)
            currentBinding.spamDetectorToggleButton.setText(R.string.detector_stop)
            currentBinding.spamDetectionAnimation.playAnimation()
        } else {
            currentBinding.spamDetectorStatusText.setText(R.string.detector_status_stopped)
            currentBinding.spamDetectorStatusDescription
                .setText(R.string.detector_status_stopped_description)
            currentBinding.spamDetectorToggleButton.setIconResource(R.drawable.play_arrow)
            currentBinding.spamDetectorToggleButton.setText(R.string.detector_start)
            currentBinding.spamDetectionAnimation.cancelAnimation()
            currentBinding.spamDetectionAnimation.setProgress(0f)
        }
        updateEmptyStates()
    }

    private fun setupFlipperDevicesListView(context: Context) {
        val scanService = (context.applicationContext as BleAdvertiserApplication).scanService
        flipperDevicesAdapter =
            FlipperDeviceScanResultListViewAdapter(scanService.getFlipperDevicesList())
        binding.spamDetectionFlipperDevicesList.apply {
            adapter = flipperDevicesAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupSpamPackagesListView(context: Context) {
        val scanService = (context.applicationContext as BleAdvertiserApplication).scanService
        spamPackagesAdapter = SpamPackageScanResultListViewAdapter(
            scanService.getSpamPackageScanResultList(),
            context
        )
        binding.spamDetectionSpamPackageList.apply {
            adapter = spamPackagesAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun updateFlipperDevicesListView(context: Context) {
        val adapter = flipperDevicesAdapter ?: return
        val scanService = (context.applicationContext as BleAdvertiserApplication).scanService

        scanService.getFlipperDevicesList().forEach { newDevice ->
            val index = adapter.mList.indexOfFirst { it.address == newDevice.address }
            if (index >= 0) {
                adapter.mList[index] = newDevice
            } else {
                adapter.mList.add(newDevice)
            }
        }

        adapter.notifyDataSetChanged()
        updateEmptyStates()
    }

    private fun updateSpamPackageListView(context: Context) {
        val adapter = spamPackagesAdapter ?: return
        val scanService = (context.applicationContext as BleAdvertiserApplication).scanService

        scanService.getSpamPackageScanResultList().forEach { newPackage ->
            val index = adapter.mList.indexOfFirst { it.address == newPackage.address }
            if (index >= 0) {
                adapter.mList[index] = newPackage
            } else {
                adapter.mList.add(newPackage)
            }
        }

        adapter.notifyDataSetChanged()
        updateEmptyStates()
    }

    private fun updateEmptyStates() {
        val currentBinding = _binding ?: return
        val isDetecting = viewModel.isDetecting.value == true
        val flipperCount = flipperDevicesAdapter?.itemCount ?: 0
        val packageCount = spamPackagesAdapter?.itemCount ?: 0

        currentBinding.spamDetectorFlipperEmptyText.apply {
            visibility = if (flipperCount == 0) View.VISIBLE else View.GONE
            setText(
                if (isDetecting) {
                    R.string.detector_flipper_empty_scanning
                } else {
                    R.string.detector_flipper_empty_idle
                }
            )
        }
        currentBinding.spamDetectionFlipperDevicesList.visibility =
            if (flipperCount == 0) View.GONE else View.VISIBLE

        currentBinding.spamDetectorPackagesEmptyText.apply {
            visibility = if (packageCount == 0) View.VISIBLE else View.GONE
            setText(
                if (isDetecting) {
                    R.string.detector_packages_empty_scanning
                } else {
                    R.string.detector_packages_empty_idle
                }
            )
        }
        currentBinding.spamDetectionSpamPackageList.visibility =
            if (packageCount == 0) View.GONE else View.VISIBLE
    }

    fun onToggleButtonClicked(context: Context) {
        if (viewModel.isDetecting.value == true) {
            BluetoothLeScanForegroundService.stopService(context)
            viewModel.isDetecting.value = false
        } else {
            val permissionsGranted = PermissionCheck.getAllRelevantPermissions().all {
                PermissionCheck.checkPermission(it, context)
            }
            if (!permissionsGranted || context.bluetoothAdapter()?.isEnabled != true) {
                Snackbar.make(binding.root, R.string.session_requirements_missing, Snackbar.LENGTH_LONG)
                    .setAction(R.string.home_tab) { findNavController().navigate(R.id.nav_start) }
                    .show()
                return
            }
            BluetoothLeScanForegroundService.startService(context)
            viewModel.isDetecting.value = true
        }
    }

    override fun onScanResult(scanResult: ScanResult) = Unit

    override fun onFlipperDeviceDetected(
        flipperDeviceScanResult: FlipperDeviceScanResult,
        alreadyKnown: Boolean
    ) = Unit

    override fun onFlipperListUpdated() {
        context?.let { updateFlipperDevicesListView(it) }
    }

    override fun onSpamResultPackageDetected(
        spamPackageScanResult: SpamPackageScanResult,
        alreadyKnown: Boolean
    ) = Unit

    override fun onSpamResultPackageListUpdated() {
        context?.let { updateSpamPackageListView(it) }
    }
}

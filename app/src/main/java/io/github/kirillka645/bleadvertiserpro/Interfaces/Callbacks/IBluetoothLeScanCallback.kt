package io.github.kirillka645.bleadvertiserpro.Interfaces.Callbacks

import android.bluetooth.le.ScanResult
import io.github.kirillka645.bleadvertiserpro.Models.FlipperDeviceScanResult
import io.github.kirillka645.bleadvertiserpro.Models.SpamPackageScanResult

interface IBluetoothLeScanCallback {
    fun onScanResult(scanResult:ScanResult)
    fun onFlipperDeviceDetected(flipperDeviceScanResult: FlipperDeviceScanResult, alreadyKnown:Boolean)
    fun onFlipperListUpdated()
    fun onSpamResultPackageDetected(spamPackageScanResult: SpamPackageScanResult, alreadyKnown:Boolean)
    fun onSpamResultPackageListUpdated()


}
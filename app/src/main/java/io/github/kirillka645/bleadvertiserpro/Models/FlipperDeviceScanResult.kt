package io.github.kirillka645.bleadvertiserpro.Models

import android.os.ParcelUuid
import io.github.kirillka645.bleadvertiserpro.Enums.FlipperDeviceType
import io.github.kirillka645.bleadvertiserpro.Helpers.BluetoothLeDeviceClassificationHelper
import java.time.LocalDate

class FlipperDeviceScanResult: BluetoothLeScanResult() {
    var flipperDeviceType = FlipperDeviceType.UNKNOWN
    var isSpamming = false
    companion object {
        fun parseFromBluetoothLeScanResult(bluetoothLeScanResult: BluetoothLeScanResult):FlipperDeviceScanResult{
            val flipperDeviceScanResult = FlipperDeviceScanResult()

            flipperDeviceScanResult.parseFromBluetoothLeScanResult(bluetoothLeScanResult)
            flipperDeviceScanResult.flipperDeviceType = BluetoothLeDeviceClassificationHelper.getFlipperDeviceType(bluetoothLeScanResult)

            return flipperDeviceScanResult
        }
    }
}
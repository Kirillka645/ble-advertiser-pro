package io.github.kirillka645.bleadvertiserpro.Models

import android.os.ParcelUuid
import io.github.kirillka645.bleadvertiserpro.Enums.FlipperDeviceType
import io.github.kirillka645.bleadvertiserpro.Enums.SpamPackageType
import io.github.kirillka645.bleadvertiserpro.Helpers.BluetoothLeDeviceClassificationHelper
import java.time.LocalDate

class SpamPackageScanResult: BluetoothLeScanResult() {
    var spamPackageType = SpamPackageType.UNKNOWN

    companion object {
        fun parseFromBluetoothLeScanResult(bluetoothLeScanResult: BluetoothLeScanResult):SpamPackageScanResult{
            val spamPackageScanResult = SpamPackageScanResult()

            spamPackageScanResult.parseFromBluetoothLeScanResult(bluetoothLeScanResult)
            spamPackageScanResult.spamPackageType = BluetoothLeDeviceClassificationHelper.getSpamPackageType(bluetoothLeScanResult)

            return spamPackageScanResult
        }
    }
}
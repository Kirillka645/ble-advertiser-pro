package io.github.kirillka645.bleadvertiserpro.Interfaces.Services

import io.github.kirillka645.bleadvertiserpro.Interfaces.Callbacks.IBluetoothLeScanCallback
import io.github.kirillka645.bleadvertiserpro.Models.FlipperDeviceScanResult
import io.github.kirillka645.bleadvertiserpro.Models.SpamPackageScanResult

interface IBluetoothLeScanService {
    fun startScanning()
    fun stopScanning()

    fun isScanning(): Boolean

    fun getFlipperDevicesList(): MutableList<FlipperDeviceScanResult>
    fun getSpamPackageScanResultList(): MutableList<SpamPackageScanResult>

    fun addBluetoothLeScanServiceCallback(callback: IBluetoothLeScanCallback)
    fun removeBluetoothLeScanServiceCallback(callback: IBluetoothLeScanCallback)
}

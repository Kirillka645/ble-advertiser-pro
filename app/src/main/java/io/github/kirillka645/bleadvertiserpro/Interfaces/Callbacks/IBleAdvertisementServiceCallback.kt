package io.github.kirillka645.bleadvertiserpro.Interfaces.Callbacks

import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertisingSet
import android.util.Log
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSet

interface IBleAdvertisementServiceCallback {
    fun onAdvertisementStarted()
    fun onAdvertisementStopped()
    fun onAdvertisementSetStarted(advertisementSet: AdvertisementSet)
    fun onAdvertisementSetStopped(advertisementSet: AdvertisementSet)
    fun onStartFailure(errorCode: Int)
    fun onStartSuccess(settingsInEffect: AdvertiseSettings?)
    fun onAdvertisingSetStarted(advertisingSet: AdvertisingSet?, txPower: Int, status: Int)
    fun onAdvertisingDataSet(advertisingSet: AdvertisingSet, status: Int)
    fun onScanResponseDataSet(advertisingSet: AdvertisingSet, status: Int)
    fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet)
}
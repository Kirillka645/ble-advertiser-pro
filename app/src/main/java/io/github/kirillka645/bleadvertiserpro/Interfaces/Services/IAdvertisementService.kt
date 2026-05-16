package io.github.kirillka645.bleadvertiserpro.Interfaces.Services

import io.github.kirillka645.bleadvertiserpro.Enums.TxPowerLevel
import io.github.kirillka645.bleadvertiserpro.Interfaces.Callbacks.IAdvertisementServiceCallback
import io.github.kirillka645.bleadvertiserpro.Interfaces.Callbacks.IBleAdvertisementServiceCallback
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSet

interface IAdvertisementService {
    fun startAdvertisement(advertisementSet: AdvertisementSet)
    fun stopAdvertisement()
    fun setTxPowerLevel(txPowerLevel:TxPowerLevel)
    fun getTxPowerLevel(): TxPowerLevel
    fun addAdvertisementServiceCallback(callback: IAdvertisementServiceCallback)
    fun removeAdvertisementServiceCallback(callback: IAdvertisementServiceCallback)
    fun isLegacyService():Boolean
}
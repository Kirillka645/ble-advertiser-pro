package io.github.kirillka645.bleadvertiserpro.Interfaces.Callbacks

import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementError
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSet

interface IAdvertisementServiceCallback {
    fun onAdvertisementSetStart(advertisementSet: AdvertisementSet?)
    fun onAdvertisementSetStop(advertisementSet: AdvertisementSet?)
    fun onAdvertisementSetSucceeded(advertisementSet: AdvertisementSet?)
    fun onAdvertisementSetFailed(advertisementSet: AdvertisementSet?, advertisementError: AdvertisementError)
}
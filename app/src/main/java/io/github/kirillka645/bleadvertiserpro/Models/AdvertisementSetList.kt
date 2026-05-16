package io.github.kirillka645.bleadvertiserpro.Models

import java.io.Serializable

class AdvertisementSetList : Serializable {
    var title = ""
    var advertisementSets:MutableList<AdvertisementSet> = mutableListOf()

    // Ui Data
    var currentlyAdvertising = false
}
package io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators

import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSet

interface IAdvertisementSetGenerator {
    fun getAdvertisementSets(inputData: Map<String, String>?):List<AdvertisementSet>

}
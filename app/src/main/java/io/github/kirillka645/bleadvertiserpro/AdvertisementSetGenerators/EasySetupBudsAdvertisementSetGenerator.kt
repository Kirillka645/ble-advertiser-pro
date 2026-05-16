package io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators

import android.bluetooth.le.AdvertisingSetParameters
import android.util.Log
import io.github.kirillka645.bleadvertiserpro.Callbacks.GenericAdvertisingCallback
import io.github.kirillka645.bleadvertiserpro.Callbacks.GenericAdvertisingSetCallback
import io.github.kirillka645.bleadvertiserpro.Constants.Constants
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertiseMode
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementSetRange
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementSetType
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementTarget
import io.github.kirillka645.bleadvertiserpro.Enums.PrimaryPhy
import io.github.kirillka645.bleadvertiserpro.Enums.SecondaryPhy
import io.github.kirillka645.bleadvertiserpro.Enums.TxPowerLevel
import io.github.kirillka645.bleadvertiserpro.Helpers.StringHelpers
import io.github.kirillka645.bleadvertiserpro.Helpers.StringHelpers.Companion.toHexString
import io.github.kirillka645.bleadvertiserpro.Models.AdvertiseData
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSet
import io.github.kirillka645.bleadvertiserpro.Models.ManufacturerSpecificData

class EasySetupBudsAdvertisementSetGenerator:IAdvertisementSetGenerator{

    // Device Id's taken from here:
    // https://github.com/Flipper-XFW/Xtreme-Firmware/blob/dev/applications/external/ble_spam/protocols/easysetup.c

    // Logic also from here:
    // https://github.com/tutozz/ble-spam-android/blob/main/app/src/main/java/com/tutozz/blespam/EasySetupSpam.java

    private val _manufacturerId = Constants.MANUFACTURER_ID_SAMSUNG
    private val prependedBudsBytes = StringHelpers.decodeHex("42098102141503210109")
    private val appendedBudsBytes = StringHelpers.decodeHex("063C948E00000000C700") // +16FF75

    //42098102941503210188 5317012A 063CE7EB000000001D00
    //private val prependedBudsBytes = StringHelpers.decodeHex("42098102941503210188")
    //private val appendedBudsBytes = StringHelpers.decodeHex("063CE7EB000000001D00")

    val _genuineBudsIds = mapOf(
        "EE7A0C" to "Fallback Buds",
        "9D1700" to "Fallback Dots",
        "39EA48" to "Light Purple Buds2",
        "A7C62C" to "Bluish Silver Buds2",
        "850116" to "Black Buds Live",
        "3D8F41" to "Gray & Black Buds2",
        "3B6D02" to "Bluish Chrome Buds2",
        "AE063C" to "Gray Beige Buds2",
        "B8B905" to "Pure White Buds",
        "EAAA17" to "Pure White Buds2",
        "D30704" to "Black Buds",
        "9DB006" to "French Flag Buds",
        "101F1A" to "Dark Purple Buds Live",
        "859608" to "Dark Blue Buds",
        "8E4503" to "Pink Buds",
        "2C6740" to "White & Black Buds2",
        "3F6718" to "Bronze Buds Live",
        "42C519" to "Red Buds Live",
        "AE073A" to "Black & White Buds2",
        "011716" to "Sleek Black Buds2",
        // Buds2 Pro / Buds3 / Buds3 Pro (additional shells)
        "5CC9F5" to "Graphite Buds2 Pro",
        "EBA28A" to "Bora Purple Buds2 Pro",
        "6D71B4" to "White Buds2 Pro",
        "F8D90A" to "Silver Buds3",
        "57A07E" to "White Buds3",
        "C19B4F" to "Silver Buds3 Pro",
        "29CE4B" to "White Buds3 Pro",
    )

    override fun getAdvertisementSets(inputData: Map<String, String>?): List<AdvertisementSet> {
        var advertisementSets:MutableList<AdvertisementSet> = mutableListOf()

        val data = inputData ?: _genuineBudsIds

        // BUDS
        data.map {
            var advertisementSet:AdvertisementSet = AdvertisementSet()
            advertisementSet.target = AdvertisementTarget.ADVERTISEMENT_TARGET_SAMSUNG
            advertisementSet.type = AdvertisementSetType.ADVERTISEMENT_TYPE_EASY_SETUP_BUDS
            advertisementSet.range = AdvertisementSetRange.ADVERTISEMENTSET_RANGE_CLOSE

            // Advertise Settings
            advertisementSet.advertiseSettings.advertiseMode = AdvertiseMode.ADVERTISEMODE_LOW_LATENCY
            advertisementSet.advertiseSettings.txPowerLevel = TxPowerLevel.TX_POWER_HIGH
            advertisementSet.advertiseSettings.connectable = false
            advertisementSet.advertiseSettings.timeout = 0

            // Advertising Parameters
            advertisementSet.advertisingSetParameters.legacyMode = true
            advertisementSet.advertisingSetParameters.interval = AdvertisingSetParameters.INTERVAL_MIN
            advertisementSet.advertisingSetParameters.txPowerLevel = TxPowerLevel.TX_POWER_HIGH
            advertisementSet.advertisingSetParameters.primaryPhy = PrimaryPhy.PHY_LE_1M
            advertisementSet.advertisingSetParameters.secondaryPhy = SecondaryPhy.PHY_LE_1M

            // AdvertiseData
            advertisementSet.advertiseData.includeDeviceName = false

            val manufacturerSpecificData = ManufacturerSpecificData()
            manufacturerSpecificData.manufacturerId = _manufacturerId

            //var deviceBytes = StringHelpers.decodeHex(it.key)
            //var payload = byteArrayOf(deviceBytes[0], deviceBytes[1], 0x01, deviceBytes[2])
            var payload = StringHelpers.decodeHex(it.key.substring(0,4) + "01" + it.key.substring(4))
            var fullPayload = prependedBudsBytes.plus(payload).plus(appendedBudsBytes)

            manufacturerSpecificData.manufacturerSpecificData = fullPayload
            //Log.d("EASY SETUP", "Full Payload(${fullPayload.size}): " + fullPayload.toHexString())

            advertisementSet.advertiseData.manufacturerData.add(manufacturerSpecificData)
            advertisementSet.advertiseData.includeTxPower = false

            // Scan Response
            advertisementSet.scanResponse = AdvertiseData()
            advertisementSet.scanResponse!!.includeDeviceName = false
            val scanResponseManufacturerSpecificData = ManufacturerSpecificData()
            scanResponseManufacturerSpecificData.manufacturerId = _manufacturerId
            scanResponseManufacturerSpecificData.manufacturerSpecificData = StringHelpers.decodeHex("0000000000000000000000000000")
            advertisementSet.scanResponse!!.manufacturerData.add(scanResponseManufacturerSpecificData)


            // General Data
            advertisementSet.title = it.value

            // Callbacks
            advertisementSet.advertisingSetCallback = GenericAdvertisingSetCallback()
            advertisementSet.advertisingCallback = GenericAdvertisingCallback()

            advertisementSets.add(advertisementSet)
        }

        return advertisementSets.toList()
    }
}

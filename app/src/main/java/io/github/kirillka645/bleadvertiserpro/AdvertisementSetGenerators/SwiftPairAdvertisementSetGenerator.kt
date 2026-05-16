package io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertisingSetParameters
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
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSet
import io.github.kirillka645.bleadvertiserpro.Models.ManufacturerSpecificData

class SwiftPairAdvertisementSetGenerator : IAdvertisementSetGenerator {

    // Generating Manufacturer Specific Data like found here:
    // https://github.com/Flipper-XFW/Xtreme-Firmware/blob/dev/applications/external/ble_spam/protocols/swiftpair.c

    private val _prependedBytes = StringHelpers.decodeHex("030080")

    private val _deviceNames = mapOf(
        "Surface Earbuds" to "Surface Earbuds",
        "Surface Headphones 2" to "Surface Headphones 2",
        "Surface Pro Keyboard" to "Surface Pro Keyboard",
        "Microsoft Modern Wireless Headset" to "Microsoft Modern Wireless Headset",
        "Microsoft Ergonomic Keyboard" to "Microsoft Ergonomic Keyboard",
        "Microsoft Sculpt Ergonomic" to "Microsoft Sculpt Ergonomic",
        "Microsoft Designer Compact Keyboard" to "Microsoft Designer Compact Keyboard",
        "Xbox Wireless Controller" to "Xbox Wireless Controller",
        "Xbox Elite Wireless Controller 2" to "Xbox Elite Wireless Controller 2",
        "Xbox Adaptive Controller" to "Xbox Adaptive Controller",
        "Microsoft Arc Mouse" to "Microsoft Arc Mouse",
        "Microsoft Bluetooth Mouse" to "Microsoft Bluetooth Mouse",
    )

    private val _manufacturerId = Constants.MANUFACTURER_ID_MICROSOFT
    override fun getAdvertisementSets(inputData: Map<String, String>?): List<AdvertisementSet> {
        var advertisementSets:MutableList<AdvertisementSet> = mutableListOf()

        val data = inputData ?: _deviceNames

        data.map {deviceName ->

            var advertisementSet:AdvertisementSet = AdvertisementSet()
            advertisementSet.target = AdvertisementTarget.ADVERTISEMENT_TARGET_WINDOWS
            advertisementSet.type = AdvertisementSetType.ADVERTISEMENT_TYPE_SWIFT_PAIRING
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
            manufacturerSpecificData.manufacturerSpecificData = _prependedBytes.plus(deviceName.key.toByteArray())
            advertisementSet.advertiseData.manufacturerData.add(manufacturerSpecificData)
            advertisementSet.advertiseData.includeTxPower = false

            // Scan Response
            // advertisementSet.scanResponse.includeTxPower = false

            // General Data
            advertisementSet.title = deviceName.key

            // Callbacks
            advertisementSet.advertisingSetCallback = GenericAdvertisingSetCallback()
            advertisementSet.advertisingCallback = GenericAdvertisingCallback()

            advertisementSets.add(advertisementSet)
        }

        return advertisementSets.toList()
    }
}

package io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators

import android.bluetooth.le.AdvertisingSetParameters
import android.os.ParcelUuid
import io.github.kirillka645.bleadvertiserpro.Callbacks.GenericAdvertisingCallback
import io.github.kirillka645.bleadvertiserpro.Callbacks.GenericAdvertisingSetCallback
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertiseMode
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementSetRange
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementSetType
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementTarget
import io.github.kirillka645.bleadvertiserpro.Enums.PrimaryPhy
import io.github.kirillka645.bleadvertiserpro.Enums.SecondaryPhy
import io.github.kirillka645.bleadvertiserpro.Enums.TxPowerLevel
import io.github.kirillka645.bleadvertiserpro.Helpers.StringHelpers
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSet
import io.github.kirillka645.bleadvertiserpro.Models.ServiceData
import java.util.UUID

class FastPairPhoneSetupAdvertisementSetGenerator:IAdvertisementSetGenerator{

    // Device Id's taken from here:
    // https://github.com/Flipper-XFW/Xtreme-Firmware/blob/dev/applications/external/ble_spam/protocols/fastpair.c

    val _genuineDeviceIds = mapOf(
        "00000C" to "Google Gphones Transfer",
        "0577B1" to "Galaxy S23 Ultra",
        "05A9BC" to "Galaxy S20+",
        "0AF124" to "Galaxy S24",
        "0AF125" to "Galaxy S24 Ultra",
        "0AF126" to "Galaxy Z Flip 6",
        "0AF127" to "Galaxy Z Fold 6",
        "0AF128" to "Pixel 9",
        "0AF129" to "Pixel 9 Pro",
        "0AF12A" to "Pixel 9 Pro XL",
        "0AF12B" to "Pixel 9 Pro Fold",
        "0AF12C" to "Pixel 8 Pro",
        "0AF12D" to "Pixel 8a",
        "0AF12E" to "Nothing Phone (2a)",
        "0AF12F" to "Nothing Phone (3)",
        "0AF130" to "OnePlus 12",
        "0AF131" to "OnePlus 12R",
    )

    val serviceUuid = ParcelUuid(UUID.fromString("0000fe2c-0000-1000-8000-00805f9b34fb"))

    override fun getAdvertisementSets(inputData: Map<String, String>?): List<AdvertisementSet> {
        var advertisementSets:MutableList<AdvertisementSet> = mutableListOf()

        val data = inputData ?: _genuineDeviceIds

        data.map {

            var advertisementSet:AdvertisementSet = AdvertisementSet()
            advertisementSet.target = AdvertisementTarget.ADVERTISEMENT_TARGET_ANDROID
            advertisementSet.type = AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_PHONE_SETUP
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

            val serviceData = ServiceData()
            serviceData.serviceUuid = serviceUuid
            serviceData.serviceData = StringHelpers.decodeHex(it.key)
            advertisementSet.advertiseData.services.add(serviceData)
            advertisementSet.advertiseData.includeTxPower = true

            // Scan Response
            //advertisementSet.scanResponse.includeTxPower = true

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

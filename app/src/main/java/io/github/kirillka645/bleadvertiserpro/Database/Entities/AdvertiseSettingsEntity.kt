package io.github.kirillka645.bleadvertiserpro.Database.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertiseMode
import io.github.kirillka645.bleadvertiserpro.Enums.TxPowerLevel

@Entity
data class AdvertiseSettingsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,

    @ColumnInfo(name = "advertiseMode") var advertiseMode: AdvertiseMode,
    @ColumnInfo(name = "txPowerLevel") var txPowerLevel: TxPowerLevel,
    @ColumnInfo(name = "connectable") var connectable: Boolean,
    @ColumnInfo(name = "timeout") var timeout: Int
)

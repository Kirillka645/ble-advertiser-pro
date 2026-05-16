package io.github.kirillka645.bleadvertiserpro.Database.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementSetType
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementTarget

@Entity
data class AdvertiseDataEntity(
    @PrimaryKey(autoGenerate = true) var id: Int,

    @ColumnInfo(name = "includeDeviceName") var includeDeviceName: Boolean,
    @ColumnInfo(name = "includeTxPower") var includeTxPower: Boolean
){
    constructor():this(0,false,false)
}
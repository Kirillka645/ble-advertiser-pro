package io.github.kirillka645.bleadvertiserpro.Database.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementSetType
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementTarget
import java.util.UUID

@Entity
data class AdvertiseDataManufacturerSpecificDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    
    @ColumnInfo(name = "advertiseDataId") var advertiseDataId: Int,
    @ColumnInfo(name = "manufacturerId") var manufacturerId: Int,
    @ColumnInfo(name = "manufacturerSpecificData") var manufacturerSpecificData: String
)

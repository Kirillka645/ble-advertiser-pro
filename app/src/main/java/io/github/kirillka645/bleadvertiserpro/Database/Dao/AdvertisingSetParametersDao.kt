package io.github.kirillka645.bleadvertiserpro.Database.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AdvertiseSettingsEntity
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AdvertisingSetParametersEntity

@Dao
interface AdvertisingSetParametersDao {
    @Query("SELECT * FROM advertisingsetparametersentity WHERE id = :id")
    fun findById(id: Int): AdvertisingSetParametersEntity

    @Query("SELECT * FROM advertisingsetparametersentity")
    fun getAll(): List<AdvertisingSetParametersEntity>

    @Insert
    fun insertAll(vararg advertisingSetParametersEntity: AdvertisingSetParametersEntity)

    @Delete
    fun delete(advertisingSetParametersEntity: AdvertisingSetParametersEntity)

    @Insert
    fun insertItem(advertisingSetParametersEntity: AdvertisingSetParametersEntity): Long
}
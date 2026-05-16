package io.github.kirillka645.bleadvertiserpro.Database.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AdvertiseSettingsEntity
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AdvertisementSetListEntity

@Dao
interface AdvertiseSettingsDao {
    @Query("SELECT * FROM advertisesettingsentity WHERE id = :id")
    fun findById(id: Int): AdvertiseSettingsEntity

    @Query("SELECT * FROM advertisesettingsentity")
    fun getAll(): List<AdvertiseSettingsEntity>

    @Insert
    fun insertAll(vararg advertiseSettingsEntity: AdvertiseSettingsEntity)

    @Delete
    fun delete(advertiseSettingsEntity: AdvertiseSettingsEntity)
    @Insert
    fun insertItem(advertiseSettingsEntity: AdvertiseSettingsEntity): Long

}
package io.github.kirillka645.bleadvertiserpro.Database.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AdvertiseDataServiceDataEntity
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AdvertisementSetCollectionEntity

@Dao
interface AdvertisementSetCollectionDao {
    @Query("SELECT * FROM advertisementsetcollectionentity WHERE id = :id")
    fun findById(id: Int): AdvertisementSetCollectionEntity

    @Query("SELECT * FROM advertisementsetcollectionentity")
    fun getAll(): List<AdvertisementSetCollectionEntity>

    @Insert
    fun insertAll(vararg advertisementSetColletionEntity: AdvertisementSetCollectionEntity)

    @Delete
    fun delete(advertisementSetColletionEntity: AdvertisementSetCollectionEntity)

    @Insert
    fun insertItem(advertisementSetCollectionEntity: AdvertisementSetCollectionEntity): Long
}
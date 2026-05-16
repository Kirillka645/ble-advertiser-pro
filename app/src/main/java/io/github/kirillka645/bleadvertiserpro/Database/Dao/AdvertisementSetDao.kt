package io.github.kirillka645.bleadvertiserpro.Database.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AdvertisementSetCollectionEntity
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AdvertisementSetEntity
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementSetType
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementTarget

@Dao
interface AdvertisementSetDao {

    @Query("SELECT * FROM advertisementsetentity WHERE id = :id")
    fun findById(id: Int): AdvertisementSetEntity

    @Query("SELECT * FROM advertisementsetentity")
    fun getAll(): List<AdvertisementSetEntity>

    @Query("SELECT * FROM advertisementsetentity WHERE id IN (:advertisementSetEntityIds)")
    fun loadAllByIds(advertisementSetEntityIds: IntArray): List<AdvertisementSetEntity>

    @Query("SELECT * FROM advertisementsetentity WHERE target = :target")
    fun findByTarget(target:AdvertisementTarget): List<AdvertisementSetEntity>

    @Query("SELECT * FROM advertisementsetentity WHERE type = :type")
    fun findByType(type:AdvertisementSetType): List<AdvertisementSetEntity>

    @Insert
    fun insertAll(vararg advertisementSetEntity: AdvertisementSetEntity)

    @Delete
    fun delete(advertisementSetEntity: AdvertisementSetEntity)

    @Insert
    fun insertItem(advertisementSetEntity: AdvertisementSetEntity): Long
}
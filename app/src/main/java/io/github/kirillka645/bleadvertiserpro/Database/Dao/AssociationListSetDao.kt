package io.github.kirillka645.bleadvertiserpro.Database.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AssociationListSetEntity

@Dao
interface AssociationListSetDao {
    @Query("SELECT * FROM associationlistsetentity WHERE id = :id")
    fun findById(id: Int): AssociationListSetEntity

    @Query("SELECT * FROM associationlistsetentity")
    fun getAll(): List<AssociationListSetEntity>

    @Insert
    fun insertAll(vararg associationListSetEntity: AssociationListSetEntity)

    @Delete
    fun delete(associationListSetEntity: AssociationListSetEntity)

    @Insert
    fun insertItem(associationListSetEntity: AssociationListSetEntity): Long
}
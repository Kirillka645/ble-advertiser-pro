package io.github.kirillka645.bleadvertiserpro.Models

import androidx.room.ColumnInfo
import java.io.Serializable

class PeriodicAdvertisingParameters : Serializable {
    var id = 0
    var includeTxPowerLevel = false
    var interval = 0
}
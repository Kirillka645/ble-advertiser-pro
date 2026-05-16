package io.github.kirillka645.bleadvertiserpro.Models

import io.github.kirillka645.bleadvertiserpro.Constants.LogLevel
import java.io.Serializable

class LogEntryModel : Serializable {
    var message:String = ""
    var level: LogLevel = LogLevel.Info
}
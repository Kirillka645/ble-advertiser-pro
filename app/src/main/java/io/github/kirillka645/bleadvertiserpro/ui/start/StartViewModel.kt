package io.github.kirillka645.bleadvertiserpro.ui.start

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StartViewModel : ViewModel() {
    val isSeeding = MutableLiveData(false)
    val allPermissionsGranted = MutableLiveData(false)
    val bluetoothAdapterIsReady = MutableLiveData(false)
    val databaseIsReady = MutableLiveData(false)
}

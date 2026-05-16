package io.github.kirillka645.bleadvertiserpro.ui.spamDetector

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SpamDetectorViewModel : ViewModel() {
    val isDetecting = MutableLiveData<Boolean>(false)
}

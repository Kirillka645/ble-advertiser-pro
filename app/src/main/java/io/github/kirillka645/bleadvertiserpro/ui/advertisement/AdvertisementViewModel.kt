package io.github.kirillka645.bleadvertiserpro.ui.advertisement

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementQueueMode
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementTarget

class AdvertisementViewModel : ViewModel() {
    val isAdvertising = MutableLiveData<Boolean>(false)

    val target = MutableLiveData<AdvertisementTarget>(
        AdvertisementTarget.ADVERTISEMENT_TARGET_UNDEFINED
    )

    val advertisementSetCollectionTitle = MutableLiveData<String>("-")
    val advertisementSetCollectionSubTitle = MutableLiveData<String>("-")
    val advertisementSetCollectionHint = MutableLiveData<String>("-")

    val advertisementSetTitle = MutableLiveData<String>("-")
    val advertisementSetSubTitle = MutableLiveData<String>("-")

    val advertisementQueueMode = MutableLiveData<AdvertisementQueueMode>(
        AdvertisementQueueMode.ADVERTISEMENT_QUEUE_MODE_RANDOM
    )

}

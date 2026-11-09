package io.github.kirillka645.bleadvertiserpro.ui.advertisementcollection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import io.github.kirillka645.bleadvertiserpro.BleAdvertiserApplication
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementQueueMode
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementSetType
import io.github.kirillka645.bleadvertiserpro.Enums.stringResId
import io.github.kirillka645.bleadvertiserpro.Helpers.DatabaseHelpers
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSetCollection
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSetList
import io.github.kirillka645.bleadvertiserpro.R
import io.github.kirillka645.bleadvertiserpro.databinding.FragmentAdvertisementCollectionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdvertisementCollectionFragment : Fragment() {

    private var _binding: FragmentAdvertisementCollectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdvertisementCollectionBinding.inflate(inflater, container, false)
        setupAdvertisementSetCollectionListView()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setupAdvertisementSetCollectionListView() {
        binding.advertisementCollectionFastPairCard.setOnClickListener {
            onFastPairCardViewClicked()
        }
        binding.advertisementCollectionContinuityCard.setOnClickListener {
            onContinuityCardViewClicked()
        }
        binding.advertisementCollectionEasySetupCard.setOnClickListener {
            onEasySetupCardViewClicked()
        }
        binding.advertisementCollectionSwiftPairCard.setOnClickListener {
            onSwiftPairingCardViewClicked()
        }
        binding.advertisementCollectionLovespouseCard.setOnClickListener {
            onLovespouseCardViewClicked()
        }
        binding.advertisementCollectionKitchenSinkCard.setOnClickListener {
            onKitchenSinkCardViewClicked(it.context)
        }
    }

    fun navigateToAdvertisementFragmentWithType(
        advertisementSetTypes: List<AdvertisementSetType>,
        advertisementSetCollectionTitle: String
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            val advertisementSetCollection = withContext(Dispatchers.IO) {
                buildAdvertisementCollection(advertisementSetTypes, advertisementSetCollectionTitle)
            }

            val hostActivity = activity ?: return@launch
            (hostActivity.applicationContext as BleAdvertiserApplication).queueHandler.apply {
                deactivate(hostActivity)
                setAdvertisementSetCollection(advertisementSetCollection)
            }
            hostActivity.findNavController(R.id.nav_host_fragment)
                .navigate(R.id.action_ad_coll_to_ad)
        }
    }

    fun buildAdvertisementCollection(
        advertisementSetTypes: List<AdvertisementSetType>,
        advertisementSetCollectionTitle: String
    ): AdvertisementSetCollection {
        val advertisementSetCollection = AdvertisementSetCollection()
        advertisementSetCollection.title = advertisementSetCollectionTitle

        if (advertisementSetTypes.contains(AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_DEVICE) ||
            advertisementSetTypes.contains(AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_PHONE_SETUP) ||
            advertisementSetTypes.contains(AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_NON_PRODUCTION) ||
            advertisementSetTypes.contains(AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_DEBUG)
        ) {
            advertisementSetCollection.hints.add(
                "Fast Pairing is patched on all modern devices due to this we no longer offer support for this feature"
            )
        }

        if (advertisementSetTypes.contains(AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_IOS_17_CRASH)) {
            advertisementSetCollection.hints.add(
                "Devices on iOS 18 or above will not crash but still get pop-ups"
            )
        }

        advertisementSetTypes.forEach { advertisementSetType ->
            val advertisementSetList = AdvertisementSetList()
            advertisementSetList.title = getString(advertisementSetType.stringResId()) + " List"

            val advertisementSets =
                DatabaseHelpers.getAllAdvertisementSetsForType(advertisementSetType)
            advertisementSetList.advertisementSets = advertisementSets.toMutableList()
            advertisementSetCollection.advertisementSetLists.add(advertisementSetList)
        }

        return advertisementSetCollection
    }

    fun onFastPairCardViewClicked() {
        navigateToAdvertisementFragmentWithType(
            listOf(
                AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_DEVICE,
                AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_PHONE_SETUP,
                AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_NON_PRODUCTION,
                AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_DEBUG
            ),
            "Fast Pair Collection"
        )
    }

    fun onEasySetupCardViewClicked() {
        navigateToAdvertisementFragmentWithType(
            listOf(
                AdvertisementSetType.ADVERTISEMENT_TYPE_EASY_SETUP_WATCH,
                AdvertisementSetType.ADVERTISEMENT_TYPE_EASY_SETUP_BUDS
            ),
            "Easy Setup Collection"
        )
    }

    fun onContinuityCardViewClicked() {
        navigateToAdvertisementFragmentWithType(
            listOf(
                AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_NEW_DEVICE,
                AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_NOT_YOUR_DEVICE,
                AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_NEW_AIRTAG,
                AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_ACTION_MODALS,
                AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_IOS_17_CRASH
            ),
            "Continuity Collection"
        )
    }

    fun onSwiftPairingCardViewClicked() {
        navigateToAdvertisementFragmentWithType(
            listOf(AdvertisementSetType.ADVERTISEMENT_TYPE_SWIFT_PAIRING),
            "Swift Pair Collection"
        )
    }

    fun onLovespouseCardViewClicked() {
        navigateToAdvertisementFragmentWithType(
            listOf(
                AdvertisementSetType.ADVERTISEMENT_TYPE_LOVESPOUSE_PLAY,
                AdvertisementSetType.ADVERTISEMENT_TYPE_LOVESPOUSE_STOP
            ),
            "Lovespouse Collection"
        )
    }

    fun onKitchenSinkCardViewClicked(context: Context) {
        (context.applicationContext as BleAdvertiserApplication).queueHandler
            .setAdvertisementQueueMode(AdvertisementQueueMode.ADVERTISEMENT_QUEUE_MODE_RANDOM)

        navigateToAdvertisementFragmentWithType(
            listOf(
                AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_DEVICE,
                AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_PHONE_SETUP,
                AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_NON_PRODUCTION,
                AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_DEBUG,
                AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_NEW_DEVICE,
                AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_NEW_AIRTAG,
                AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_NOT_YOUR_DEVICE,
                AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_ACTION_MODALS,
                AdvertisementSetType.ADVERTISEMENT_TYPE_EASY_SETUP_WATCH,
                AdvertisementSetType.ADVERTISEMENT_TYPE_EASY_SETUP_BUDS,
                AdvertisementSetType.ADVERTISEMENT_TYPE_SWIFT_PAIRING,
                AdvertisementSetType.ADVERTISEMENT_TYPE_LOVESPOUSE_PLAY,
                AdvertisementSetType.ADVERTISEMENT_TYPE_LOVESPOUSE_STOP
            ),
            "Kitchen Sink Collection"
        )
    }
}

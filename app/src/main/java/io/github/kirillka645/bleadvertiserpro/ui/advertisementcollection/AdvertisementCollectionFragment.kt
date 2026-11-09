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
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementTarget
import io.github.kirillka645.bleadvertiserpro.Enums.getDrawableId
import io.github.kirillka645.bleadvertiserpro.Enums.stringResId
import io.github.kirillka645.bleadvertiserpro.Helpers.DatabaseHelpers
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSetCollection
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSetList
import io.github.kirillka645.bleadvertiserpro.R
import io.github.kirillka645.bleadvertiserpro.databinding.FragmentAdvertisementCollectionBinding
import io.github.kirillka645.bleadvertiserpro.databinding.ListitemAdvertisementCollectionStartBinding
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

    private fun setupAdvertisementSetCollectionListView() {
        listOf(
            AdvertisementTarget.ADVERTISEMENT_TARGET_ANDROID,
            AdvertisementTarget.ADVERTISEMENT_TARGET_IOS,
            AdvertisementTarget.ADVERTISEMENT_TARGET_SAMSUNG,
            AdvertisementTarget.ADVERTISEMENT_TARGET_WINDOWS,
            AdvertisementTarget.ADVERTISEMENT_TARGET_LOVESPOUSE,
            AdvertisementTarget.ADVERTISEMENT_TARGET_KITCHEN_SINK
        ).forEach(::setupAdvertisementTarget)
    }

    private fun setupAdvertisementTarget(advertisementTarget: AdvertisementTarget) {
        val itemBinding = ListitemAdvertisementCollectionStartBinding.inflate(
            layoutInflater,
            binding.advertisementCollectionListView,
            false
        )
        val (titleRes, targetRes, distanceRes) = when (advertisementTarget) {
            AdvertisementTarget.ADVERTISEMENT_TARGET_ANDROID -> Triple(
                R.string.collection_fast_pair_title,
                R.string.collection_target_android,
                R.string.collection_distance_close
            )
            AdvertisementTarget.ADVERTISEMENT_TARGET_IOS -> Triple(
                R.string.collection_continuity_title,
                R.string.collection_target_ios,
                R.string.collection_distance_mixed
            )
            AdvertisementTarget.ADVERTISEMENT_TARGET_SAMSUNG -> Triple(
                R.string.collection_easy_setup_title,
                R.string.collection_target_samsung,
                R.string.collection_distance_close
            )
            AdvertisementTarget.ADVERTISEMENT_TARGET_WINDOWS -> Triple(
                R.string.collection_swift_pair_title,
                R.string.collection_target_windows,
                R.string.collection_distance_close
            )
            AdvertisementTarget.ADVERTISEMENT_TARGET_LOVESPOUSE -> Triple(
                R.string.collection_lovespouse_title,
                R.string.collection_target_lovespouse,
                R.string.collection_distance_far
            )
            AdvertisementTarget.ADVERTISEMENT_TARGET_KITCHEN_SINK -> Triple(
                R.string.collection_kitchen_sink_title,
                R.string.collection_target_all,
                R.string.collection_distance_mixed
            )
            AdvertisementTarget.ADVERTISEMENT_TARGET_UNDEFINED -> return
        }

        itemBinding.listItemAdvertisementSetCollectionStartTitle.setText(titleRes)
        itemBinding.listItemAdvertisementSetCollectionStartTarget.setText(targetRes)
        itemBinding.listItemAdvertisementSetCollectionStartDistance.setText(distanceRes)
        itemBinding.listItemAdvertisementSetCollectionStartImage
            .setImageResource(advertisementTarget.getDrawableId())
        itemBinding.listItemAdvertisementSetCollectionStartCardview.setOnClickListener { view ->
            when (advertisementTarget) {
                AdvertisementTarget.ADVERTISEMENT_TARGET_ANDROID -> onFastPairCardViewClicked()
                AdvertisementTarget.ADVERTISEMENT_TARGET_IOS -> onContinuityCardViewClicked()
                AdvertisementTarget.ADVERTISEMENT_TARGET_SAMSUNG -> onEasySetupCardViewClicked()
                AdvertisementTarget.ADVERTISEMENT_TARGET_WINDOWS -> onSwiftPairingCardViewClicked()
                AdvertisementTarget.ADVERTISEMENT_TARGET_LOVESPOUSE -> onLovespouseCardViewClicked()
                AdvertisementTarget.ADVERTISEMENT_TARGET_KITCHEN_SINK ->
                    onKitchenSinkCardViewClicked(view.context)
                AdvertisementTarget.ADVERTISEMENT_TARGET_UNDEFINED -> Unit
            }
        }

        binding.advertisementCollectionListView.addView(itemBinding.root)
    }

    private fun navigateToAdvertisementFragmentWithType(
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

    private fun buildAdvertisementCollection(
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
                getString(R.string.collection_fast_pair_hint)
            )
        }

        if (advertisementSetTypes.contains(AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_IOS_17_CRASH)) {
            advertisementSetCollection.hints.add(
                getString(R.string.collection_continuity_hint)
            )
        }

        advertisementSetTypes.forEach { advertisementSetType ->
            val advertisementSetList = AdvertisementSetList()
            advertisementSetList.title = getString(advertisementSetType.stringResId())

            val advertisementSets =
                DatabaseHelpers.getAllAdvertisementSetsForType(advertisementSetType)
            advertisementSetList.advertisementSets = advertisementSets.toMutableList()
            advertisementSetCollection.advertisementSetLists.add(advertisementSetList)
        }

        return advertisementSetCollection
    }

    private fun onFastPairCardViewClicked() {
        navigateToAdvertisementFragmentWithType(
            listOf(
                AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_DEVICE,
                AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_PHONE_SETUP,
                AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_NON_PRODUCTION,
                AdvertisementSetType.ADVERTISEMENT_TYPE_FAST_PAIRING_DEBUG
            ),
            getString(R.string.collection_fast_pair_title)
        )
    }

    private fun onEasySetupCardViewClicked() {
        navigateToAdvertisementFragmentWithType(
            listOf(
                AdvertisementSetType.ADVERTISEMENT_TYPE_EASY_SETUP_WATCH,
                AdvertisementSetType.ADVERTISEMENT_TYPE_EASY_SETUP_BUDS
            ),
            getString(R.string.collection_easy_setup_title)
        )
    }

    private fun onContinuityCardViewClicked() {
        navigateToAdvertisementFragmentWithType(
            listOf(
                AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_NEW_DEVICE,
                AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_NOT_YOUR_DEVICE,
                AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_NEW_AIRTAG,
                AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_ACTION_MODALS,
                AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_IOS_17_CRASH
            ),
            getString(R.string.collection_continuity_title)
        )
    }

    private fun onSwiftPairingCardViewClicked() {
        navigateToAdvertisementFragmentWithType(
            listOf(AdvertisementSetType.ADVERTISEMENT_TYPE_SWIFT_PAIRING),
            getString(R.string.collection_swift_pair_title)
        )
    }

    private fun onLovespouseCardViewClicked() {
        navigateToAdvertisementFragmentWithType(
            listOf(
                AdvertisementSetType.ADVERTISEMENT_TYPE_LOVESPOUSE_PLAY,
                AdvertisementSetType.ADVERTISEMENT_TYPE_LOVESPOUSE_STOP
            ),
            getString(R.string.collection_lovespouse_title)
        )
    }

    private fun onKitchenSinkCardViewClicked(context: Context) {
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
            getString(R.string.collection_kitchen_sink_title)
        )
    }
}

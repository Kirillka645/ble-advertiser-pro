package io.github.kirillka645.bleadvertiserpro.ui.advertisement

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListAdapter
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.github.kirillka645.bleadvertiserpro.Adapters.AdvertisementSetCollectionExpandableListViewAdapter
import io.github.kirillka645.bleadvertiserpro.BleAdvertiserApplication
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementError
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementQueueMode
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementSetRange
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementState
import io.github.kirillka645.bleadvertiserpro.Enums.getDrawableId
import io.github.kirillka645.bleadvertiserpro.Enums.stringResId
import io.github.kirillka645.bleadvertiserpro.Helpers.BluetoothHelpers.Companion.bluetoothAdapter
import io.github.kirillka645.bleadvertiserpro.PermissionCheck.PermissionCheck
import io.github.kirillka645.bleadvertiserpro.Handlers.AdvertisementSetQueueHandler
import io.github.kirillka645.bleadvertiserpro.Interfaces.Callbacks.IAdvertisementServiceCallback
import io.github.kirillka645.bleadvertiserpro.Interfaces.Callbacks.IAdvertisementSetQueueHandlerCallback
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSet
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSetCollection
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSetList
import io.github.kirillka645.bleadvertiserpro.R
import io.github.kirillka645.bleadvertiserpro.databinding.FragmentAdvertisementBinding
import io.github.kirillka645.bleadvertiserpro.databinding.ViewAdvertisementHeaderBinding


class AdvertisementFragment : Fragment(), IAdvertisementServiceCallback, IAdvertisementSetQueueHandlerCallback {

    private val _logTag = "AdvertisementFragment"

    private var _viewModel: AdvertisementViewModel? = null
    private val viewModel get() = _viewModel!!

    private var _binding: FragmentAdvertisementBinding? = null
    private val binding get() = _binding!!
    private var _headerBinding: ViewAdvertisementHeaderBinding? = null
    private val headerBinding get() = _headerBinding!!

    private val expandableListView get() = binding.advertisementFragmentCollectionExpandableListview
    private var adapter: AdvertisementSetCollectionExpandableListViewAdapter? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewModel = ViewModelProvider(this)[AdvertisementViewModel::class.java]
        _binding = FragmentAdvertisementBinding.inflate(inflater, container, false)
        val root: View = binding.root

        _headerBinding = ViewAdvertisementHeaderBinding.inflate(inflater, expandableListView, false)
        expandableListView.addHeaderView(headerBinding.root, null, false)
        setupUi(root.context)

        return root
    }

    override fun onResume() {
        super.onResume()

        val context = requireContext()
        val queue = (context.applicationContext as BleAdvertiserApplication).queueHandler
        queue.addAdvertisementServiceCallback(this)
        queue.addAdvertisementQueueHandlerCallback(this)

        syncUiStateWithQueue(context, queue)
    }

    override fun onPause() {
        super.onPause()

        val app = (requireContext().applicationContext as BleAdvertiserApplication)
        app.queueHandler.removeAdvertisementServiceCallback(this)
        app.queueHandler.removeAdvertisementQueueHandlerCallback(this)
        //AppContext.getAdvertisementSetQueueHandler().deactivate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        expandableListView.setAdapter(null as ExpandableListAdapter?)
        adapter = null
        _headerBinding = null
        _binding = null
    }

    fun onPlayButtonClicked(context: Context) {
        val queue = (context.applicationContext as BleAdvertiserApplication).queueHandler
        if (viewModel.isAdvertising.value == true) {
            queue.deactivate(context)
        } else {
            if (canStartSession(context)) queue.activate(context)
        }
    }

    fun syncUiStateWithQueue(context: Context, queue: AdvertisementSetQueueHandler) {
        viewModel.advertisementQueueMode.postValue(queue.getAdvertisementQueueMode())
        viewModel.isAdvertising.postValue(queue.isActive())

        val collection = queue.getAdvertisementSetCollection()
        viewModel.advertisementSetCollectionTitle.postValue(collection.title)
        viewModel.advertisementSetCollectionSubTitle.postValue(getAdvertisementSetCollectionSubTitle(collection))
        viewModel.advertisementSetCollectionHint.postValue(getAdvertisementSetCollectionHint(collection))

        setupExpandableListView(context, collection)
    }

    fun getAdvertisementSetCollectionHint(collection: AdvertisementSetCollection): String =
        collection.hints.joinToString("\n")

    private fun setupExpandableListView(
        context: Context,
        advertisementSetCollection: AdvertisementSetCollection,
    ) {

        Log.d(_logTag, "Collection: " + advertisementSetCollection.advertisementSetLists.count())
        // Setup grouped Data
        var titleList = advertisementSetCollection.advertisementSetLists.toList()
        var dataList = HashMap<AdvertisementSetList, List<AdvertisementSet>>()
        advertisementSetCollection.advertisementSetLists.forEach{ advertisementSetList ->
            dataList[advertisementSetList] = advertisementSetList.advertisementSets
        }

        val listAdapter = AdvertisementSetCollectionExpandableListViewAdapter(context,titleList,dataList)
        adapter = listAdapter
        expandableListView.setAdapter(listAdapter)

        if(listAdapter.advertisementSetLists.isNotEmpty() && advertisementSetCollection.advertisementSetLists.size == 1){
            expandableListView.expandGroup(0)
        }

        expandableListView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            val app = (context.applicationContext as BleAdvertiserApplication)
            app.queueHandler.setSelectedAdvertisementSet(groupPosition, childPosition)

            var advertisementSet = dataList[titleList[groupPosition]]!![childPosition]
            highlightCurrentAdverstisementSet(advertisementSet, AdvertisementState.ADVERTISEMENT_STATE_UNDEFINED)
            false
        }
    }

    fun getAdvertisementSetCollectionSubTitle(collection: AdvertisementSetCollection): String =
        getString(R.string.session_count, collection.getTotalNumberOfAdvertisementSets(), collection.getNumberOfLists())

    fun getAdvertisementSetSubtitle(advertisementSet: AdvertisementSet): String {
        val range = when (advertisementSet.range) {
            AdvertisementSetRange.ADVERTISEMENTSET_RANGE_CLOSE -> R.string.session_range_close
            AdvertisementSetRange.ADVERTISEMENTSET_RANGE_MEDIUM -> R.string.session_range_medium
            AdvertisementSetRange.ADVERTISEMENTSET_RANGE_FAR -> R.string.session_range_far
            AdvertisementSetRange.ADVERTISEMENTSET_RANGE_UNKNOWN -> R.string.session_range_unknown
        }
        return getString(R.string.session_device_summary, getString(advertisementSet.type.stringResId()), getString(range))
    }

    private fun canStartSession(context: Context): Boolean {
        if (!PermissionCheck.getAllRelevantPermissions().all { PermissionCheck.checkPermission(it, context) }) return false
        return context.bluetoothAdapter()?.isEnabled == true
    }

    fun setAdvertisementQueueMode(advertisementQueueMode: AdvertisementQueueMode) {
        val app = (requireContext().applicationContext as BleAdvertiserApplication)
        app.queueHandler.setAdvertisementQueueMode(advertisementQueueMode)

        viewModel.advertisementQueueMode.postValue(advertisementQueueMode)
    }

    fun setupUi(context: Context) {

        // Views
        val playButton = binding.advertisementFragmentPlayButton
        val queueModeButtonLinear = headerBinding.advertisementFragmentQueueModeLinearButton
        val queueModeButtonRandom = headerBinding.advertisementFragmentQueueModeRandomButton

        // Listeners
        playButton.setOnClickListener {
            onPlayButtonClicked(playButton.context)
        }
        queueModeButtonLinear.setOnClickListener{
            setAdvertisementQueueMode(AdvertisementQueueMode.ADVERTISEMENT_QUEUE_MODE_LINEAR)
        }
        queueModeButtonRandom.setOnClickListener{
            setAdvertisementQueueMode(AdvertisementQueueMode.ADVERTISEMENT_QUEUE_MODE_RANDOM)
        }

        // Observers
        viewModel.isAdvertising.observe(viewLifecycleOwner) { isAdvertising ->
            playButton.setIconResource(if (isAdvertising) R.drawable.pause else R.drawable.play_arrow)
            playButton.setText(if (isAdvertising) R.string.session_pause else R.string.session_start)
            headerBinding.advertisementSessionIndicator.isVisible = isAdvertising
            val ready = isAdvertising || canStartSession(context)
            playButton.isEnabled = ready
            binding.sessionReadinessHint.isVisible = !ready
        }

        viewModel.target.observe(viewLifecycleOwner) { target ->
            binding.advertisementFragmentTargetImage.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources, target.getDrawableId(), context.theme
                )
            )
        }

        viewModel.advertisementSetCollectionTitle.observe(viewLifecycleOwner) { value ->
            headerBinding.advertisementFragmentCollectionTitle.text = value
        }
        viewModel.advertisementSetCollectionSubTitle.observe(viewLifecycleOwner) { value ->
            headerBinding.advertisementFragmentCollectionSubtitle.text = value
        }
        viewModel.advertisementSetCollectionHint.observe(viewLifecycleOwner) { value ->
            headerBinding.advertisementFragmentCollectionHint.text = value
            headerBinding.advertisementFragmentCollectionHint.isVisible = !value.isNullOrBlank()
        }
        viewModel.advertisementSetTitle.observe(viewLifecycleOwner) { value ->
            binding.advertisementFragmentCurrentSetTitle.text =
                value.takeUnless { it.isBlank() || it == "-" } ?: getString(R.string.session_ready)
        }
        viewModel.advertisementSetSubTitle.observe(viewLifecycleOwner) { value ->
            binding.advertisementFragmentCurrentSetSubTitle.text =
                value.takeUnless { it.isBlank() || it == "-" } ?: getString(R.string.session_selection_hint)
        }

        viewModel.advertisementQueueMode.observe(viewLifecycleOwner) { mode ->
            headerBinding.advertisementQueueMode.check(when (mode) {
                AdvertisementQueueMode.ADVERTISEMENT_QUEUE_MODE_LINEAR -> queueModeButtonLinear.id
                AdvertisementQueueMode.ADVERTISEMENT_QUEUE_MODE_RANDOM -> queueModeButtonRandom.id
            })
        }
    }

    fun highlightCurrentAdverstisementSet(currentAdvertisementSet: AdvertisementSet, advertisementState: AdvertisementState) {
        activity?.runOnUiThread {
            if (_binding == null) return@runOnUiThread
            val listAdapter = adapter ?: return@runOnUiThread
            listAdapter.advertisementSetLists.forEach { list ->
                list.currentlyAdvertising = false
                list.advertisementSets.forEach { set ->
                    set.currentlyAdvertising = set == currentAdvertisementSet
                    if (set.currentlyAdvertising) {
                        set.advertisementState = advertisementState
                        list.currentlyAdvertising = true
                    }
                }
            }
            listAdapter.notifyDataSetChanged()
        }
    }

    // AdvertismentServiceCallback
    override fun onAdvertisementSetStart(advertisementSet: AdvertisementSet?) {
        Log.d(_logTag, "onAdvertisementSetStart ${advertisementSet?.title}")
        if (advertisementSet != null && isAdded) {
            viewModel.target.postValue(advertisementSet.target)
            viewModel.advertisementSetTitle.postValue(advertisementSet.title)
            viewModel.advertisementSetSubTitle.postValue(getAdvertisementSetSubtitle(advertisementSet))
            highlightCurrentAdverstisementSet(advertisementSet, AdvertisementState.ADVERTISEMENT_STATE_STARTED)
        }
    }

    override fun onAdvertisementSetStop(advertisementSet: AdvertisementSet?) {
        Log.d(_logTag, "onAdvertisementSetStop")
    }

    override fun onAdvertisementSetSucceeded(advertisementSet: AdvertisementSet?) {
        if(advertisementSet != null){
            highlightCurrentAdverstisementSet(advertisementSet, AdvertisementState.ADVERTISEMENT_STATE_SUCCEEDED)
        }
    }

    override fun onAdvertisementSetFailed(advertisementSet: AdvertisementSet?, advertisementError: AdvertisementError) {
        if(advertisementSet != null){
            highlightCurrentAdverstisementSet(advertisementSet, AdvertisementState.ADVERTISEMENT_STATE_FAILED)
            activity?.runOnUiThread {
                if (_binding != null) Toast.makeText(requireContext(), getString(R.string.session_failed, advertisementError), Toast.LENGTH_SHORT).show()
            }
        }
    }
    // END: AdvertismentServiceCallback

    override fun onQueueHandlerActivated() {
        Log.d(_logTag, "onQueueHandlerActivated")
        viewModel.isAdvertising.postValue(true)
    }

    override fun onQueueHandlerDeactivated() {
        Log.d(_logTag, "onQueueHandlerDeactivated")
        viewModel.isAdvertising.postValue(false)
    }
}

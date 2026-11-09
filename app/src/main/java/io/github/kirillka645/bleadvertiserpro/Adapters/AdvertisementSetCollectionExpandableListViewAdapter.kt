package io.github.kirillka645.bleadvertiserpro.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.color.MaterialColors
import io.github.kirillka645.bleadvertiserpro.Enums.AdvertisementState
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSet
import io.github.kirillka645.bleadvertiserpro.Models.AdvertisementSetList
import io.github.kirillka645.bleadvertiserpro.R
import io.github.kirillka645.bleadvertiserpro.databinding.ListItemAdvertisementListBinding
import io.github.kirillka645.bleadvertiserpro.databinding.ListItemAdvertisementSetBinding

class AdvertisementSetCollectionExpandableListViewAdapter internal constructor(
    private val context: Context,
    val advertisementSetLists: List<AdvertisementSetList>,
    val dataList: HashMap<AdvertisementSetList, List<AdvertisementSet>>,
) : BaseExpandableListAdapter() {
    private val inflater = LayoutInflater.from(context)

    override fun getChild(groupPosition: Int, childPosition: Int): AdvertisementSet =
        dataList.getValue(advertisementSetLists[groupPosition])[childPosition]

    override fun getChildId(groupPosition: Int, childPosition: Int) = childPosition.toLong()
    override fun getChildrenCount(groupPosition: Int) = dataList.getValue(advertisementSetLists[groupPosition]).size
    override fun getGroup(groupPosition: Int) = advertisementSetLists[groupPosition]
    override fun getGroupCount() = advertisementSetLists.size
    override fun getGroupId(groupPosition: Int) = groupPosition.toLong()
    override fun hasStableIds() = false
    override fun isChildSelectable(groupPosition: Int, childPosition: Int) = true

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, view: View?, parent: ViewGroup): View {
        val row = view ?: ListItemAdvertisementSetBinding.inflate(inflater, parent, false).let { binding ->
            binding.root.apply { tag = RowHolder(binding.listItemAdvertisementSetTextView, binding.checkboxAdvertisementSet) }
        }
        val holder = row.tag as RowHolder
        val item = getChild(groupPosition, childPosition)
        val state = if (item.currentlyAdvertising) when (item.advertisementState) {
            AdvertisementState.ADVERTISEMENT_STATE_SUCCEEDED -> R.string.selection_succeeded
            AdvertisementState.ADVERTISEMENT_STATE_FAILED -> R.string.selection_failed
            AdvertisementState.ADVERTISEMENT_STATE_STARTED -> R.string.selection_active
            else -> R.string.selection_selected
        } else null
        holder.label.text = if (state == null) item.title else "${item.title} · ${context.getString(state)}"
        val color = when {
            item.currentlyAdvertising && item.advertisementState == AdvertisementState.ADVERTISEMENT_STATE_FAILED -> com.google.android.material.R.attr.colorError
            item.currentlyAdvertising -> com.google.android.material.R.attr.colorPrimary
            else -> com.google.android.material.R.attr.colorOnSurface
        }
        holder.label.setTextColor(MaterialColors.getColor(row, color))
        ViewCompat.setStateDescription(row, state?.let(context::getString))
        holder.checkbox.contentDescription = context.getString(R.string.selection_device, item.title)
        holder.checkbox.isChecked = item.isChecked
        holder.checkbox.setOnClickListener {
            item.isChecked = holder.checkbox.isChecked
            notifyDataSetChanged()
        }
        return row
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, view: View?, parent: ViewGroup): View {
        val row = view ?: ListItemAdvertisementListBinding.inflate(inflater, parent, false).let { binding ->
            binding.root.apply { tag = RowHolder(binding.listItemAdvertisementSetList, binding.checkboxAdvertisementList) }
        }
        val holder = row.tag as RowHolder
        val group = getGroup(groupPosition)
        holder.label.text = group.title
        holder.label.setTextColor(MaterialColors.getColor(row,
            if (group.currentlyAdvertising) com.google.android.material.R.attr.colorPrimary
            else com.google.android.material.R.attr.colorOnSurface
        ))
        ViewCompat.setStateDescription(row, context.getString(
            if (isExpanded) R.string.selection_collapse else R.string.selection_expand
        ))
        val children = dataList.getValue(group)
        val allChecked = children.isNotEmpty() && children.all { it.isChecked }
        holder.checkbox.contentDescription = context.getString(R.string.selection_group, group.title)
        val checkbox = holder.checkbox
        if (checkbox is MaterialCheckBox) {
            checkbox.checkedState = when {
                allChecked -> MaterialCheckBox.STATE_CHECKED
                children.any { it.isChecked } -> MaterialCheckBox.STATE_INDETERMINATE
                else -> MaterialCheckBox.STATE_UNCHECKED
            }
        } else {
            checkbox.isChecked = allChecked
        }
        checkbox.setOnClickListener {
            children.forEach { it.isChecked = checkbox.isChecked }
            notifyDataSetChanged()
        }
        return row
    }

    private data class RowHolder(val label: TextView, val checkbox: CheckBox)
}

package com.dacosys.assetControl.ui.adapters.route

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.dacosys.assetControl.R
import com.dacosys.assetControl.data.room.dto.dataCollection.DataCollectionContent

class DccAdapter(
    context: Context,
    private val dataSource: List<DataCollectionContent>
) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = dataSource.size

    override fun getItem(position: Int): Any = dataSource[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: inflater.inflate(R.layout.data_collection_content_row, parent, false)

        val item = getItem(position) as DataCollectionContent

        view.findViewById<TextView>(R.id.ruleTextView).text = item.dataCollectionRuleContentStr
        view.findViewById<TextView>(R.id.dateTextView).text = item.dataCollectionDate.toString()
        view.findViewById<TextView>(R.id.compositionAutoSize).text = item.attributeCompositionStr
        view.findViewById<TextView>(R.id.valueAutoSize).text = item.valueStr

        return view
    }
}
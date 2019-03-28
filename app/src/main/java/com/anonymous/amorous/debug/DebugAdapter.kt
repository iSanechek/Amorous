package com.anonymous.amorous.debug

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.anonymous.amorous.R
import com.anonymous.amorous.data.models.Candidate

class DebugAdapter(private val items: List<Candidate>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var v: View? = convertView
        var h: ViewHolder? = null
        if (v == null) {
            val inflater = LayoutInflater.from(parent!!.context)
            v = inflater.inflate(R.layout.debug_list_item_layout, parent, false)
            h = ViewHolder()
            h.cover = v.findViewById(R.id.debug_list_item_iv)
            h.tv = v.findViewById(R.id.debug_list_item_tv)
            v?.tag = h
        } else {
            h = v.tag as ViewHolder
        }
//        h.tv.text = items[position].size
//        h.cover.setImageBitmap(ScannerUtils.getVideoThumbnail(items[position].originalPath))
        return v!!
    }

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = items[position].uid?.toLong() ?: 0L

    override fun getCount(): Int = items.size

    inner class ViewHolder {
        lateinit var tv: TextView
        lateinit var cover: ImageView
    }

}
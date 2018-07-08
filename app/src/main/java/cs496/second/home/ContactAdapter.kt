package cs496.second.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import cs496.second.R
import java.util.*
import java.util.ArrayList
import java.util.Collections

class ContactAdapter(internal var mContext: Context, resource: Int, private val filtered: MutableList<String>) : BaseAdapter() {
    internal var mInflater: LayoutInflater
    private val dataset: MutableList<String>

    init {
        this.mInflater = LayoutInflater.from(mContext)
        this.dataset = ArrayList()
        dataset.addAll(filtered)
        Collections.sort(dataset)
        filter("")
    }


    override fun getCount(): Int {
        return filtered.size
    }

    override fun getItem(i: Int): Any {
        return filtered[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, convertView: View?, viewGroup: ViewGroup): View {
        var convertView = convertView

        val text_name: TextView
        val img_person: ImageView
        val phone_number: TextView

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.contact_item, viewGroup, false)
        }
        text_name = convertView!!.findViewById<View>(R.id.tv_name) as TextView
        img_person = convertView.findViewById<View>(R.id.iv_profile) as ImageView
        text_name.text = getItem(i) as String
        img_person.setImageResource(R.mipmap.profilepic)
        return convertView

    }

    fun filter(str: String) {
        filtered.clear()
        if (str.length == 0) {
            filtered.addAll(dataset)
        } else {
            for (item in dataset) {
                if (item.toLowerCase().contains(str)) {
                    filtered.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

}
package tk.ifroz.loctrackcar.ui.adapter

import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import tk.ifroz.loctrackcar.R
import tk.ifroz.loctrackcar.ui.adapter.ItemAdapter.ItemViewHolder

class ItemAdapter(private val itemList: List<Item>) : Adapter<ItemViewHolder>() {

    inner class ItemViewHolder(parent: View) : ViewHolder(parent) {
        var title: TextView = parent.findViewById(R.id.title_tv)
        var subtitle: TextView = parent.findViewById(R.id.subtitle_tv)
        var icon: ImageView = parent.findViewById(R.id.icon_iv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            from(parent.context).inflate(R.layout.row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val row = itemList[position]
        holder.title.text = row.title
        holder.subtitle.text = row.subtitle
        row.imageId?.let {
            holder.icon.setImageResource(it)
        }
    }

    override fun getItemCount(): Int = itemList.size
}
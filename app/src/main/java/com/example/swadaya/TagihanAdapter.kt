import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.swadaya.R
import com.example.swadaya.Tagihan

class TagihanAdapter(
    private var items: List<Tagihan>,
    private val onClick: (Tagihan) -> Unit
) : RecyclerView.Adapter<TagihanAdapter.TagihanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagihanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tagihan, parent, false)
        return TagihanViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagihanViewHolder, position: Int) {
        holder.bind(items[position], onClick)
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<Tagihan>) {
        items = newItems
        notifyDataSetChanged()
    }

    class TagihanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaClient: TextView = itemView.findViewById(R.id.tvNamaClient)
        private val tvKodeClient: TextView = itemView.findViewById(R.id.tvKodeClient)
        private val tvNomorMeter: TextView = itemView.findViewById(R.id.tvNomorMeter)

        fun bind(tagihan: Tagihan, onClick: (Tagihan) -> Unit) {
            tvNamaClient.text = "Nama: ${tagihan.namaClient}"
            tvKodeClient.text = "Kode: ${tagihan.kodeClient}"
            tvNomorMeter.text = "Nomor Meter: ${tagihan.nomorMeter}"

            itemView.setOnClickListener {
                onClick(tagihan)
            }
        }
    }
}



package com.utec.appmovil.modelo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utec.appmovil.R
import com.utec.appmovil.api.ReclamoData
import com.utec.appmovil.api.ReclamoResponse

class ReclamosAdapter(private var reclamos: List<ReclamoData> = emptyList(), private val itemSeleccionado: (Int) -> Unit) :
    RecyclerView.Adapter<ReclamosViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReclamosViewHolder {
        return ReclamosViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_reclamo, parent, false))
    }

    override fun getItemCount(): Int = reclamos.size

    override fun onBindViewHolder(holder: ReclamosViewHolder, position: Int) {
        holder.render(reclamos[position], itemSeleccionado)
    }

    fun actualizarLista(reclamos: List<ReclamoData>) {
        this.reclamos = reclamos
        notifyDataSetChanged()
    }
}
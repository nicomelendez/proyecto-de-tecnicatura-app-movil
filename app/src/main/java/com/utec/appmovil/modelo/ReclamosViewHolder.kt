package com.utec.appmovil.modelo

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.utec.appmovil.R
import com.utec.appmovil.api.ReclamoData
import com.utec.appmovil.databinding.ItemReclamoBinding

class ReclamosViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemReclamoBinding.bind(view)

    fun render(reclamo: ReclamoData, itemSeleccionado: (Int) -> Unit) {
        // (with) para evitar la repetición del binding
        with(binding) {
            tvTitulo.text = reclamo.detalle
            tvDetalle.text = reclamo.estadosReclamos.firstOrNull()?.detalle.orEmpty()
            tvFecha.text = reclamo.ultimaFecha
            tvTituloEvento.text = reclamo.evento.titulo

            reclamo.estadosReclamos.lastOrNull()?.let { ultimoEstadoReclamo ->
                tvEstado.text = ultimoEstadoReclamo.estado.descripcion
                val colorRes = when (ultimoEstadoReclamo.estado.idEstado) {
                    1 -> R.color.red
                    2 -> R.color.yellow
                    3 -> R.color.green
                    else -> R.color.black
                }
                tvEstado.setTextColor(ContextCompat.getColor(itemView.context, colorRes))
            }
        }

        // Obtengo la ID del reclamo seleccionado
        itemView.setOnClickListener { itemSeleccionado(reclamo.idReclamo) }
    }
}
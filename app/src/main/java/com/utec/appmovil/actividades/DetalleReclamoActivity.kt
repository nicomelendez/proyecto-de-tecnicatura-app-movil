package com.utec.appmovil.actividades

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.JsonObject
import com.utec.appmovil.R
import com.utec.appmovil.api.ApiService
import com.utec.appmovil.api.EventoData
import com.utec.appmovil.api.ReclamoData
import com.utec.appmovil.api.ReclamoResponse
import com.utec.appmovil.api.UserData
import com.utec.appmovil.api.getRetrofit
import com.utec.appmovil.databinding.ActivityDetalleReclamoBinding
import com.utec.appmovil.databinding.DialogCrearReclamoBinding
import com.utec.appmovil.obtenerIconoColor
import com.utec.appmovil.recuperarUsuario
import com.utec.appmovil.validarCamposReclamo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class DetalleReclamoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleReclamoBinding
    private var usuario: UserData? = null
    private var listaEventos: List<EventoData> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetalleReclamoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idReclamo: Int = intent.getIntExtra("extra_id", 0)

        usuario = recuperarUsuario(this)

        if (usuario != null) {
            mostrarDetalle(idReclamo)
            initListeners(idReclamo)
        }
    }


    private fun mostrarDetalle(idReclamo: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val detalleReclamo: ReclamoData? = getRetrofit().create(ApiService::class.java).getReclamo(idReclamo).body()

            runOnUiThread {
                if (detalleReclamo != null) {
                    initUI(detalleReclamo)
                }
            }
        }
    }

    private fun initUI(reclamo: ReclamoData) {
        with(binding) {

            if (usuario!!.rol.descripcion == "Estudiante") {
                tvEstudiante.visibility = View.GONE

                if (reclamo.estadosReclamos.last().estado.descripcion == "Ingresado") {
                    contenedorBotones.visibility = View.VISIBLE
                }
            }

            if (reclamo.estadosReclamos.last().estado.descripcion == "Ingresado") {
                contenedorNotaAnalista.visibility = View.GONE
            }

            tvEstudiante.text = "Estudiante: ${reclamo.estudiante.oUsuario.nombreCompleto}"
            tvTituloReclamo.text = reclamo.detalle
            tvDetalle.text = reclamo.estadosReclamos.first().detalle
            tvFecha.text = reclamo.ultimaFecha
            tvTituloEvento.text = reclamo.evento.titulo
            tvNotaAnalista.text = reclamo.estadosReclamos.last().detalle

            reclamo.estadosReclamos.lastOrNull()?.let { ultimoEstadoReclamo ->
                tvEstado.text = ultimoEstadoReclamo.estado.descripcion
                val colorRes = when (ultimoEstadoReclamo.estado.idEstado) {
                    1 -> R.color.red
                    2 -> R.color.yellow
                    3 -> R.color.green
                    else -> R.color.black
                }
                tvEstado.setTextColor(ContextCompat.getColor(this@DetalleReclamoActivity, colorRes))
            }
        }
    }

    private fun initListeners(idReclamo: Int) {
        with(binding) {
            btnEliminar.setOnClickListener { eliminarReclamo(idReclamo) }
            btnEditar.setOnClickListener { editarReclamo(idReclamo) }
        }
    }

    private fun eliminarReclamo(idReclamo: Int) {
        AlertDialog.Builder(this)
            .setIcon(obtenerIconoColor(this, R.drawable.ic_warning, R.color.yellow))
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar este reclamo?")
            .setPositiveButton("Eliminar") { dialog, which ->
                // Eliminamos si el usuario confirma
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = getRetrofit().create(ApiService::class.java).deleteReclamo(idReclamo)

                        if (response.isSuccessful) {
                            runOnUiThread {
                                Toast.makeText(this@DetalleReclamoActivity, "Reclamo eliminado correctamente.", Toast.LENGTH_SHORT).show()
                                onBackPressedDispatcher.onBackPressed()
                            }
                        } else {
                            // Manejamos de errores si la respuesta no es exitosa
                            runOnUiThread {
                                Toast.makeText(this@DetalleReclamoActivity, "Error al eliminar reclamo: ${response.message()}", Toast.LENGTH_LONG).show()
                                onBackPressedDispatcher.onBackPressed()
                            }
                        }
                    } catch (e: Exception) {
                        // Capturamos excepciones en caso de fallos en la llamada a la API
                        runOnUiThread {
                            Toast.makeText(this@DetalleReclamoActivity, "Error de conexión: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null) // No se realiza ninguna acción si el usuario cancela
            .show()
    }

    private fun editarReclamo(idReclamo: Int) {
        val inflater = LayoutInflater.from(this)
        val dialogoBinding = DialogCrearReclamoBinding.inflate(inflater)
        cargarDatosReclamo(idReclamo, dialogoBinding)
    }

    private fun cargarDatosReclamo(idReclamo: Int, dialogoBinding: DialogCrearReclamoBinding) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = getRetrofit().create(ApiService::class.java).getReclamo(idReclamo)
            if (response.isSuccessful) {
                val reclamoActual = response.body()!!
                runOnUiThread {
                    configurarVistaReclamo(dialogoBinding, reclamoActual)
                }
            }
        }
    }

    private fun configurarVistaReclamo(dialogoBinding: DialogCrearReclamoBinding, reclamoActual: ReclamoData) {
        dialogoBinding.etTitulo.setText(reclamoActual.detalle)
        dialogoBinding.etDetalle.setText(reclamoActual.estadosReclamos.first().detalle)
        configurarSpinnerEventos(dialogoBinding, reclamoActual)
        mostrarDialogoEdicion(dialogoBinding, reclamoActual.idReclamo)
    }

    private fun configurarSpinnerEventos(dialogoBinding: DialogCrearReclamoBinding, reclamoActual: ReclamoData) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = getRetrofit().create(ApiService::class.java).getEventos()
                if (response.isSuccessful) {
                    val listaEventosResponse = response.body() ?: emptyList()
                    listaEventos = listaEventosResponse

                    runOnUiThread {
                        if (listaEventos.isNotEmpty()) {
                            // Si hay eventos, llenamos y habilitamos el spinner
                            val eventosNombres = listaEventos.map { it.titulo }
                            val adaptador = ArrayAdapter(this@DetalleReclamoActivity, android.R.layout.simple_spinner_item, eventosNombres)
                            adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            dialogoBinding.spinnerEventos.adapter = adaptador
                            // Establecemos la selección del spinner según el evento actual del reclamo
                            val posicionEvento = listaEventos.indexOfFirst { it.idEvento == reclamoActual.evento.idEvento }
                            if (posicionEvento != -1) {
                                dialogoBinding.spinnerEventos.setSelection(posicionEvento)
                            }
                            dialogoBinding.spinnerEventos.isEnabled = true
                        } else {
                            // Si no hay eventos, deshabilitamos el spinner
                            dialogoBinding.spinnerEventos.adapter = null
                            dialogoBinding.spinnerEventos.isEnabled = false
                        }
                    }
                } else {
                    // Manejamos la respuesta no exitosa
                    Log.e("ApiError", "Error al obtener eventos: ${response.code()}")
                }
            } catch (e: Exception) {
                // Cualquier otro error
                Log.e("ApiError", "Error al obtener eventos", e)
            }
        }
    }

    private fun mostrarDialogoEdicion(dialogoBinding: DialogCrearReclamoBinding, idReclamo: Int) {
        val dialogo = AlertDialog.Builder(this@DetalleReclamoActivity)
            .setIcon(obtenerIconoColor(this, R.drawable.ic_edit, R.color.yellow))
            .setTitle("Editar reclamo")
            .setView(dialogoBinding.root)
            .setPositiveButton("Editar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialogo.setOnShowListener {
            val boton: Button = dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
            boton.setOnClickListener {
                if (validarCamposReclamo(this, dialogoBinding)) {
                    confirmarActualizacion(dialogoBinding, idReclamo)
                    dialogo.dismiss() // Solo se cierra el diálogo si la validación de campos es true ✅
                }
            }
        }

        dialogo.show()
    }

    private fun confirmarActualizacion(dialogoBinding: DialogCrearReclamoBinding, idReclamo: Int) {
        val titulo = dialogoBinding.etTitulo.text.toString().trim()
        val detalle = dialogoBinding.etDetalle.text.toString().trim()

        AlertDialog.Builder(this@DetalleReclamoActivity)
            .setIcon(obtenerIconoColor(this, R.drawable.ic_warning, R.color.yellow))
            .setTitle("Confirmar edición")
            .setMessage("¿Estás seguro de que quieres editar este reclamo?")
            .setPositiveButton("Sí") { _, _ ->
                actualizarReclamo(dialogoBinding, idReclamo)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun actualizarReclamo(dialogoBinding: DialogCrearReclamoBinding, idReclamo: Int) {
        val titulo = dialogoBinding.etTitulo.text.toString().trim()
        val detalle = dialogoBinding.etDetalle.text.toString().trim()
        val eventoPosicion = dialogoBinding.spinnerEventos.selectedItemPosition
        // Obtenemos el ID del evento seleccionado mediante la posición en la lista del spinner
        val idEvento = listaEventos[eventoPosicion].idEvento

        val reclamo = JsonObject().apply {
            addProperty("idReclamo", idReclamo)
            addProperty("titulo", titulo)
            addProperty("detalle", detalle)
            addProperty("idEvento", idEvento)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val updateResponse = getRetrofit().create(ApiService::class.java).updateReclamo(reclamo)
            runOnUiThread {
                if (updateResponse.isSuccessful) {
                    Toast.makeText(this@DetalleReclamoActivity, "El reclamo ha sido editado exitosamente.", Toast.LENGTH_LONG).show()
                    // Refrezcamos la actividad actual para mostrar los datos actualizados
                    finish()
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                } else {
                    // Si la respuesta no es exitosa
                    val errorBody = updateResponse.errorBody()?.string()
                    Toast.makeText(this@DetalleReclamoActivity, "Error al editar el reclamo: $errorBody", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}
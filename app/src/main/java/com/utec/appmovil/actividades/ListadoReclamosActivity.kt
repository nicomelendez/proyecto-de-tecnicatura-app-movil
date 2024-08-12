package com.utec.appmovil.actividades

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.utec.appmovil.R
import com.utec.appmovil.modelo.ReclamosAdapter
import com.utec.appmovil.api.ApiService
import com.utec.appmovil.api.EventoData
import com.utec.appmovil.api.ReclamoResponse
import com.utec.appmovil.api.UserData
import com.utec.appmovil.api.getRetrofit
import com.utec.appmovil.databinding.ActivityListadoReclamosBinding
import com.utec.appmovil.databinding.DialogCrearReclamoBinding
import com.utec.appmovil.mostrarDialogo
import com.utec.appmovil.obtenerIconoColor
import com.utec.appmovil.validarCamposReclamo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class ListadoReclamosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListadoReclamosBinding
    private lateinit var preferencias: SharedPreferences.Editor
    private lateinit var reclamosAdapter: ReclamosAdapter
    private var usuario: UserData? = null
    private var listaEventos: List<EventoData> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListadoReclamosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        guardarUsuario()
        initComponents()
        initListeners()

        if (usuario != null) {
            listarReclamos()
            mostrarCrearReclamo(usuario!!.rol.descripcion)
        } else {
            Log.i("Usuario", "El usuario es nulo")
            return
        }
    }

    private fun guardarUsuario() {
        usuario = intent.getSerializableExtra("usuario") as? UserData

        if (usuario != null) {
            val gson = Gson()
            val usuarioJson = gson.toJson(usuario)

            preferencias = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            preferencias.putString("usuario", usuarioJson)
            preferencias.apply()

            bienvenidaPorGenero(usuario!!)
        }
    }

    private fun cerrarSesion() {
        preferencias.clear()
        preferencias.apply()

        onBackPressedDispatcher.onBackPressed()
    }

    private fun initComponents() {
        reclamosAdapter = ReclamosAdapter() { idReclamo -> navegarADetalle(idReclamo) }
        binding.rvListadoReclamos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvListadoReclamos.adapter = reclamosAdapter
    }

    private fun initListeners() {
        with(binding) {
            btnCerrarSesion.setOnClickListener { cerrarSesion() }
            fabCrearReclamo.setOnClickListener { crearReclamo() }
        }
    }

    private fun listarReclamos() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = withContext(Dispatchers.IO) {
                when (usuario!!.rol.descripcion) {
                    "Analista" -> getRetrofit().create(ApiService::class.java).getReclamos()
                    "Estudiante" -> getRetrofit().create(ApiService::class.java).getMisReclamos(usuario!!.idRol)
                    else -> null
                }
            }

            response?.let { resp ->
                if (resp.isSuccessful) {
                    val listaReclamos = resp.body()?.reversed() // Invertimos la lista
                    Log.i("Lista", listaReclamos.toString())

                    listaReclamos?.let {
                        runOnUiThread {
                            reclamosAdapter.actualizarLista(it)
                        }
                    } ?: runOnUiThread {
                        // Manejo en caso de lista nula
                        Log.i("Listado", "Lista de reclamos vacía o nula.")
                    }
                } else {
                    runOnUiThread {
                        // Acción en caso de respuesta fallida
                        Log.e("Listado", "Error en la respuesta: ${resp.message()}")
                    }
                }
            } ?: runOnUiThread {
                // Acción en caso de que response sea null
                Log.e("Listado", "Error obteniendo los reclamos, response es null")
            }
        }
    }

    private fun crearReclamo() {
        val inflater = LayoutInflater.from(this)
        val dialogoBinding = DialogCrearReclamoBinding.inflate(inflater)

        configurarSpinnerEventos(dialogoBinding)
        mostrarDialogoReclamo(dialogoBinding)
    }

    private fun configurarSpinnerEventos(dialogoBinding: DialogCrearReclamoBinding) {
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
                            val adaptador = ArrayAdapter(this@ListadoReclamosActivity, android.R.layout.simple_spinner_item, eventosNombres)
                            adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            dialogoBinding.spinnerEventos.adapter = adaptador
                            dialogoBinding.spinnerEventos.isEnabled = true
                        } else {
                            // Si no hay eventos, deshabilitamos el spinner
                            dialogoBinding.spinnerEventos.adapter = null
                            dialogoBinding.spinnerEventos.isEnabled = false
                        }
                    }
                } else {
                    // Maneja la respuesta no exitosa si es necesario
                    runOnUiThread {
                        Toast.makeText(this@ListadoReclamosActivity, "Error al obtener eventos: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("ApiError", "Error al obtener eventos: ${response.code()}")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ListadoReclamosActivity, "Error al conectar con el servidor.", Toast.LENGTH_SHORT).show()
                }
                Log.e("ApiError", "Error al conectar con el servidor", e)
            }
        }
    }

    private fun mostrarDialogoReclamo(dialogoBinding: DialogCrearReclamoBinding) {
        val dialogo = AlertDialog.Builder(this)
            .setTitle("Crear reclamo")
            .setView(dialogoBinding.root)
            .setIcon(obtenerIconoColor(this, R.drawable.ic_add, R.color.green))
            .setPositiveButton("Enviar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialogo.setOnShowListener {
            val boton: Button = dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
            boton.setOnClickListener {
                if (validarCamposReclamo(this, dialogoBinding)) {
                    enviarReclamo(dialogoBinding)
                    dialogo.dismiss() // Solo se cierra el diálogo si la validación de campos es true ✅
                }
            }
        }

        dialogo.show()
    }

    private fun enviarReclamo(dialogoBinding: DialogCrearReclamoBinding) {
        val titulo = dialogoBinding.etTitulo.text.toString().trim()
        val detalle = dialogoBinding.etDetalle.text.toString().trim()
        val eventoPosicion = dialogoBinding.spinnerEventos.selectedItemPosition
        // Obtenemos el ID del evento seleccionado mediante la posición en la lista del spinner
        val idEvento = listaEventos[eventoPosicion].idEvento

        val reclamo = JsonObject().apply {
            addProperty("titulo", titulo)
            addProperty("detalle", detalle)
            addProperty("documento", usuario?.documento.toString())
            addProperty("idEvento", idEvento)
        }

        CoroutineScope(Dispatchers.IO).launch {
            procesarEnvioReclamo(reclamo)
        }
    }

    // Función para llamar a la API y realizar la creación del reclamo (por eso es suspend)
    private suspend fun procesarEnvioReclamo(reclamo: JsonObject) {
        try {
            val response: Response<ReclamoResponse> = getRetrofit().create(ApiService::class.java).createReclamo(reclamo)

            runOnUiThread {
                if (response.isSuccessful) {
                    Toast.makeText(this@ListadoReclamosActivity, "Se ha creado exitosamente el reclamo.", Toast.LENGTH_LONG).show()
                    listarReclamos()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@ListadoReclamosActivity, "Error al crear el reclamo: $errorBody", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this@ListadoReclamosActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarCrearReclamo(rol: String) {
        if (rol == "Estudiante") {
            binding.fabCrearReclamo.visibility = View.VISIBLE
        }
    }

    private fun navegarADetalle(idReclamo: Int) {
        val reclamoDetalleIntent = Intent(this, DetalleReclamoActivity::class.java).apply {
            putExtra("extra_id", idReclamo)
        }
        startActivity(reclamoDetalleIntent)
    }

    private fun bienvenidaPorGenero(usuario: UserData) {
        when (usuario.genero.idGenero) {
            1 -> binding.tvNombreUsuario.text = "Bienvenido, ${usuario.nombreCompleto}!"
            2 -> binding.tvNombreUsuario.text = "Bienvenida, ${usuario.nombreCompleto}!"
            in 3..Int.MAX_VALUE -> binding.tvNombreUsuario.text = "Bienvenide, ${usuario.nombreCompleto}!"
            else -> mostrarDialogo(this@ListadoReclamosActivity, "Error", "No se pudo mostrar el cartel de bienvenida.", R.drawable.ic_delete, R.color.red)
        }
    }

    override fun onResume() {
        super.onResume()
        listarReclamos()
    }
}
package com.utec.appmovil.actividades

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.utec.appmovil.R
import com.utec.appmovil.api.ApiService
import com.utec.appmovil.api.LoginResponse
import com.utec.appmovil.api.UserData
import com.utec.appmovil.api.getRetrofit
import com.utec.appmovil.databinding.ActivityLoginBinding
import com.utec.appmovil.mostrarDialogo
import com.utec.appmovil.recuperarUsuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferencias: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recupera el usuario y, si es no nulo, navega según su rol
        recuperarUsuario(this)?.let { usuario -> navegarPorRol(usuario) }

        iniciarSesion()
    }

    private fun iniciarSesion() {
        binding.btnIniciarSesion.setOnClickListener {
            val nombreUsuario: String = binding.etNombreUsuario.text.toString()
            val clave: String = binding.etClave.text.toString()

            if (nombreUsuario.trim().isEmpty() || nombreUsuario.trim().isEmpty()) {
                mostrarDialogo(this@LoginActivity, "Error", "Por favor, complete todos los campos para iniciar sesión.", R.drawable.ic_delete, R.color.red)
                return@setOnClickListener
            }

            mostrarCargando(true)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response: Response<LoginResponse> = getRetrofit().create(ApiService::class.java).login(nombreUsuario, clave)

                    if (response.isSuccessful) {
                        val miResponse: LoginResponse? = response.body()
                        val usuario: UserData? = response.body()?.data

                        if (miResponse != null && miResponse.status != "success") {
                            runOnUiThread {
                                mostrarDialogo(this@LoginActivity, "Error", miResponse.message, R.drawable.ic_delete, R.color.red)
                                mostrarCargando(false)
                                return@runOnUiThread
                            }
                        }

                        if (usuario != null) {
                            runOnUiThread {
                                navegarPorRol(usuario)
                                limpiarCampos()
                                mostrarCargando(false)
                            }
                        }
                    }

                } catch (e: IOException) {
                    // Error de red o de conexión (e.g., la API no está corriendo)
                    runOnUiThread {
                        Log.e("Login", "Error de red", e)
                        mostrarDialogo(this@LoginActivity, "Error", "No se pudo conectar al servidor. Verifique su conexión a internet o intente más tarde.", R.drawable.ic_delete, R.color.red)
                        mostrarCargando(false)
                    }
                } catch (e: HttpException) {
                    // Error HTTP
                    runOnUiThread {
                        Log.e("Login", "Error HTTP", e)
                        mostrarDialogo(this@LoginActivity, "Error", "Error en la solicitud. Código: ${e.code()}", R.drawable.ic_delete, R.color.red)
                        mostrarCargando(false)
                    }
                } catch (e: Exception) {
                    // Cualquier otro error
                    runOnUiThread {
                        Log.e("Login", "Error desconocido", e)
                        mostrarDialogo(this@LoginActivity, "Error", "Ocurrió un error inesperado. Intente más tarde.", R.drawable.ic_delete, R.color.red)
                        mostrarCargando(false)
                    }
                }
            }
        }
    }

    private fun navegarPorRol(usuario: UserData) {
        when (usuario.rol.descripcion) {
            "Analista", "Estudiante" -> navegarListadoReclamos(ListadoReclamosActivity::class.java, usuario)
            else -> runOnUiThread {
                mostrarDialogo(this@LoginActivity, "Advertencia", "Para iniciar sesión, debe tener un rol de Analista o Estudiante.", R.drawable.ic_warning, R.color.yellow)
            }
        }
    }

    private fun navegarListadoReclamos(destino: Class<*>, usuario: UserData) {
        val intent = Intent(this, destino).apply {
            putExtra("usuario", usuario)
        }
        startActivity(intent)
    }

    // Función para mostrar el cargando y ocultar los campos de entrada
    private fun mostrarCargando(mostrar: Boolean) {
        if (mostrar) {
            binding.contenedorEditTexts.visibility = View.GONE
            binding.pbCargando.visibility = View.VISIBLE
        } else {
            binding.contenedorEditTexts.visibility = View.VISIBLE
            binding.pbCargando.visibility = View.GONE
        }
    }

    private fun limpiarCampos() {
        binding.etNombreUsuario.text!!.clear()
        binding.etClave.text!!.clear()
    }

    override fun onStart() {
        super.onStart()

        mostrarCargando(false)
    }
}
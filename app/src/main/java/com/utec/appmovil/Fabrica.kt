package com.utec.appmovil

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.utec.appmovil.api.UserData
import com.utec.appmovil.databinding.DialogCrearReclamoBinding

private lateinit var preferencias: SharedPreferences

fun mostrarDialogo(contexto: Context, titulo: String, mensaje: String, drawableId: Int, colorId: Int) {
    val dialogo = AlertDialog.Builder(contexto)
        .setIcon(obtenerIconoColor(contexto, drawableId, colorId))
        .setTitle(titulo)
        .setMessage(mensaje)
        .setPositiveButton("Aceptar", null)
        .create()
        .show()
}

fun recuperarUsuario(contexto: Context): UserData? {
    preferencias = contexto.getSharedPreferences(contexto.getString(R.string.prefs_file), Context.MODE_PRIVATE)
    val usuarioJson = preferencias.getString("usuario", null)

    return if (usuarioJson != null) {
        val gson = Gson()
        gson.fromJson(usuarioJson, UserData::class.java)
    } else {
        null
    }
}

fun validarCamposReclamo(contexto: Context, dialogoBinding: DialogCrearReclamoBinding): Boolean {
    val titulo = dialogoBinding.etTitulo.text.toString().trim()
    val detalle = dialogoBinding.etDetalle.text.toString().trim()

    val camposVacios = titulo.isEmpty() || detalle.isEmpty()
    val rangoTituloIncorrecto = titulo.length !in 6..40
    val rangoDetalleIncorrecto = detalle.length !in 10..120

    return when {
        // Si están vacíos
        camposVacios -> {
            Toast.makeText(contexto, "Por favor, completa todos los campos.", Toast.LENGTH_LONG).show()
            false
        }

        // Si el título o el detalle no cumplen con los caracteres requeridos
        rangoTituloIncorrecto && rangoDetalleIncorrecto -> {
            Toast.makeText(contexto, "El título debe tener entre 6-40 caracteres y el detalle entre 10-120 caracteres.", Toast.LENGTH_LONG).show()
            false
        }

        // Si el título no cumple con los caracteres requeridos
        rangoTituloIncorrecto -> {
            Toast.makeText(contexto, "El título debe tener entre 6-40 caracteres.", Toast.LENGTH_LONG).show()
            false
        }

        // Si el detalle no cumple con los caracteres requeridos
        rangoDetalleIncorrecto -> {
            Toast.makeText(contexto, "El detalle debe tener entre 10-120 caracteres.", Toast.LENGTH_LONG).show()
            false
        }

        else -> true
    }
}

fun obtenerIconoColor(context: Context, drawableId: Int, colorId: Int): Drawable? {
    return ContextCompat.getDrawable(context, drawableId)?.apply {
        setColorFilter(ContextCompat.getColor(context, colorId), PorterDuff.Mode.SRC_IN)
    }
}
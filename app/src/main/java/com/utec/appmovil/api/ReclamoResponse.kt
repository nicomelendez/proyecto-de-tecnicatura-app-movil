package com.utec.appmovil.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class ReclamoResponse(
    @SerializedName("data") val data: ReclamoData,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: String
) : Serializable

data class ReclamoData(
    @SerializedName("idReclamo") val idReclamo: Int,
    @SerializedName("activo") val activo: String,
    @SerializedName("detalle") val detalle: String,
    @SerializedName("estadosReclamos") val estadosReclamos: List<EstadoReclamo>,
    @SerializedName("estudiante") val estudiante: Estudiante,
    @SerializedName("evento") var evento: EventoData,
    @SerializedName("ultimaFecha") var ultimaFecha: String
)

data class EstadoReclamo(
    @SerializedName("detalle") val detalle: String,
    @SerializedName("estado") val estado: Estado,
    @SerializedName("fechaHora") val fechaHora: String
)

data class Estudiante(
    @SerializedName("idEstudiante") val idEstudiante: Int,
    @SerializedName("oUsuario") val oUsuario: Usuario,
)

data class Evento(
    @SerializedName("idEvento") val idEvento: Int,
    @SerializedName("titulo") val titulo: String
)

data class Usuario(
    @SerializedName("nombreCompleto") val nombreCompleto: String
)

data class Estado(
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("idEstado") val idEstado: Int
)
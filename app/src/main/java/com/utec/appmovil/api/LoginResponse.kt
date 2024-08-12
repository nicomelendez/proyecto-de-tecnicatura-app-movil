package com.utec.appmovil.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LoginResponse(
    @SerializedName("data") val data: UserData,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: String
) : Serializable

data class UserData(
    @SerializedName("nombreUsuario") val nombreUsuario: String,
    @SerializedName("nombreCompleto") val nombreCompleto: String,
    @SerializedName("documento") val documento: Int,
    @SerializedName("activo") val activo: String,
    @SerializedName("genero") val genero: Genero,
    @SerializedName("rol") val rol: Rol,
    @SerializedName("idRol") val idRol: Int
) : Serializable

data class Genero(
    @SerializedName("idGenero") val idGenero: Int,
) : Serializable

data class Rol(
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("idRol") val idRol: Int
) : Serializable
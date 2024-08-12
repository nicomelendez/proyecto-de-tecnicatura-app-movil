package com.utec.appmovil.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReclamoRequest(
    @SerializedName("idReclamo") var idReclamo: Long?,
    @SerializedName("titulo") var titulo: String,
    @SerializedName("detalle") var detalle: String,
    @SerializedName("documento") var documento: String,
    @SerializedName("idEvento") var idEvento: Int
) : Serializable
package com.utec.appmovil.api

import com.google.gson.annotations.SerializedName

data class EventoData(
    @SerializedName("idEvento") val idEvento: Int,
    @SerializedName("titulo") val titulo: String
)
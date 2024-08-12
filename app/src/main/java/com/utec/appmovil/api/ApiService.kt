package com.utec.appmovil.api

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {

    @FormUrlEncoded
    @POST("/Proyecto-PInfra/api/login")
    suspend fun login(
        @Field("nombreUsuario") nombreUsuario: String,
        @Field("clave") clave: String
    ): Response<LoginResponse>

    @GET("/Proyecto-PInfra/api/eventos")
    suspend fun getEventos(): Response<List<EventoData>>

    @GET("/Proyecto-PInfra/api/reclamos")
    suspend fun getReclamos(): Response<List<ReclamoData>>

    @GET("/Proyecto-PInfra/api/reclamos/estudiante")
    suspend fun getMisReclamos(@Query("idEstudiante") idEstudiante: Int): Response<List<ReclamoData>>

    @GET("/Proyecto-PInfra/api/reclamos/id")
    suspend fun getReclamo(@Query("idReclamo") idReclamo: Int): Response<ReclamoData>

    @POST("/Proyecto-PInfra/api/reclamos")
    suspend fun createReclamo(@Body reclamo: JsonObject): Response<ReclamoResponse>

    @PUT("/Proyecto-PInfra/api/reclamos")
    suspend fun updateReclamo(@Body reclamo: JsonObject): Response<ReclamoResponse>

    @DELETE("/Proyecto-PInfra/api/reclamos/id")
    suspend fun deleteReclamo(@Query("idReclamo") idReclamo: Int): Response<ReclamoResponse>

}


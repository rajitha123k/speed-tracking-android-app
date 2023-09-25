package com.example.webexandroid


import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface APIService {
    @POST("/apheleia/api/v1/events")
    suspend fun createEmployee(@Header("Authorization") authHeader: String, @Body requestBody: RequestBody): Response<ResponseBody>
}
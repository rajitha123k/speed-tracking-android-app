package com.example.webexandroid

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface StatusMessageAPIService {
    @POST("/usersub/api/v1/publish")
    suspend fun createMessageChangeRequest(@Header("Authorization") authHeader: String, @Body requestBody: RequestBody): Response<ResponseBody>
}
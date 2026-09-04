package com.librespec.sync

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class SyncPayload(
    @SerializedName("test_id") val testId: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("predicted_class") val predictedClass: String,
    @SerializedName("confidence_score") val confidenceScore: Float,
    @SerializedName("baseline_vector") val baselineVector: List<Int>,
    @SerializedName("plateau_vector") val plateauVector: List<Int>,
    @SerializedName("baseline_hmac") val baselineHmac: String,
    @SerializedName("plateau_hmac") val plateauHmac: String,
    @SerializedName("gps_latitude") val latitude: Double,
    @SerializedName("gps_longitude") val longitude: Double
)

data class SyncResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("test_id") val testId: String? = null,
    @SerializedName("error") val error: String? = null
)

interface AwsApiService {
    @POST("default/BioChemicalAnalyser")
    suspend fun syncLog(@Body payload: SyncPayload): Response<SyncResponse>
}

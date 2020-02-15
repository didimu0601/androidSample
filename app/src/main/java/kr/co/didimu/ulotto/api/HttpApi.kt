package kr.co.didimu.ulotto.api

import kr.co.didimu.ulotto.data.dto.DeviceRegistration
import kr.co.didimu.ulotto.data.dto.DeviceResponse
import kr.co.didimu.ulotto.data.dto.LoginResponse
import kr.co.didimu.ulotto.data.dto.Version
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

interface HttpApi {
    @POST("/appVersion.php")
    fun requestVersion(@Query("siteId")siteId: String,
                       @Query("os")os: String): Call<Version>

//    @Headers("Content-Type: application/json")
    @POST("/appLogin.php")
    fun login(@Query("userid")userid: String,
              @Query("passwd")passwd: String,
              @Query("siteId")siteId: String,
              @Query("userGubun")userGubun: String): Call<LoginResponse>
//    fun login(@Body loginInfo: JsonObject): Call<LoginResponse>


    @GET("/api/setup/retrieveBioauthDevice.do")
    fun confirmPromptDevice(@Header("udid") deviceId: String, @Query("userId")userId: String): Call<DeviceResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/setup/writeBioauthDevice.do")
    fun registerPromptDevice(@Body deviceInfo: JsonObject): Call<DeviceRegistration>

    //20191018 psg remove registered prompt device
    @Headers("Content-Type: application/json")
    @POST("/api/setup/removeBioauthDevice.do")
    fun removePromptDevice(@Body deviceInfo: JsonObject): Call<DeviceRegistration>

//    companion object {
//        fun retrofit(): HttpApi {
//            return Retrofit.Builder()
//                .baseUrl(ServerType.baseUrl)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//                .create(HttpApi::class.java)
//        }
//    }
}
package xyz.bluepencil.bluepencil

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://blue-pencil-razor.herokuapp.com/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

/**
 * A public interface that exposes the [getProperties] method
 */
interface OrderApiService {
    /**
     * Returns a Retrofit callback that delivers a String
     * The @GET annotation indicates that the "realestate" endpoint will be requested with the GET
     * HTTP method
     */
    @GET("order")
    fun getOrderID(@Query("amount") type: String): Call<String>
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object OrderApi {
    val retrofitService : OrderApiService by lazy { retrofit.create(OrderApiService::class.java) }
}
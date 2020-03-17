package kaaes.spotify.webapi.android

import android.text.TextUtils
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Creates and configures a REST adapter for Spotify Web API.
 *
 *
 * Basic usage:
 * SpotifyApi wrapper = new SpotifyApi();
 *
 *
 * Setting access token is optional for certain endpoints
 * so if you know you'll only use the ones that don't require authorisation
 * you can skip this step:
 * wrapper.setAccessToken(authenticationResponse.getAccessToken());
 *
 *
 * SpotifyService spotify = wrapper.getService();
 *
 *
 * Album album = spotify.getAlbum("2dIGnmEIy1WZIcZCFSj6i8");
 */
class SpotifyApi {
    /**
     * The request interceptor that will add the header with OAuth
     * token to every request made with the wrapper.
     */
    private inner class WebApiAuthenticator : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            return if (!TextUtils.isEmpty(mAccessToken)) {
                val authRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $mAccessToken")
                        .build()
                chain.proceed(authRequest)
            } else {
                chain.proceed(originalRequest)
            }
        }
    }

    /**
     * @return The SpotifyApi instance
     */
    val service: SpotifyService
    private var mAccessToken: String? = null

    /**
     * Create instance of SpotifyApi with given executors.
     *
     * @param httpExecutor     executor for http request. Cannot be null.
     */
    constructor(httpExecutor: Executor, callbackExecutor: Executor) {
        service = init(httpExecutor, callbackExecutor)
    }

    /**
     * New instance of SpotifyApi,
     * with single thread executor both for http and callbacks.
     */
    constructor() {
        val httpExecutor: Executor = Executors.newSingleThreadExecutor()
        val callbackExecutor: Executor = Executors.newFixedThreadPool(CORE_POOL_SIZE)
        service = init(httpExecutor, callbackExecutor)
    }

    private fun init(httpExecutor: Executor, callbackExecutor: Executor): SpotifyService {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        /*
        * https://github.com/square/retrofit/issues/1259
        * create a dispatcher for the client if http executor is needed
        * */
        val authClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(WebApiAuthenticator())
                .addInterceptor(logging)
        val adapter = Retrofit.Builder()
                .baseUrl(SPOTIFY_WEB_API_ENDPOINT)
                .callbackExecutor(callbackExecutor)
                .addConverterFactory(JacksonConverterFactory.create(newObjectMapper()))
                .client(authClient.build())
                .build()
        return adapter.create(SpotifyService::class.java)
    }

    /**
     * / **
     * Sets access token on the wrapper.
     * Use to set or update token with the new value.
     * If you want to remove token set it to null.
     *
     * @param accessToken The token to set on the wrapper.
     * @return The instance of the wrapper.
     */
    fun setAccessToken(accessToken: String?): SpotifyApi {
        mAccessToken = accessToken
        return this
    }

    companion object {
        private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
        private val CORE_POOL_SIZE = CPU_COUNT + 1

        /**
         * Main Spotify Web API endpoint
         */
        const val SPOTIFY_WEB_API_ENDPOINT = "https://api.spotify.com/v1/"
        @JvmStatic
        fun newObjectMapper(): ObjectMapper {
            val objectMapper = ObjectMapper()
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
            objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
            return objectMapper
        }
    }
}
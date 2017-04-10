package kaaes.spotify.webapi.android;

import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Creates and configures a REST adapter for Spotify Web API.
 * <p>
 * Basic usage:
 * SpotifyApi wrapper = new SpotifyApi();
 * <p>
 * Setting access token is optional for certain endpoints
 * so if you know you'll only use the ones that don't require authorisation
 * you can skip this step:
 * wrapper.setAccessToken(authenticationResponse.getAccessToken());
 * <p>
 * SpotifyService spotify = wrapper.getService();
 * <p>
 * Album album = spotify.getAlbum("2dIGnmEIy1WZIcZCFSj6i8");
 */
public class SpotifyApi {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;

    /**
     * Main Spotify Web API endpoint
     */
    public static final String SPOTIFY_WEB_API_ENDPOINT = "https://api.spotify.com/v1/";

    /**
     * The request interceptor that will add the header with OAuth
     * token to every request made with the wrapper.
     */
    private class WebApiAuthenticator implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();

            if (!TextUtils.isEmpty(mAccessToken)) {
                Request authRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + mAccessToken)
                        .build();
                return chain.proceed(authRequest);
            } else {
                return chain.proceed(originalRequest);
            }
        }
    }

    private final SpotifyService mSpotifyService;

    private String mAccessToken;

    /**
     * Create instance of SpotifyApi with given executors.
     *
     * @param httpExecutor     executor for http request. Cannot be null.
     */
    public SpotifyApi(Executor httpExecutor, Executor callbackExecutor) {
        mSpotifyService = init(httpExecutor, callbackExecutor);
    }

    /**
     * New instance of SpotifyApi,
     * with single thread executor both for http and callbacks.
     */
    public SpotifyApi() {
        Executor httpExecutor = Executors.newSingleThreadExecutor();
        Executor callbackExecutor = Executors.newFixedThreadPool(CORE_POOL_SIZE);
        mSpotifyService = init(httpExecutor, callbackExecutor);
    }

    private SpotifyService init(Executor httpExecutor, Executor callbackExecutor) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        /*
        * https://github.com/square/retrofit/issues/1259
        * create a dispatcher for the client if http executor is needed
        * */
        OkHttpClient.Builder authClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new WebApiAuthenticator())
                .addInterceptor(logging);


        final Retrofit adapter = new Retrofit.Builder()
                .baseUrl(SPOTIFY_WEB_API_ENDPOINT)
                .callbackExecutor(callbackExecutor)
                .addConverterFactory(JacksonConverterFactory.create(newObjectMapper()))
                .client(authClient.build())
                .build();

        return adapter.create(SpotifyService.class);
    }

    public static ObjectMapper newObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
        return objectMapper;
    }

    /**
    /**
     * Sets access token on the wrapper.
     * Use to set or update token with the new value.
     * If you want to remove token set it to null.
     *
     * @param accessToken The token to set on the wrapper.
     * @return The instance of the wrapper.
     */
    public SpotifyApi setAccessToken(String accessToken) {
        mAccessToken = accessToken;
        return this;
    }

    /**
     * @return The SpotifyApi instance
     */
    public SpotifyService getService() {
        return mSpotifyService;
    }
}

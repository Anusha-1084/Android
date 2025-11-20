package com.astinil.AndroidTimesheet.api;

import android.content.Context;

import com.astinil.AndroidTimesheet.util.AuthInterceptor;
import com.astinil.AndroidTimesheet.util.Prefs;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:8888/";

    private static Retrofit retrofitNoAuth = null;
    private static Retrofit retrofitWithAuth = null;

    // For LOGIN (no interceptor)
    public static ApiService getApiService(Context ctx){
        if (retrofitNoAuth == null){
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(log)
                    .build();

            retrofitNoAuth = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitNoAuth.create(ApiService.class);
    }

    // For all other APIs (with interceptor)
    public static ApiService getSecuredApi(Context ctx){
        if (retrofitWithAuth == null){
            Prefs prefs = new Prefs(ctx);

            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(prefs))
                    .addInterceptor(log)
                    .build();

            retrofitWithAuth = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitWithAuth.create(ApiService.class);
    }
}

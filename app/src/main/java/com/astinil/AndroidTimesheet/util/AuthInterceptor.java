package com.astinil.AndroidTimesheet.util;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final Prefs prefs;

    public AuthInterceptor(Prefs prefs) { this.prefs = prefs; }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = prefs.getToken();
        if (token != null) {
            Request req = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(req);
        } else {
            return chain.proceed(original);
        }
    }
}

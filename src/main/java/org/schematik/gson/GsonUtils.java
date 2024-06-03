package org.schematik.gson;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;

public class GsonUtils {
    public static Gson getDefaultGson() {
        GsonBuilder builder = (new GsonBuilder())
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        return builder.create();
    }
}

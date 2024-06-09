package org.schematik.gson;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;

public class GsonUtils {
    static Gson gson;

    public static Gson getDefaultGson() {
        if (gson == null) {
            GsonBuilder builder = (new GsonBuilder())
                    .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
            gson = builder.create();
        }

        return gson;
    }
}

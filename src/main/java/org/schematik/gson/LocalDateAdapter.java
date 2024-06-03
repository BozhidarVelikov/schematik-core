package org.schematik.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends TypeAdapter<LocalDate> {
    @Override
    public LocalDate read(JsonReader reader) throws IOException {
        LocalDate localDate = null;

        String fieldName = null;

        reader.beginObject();
        if (reader.peek().equals(JsonToken.NAME)) {
            fieldName = reader.nextName();
        }

        if ("date".equals(fieldName)) {
            reader.peek();
            localDate = LocalDate.parse(reader.nextString());
        }
        reader.endObject();

        return localDate;
    }

    @Override
    public void write(JsonWriter writer, LocalDate localDate) throws IOException {
        writer.beginObject();
        writer.name("date");
        writer.value(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        writer.endObject();
    }
}

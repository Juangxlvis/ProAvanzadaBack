package org.uniquindio.edu.co.gpsanjuan_backend.utils; // O el paquete que prefieras para utilidades

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.JsonToken; // Asegúrate de importar JsonToken
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends TypeAdapter<LocalDate> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(FORMATTER.format(value));
        }
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) { // Comprueba si el token es NULL
            in.nextNull();
            return null;
        } else {
            String date = in.nextString();
            return LocalDate.parse(date, FORMATTER);
        }
    }
}
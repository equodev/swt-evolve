package org.eclipse.swt.internal;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.eclipse.swt.widgets.Layout;

import java.lang.reflect.Type;

public class TypperJsonSerializer implements JsonSerializer<Object> {
    private final Gson gsonDefault = new Gson();

    @Override
    public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
        JsonElement json = gsonDefault.toJsonTree(src);
        json.getAsJsonObject().addProperty("swt", src.getClass().getSimpleName());
        return json;
    }
}

package org.eclipse.swt.widgets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.eclipse.swt.internal.OptionalTypeAdapters;
import org.eclipse.swt.internal.TypperJsonSerializer;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.values.WidgetValue;

import java.util.List;

public class Serializer {

	final Gson gson = configureGson();

	private Gson configureGson() {
		GsonBuilder builder = new GsonBuilder();
		OptionalTypeAdapters.register(builder);
		TypperJsonSerializer typper = new TypperJsonSerializer();
		builder.registerTypeHierarchyAdapter(Layout.class, typper);
		builder.registerTypeAdapter(RowData.class, typper);
		return builder.create();
	}

	public String to(Object obj) {
		return gson.toJson(obj);
	}

	@SuppressWarnings("unchecked")
	public <T> T from(String payload, Class<? super T> clazz) {
		return (T) gson.fromJson(payload, clazz);
	}

	@SuppressWarnings("unchecked")
	public <T> T from(String payload, WidgetValue.Builder builder) {
		return (T) from(payload, builder.setId(-1).build().getClass());
	}

	public <T> List<T> fromList(String payload, Class<? extends WidgetValue> clazz) {
		return gson.fromJson(payload, TypeToken.getParameterized(List.class, clazz).getType());
	}

	public <T> List<T> fromList(String payload, WidgetValue.Builder builder) {
		return fromList(payload, builder.setId(-1).build().getClass());
	}
}

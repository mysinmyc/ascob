package ascob.server.util;

import com.google.gson.Gson;

public class SerializationUtil {

	static final Gson GSON = new Gson();
	
	public static String serialize(Object object) {
		if (object==null) {
			return null;
		}
		
		return GSON.toJson(object);
	}
	
	
	public static <T>  T deserialize(Class<T> targetClass, String data) {
		if (data==null || data.isEmpty()) {
			return null;
		}
		return GSON.fromJson(data, targetClass);
	}

	public static <T> T clone(T object) {
		if (object==null) {
			return null;
		}
		return deserialize((Class<T>)object.getClass(), serialize(object));
	}
}

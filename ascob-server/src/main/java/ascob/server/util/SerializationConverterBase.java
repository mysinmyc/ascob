package ascob.server.util;

import jakarta.persistence.AttributeConverter;

public abstract class SerializationConverterBase<T> implements AttributeConverter<T, String> {

	protected abstract Class<T> getObjecType();
	
	@Override
	public String convertToDatabaseColumn(T attribute) {
		return SerializationUtil.serialize(attribute);
	}
	
	@Override
	public T convertToEntityAttribute(String dbData) {
		return SerializationUtil.deserialize(getObjecType(), dbData);
	}
}

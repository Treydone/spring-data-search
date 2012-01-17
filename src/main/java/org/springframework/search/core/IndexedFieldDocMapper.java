package org.springframework.search.core;

import java.lang.reflect.Field;

import org.springframework.beans.BeanUtils;
import org.springframework.search.DocMapper;
import org.springframework.search.Document;
import org.springframework.search.DocumentMappingException;
import org.springframework.search.annotation.Indexed;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public class IndexedFieldDocMapper<T> implements DocMapper<T> {

	private Class<T> requiredType;

	public IndexedFieldDocMapper(Class<T> requiredType) {
		this.requiredType = requiredType;
	}

	@Override
	public T docMap(Document doc) {

		T bean = BeanUtils.instantiate(requiredType);

		Field[] fields = requiredType.getDeclaredFields();

		for (Field field : fields) {
			field.setAccessible(true);
			Indexed annotation = field.getAnnotation(Indexed.class);
			if (annotation != null) {

				String fieldName = annotation.fieldName();
				if (!StringUtils.hasText(fieldName)) {
					fieldName = field.getName();
				}
				try {
					ReflectionUtils.setField(field, bean, getValue(doc.get(fieldName), field.getType()));
				} catch (IllegalArgumentException e) {
					throw new DocumentMappingException("Unable to set the field " + field.getName() + " of type " + field.getType() + " with value " + doc.get(fieldName) + " of type "
							+ doc.get(fieldName).getClass(), e);
				}
			}
		}

		return bean;
	}

	public Object getValue(Object bean, Class<?> requiredType) {
		return bean;
	}

}

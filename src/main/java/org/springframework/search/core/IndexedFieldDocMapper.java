package org.springframework.search.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.apache.commons.lang.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.search.DocMapper;
import org.springframework.search.Document;
import org.springframework.search.DocumentMappingException;
import org.springframework.search.annotation.Indexed;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public class IndexedFieldDocMapper<T> implements DocMapper<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexedFieldDocMapper.class);

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
					Class<?> clazz = doc.get(fieldName).getClass();
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("Unable to set the field " + field.getName() + " of type " + field.getType() + " with value " + doc.get(fieldName) + " of type " + clazz);
					}
					try {
						Constructor constructor;
						Class<?> primitiveClass = ClassUtils.resolvePrimitiveClassName(clazz.getSimpleName().toLowerCase());
						if (primitiveClass != null) {
							constructor = field.getType().getConstructor(primitiveClass);
						} else {
							constructor = field.getType().getConstructor(clazz);
						}

						ReflectionUtils.setField(field, bean, constructor.newInstance(doc.get(fieldName)));
					} catch (Exception e1) {
						throw new DocumentMappingException("Unable to set the field " + field.getName() + " of type " + field.getType() + " with value " + doc.get(fieldName) + " of type "
								+ clazz, e1);
					}
				}
			}
		}

		return bean;
	}

	public Object getValue(Object bean, Class<?> requiredType) {
		return bean;
	}

}

package org.springframework.search.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD, ElementType.TYPE})
public @interface Indexed {

	/**
     * Name of the index to use.
     */
    String indexName() default "";
    
    String fieldName() default "";
}

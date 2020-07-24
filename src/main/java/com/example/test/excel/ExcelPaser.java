package com.example.test.excel;


import org.codehaus.jackson.annotate.JacksonAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelPaser {

    Class<? extends FieldPaser> paserClass() default FieldPaser.FieldPaser1.class;

    boolean ignore() default false;

    boolean required() default false;

}

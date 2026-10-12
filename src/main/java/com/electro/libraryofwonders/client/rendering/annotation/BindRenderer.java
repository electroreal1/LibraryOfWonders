package com.electro.libraryofwonders.client.rendering.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BindRenderer {

    /**
     * The registry ID of the entity.
     * Example: "mythos:testing_entity"
     */
    String value();
}

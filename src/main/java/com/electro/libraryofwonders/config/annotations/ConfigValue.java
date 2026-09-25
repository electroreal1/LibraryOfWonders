package com.electro.libraryofwonders.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigValue {

    /**
     * The comment that will be written above this value in the TOML file.
     */
    String comment() default "";

    /**
     * Optional: Override the TOML key name. If empty, the variable's name is used.
     */
    String name() default "";
}

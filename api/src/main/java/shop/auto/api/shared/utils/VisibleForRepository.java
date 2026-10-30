package shop.auto.api.shared.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.CLASS)
@Target(value = { ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface VisibleForRepository {}

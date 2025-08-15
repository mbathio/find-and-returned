package com.retrouvtout.config;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponse
public @interface SwaggerApiResponse {
    String responseCode() default "200";
    String description() default "";
}
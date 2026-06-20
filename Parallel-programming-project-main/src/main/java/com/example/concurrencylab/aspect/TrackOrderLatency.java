package com.example.concurrencylab.aspect;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackOrderLatency {
    String scenario();
}
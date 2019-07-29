/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.data.cosmosdb.core.mapping;

import com.microsoft.azure.spring.data.cosmosdb.Constants;
import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Document {

    String collection() default Constants.DEFAULT_COLLECTION_NAME;

    String ru() default Constants.DEFAULT_REQUEST_UNIT;

    int timeToLive() default Constants.DEFAULT_TIME_TO_LIVE;
}

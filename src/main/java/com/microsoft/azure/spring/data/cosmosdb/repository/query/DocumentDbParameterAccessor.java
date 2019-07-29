/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.data.cosmosdb.repository.query;

import org.springframework.data.repository.query.ParameterAccessor;

public interface DocumentDbParameterAccessor extends ParameterAccessor {
    Object[] getValues();
}

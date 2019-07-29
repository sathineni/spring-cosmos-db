/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.data.cosmosdb.core.mapping;

import org.springframework.context.ApplicationContext;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;


public class DocumentDbMappingContext
        extends AbstractMappingContext<BasicDocumentDbPersistentEntity<?>, DocumentDbPersistentProperty> {

    private ApplicationContext context;

    @Override
    protected <T> BasicDocumentDbPersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {
        final BasicDocumentDbPersistentEntity<T> entity = new BasicDocumentDbPersistentEntity<>(typeInformation);

        if (context != null) {
            entity.setApplicationContext(context);
        }
        return entity;
    }

    @Override
    public DocumentDbPersistentProperty createPersistentProperty(Property property,
                                                                 BasicDocumentDbPersistentEntity<?> owner,
                                                                 SimpleTypeHolder simpleTypeHolder) {
        return new BasicDocumentDbPersistentProperty(property, owner, simpleTypeHolder);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }
}

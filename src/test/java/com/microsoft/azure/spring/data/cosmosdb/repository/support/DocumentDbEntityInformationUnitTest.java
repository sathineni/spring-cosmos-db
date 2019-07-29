/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.data.cosmosdb.repository.support;

import com.microsoft.azure.spring.data.cosmosdb.common.TestConstants;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.Document;
import com.microsoft.azure.spring.data.cosmosdb.domain.Address;
import com.microsoft.azure.spring.data.cosmosdb.domain.Person;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentDbEntityInformationUnitTest {
    private static final String ID = "entity_info_test_id";
    private static final String FIRST_NAME = "first name";
    private static final String LAST_NAME = "last name";
    private static final List<String> HOBBIES = TestConstants.HOBBIES;
    private static final List<Address> ADDRESSES = TestConstants.ADDRESSES;

    @Test
    public void testGetId() {
        final Person testPerson = new Person(ID, FIRST_NAME, LAST_NAME, HOBBIES, ADDRESSES);
        final DocumentDbEntityInformation<Person, String> entityInformation =
                new DocumentDbEntityInformation<Person, String>(Person.class);

        final String idField = entityInformation.getId(testPerson);

        assertThat(idField).isEqualTo(testPerson.getId());
    }

    @Test
    public void testGetIdType() {
        final DocumentDbEntityInformation<Person, String> entityInformation =
                new DocumentDbEntityInformation<Person, String>(Person.class);

        final Class<?> idType = entityInformation.getIdType();
        assertThat(idType.getSimpleName()).isEqualTo(String.class.getSimpleName());
    }

    @Test
    public void testGetCollectionName() {
        final DocumentDbEntityInformation<Person, String> entityInformation =
                new DocumentDbEntityInformation<Person, String>(Person.class);

        final String collectionName = entityInformation.getCollectionName();
        assertThat(collectionName).isEqualTo(Person.class.getSimpleName());
    }

    @Test
    public void testCustomCollectionName() {
        final DocumentDbEntityInformation<Volunteer, String> entityInformation =
                new DocumentDbEntityInformation<Volunteer, String>(Volunteer.class);

        final String collectionName = entityInformation.getCollectionName();
        assertThat(collectionName).isEqualTo("testCollection");
    }

    @Document(collection = "testCollection")
    class Volunteer {
        String id;
        String name;
    }
}

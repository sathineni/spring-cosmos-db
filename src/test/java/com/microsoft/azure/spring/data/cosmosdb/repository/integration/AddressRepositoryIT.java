/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.data.cosmosdb.repository.integration;

import com.microsoft.azure.spring.data.cosmosdb.common.TestConstants;
import com.microsoft.azure.spring.data.cosmosdb.common.TestUtils;
import com.microsoft.azure.spring.data.cosmosdb.core.DocumentDbTemplate;
import com.microsoft.azure.spring.data.cosmosdb.domain.Address;
import com.microsoft.azure.spring.data.cosmosdb.repository.TestRepositoryConfig;
import com.microsoft.azure.spring.data.cosmosdb.repository.repository.AddressRepository;
import com.microsoft.azure.spring.data.cosmosdb.repository.support.DocumentDbEntityInformation;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class AddressRepositoryIT {

    private static final Address TEST_ADDRESS1_PARTITION1 = new Address(
            TestConstants.POSTAL_CODE, TestConstants.STREET, TestConstants.CITY);
    private static final Address TEST_ADDRESS2_PARTITION1 = new Address(
            TestConstants.POSTAL_CODE_0, TestConstants.STREET_0, TestConstants.CITY);
    private static final Address TEST_ADDRESS1_PARTITION2 = new Address(
            TestConstants.POSTAL_CODE_1, TestConstants.STREET_1, TestConstants.CITY_0);
    private static final Address TEST_ADDRESS4_PARTITION3 = new Address(
            TestConstants.POSTAL_CODE, TestConstants.STREET_2, TestConstants.CITY_1);

    private final DocumentDbEntityInformation<Address, String> entityInformation
            = new DocumentDbEntityInformation<>(Address.class);

    @Autowired
    AddressRepository repository;

    @Autowired
    private DocumentDbTemplate template;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @PreDestroy
    public void cleanUpCollection() {
        template.deleteCollection(entityInformation.getCollectionName());
    }

    @Before
    public void setup() {
        repository.save(TEST_ADDRESS1_PARTITION1);
        repository.save(TEST_ADDRESS1_PARTITION2);
        repository.save(TEST_ADDRESS2_PARTITION1);
        repository.save(TEST_ADDRESS4_PARTITION3);
    }

    @After
    public void cleanup() {
        repository.deleteAll();
    }

    @Test
    public void testFindAll() {
        // findAll cross partition
        final List<Address> result = TestUtils.toList(repository.findAll());

        assertThat(result.size()).isEqualTo(4);
    }

    @Test
    public void testFindByIdForPartitionedCollection() {
        final List<Address> addresses = repository.findByPostalCode(TestConstants.POSTAL_CODE);

        assertThat(addresses.size()).isEqualTo(2);
        assertThat(addresses.get(0).getPostalCode().equals(TestConstants.POSTAL_CODE));
        assertThat(addresses.get(1).getPostalCode().equals(TestConstants.POSTAL_CODE));
    }

    @Test
    public void testFindByPartitionedCity() {
        final String city = TEST_ADDRESS1_PARTITION1.getCity();
        final List<Address> result = TestUtils.toList(repository.findByCity(city));

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getCity()).isEqualTo(city);
        assertThat(result.get(1).getCity()).isEqualTo(city);
    }

    @Test
    public void testFindByStreetOrCity() {
        final String city = TEST_ADDRESS1_PARTITION1.getCity();
        final String street = TEST_ADDRESS1_PARTITION2.getStreet();

        final List<Address> result = repository.findByStreetOrCity(street, city);
        final List<Address> reference = Arrays.asList(
                TEST_ADDRESS1_PARTITION1, TEST_ADDRESS1_PARTITION2, TEST_ADDRESS2_PARTITION1);

        result.sort(Comparator.comparing(Address::getPostalCode));
        reference.sort(Comparator.comparing(Address::getPostalCode));

        Assert.assertEquals(reference.size(), result.size());
        Assert.assertEquals(reference, result);
    }

    @Test
    public void testCount() {
        final long count = repository.count();
        assertThat(count).isEqualTo(4);

        repository.deleteByCity(TestConstants.CITY);
        final long newCount = repository.count();
        assertThat(newCount).isEqualTo(2);
    }

    @Test
    public void deleteWithoutPartitionedColumnShouldFail() {
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("PartitionKey value must be supplied for this operation.");

        repository.deleteById(TEST_ADDRESS1_PARTITION1.getPostalCode());
    }

    @Test
    public void canDeleteByIdAndPartitionedCity() {
        final long count = repository.count();
        assertThat(count).isEqualTo(4);

        repository.deleteByPostalCodeAndCity(
                TEST_ADDRESS1_PARTITION1.getPostalCode(), TEST_ADDRESS1_PARTITION1.getCity());

        final List<Address> result = TestUtils.toList(repository.findAll());

        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    public void canDeleteByPartitionedCity() {
        final long count = repository.count();
        assertThat(count).isEqualTo(4);

        repository.deleteByCity(TEST_ADDRESS1_PARTITION1.getCity());

        final List<Address> result = TestUtils.toList(repository.findAll());

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getCity()).isNotEqualTo(TEST_ADDRESS1_PARTITION1.getCity());
    }

    @Test
    public void testUpdateEntity() {
        final Address updatedAddress = new Address(TEST_ADDRESS1_PARTITION1.getPostalCode(), TestConstants.NEW_STREET,
                TEST_ADDRESS1_PARTITION1.getCity());

        repository.save(updatedAddress);

        final List<Address> results =
                repository.findByPostalCodeAndCity(updatedAddress.getPostalCode(), updatedAddress.getCity());

        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0).getStreet()).isEqualTo(updatedAddress.getStreet());
        assertThat(results.get(0).getPostalCode()).isEqualTo(updatedAddress.getPostalCode());
    }
}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.data.cosmosdb.repository.integration;

import com.microsoft.azure.spring.data.cosmosdb.common.TestUtils;
import com.microsoft.azure.spring.data.cosmosdb.core.DocumentDbTemplate;
import com.microsoft.azure.spring.data.cosmosdb.domain.Contact;
import com.microsoft.azure.spring.data.cosmosdb.repository.TestRepositoryConfig;
import com.microsoft.azure.spring.data.cosmosdb.repository.repository.ContactRepository;
import com.microsoft.azure.spring.data.cosmosdb.repository.support.DocumentDbEntityInformation;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ContactRepositoryIT {

    private static final Contact TEST_CONTACT = new Contact("testId", "faketitle");

    private final DocumentDbEntityInformation<Contact, String> entityInformation =
            new DocumentDbEntityInformation<>(Contact.class);

    @Autowired
    ContactRepository repository;

    @Autowired
    private DocumentDbTemplate template;

    @PreDestroy
    public void cleanUpCollection() {
        template.deleteCollection(entityInformation.getCollectionName());
    }

    @Before
    public void setup() {
        repository.save(TEST_CONTACT);
    }

    @After
    public void cleanup() {
        repository.deleteAll();
    }

    @Test
    public void testFindAll() {
        final List<Contact> result = TestUtils.toList(repository.findAll());

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getLogicId()).isEqualTo(TEST_CONTACT.getLogicId());
        assertThat(result.get(0).getTitle()).isEqualTo(TEST_CONTACT.getTitle());

        final Contact contact = repository.findById(TEST_CONTACT.getLogicId()).get();

        assertThat(contact.getLogicId()).isEqualTo(TEST_CONTACT.getLogicId());
        assertThat(contact.getTitle()).isEqualTo(TEST_CONTACT.getTitle());
    }

    @Test
    public void testCountAndDeleteByID() {
        final Contact contact2 = new Contact("newid", "newtitle");
        repository.save(contact2);
        final List<Contact> all = TestUtils.toList(repository.findAll());
        assertThat(all.size()).isEqualTo(2);

        long count = repository.count();
        assertThat(count).isEqualTo(2);

        repository.deleteById(contact2.getLogicId());

        final List<Contact> result = TestUtils.toList(repository.findAll());

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getLogicId()).isEqualTo(TEST_CONTACT.getLogicId());
        assertThat(result.get(0).getTitle()).isEqualTo(TEST_CONTACT.getTitle());

        count = repository.count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testCountAndDeleteEntity() {
        final Contact contact2 = new Contact("newid", "newtitle");
        repository.save(contact2);
        final List<Contact> all = TestUtils.toList(repository.findAll());
        assertThat(all.size()).isEqualTo(2);

        repository.delete(contact2);

        final List<Contact> result = TestUtils.toList(repository.findAll());

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getLogicId()).isEqualTo(TEST_CONTACT.getLogicId());
        assertThat(result.get(0).getTitle()).isEqualTo(TEST_CONTACT.getTitle());
    }

    @Test
    public void testUpdateEntity() {
        final Contact updatedContact = new Contact(TEST_CONTACT.getLogicId(), "updated");

        repository.save(updatedContact);

        final Contact contact = repository.findById(TEST_CONTACT.getLogicId()).get();

        assertThat(contact.getLogicId()).isEqualTo(updatedContact.getLogicId());
        assertThat(contact.getTitle()).isEqualTo(updatedContact.getTitle());
    }

    @Test
    public void testBatchOperations() {

        final Contact contact1 = new Contact("newid1", "newtitle");
        final Contact contact2 = new Contact("newid2", "newtitle");
        final ArrayList<Contact> contacts = new ArrayList<Contact>();
        contacts.add(contact1);
        contacts.add(contact2);
        repository.saveAll(contacts);

        final ArrayList<String> ids = new ArrayList<String>();
        ids.add(contact1.getLogicId());
        ids.add(contact2.getLogicId());
        final List<Contact> result = Lists.newArrayList(repository.findAllById(ids));

        assertThat(result.size()).isEqualTo(2);

        repository.deleteAll(contacts);

        final List<Contact> result2 = Lists.newArrayList(repository.findAllById(ids));
        assertThat(result2.size()).isEqualTo(0);
    }

    @Test
    public void testCustomQuery() {
        final List<Contact> result = repository.findByTitle(TEST_CONTACT.getTitle());

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getLogicId()).isEqualTo(TEST_CONTACT.getLogicId());
        assertThat(result.get(0).getTitle()).isEqualTo(TEST_CONTACT.getTitle());

    }

    @Test
    public void testNullIdContact() {
        final Contact nullIdContact = new Contact(null, "testTitile");
        final Contact savedContact = this.repository.save(nullIdContact);

        Assert.assertNotNull(savedContact.getLogicId());
        Assert.assertEquals(nullIdContact.getTitle(), savedContact.getTitle());
    }

    @Test
    public void testFindById() {
        final Optional<Contact> optional = this.repository.findById(TEST_CONTACT.getLogicId());

        Assert.assertTrue(optional.isPresent());
        Assert.assertEquals(TEST_CONTACT, optional.get());
        Assert.assertFalse(this.repository.findById("").isPresent());
    }
}

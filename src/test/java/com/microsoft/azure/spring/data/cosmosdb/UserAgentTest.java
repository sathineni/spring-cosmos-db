/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.data.cosmosdb;

import com.microsoft.azure.spring.data.cosmosdb.common.PropertyLoader;
import com.microsoft.azure.spring.data.cosmosdb.common.TestConstants;
import com.microsoft.azure.spring.data.cosmosdb.config.DocumentDBConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PropertyLoader.class)
@PowerMockIgnore({"javax.net.ssl.*", "javax.crypto.*"})
public class UserAgentTest {
    private static final String TEST_VERSION = "1.0.0-FOR-TEST";

    @Test
    public void testUserAgentSuffixAppended() {
        PowerMockito.mockStatic(PropertyLoader.class);
        BDDMockito.given(PropertyLoader.getProjectVersion()).willReturn(TEST_VERSION);

        assertThat(PropertyLoader.getProjectVersion()).isEqualTo(TEST_VERSION);

        final DocumentDBConfig dbConfig = DocumentDBConfig.builder("https://uri", "key", TestConstants.DB_NAME).build();
        final DocumentDbFactory factory = new DocumentDbFactory(dbConfig);
        assertThat(factory.getDocumentClient().getConnectionPolicy().getUserAgentSuffix()).contains(TEST_VERSION);
    }

}

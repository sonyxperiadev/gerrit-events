/*
 *  The MIT License
 *
 *  Copyright 2026 Amarula Solutions All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.sonymobile.tools.gerrit.gerritevents.helpers;

import org.apache.http.HttpHost;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

//CS IGNORE MagicNumber FOR NEXT 50 LINES. REASON: Test data.

/**
 * Tests for {@link HttpClientFactory}.
 */
public class HttpClientFactoryTest {

    /**
     * A null or empty proxy URL resolves to no proxy.
     */
    @Test
    public void testResolveProxyNullAndEmpty() {
        assertNull("null URL should resolve to no proxy", HttpClientFactory.resolveProxy(null));
        assertNull("empty URL should resolve to no proxy", HttpClientFactory.resolveProxy(""));
    }

    /**
     * A valid proxy URL resolves to the correct host, port and scheme.
     */
    @Test
    public void testResolveProxyValidUrl() {
        HttpHost proxy = HttpClientFactory.resolveProxy("http://proxy.example.com:8080");
        assertNotNull("Proxy should be set for valid URL", proxy);
        assertEquals("proxy.example.com", proxy.getHostName());
        assertEquals(8080, proxy.getPort());
        assertEquals("http", proxy.getSchemeName());
    }

    /**
     * A malformed proxy URL resolves to no proxy and does not throw.
     */
    @Test
    public void testResolveProxyInvalidUrl() {
        assertNull("malformed URL should resolve to no proxy",
                HttpClientFactory.resolveProxy(":::not-a-valid-url:::"));
    }

    /**
     * {@link HttpClientFactory#createClient} produces a usable client regardless of proxy validity.
     */
    @Test
    public void testCreateClient() {
        Credentials creds = new UsernamePasswordCredentials("user", "pass");
        HttpClient client = HttpClientFactory.createClient(creds, "http://proxy.example.com:8080");
        assertNotNull("Client should be created with valid proxy", client);
        assertNotNull("Client should be created with null proxy",
                HttpClientFactory.createClient(creds, null));
        assertNotNull("Client should be created even with invalid proxy",
                HttpClientFactory.createClient(creds, ":::not-a-valid-url:::"));
    }
}

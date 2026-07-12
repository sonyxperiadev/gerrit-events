package com.sonymobile.tools.gerrit.gerritevents.helpers;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating pre-configured {@link HttpClient} instances with
 * HTTP credentials and optional proxy support.
 *
 * <p>Centralises the {@code BasicCredentialsProvider} + proxy construction
 * that was duplicated across {@code GerritRestQueryHandler},
 * {@code GerritRestPoller}, {@code AbstractRestCommandJob} and
 * {@code AbstractRestCommandJob2}.</p>
 */
public final class HttpClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientFactory.class);

    /**
     * Utility class — no instantiation.
     */
    private HttpClientFactory() {
    }

    /**
     * Creates an {@link HttpClient} configured with the given credentials
     * and optional HTTP proxy URL.
     *
     * @param credentials the HTTP credentials (may be {@code null}).
     * @param proxyUrl    the proxy URL, or {@code null}/empty to skip.
     * @return a configured {@link HttpClient}.
     */
    public static HttpClient createClient(Credentials credentials, String proxyUrl) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, credentials);

        HttpClientBuilder builder = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider);

        HttpHost proxy = resolveProxy(proxyUrl);
        if (proxy != null) {
            builder.setProxy(proxy);
        }

        return builder.build();
    }

    /**
     * Resolves a proxy URL string into an {@link HttpHost}, or {@code null} if none should be used.
     * Package-private (rather than folded into {@link #createClient}) as a test seam so the proxy
     * decision can be asserted directly instead of reflecting into the built client. Not public API.
     *
     * @param proxyUrl the proxy URL, or {@code null}/empty for no proxy.
     * @return the proxy host, or {@code null} if not set or the URL is malformed.
     */
    static HttpHost resolveProxy(String proxyUrl) {
        if (proxyUrl == null || proxyUrl.isEmpty()) {
            return null;
        }
        try {
            URL url = new URL(proxyUrl);
            return new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        } catch (MalformedURLException e) {
            logger.warn("Could not parse HTTP proxy URL, proceeding without proxy: {}",
                    e.getMessage());
            return null;
        }
    }
}

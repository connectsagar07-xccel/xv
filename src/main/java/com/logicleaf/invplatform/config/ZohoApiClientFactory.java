package com.logicleaf.invplatform.config;

import com.invplatform.generated.salesorder.invoker.ApiClient;
import com.logicleaf.invplatform.model.OAuthToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Factory that creates ApiClient instances configured with the correct basePath and Authorization header
 * using the OAuthToken (which contains apiDomain + accessToken).
 */
@Component
public class ZohoApiClientFactory {

    /**
     * fallback base url (may already include /books/v3)
     * e.g. "https://www.zohoapis.com/books/v3"
     */
    @Value("${zoho.api.base-url}")
    private String fallbackApiBaseUrl;

    /**
     * Create an ApiClient for the provided token.
     */
    public ApiClient createApiClient(OAuthToken token) {
        ApiClient client = new ApiClient();

        // Prefer apiDomain from token if present, else use fallback property
        if (token != null && StringUtils.hasText(token.getApiDomain())) {
            String base = token.getApiDomain();
            // ensure path ends in /books/v3
            if (!base.endsWith("/books/v3")) {
                base = base.endsWith("/") ? base + "books/v3" : base + "/books/v3";
            }
            client.setBasePath(base);
        } else {
            // fallback (property might already contain /books/v3)
            client.setBasePath(fallbackApiBaseUrl.replaceAll("/+$", ""));
        }

        if (token != null && StringUtils.hasText(token.getAccessToken())) {
            client.addDefaultHeader("Authorization", "Zoho-oauthtoken " + token.getAccessToken());
        }

        return client;
    }
}

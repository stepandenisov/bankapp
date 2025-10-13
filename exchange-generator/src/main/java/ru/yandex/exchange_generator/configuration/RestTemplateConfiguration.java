package ru.yandex.exchange_generator.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;

@Configuration
public class RestTemplateConfiguration {
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

//    @Autowired
//    private OAuth2AuthorizedClientRepository authorizedClientRepository;

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(OAuth2AuthorizedClientService authorizedClientService) {
        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);

        manager.setAuthorizedClientProvider(authorizedClientProvider);

        return manager;
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(OAuth2AuthorizedClientManager authorizedClientManager) {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    return;
                }
                super.handleError(response);
            }
        });

        restTemplate.setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    OAuth2AuthorizeRequest authRequest = OAuth2AuthorizeRequest
                            .withClientRegistrationId("keycloak")
                            .principal("exchange-generator-service")
                            .build();

                    OAuth2AuthorizedClient client =
                            authorizedClientManager.authorize(authRequest);

                    if (client == null) {
                        throw new IllegalStateException("Cannot authorize OAuth2 client");
                    }

                    OAuth2AccessToken accessToken = client.getAccessToken();
                    request.getHeaders().add("Authorization", "Bearer " + accessToken.getTokenValue());
                    return execution.execute(request, body);
                }));

        return restTemplate;
    }
}


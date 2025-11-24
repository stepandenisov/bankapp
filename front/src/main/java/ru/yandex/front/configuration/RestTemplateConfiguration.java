package ru.yandex.front.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.client.RestTemplateBuilder;

import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.security.oauth2.client.*;

import java.io.IOException;

@Configuration
@Profile("!test")
public class RestTemplateConfiguration {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            OAuth2AuthorizedClientManager authorizedClientManager
    ) {

        return builder
                .errorHandler(new SafeErrorHandler())
                .additionalInterceptors(new OAuth2Interceptor(authorizedClientManager))
                .build();
    }

    static class SafeErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return;
            }
            super.handleError(response);
        }
    }

    static class OAuth2Interceptor implements ClientHttpRequestInterceptor {

        private final OAuth2AuthorizedClientManager clientManager;

        OAuth2Interceptor(OAuth2AuthorizedClientManager clientManager) {
            this.clientManager = clientManager;
        }

        @Override
        public ClientHttpResponse intercept(
                org.springframework.http.HttpRequest request,
                byte[] body,
                org.springframework.http.client.ClientHttpRequestExecution execution
        ) throws IOException {

            if (!request.getHeaders().containsKey("Authorization")) {

                OAuth2AuthorizeRequest authRequest =
                        OAuth2AuthorizeRequest.withClientRegistrationId("keycloak")
                                .principal("front")
                                .build();

                OAuth2AuthorizedClient client = clientManager.authorize(authRequest);

                if (client == null) {
                    throw new IllegalStateException("Cannot authorize OAuth2 client");
                }

                request.getHeaders().add(
                        "Authorization",
                        "Bearer " + client.getAccessToken().getTokenValue()
                );
            }

            return execution.execute(request, body);
        }
    }
}

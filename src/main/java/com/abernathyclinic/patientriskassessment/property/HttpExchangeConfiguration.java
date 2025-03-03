package com.abernathyclinic.patientriskassessment.property;

import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * **Deprecated:** The `/actuator/trace` endpoint was removed in Spring Boot 2.2.0 and later due to security concerns and bugs.
 * <p>
 * Use the `/actuator/httpexchange` endpoint instead. It offers a more secure and controlled way to access HTTP exchange information.
 * <p>
 * **Custom HttpExchangeRepository:** Since the default settings weren't suitable for the `/actuator/httpexchange` endpoint in this application,
 * a custom `InMemoryHttpExchangeRepository` bean was created.
 * <p>
 * Reference: https://stackoverflow.com/questions/59115578/httptrace-endpoint-of-spring-boot-actuator-doesnt-exist-anymore-with-spring-b
 */
@Configuration
public class HttpExchangeConfiguration {

    @Bean
    public HttpExchangeRepository createTraceRepository() {
        return new InMemoryHttpExchangeRepository();
    }
}
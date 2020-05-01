/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import org.bremersee.web.app.reactive.WebfluxTestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

/**
 * The webflux in memory integration test.
 *
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = WebfluxTestConfiguration.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.web-application-type=reactive",
        "spring.application.name=reactive-test",
        "bremersee.security.authentication.resource-server-auto-configuration=true",
        "bremersee.security.authentication.enable-jwt-support=false",
        "bremersee.security.authentication.basic-auth-users[0].name=user",
        "bremersee.security.authentication.basic-auth-users[0].password=user",
        "bremersee.security.authentication.basic-auth-users[0].authorities=ROLE_USER",
        "bremersee.security.authentication.basic-auth-users[1].name=admin",
        "bremersee.security.authentication.basic-auth-users[1].password=admin",
        "bremersee.security.authentication.basic-auth-users[1].authorities=ROLE_ADMIN",
        "bremersee.security.authentication.basic-auth-users[2].name=someone",
        "bremersee.security.authentication.basic-auth-users[2].password=someone",
        "bremersee.security.authentication.basic-auth-users[2].authorities=ROLE_SOMETHING",
        "bremersee.security.authentication.any-access-mode=deny_all",
        "bremersee.security.authentication.path-matchers[0].ant-pattern=/public/**",
        "bremersee.security.authentication.path-matchers[0].access-mode=permit_all",
        "bremersee.security.authentication.path-matchers[1].ant-pattern=/protected/**",
        "bremersee.security.authentication.path-matchers[1].roles=ROLE_USER",
        "bremersee.security.authentication.path-matchers[2].ant-pattern=/protected/**",
        "bremersee.security.authentication.path-matchers[2].http-method=POST",
        "bremersee.security.authentication.path-matchers[2].roles=ROLE_ADMIN",
        "bremersee.exception-mapping.api-paths=/**",
    })
@TestInstance(Lifecycle.PER_CLASS) // allows us to use @BeforeAll with a non-static method
public class WebfluxInMemoryIntegrationTest {

  /**
   * The application context.
   */
  @Autowired
  ApplicationContext context;

  /**
   * The test web client (security configuration is by-passed).
   */
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  WebTestClient webTestClient;

  /**
   * The local server port.
   */
  @LocalServerPort
  int port;

  /**
   * Base url of the local server.
   *
   * @return the base url of the local server
   */
  String baseUrl() {
    return "http://localhost:" + port;
  }

  /**
   * Creates a new web client, that uses the real security configuration.
   *
   * @return the web client
   */
  WebClient newWebClient() {
    return WebClient.builder()
        .baseUrl(baseUrl())
        .build();
  }

  /**
   * Setup tests.
   */
  @BeforeAll
  void setUp() {
    // https://docs.spring.io/spring-security/site/docs/current/reference/html/test-webflux.html
    WebTestClient
        .bindToApplicationContext(this.context)
        .configureClient()
        .build();
  }

  /**
   * Gets public.
   */
  @Test
  void getPublic() {
    webTestClient
        .get()
        .uri("/public")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(response -> assertEquals("public", response));
  }

  /**
   * Gets protected.
   */
  @Test
  void getProtected() {
    StepVerifier.create(newWebClient()
        .get()
        .uri("/protected")
        .headers(httpHeaders -> httpHeaders
            .setBasicAuth("user", "user", StandardCharsets.UTF_8))
        .retrieve()
        .bodyToMono(String.class))
        .assertNext(body -> assertEquals("protected", body))
        .verifyComplete();
  }

  /**
   * Gets protected anf expect forbidden.
   */
  @Test
  void getProtectedAnfExpectForbidden() {
    StepVerifier.create(newWebClient()
        .get()
        .uri("/protected")
        .headers(httpHeaders -> httpHeaders
            .setBasicAuth("someone", "someone", StandardCharsets.UTF_8))
        .exchange())
        .assertNext(clientResponse -> assertEquals(
            HttpStatus.FORBIDDEN,
            clientResponse.statusCode()))
        .verifyComplete();
  }

  /**
   * Post protected.
   */
  @WithMockUser(username = "admin", password = "admin", authorities = "ROLE_ADMIN")
  @Test
  void postProtected() {
    webTestClient
        .post()
        .uri("/protected")
        .contentType(MediaType.TEXT_PLAIN)
        .accept(MediaType.TEXT_PLAIN)
        .body(BodyInserters.fromValue("hello"))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .value(response -> assertEquals("hello", response));
  }

}

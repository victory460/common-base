/*
 * Copyright 2019 the original author or authors.
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

package org.bremersee.security.authentication;

import java.util.Arrays;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * The password flow properties.
 *
 * @author Christian Bremer
 */
public interface PasswordFlowProperties extends ClientCredentialsFlowProperties {

  /**
   * Gets username.
   *
   * @return the username
   */
  String getUsername();

  /**
   * Gets password.
   *
   * @return the password
   */
  String getPassword();

  @Override
  default Optional<BasicAuthProperties> getBasicAuthProperties() {
    return Optional.empty();
  }

  @Override
  default String createCacheKey() {
    return getTokenEndpoint()
        + ":" + getClientId()
        + ":" + getClientSecret()
        + ":" + getUsername()
        + ":" + getPassword();
  }

  @Override
  default MultiValueMap<String, String> createBody() {
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    if (getAdditionalProperties() != null) {
      body.addAll(getAdditionalProperties());
    }
    body.set("grant_type", "password");
    body.set("client_id", getClientId());
    if (getClientSecret() != null) {
      body.set("client_secret", getClientSecret());
    } else {
      body.set("client_secret", "");
    }
    body.set("username", getUsername());
    if (getPassword() != null) {
      body.set("password", getPassword());
    } else {
      body.set("password", "");
    }
    return body;
  }

  /**
   * Returns a builder for password flow properties.
   *
   * @return the builder
   */
  static Builder builder() {
    return new Builder();
  }

  /**
   * The builder implementation.
   */
  @ToString(exclude = {"clientSecret", "password"})
  @EqualsAndHashCode(exclude = {"clientSecret", "password"})
  class Builder {

    private String tokenEndpoint;

    private String clientId;

    private String clientSecret;

    private String username;

    private String password;

    private final MultiValueMap<String, String> additionalProperties = new LinkedMultiValueMap<>();

    /**
     * Sets token endpoint on builder.
     *
     * @param tokenEndpoint the token endpoint
     * @return the builder
     */
    public Builder tokenEndpoint(String tokenEndpoint) {
      this.tokenEndpoint = tokenEndpoint;
      return this;
    }

    /**
     * Sets client id on builder.
     *
     * @param clientId the client id
     * @return the builder
     */
    public Builder clientId(String clientId) {
      this.clientId = clientId;
      return this;
    }

    /**
     * Sets client secret on builder.
     *
     * @param clientSecret the client secret
     * @return the builder
     */
    public Builder clientSecret(String clientSecret) {
      this.clientSecret = clientSecret;
      return this;
    }

    /**
     * Sets username on builder.
     *
     * @param username the username
     * @return the builder
     */
    public Builder username(String username) {
      this.username = username;
      return this;
    }

    /**
     * Sets password on builder.
     *
     * @param password the password
     * @return the builder
     */
    public Builder password(String password) {
      this.password = password;
      return this;
    }

    /**
     * Adds an additional property on builder.
     *
     * @param key the key
     * @param values the values
     * @return the builder
     */
    public Builder add(String key, String... values) {
      if (StringUtils.hasText(key) && values != null) {
        additionalProperties.addAll(key, Arrays.asList(values));
      }
      return this;
    }

    /**
     * Gets the values from the given properties.
     *
     * @param properties the properties
     * @return the builder
     */
    public Builder from(ClientCredentialsFlowProperties properties) {
      if (properties != null) {
        if (properties.getAdditionalProperties() != null) {
          this.additionalProperties.addAll(properties.getAdditionalProperties());
        }
        final String username = properties instanceof PasswordFlowProperties
            ? ((PasswordFlowProperties) properties).getUsername()
            : null;
        final String password = properties instanceof PasswordFlowProperties
            ? ((PasswordFlowProperties) properties).getPassword()
            : null;
        return tokenEndpoint(properties.getTokenEndpoint())
            .clientId(properties.getClientId())
            .clientSecret(properties.getClientSecret())
            .username(username)
            .password(password);
      }
      return this;
    }

    /**
     * Build password flow properties.
     *
     * @return the password flow properties
     */
    public PasswordFlowProperties build() {
      return new Impl(
          tokenEndpoint,
          clientId,
          clientSecret,
          username,
          password,
          additionalProperties);
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @ToString(exclude = {"clientSecret", "password"})
    @EqualsAndHashCode
    private static class Impl implements PasswordFlowProperties {

      private final String tokenEndpoint;

      private final String clientId;

      private final String clientSecret;

      private final String username;

      private final String password;

      private final MultiValueMap<String, String> additionalProperties;
    }
  }

}

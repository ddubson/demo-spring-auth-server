/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ddubson.demo.auth.authentication;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.oidc.OidcClientRegistration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dmitriy Dubson
 */
public class CustomClientRegistrationConverterTests {

	@Test
	void convertsRegisteredClientToOidcClientRegistrationWithCustomMetadata() {
		AuthorizationServerContextHolder.setContext(new TestAuthorizationServerContext());
		// @formatter:off
		RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
			.clientId("client-1")
			.clientSecret("{noop}secret")
			.clientIdIssuedAt(Instant.now())
			.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
			.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
			.redirectUri("http://127.0.0.1:8080/authorized")
			.scope(OidcScopes.OPENID)
			.scope("message.read")
			.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true)
					.setting("testdata", "some-test-data")
					.setting("unregistered-testdata", "unregistered")
					.build())
			.build();
		// @formatter:on

		List<String> customMetadataClaimNames = List.of("testdata");
		CustomClientRegistrationConverter converter = new CustomClientRegistrationConverter(customMetadataClaimNames);
		OidcClientRegistration oidcClientRegistration = converter.convert(client);

		assertThat(oidcClientRegistration.<String>getClaim("testdata")).isEqualTo("some-test-data");
		assertThat(oidcClientRegistration.<String>getClaim("unregistered-testdata")).isNull();
	}

	static class TestAuthorizationServerContext implements AuthorizationServerContext {
		private static final String testIssuer = "https://auth-server.com";

		private static final AuthorizationServerSettings settings = AuthorizationServerSettings.builder().issuer(testIssuer).build();

		@Override
		public String getIssuer() {
			return testIssuer;
		}

		@Override
		public AuthorizationServerSettings getAuthorizationServerSettings() {
			return settings;
		}
	}
}

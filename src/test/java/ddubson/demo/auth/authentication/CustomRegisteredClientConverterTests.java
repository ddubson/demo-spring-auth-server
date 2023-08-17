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
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.oidc.OidcClientRegistration;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dmitriy Dubson
 */
class CustomRegisteredClientConverterTests {
	@Test
	void convertsRegisteredClientToOidcClientRegistrationWithCustomMetadata() {
		// @formatter:off
		OidcClientRegistration clientRegistration = OidcClientRegistration.builder()
				.clientName("client-name")
				.redirectUri("https://client.example.com")
				.grantType(AuthorizationGrantType.AUTHORIZATION_CODE.getValue())
				.grantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue())
				.scope("scope1")
				.scope("scope2")
				.claim("testdata", "some-test-data")
				.claim("unregistered-testdata", "unregistered")
				.build();
		// @formatter:on

		List<String> customClientMetadataNames = List.of("testdata");
		CustomRegisteredClientConverter converter = new CustomRegisteredClientConverter(customClientMetadataNames);
		RegisteredClient client = converter.convert(clientRegistration);

		ClientSettings clientSettings = client.getClientSettings();
		assertThat(clientSettings.<String>getSetting("testdata")).isEqualTo("some-test-data");
		assertThat(clientSettings.<String>getSetting("unregistered-testdata")).isNull();
	}
}

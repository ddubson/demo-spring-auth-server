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

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.oidc.OidcClientRegistration;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcClientRegistrationRegisteredClientConverter;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author Dmitriy Dubson
 * @since 1.2
 */
public class CustomRegisteredClientConverter implements Converter<OidcClientRegistration, RegisteredClient> {
	private final List<String> customMetadata;

	private final OidcClientRegistrationRegisteredClientConverter delegate;

	public CustomRegisteredClientConverter(List<String> customMetadata) {
		this.customMetadata = customMetadata;
	}
	@Override
	public RegisteredClient convert(OidcClientRegistration clientRegistration) {
		RegisteredClient convertedClient = delegate.convert(clientRegistration);
		ClientSettings.Builder clientSettingsBuilder = ClientSettings
				.withSettings(convertedClient.getClientSettings().getSettings());

		if(!CollectionUtils.isEmpty(this.customMetadata)) {
			clientRegistration.getClaims().forEach((claim, value) -> {
				if(this.customMetadata.contains(claim)) {
					clientSettingsBuilder.setting(claim, value);
				}
			});
		}

		return RegisteredClient.from(convertedClient).clientSettings(clientSettingsBuilder.build()).build();
	}
}

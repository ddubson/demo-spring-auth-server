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
import org.springframework.security.oauth2.server.authorization.oidc.authentication.RegisteredClientOidcClientRegistrationConverter;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Dmitriy Dubson
 * @since 1.2
 */
public class CustomClientRegistrationConverter implements Converter<RegisteredClient, OidcClientRegistration> {
	private final List<String> customMetadata;

	private final RegisteredClientOidcClientRegistrationConverter delegate;

	public CustomClientRegistrationConverter(List<String> customMetadata) {
		this.customMetadata = customMetadata;
	}

	@Override
	public OidcClientRegistration convert(RegisteredClient registeredClient) {
		OidcClientRegistration clientRegistration = delegate.convert(registeredClient);
		Map<String, Object> claims = new HashMap<>(clientRegistration.getClaims());
		if (!CollectionUtils.isEmpty(customMetadata)) {
			ClientSettings clientSettings = registeredClient.getClientSettings();

			claims.putAll(customMetadata.stream()
					.filter(metadatum -> clientSettings.getSetting(metadatum) != null)
					.collect(Collectors.toMap(Function.identity(), clientSettings::getSetting)));
		}
		return OidcClientRegistration.withClaims(claims).build();
	}
}

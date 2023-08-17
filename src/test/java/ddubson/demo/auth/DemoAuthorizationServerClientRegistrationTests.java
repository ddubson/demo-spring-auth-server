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
package ddubson.demo.auth;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Base64;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Dmitriy Dubson
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class DemoAuthorizationServerClientRegistrationTests {
	@Autowired
	private MockMvc mvc;

	private static final HttpMessageConverter<OAuth2AccessTokenResponse> accessTokenHttpResponseConverter =
			new OAuth2AccessTokenResponseHttpMessageConverter();

	@Test
	void registerANewClientDynamicallyWithCustomMetadata() throws Exception {
		String tokenRequestBody = """
			scope=client.create&grant_type=client_credentials
		""";

		String encodedClientCreds = Base64.getEncoder().encodeToString("client-creator:secret2".getBytes());
		MvcResult tokenResponse = this.mvc.perform(post("/oauth2/token")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.header(HttpHeaders.AUTHORIZATION, "Basic " + encodedClientCreds)
				.content(tokenRequestBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.access_token").exists())
				.andReturn();
		OAuth2AccessTokenResponse token = readAccessTokenResponse(tokenResponse);

		String clientRegistrationBody = """
			{
				"client_name": "client-1",
				"grant_types": ["authorization_code"],
				"redirect_uris": ["https://client.example.org/callback", "https://client.example.org/callback2"],
				"scope": "openid email profile",
				"logo_uri": "https://client.example.org/logo.png"
		    }
		""";

		MvcResult clientRegistrationResponse = this.mvc.perform(post("/connect/register")
				.accept(MediaType.APPLICATION_JSON_VALUE)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken().getTokenValue())
				.content(clientRegistrationBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.client_name").value("client-1"))
				.andExpect(jsonPath("$.registration_access_token").exists())
				.andExpect(jsonPath("$.registration_client_uri").exists())
				.andExpect(jsonPath("$.grant_types").value(containsInAnyOrder("authorization_code")))
				.andExpect(jsonPath("$.redirect_uris").value(containsInAnyOrder(
						"https://client.example.org/callback", "https://client.example.org/callback2")))
				.andExpect(jsonPath("$.scope").value("openid profile email"))
				// Custom metadata 'logo_uri' must be included in the response
				.andExpect(jsonPath("$.logo_uri").value("https://client.example.org/logo.png"))
				.andReturn();

		String clientRegistrationResponseContent = clientRegistrationResponse.getResponse().getContentAsString();
		String registrationAccessToken = JsonPath.read(clientRegistrationResponseContent, "$.registration_access_token");
		String registrationClientUri = JsonPath.read(clientRegistrationResponseContent, "$.registration_client_uri");

		this.mvc.perform(get(registrationClientUri)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + registrationAccessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.client_name").value("client-1"))
				.andExpect(jsonPath("$.registration_access_token").doesNotExist())
				.andExpect(jsonPath("$.registration_client_uri").exists())
				.andExpect(jsonPath("$.grant_types").value(containsInAnyOrder("authorization_code")))
				.andExpect(jsonPath("$.redirect_uris").value(containsInAnyOrder(
						"https://client.example.org/callback", "https://client.example.org/callback2")))
				.andExpect(jsonPath("$.scope").value("openid profile email"))
				// Custom metadata 'logo_uri' must be included in the response
				.andExpect(jsonPath("$.logo_uri").value("https://client.example.org/logo.png"));
	}

	private static OAuth2AccessTokenResponse readAccessTokenResponse(MvcResult mvcResult) throws Exception {
		MockHttpServletResponse servletResponse = mvcResult.getResponse();
		MockClientHttpResponse httpResponse = new MockClientHttpResponse(
				servletResponse.getContentAsByteArray(), HttpStatus.valueOf(servletResponse.getStatus()));
		return accessTokenHttpResponseConverter.read(OAuth2AccessTokenResponse.class, httpResponse);
	}
}

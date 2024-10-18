package com.example.keycloak;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jboss.logging.Logger;
import jakarta.ws.rs.core.MultivaluedMap;

public class CustomClaimMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    public static final String PROVIDER_ID = "custom-claim-mapper";
    private static final Logger logger = Logger.getLogger(CustomClaimMapper.class);
    private static final String CUSTOM_CLAIM_PARAM_KEY = "custom_claim";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, CustomClaimMapper.class);
    }

    @Override
    public String getDisplayCategory() {
        return "Custom Token Claim Mapper";
    }

    @Override
    public String getDisplayType() {
        return "Custom Token Claim Mapper";
    }

    @Override
    public String getHelpText() {
        return "Adds custom key-value pairs to the token claims";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel,
            UserSessionModel userSession, KeycloakSession keycloakSession,
            ClientSessionContext clientSessionCtx) {
        try {

            // extract the request parameters from the token request which is part of the
            // keycloak session object.
            KeycloakContext keycloakContext = keycloakSession.getContext();
            keycloakContext.getHttpRequest().getMultiPartFormParameters().forEach((k, v) -> {
                logger.info("k:" + k + ", v:" + v);
            });

            keycloakContext.getHttpRequest()
                    .getMultiPartFormParameters()
                    .get(CUSTOM_CLAIM_PARAM_KEY);

            // Extract the multipart form parameters
            MultivaluedMap<String, String> formParameters = keycloakContext.getHttpRequest().getDecodedFormParameters();

            // Extract the value of the request parameter.
            List<String> customClaimValues = formParameters.get(CUSTOM_CLAIM_PARAM_KEY);

            if (customClaimValues != null && !customClaimValues.isEmpty()) {
                // Assuming FormatPartValue has a method getValue() that returns the string
                // value
                String customClaimValue = customClaimValues.get(0);
                logger.debug("request claim: " + customClaimValue);

                // Parse the JSON value of the parameter key
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(customClaimValue);

                // Iterate over the JSON object and add each key-value pair
                // as a claim key and claim value to the JWT Token.
                Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String key = field.getKey();
                    String value = field.getValue().asText();
                    logger.debug("Claim key: " + key + ", Claim value: " + value);

                    // Add the extracted key-value pairs to the token
                    token.getOtherClaims().put(key, value);
                }
            }
        } catch (Exception e) {
            logger.error("Error setting custom claim", e);
        }
    }
}

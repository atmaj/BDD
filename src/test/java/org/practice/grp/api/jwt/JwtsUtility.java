package org.practice.grp.api.jwt;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.gson.io.GsonSerializer;

public class JwtsUtility {
    private String privateKey;
    private JwtsStructure jwtRaw;
    private JwtsType keyType;

    public enum JwtsType {
        JWT, JWS
    }

    public JwtsUtility(File keyFile, String payload, JwtsType jwtsType) {
        try {
            privateKey = FileUtils.readFileToString(keyFile, StandardCharsets.UTF_8).trim();
            jwtRaw = new Gson().fromJson(payload, JwtsStructure.class);
            keyType = jwtsType;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String generateJwt() {
        KeyDecoder keyDecoder = new KeyDecoder(privateKey);
        String jws = Jwts.builder().serializeToJsonWith(new GsonSerializer<Map<String, ?>>()).setHeader(getJwtHeader())
                .setClaims(getJwtPayload()).signWith(keyDecoder.getPrivateKey(), SignatureAlgorithm.PS256).compact();

        return jws;
    }

    public String generateJws() {
        KeyDecoder keyDecoder = new KeyDecoder(privateKey);
        String jws = Jwts.builder().serializeToJsonWith(new GsonSerializer<Map<String, ?>>()).setHeader(getJwsHeader())
                .setClaims(getJwsPayload()).signWith(keyDecoder.getPrivateKey(), SignatureAlgorithm.PS256).compact();

        return jws;
    }

    private Map<String, Object> getJwtHeader() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("alg", jwtRaw.getHeader().getAlgorithm());
        headerMap.put("typ", jwtRaw.getHeader().getType());
        headerMap.put("kid", jwtRaw.getHeader().getKId());

        return headerMap;
    }

    private Claims getJwtPayload() {
        Claims openbanking_claims = Jwts.claims();
        openbanking_claims.put("value",
                jwtRaw.getPayload().getClaims().getIdToken().getOpenBankingIntentId().getConsentId());
        openbanking_claims.put("essential",
                jwtRaw.getPayload().getClaims().getIdToken().getOpenBankingIntentId().isEssential());

        Claims openbanking_intent_id = Jwts.claims();
        openbanking_intent_id.put("openbanking_intent_id", openbanking_claims);

        Claims id_token = Jwts.claims();
        id_token.put("id_token", openbanking_intent_id);

        Claims jwtClaims = Jwts.claims();
        jwtClaims.put("iss", jwtRaw.getPayload().getIssuer());
        jwtClaims.put("aud", jwtRaw.getPayload().getAudience());
        jwtClaims.put("response_type", jwtRaw.getPayload().getResponseType());
        jwtClaims.put("client_id", jwtRaw.getPayload().getClientId());
        jwtClaims.put("redirect_uri", jwtRaw.getPayload().getRedirectUri());
        jwtClaims.put("scope", jwtRaw.getPayload().getScope());
        jwtClaims.put("state", jwtRaw.getPayload().getState());
        jwtClaims.put("nonce", jwtRaw.getPayload().getNonce());
        jwtClaims.put("exp", jwtRaw.getPayload().getExpire());
        jwtClaims.put("max_age", jwtRaw.getPayload().getMaxAge());
        jwtClaims.put("claims", id_token);

        return jwtClaims;
    }

    private Map<String, Object> getJwsHeader() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("alg", jwtRaw.getHeader().getAlgorithm());
        headerMap.put("typ", jwtRaw.getHeader().getType());
        headerMap.put("kid", jwtRaw.getHeader().getKId());
        headerMap.put("b64", jwtRaw.getHeader().isB64());
        headerMap.put("cty", jwtRaw.getHeader().getCty());
        headerMap.put(String.format(jwtRaw.getHeader().getIatKey()), jwtRaw.getHeader().getIat());
        headerMap.put(String.format(jwtRaw.getHeader().getIssKey()), jwtRaw.getHeader().getIss());
        headerMap.put(String.format(jwtRaw.getHeader().getTanKey()), jwtRaw.getHeader().getTan());
        headerMap.put("crit", jwtRaw.getHeader().getCrit());

        return headerMap;
    }

    private Claims getJwsPayload() {
        Claims jwsClaims = Jwts.claims();

        Claims remittanceInformationClaims = Jwts.claims();
        remittanceInformationClaims.put("Unstructured",
                jwtRaw.getPayload().getData().getInitiation().getRemittanceInformation().getUnstructured());
        remittanceInformationClaims.put("Reference",
                jwtRaw.getPayload().getData().getInitiation().getRemittanceInformation().getReference());

        Claims creditorAccountClaims = Jwts.claims();
        creditorAccountClaims.put("SchemeName",
                jwtRaw.getPayload().getData().getInitiation().getCreditorAccount().getSchemeName());
        creditorAccountClaims.put("Identification",
                jwtRaw.getPayload().getData().getInitiation().getCreditorAccount().getIdentification());
        creditorAccountClaims.put("Name", jwtRaw.getPayload().getData().getInitiation().getCreditorAccount().getName());
        creditorAccountClaims.put("SecondaryIdentification",
                jwtRaw.getPayload().getData().getInitiation().getCreditorAccount().getSecondaryIdentification());

        Claims instructedAmountClaims = Jwts.claims();
        instructedAmountClaims.put("Amount",
                jwtRaw.getPayload().getData().getInitiation().getInstructedAmount().getAmount());
        instructedAmountClaims.put("Currency",
                jwtRaw.getPayload().getData().getInitiation().getInstructedAmount().getCurrency());

        Claims initiationClaims = Jwts.claims();
        initiationClaims.put("InstructionIdentification",
                jwtRaw.getPayload().getData().getInitiation().getInstructionIdentification());
        initiationClaims.put("EndToEndIdentification",
                jwtRaw.getPayload().getData().getInitiation().getEndToEndIdentification());
        initiationClaims.put("InstructedAmount", instructedAmountClaims);
        initiationClaims.put("CreditorAccount", creditorAccountClaims);
        initiationClaims.put("RemittanceInformation", remittanceInformationClaims);

        Claims dataClaims = Jwts.claims();
        dataClaims.put("ConsentId", jwtRaw.getPayload().getData().getConsentId());
        dataClaims.put("Initiation", initiationClaims);

        Claims deliveryAddressClaims = Jwts.claims();
        deliveryAddressClaims.put("TownName", jwtRaw.getPayload().getRisk().getDeliveryAddress().getTownName());
        deliveryAddressClaims.put("Country", jwtRaw.getPayload().getRisk().getDeliveryAddress().getCountry());

        jwsClaims.put("Data", dataClaims);
        jwsClaims.put("Risk", deliveryAddressClaims);

        return jwsClaims;
    }
}
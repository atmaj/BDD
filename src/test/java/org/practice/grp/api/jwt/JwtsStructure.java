package org.practice.grp.api.jwt;

public class JwtsStructure {
    private JwsHeader header;
    private JwsPayload payload;

    public JwsHeader getHeader() {
        return header;
    }

    public JwsPayload getPayload() {
        return payload;
    }

    public class JwsHeader {
        private String alg;
        private String typ;
        private String kid;
        private boolean b64;
        private long iat;
        private String iss;
        private String tan;
        private String cty;

        public String getAlgorithm() {
            return alg;
        }

        public String getType() {
            return typ;
        }

        public String getKId() {
            return kid;
        }

        public boolean isB64() {
            return b64;
        }

        public long getIat() {
            return iat;
        }

        public String getIatKey() {
            return String.format("http://%s/iat", tan);
        }

        public String getIss() {
            return iss;
        }

        public String getIssKey() {
            return String.format("http://%s/iss", tan);
        }

        public String getTan() {
            return tan;
        }

        public String getTanKey() {
            return String.format("http://%s/tan", tan);
        }

        public String getCty() {
            return cty;
        }

        public String[] getCrit() {
            return new String[] { "b64", getIatKey(), getIssKey(), getTanKey() };
        }
    }

    public class JwsPayload {
        private String iss;
        private String aud;
        private String response_type;
        private String client_id;
        private String redirect_uri;
        private String scope;
        private String state;
        private String nonce;
        private long exp;
        private long max_age;
        private Claims claims;
        private JwsData Data;
        private JwsRisk Risk;

        public JwsData getData() {
            return Data;
        }

        public JwsRisk getRisk() {
            return Risk;
        }

        public String getIssuer() {
            return iss;
        }

        public String getAudience() {
            return aud;
        }

        public String getResponseType() {
            return response_type;
        }

        public String getClientId() {
            return client_id;
        }

        public String getRedirectUri() {
            return redirect_uri;
        }

        public String getScope() {
            return scope;
        }

        public String getState() {
            return state;
        }

        public String getNonce() {
            return nonce;
        }

        public long getExpire() {
            return exp;
        }

        public long getMaxAge() {
            return max_age;
        }

        public Claims getClaims() {
            return claims;
        }
    }

    public class JwsData {
        private JwsInitiation Initiation;
        private String ConsentId;

        public String getConsentId() {
            return ConsentId;
        }

        public JwsInitiation getInitiation() {
            return Initiation;
        }
    }

    public class JwsInitiation {
        private String InstructionIdentification;
        private String EndToEndIdentification;
        private JwsInstructedAmount InstructedAmount;
        private JwsCreditorAccount CreditorAccount;
        private JwsRemittanceInformation RemittanceInformation;

        public String getInstructionIdentification() {
            return InstructionIdentification;
        }

        public String getEndToEndIdentification() {
            return EndToEndIdentification;
        }

        public JwsInstructedAmount getInstructedAmount() {
            return InstructedAmount;
        }

        public JwsCreditorAccount getCreditorAccount() {
            return CreditorAccount;
        }

        public JwsRemittanceInformation getRemittanceInformation() {
            return RemittanceInformation;
        }
    }

    public class JwsInstructedAmount {
        private String Amount;
        private String Currency;

        public String getAmount() {
            return Amount;
        }

        public String getCurrency() {
            return Currency;
        }
    }

    public class JwsCreditorAccount {
        private String SchemeName;
        private String Identification;
        private String Name;
        private String SecondaryIdentification;

        public String getSchemeName() {
            return SchemeName;
        }

        public String getIdentification() {
            return Identification;
        }

        public String getName() {
            return Name;
        }

        public String getSecondaryIdentification() {
            return SecondaryIdentification;
        }
    }

    public class JwsRemittanceInformation {
        private String Unstructured;
        private String Reference;

        public String getUnstructured() {
            return Unstructured;
        }

        public String getReference() {
            return Reference;
        }
    }

    public class JwsRisk {
        private JwsDeliveryAddress DeliveryAddress;

        public JwsDeliveryAddress getDeliveryAddress() {
            return DeliveryAddress;
        }
    }

    public class JwsDeliveryAddress {
        private String TownName;
        private String Country;

        public String getTownName() {
            return TownName;
        }

        public String getCountry() {
            return Country;
        }
    }

    public class Claims {
        private Token id_token;

        public Token getIdToken() {
            return id_token;
        }
    }

    public class Token {
        private OpenBankingIntent openbanking_intent_id;

        public OpenBankingIntent getOpenBankingIntentId() {
            return openbanking_intent_id;
        }
    }

    public class OpenBankingIntent {
        private String value;
        private boolean essential;

        public String getConsentId() {
            return value;
        }

        public boolean isEssential() {
            return essential;
        }
    }
}
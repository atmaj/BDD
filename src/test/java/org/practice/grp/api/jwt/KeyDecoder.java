package org.practice.grp.api.jwt;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateCrtKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class KeyDecoder {
    private final String BEGIN_RSA_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----";
    private final String END_RSA_PRIVATE_KEY = "-----END RSA PRIVATE KEY-----";
    private String rsaPrivateKey;
    private KeyPair keyPair;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public KeyDecoder(String privateKey) {
        privateKey = privateKey.replaceAll("\\n", "").replaceAll("\\r", "");
        rsaPrivateKey = privateKey.replace(BEGIN_RSA_PRIVATE_KEY, "").replace(END_RSA_PRIVATE_KEY, "");
        generateKeyPair(rsaPrivateKey);
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    private void generateKeyPair(String privateKey) {
        byte[] privateKeyBytes = Base64.getMimeDecoder().decode(privateKey);
        BCRSAPrivateCrtKey pvtKey = (BCRSAPrivateCrtKey) fetchPrivateKey(privateKeyBytes);
        PublicKey pubKey = null;

        try {
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(pvtKey.getModulus(), pvtKey.getPublicExponent());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            pubKey = keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        keyPair = new KeyPair(pubKey, pvtKey);
    }

    private PrivateKey fetchPrivateKey(byte[] privateKeyBytes) {
        try {
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

//	public String extract(String privateKeyString) throws Exception {
//		BCRSAPrivateCrtKey rsaPrivateKey = null;// (BCRSAPrivateCrtKey) getPrivateKey(privateKeyString);
//
//		@SuppressWarnings("null")
//		RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
//				.generatePublic(new RSAPublicKeySpec(rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent()));
//
//		byte[] bytes = encodePublicKey(publicKey);
//		return "ssh-rsa " + new String(Base64.getEncoder().encode(bytes), StandardCharsets.UTF_8) + " some@user";
//	}
//
//	private byte[] encodePublicKey(RSAPublicKey key) throws IOException {
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		/* encode the "ssh-rsa" string */
//		byte[] sshrsa = new byte[] { 0, 0, 0, 7, 's', 's', 'h', '-', 'r', 's', 'a' };
//		out.write(sshrsa);
//		/* Encode the public exponent */
//		BigInteger e = key.getPublicExponent();
//		byte[] data = e.toByteArray();
//		encodeUInt32(data.length, out);
//		out.write(data);
//		/* Encode the modulus */
//		BigInteger m = key.getModulus();
//		data = m.toByteArray();
//		encodeUInt32(data.length, out);
//		out.write(data);
//		return out.toByteArray();
//	}
//
//	private void encodeUInt32(int value, OutputStream out) throws IOException {
//		byte[] tmp = new byte[4];
//		tmp[0] = (byte) ((value >>> 24) & 0xff);
//		tmp[1] = (byte) ((value >>> 16) & 0xff);
//		tmp[2] = (byte) ((value >>> 8) & 0xff);
//		tmp[3] = (byte) (value & 0xff);
//		out.write(tmp);
//	}
}
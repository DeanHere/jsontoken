package net.oauth.jsontoken;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Duration;
import org.joda.time.Instant;

import net.oauth.jsontoken.crypto.HmacSHA256Signer;
import net.oauth.jsontoken.crypto.HmacSHA256Verifier;
import net.oauth.jsontoken.crypto.SignatureAlgorithm;
import net.oauth.jsontoken.crypto.Verifier;
import net.oauth.jsontoken.discovery.VerifierProvider;
import net.oauth.jsontoken.discovery.VerifierProviders;
import net.oauth.signatures.SignedTokenAudienceChecker;

public class JwtUtil2 {
    final static String ISSUER = "zr-i.com";
    final static String SIGNING_KEY = "09003938249038094884";
    static VerifierProviders verifierProviders = null;
    static{
        try {
            final Verifier hmacVerifier = new HmacSHA256Verifier(SIGNING_KEY.getBytes());
            VerifierProvider hmacLocator = new VerifierProvider() {
                @Override
                public List<Verifier> findVerifier(String signerId, String keyId) {
                    List<Verifier> list = new ArrayList<Verifier>();
                    list.add(hmacVerifier);
                    return list;
                }
            };
            verifierProviders = new VerifierProviders();
            verifierProviders.setVerifierProvider(SignatureAlgorithm.HS256, hmacLocator);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Map<String, Object> userMap = new LinkedHashMap<String, Object>();
        userMap.put("email", "steve@gmail.com");
        String jwt = getJwt(userMap);
        System.out.println("jwt = " + jwt);
        JsonToken token = Deserialize(jwt);
        System.out.println("token = " + token);
        token = VerifyAndDeserialize(jwt);
        System.out.println("token = " + token);
    }


    public static String getJwt(Map<String, Object> userMap) throws InvalidKeyException, SignatureException {
        JsonToken token = createToken(userMap);
        return token.serializeAndSign();
    }

    private static JsonToken createToken(Map<String, Object> userMap) throws InvalidKeyException {
        // Current time and signing algorithm
        HmacSHA256Signer signer = new HmacSHA256Signer(ISSUER, null, SIGNING_KEY.getBytes());

        // Configure JSON token with signer and SystemClock
        JsonToken token = new JsonToken(signer);
        token.setAudience("zr-i.com");
//        token.setParam("typ", "zr-i.com/auth/v1");
        token.setIssuedAt(Instant.now());
        //token.setExpiration(Instant.now().plusSeconds(3600));  // 1 hour
        token.setExpiration(Instant.now().plus(Duration.standardSeconds(3600)));  // 1 hour

        Map<String, Object> payload = token.getPayload();
        payload.put("user", userMap);
        return token;
    }

    public static JsonToken Deserialize(String jwt) throws Exception {
        JsonTokenParser parser = new JsonTokenParser(verifierProviders, new SignedTokenAudienceChecker("zr-i.com"));
        return parser.deserialize(jwt);
    }
    public static JsonToken VerifyAndDeserialize(String jwt) throws Exception {
        JsonTokenParser parser = new JsonTokenParser(verifierProviders, new SignedTokenAudienceChecker("zr-i.com"));
        return parser.verifyAndDeserialize(jwt);
    }
}
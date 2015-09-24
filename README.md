jsontoken
=========

This is a fork of original google jsontoken from the following url.

https://code.google.com/p/jsontoken/

What I have done:

=======
1. Apply changes from networknt/jsontoken (https://github.com/networknt/jsontoken), but still use Joda time, so it is good for Java 7.
2. Covert Json parser from Gson to fastjson.
3. Remove google collections from dependency list.
4. Add "JWT" as tpy in header.

I did NOT run the existing unit tests. And I did NOT change the pom.xml.
=======
Dependencies: Joda time, fastjson, commons-codec
=======
Here is a sample to generate token and verify the token.

```
public class JwtUtil2 {
    final static String ISSUER = "imissuer";
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
        token.setAudience("Audience");
        token.setIssuedAt(Instant.now());
        token.setExpiration(Instant.now().plus(Duration.standardSeconds(3600)));  // 1 hour

        Map<String, Object> payload = token.getPayload();
        payload.put("user", userMap);
        return token;
    }

    public static JsonToken Deserialize(String jwt) throws Exception {
        JsonTokenParser parser = new JsonTokenParser(verifierProviders, new SignedTokenAudienceChecker("Audience"));
        return parser.deserialize(jwt);
    }
    public static JsonToken VerifyAndDeserialize(String jwt) throws Exception {
        JsonTokenParser parser = new JsonTokenParser(verifierProviders, new SignedTokenAudienceChecker("Audience"));
        return parser.verifyAndDeserialize(jwt);
    }
}
```
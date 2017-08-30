package me.tigerhe.shoppingpal.singletons;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.OkHttpClient;
import okhttp3.Request;


/**
 * Singleton class for making signed requests to Amazon web advertising api
 */

public class SignedRequestsHelper {
    private static int TIMEOUT = 60000;
    private static SignedRequestsHelper instance;

    private static final String UTF8_CHARSET = "UTF-8";
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String REQUEST_URI = "/onca/xml";
    private static final String REQUEST_METHOD = "GET";
    private static final String WEB_SERVER = "https://shopping-pal-server.herokuapp.com/credentials";

    private String endpoint = "webservices.amazon.com"; // must be lowercase
    private String awsAccessKeyId;
    private String awsSecretKey;
    private OkHttpClient client;

    private SecretKeySpec secretKeySpec = null;
    private Mac mac = null;

    public static SignedRequestsHelper getInstance() {
        if (instance == null) {
            synchronized (SignedRequestsHelper.class) {
                if (instance == null) {
                    instance = new SignedRequestsHelper();
                }
            }
        }
        return instance;
    }

    private SignedRequestsHelper() {
        initCredentials();
    }

    public String sign(Map<String, String> params) {
        params.put("AWSAccessKeyId", awsAccessKeyId);
        params.put("Timestamp", timestamp());
        params.put("AssociateTag", "kzzhang08-20");
        params.put("Service", "AWSECommerceService");

        SortedMap<String, String> sortedParamMap =
                new TreeMap<String, String>(params);
        String canonicalQS = canonicalize(sortedParamMap);
        String toSign =
                REQUEST_METHOD + "\n"
                        + endpoint + "\n"
                        + REQUEST_URI + "\n"
                        + canonicalQS;

        String hmac = hmac(toSign);
        String sig = percentEncodeRfc3986(hmac);
        String url = "http://" + endpoint + REQUEST_URI + "?" +
                canonicalQS + "&Signature=" + sig.replaceAll("%3D%0A","%3D");

        return url;
    }

    private String hmac(String stringToSign) {
        String signature = null;
        byte[] data;
        byte[] rawHmac;
        try {
            data = stringToSign.getBytes(UTF8_CHARSET);
            rawHmac = mac.doFinal(data);
            signature = new String(Base64.encodeToString(rawHmac, Base64.DEFAULT));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(UTF8_CHARSET + " is unsupported!", e);
        }
        return signature;
    }

    private String timestamp() {
        String timestamp = null;
        Calendar cal = Calendar.getInstance();
        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dfm.setTimeZone(TimeZone.getTimeZone("GMT"));
        timestamp = dfm.format(cal.getTime());
        return timestamp;
    }

    private String canonicalize(SortedMap<String, String> sortedParamMap)
    {
        if (sortedParamMap.isEmpty()) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        Iterator<Map.Entry<String, String>> iter =
                sortedParamMap.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<String, String> kvpair = iter.next();
            buffer.append(percentEncodeRfc3986(kvpair.getKey()));
            buffer.append("=");
            buffer.append(percentEncodeRfc3986(kvpair.getValue()));
            if (iter.hasNext()) {
                buffer.append("&");
            }
        }
        String canonical = buffer.toString();
        return canonical;
    }

    private String percentEncodeRfc3986(String s) {
        String out;
        try {
            out = URLEncoder.encode(s, UTF8_CHARSET)
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            out = s;
        }
        return out;
    }

    private void initCredentials() {
        client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
        Request request = new Request.Builder()
                .get()
                .url(WEB_SERVER)
                .build();

        try {
            JSONObject result = new JSONObject(client.newCall(request).execute().body().string());
            awsAccessKeyId = result.getString("awsAccessKeyId");
            awsSecretKey = result.getString("awsSecretKey");
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        byte[] secretyKeyBytes = awsSecretKey.getBytes();
        secretKeySpec = new SecretKeySpec(secretyKeyBytes, HMAC_SHA256_ALGORITHM);
        try {
            mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        }
        catch (NoSuchAlgorithmException e) {
        }
        try {
            mac.init(secretKeySpec);
        }
        catch (InvalidKeyException e) {
        }
    }

    public OkHttpClient getClient() {
        return client;
    }

}
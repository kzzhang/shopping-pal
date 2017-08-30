package me.tigerhe.shoppingpal.models;

import android.util.Log;
import android.util.Xml;

import me.tigerhe.shoppingpal.singletons.SignedRequestsHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kzhan_000 on 2016-12-30.
 */

public class AmazonCart {
    private String checkout, cartID, HMAC;
    private List<AmazonProduct> products;
    boolean status = true;

    public AmazonCart() {
        products = new ArrayList<>();
        checkout = null;
        cartID = null;
        HMAC = null;
        status = true;
    }

    public AmazonCart(AmazonProduct input, Integer quantity){
        final AmazonProduct product = input;
        products = new ArrayList<>();
        products.add(input);
        final Integer number = quantity;
        Thread thread = new Thread(){
            public void run(){
                try{
                    createCart(product, number);
                    while(status == true) {
                        Thread.sleep(200);
                    }
                }catch (InterruptedException e){
                    Log.d("Interrupted", e.toString());
                }
            }
        };
        thread.start();
        try{
            thread.join();
            status = true;
        }catch (InterruptedException e){
            Log.d("Interrupted", e.toString());
        }
    }

    protected void createCart(final AmazonProduct input, final Integer quantity) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Operation", "CartCreate");
        map.put("Item.1.ASIN", input.ASIN);
        map.put("Item.1.Quantity", quantity.toString());

        SignedRequestsHelper url = SignedRequestsHelper.getInstance();
        final String query = url.sign(map);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(query, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                Log.d("Success", "create");
                String searchResult = new String(response);
                Log.d("Create CartActivity", searchResult);
                int index1, index2;
                index1 = searchResult.indexOf("<PurchaseURL>")+13;
                index2 = searchResult.indexOf("</PurchaseURL>");
                checkout = searchResult.substring(index1, index2);
                index1 = searchResult.indexOf("<CartId>")+8;
                index2 = searchResult.indexOf("</CartId>");
                cartID = searchResult.substring(index1, index2);
                index1 = searchResult.indexOf("<HMAC>")+6;
                index2 = searchResult.indexOf("</HMAC>");
                HMAC = searchResult.substring(index1, index2);

                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(new StringReader(searchResult));
                    int eventType = parser.getEventType();
                    String text = "";
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        String tagName = parser.getName();
                        switch (eventType) {
                            case (XmlPullParser.TEXT):
                                text = parser.getText();
                                Log.d("TEXT", text);
                                break;
                            case (XmlPullParser.END_TAG):
                                if (tagName.equalsIgnoreCase("CartItemId")) {
                                    input.cartItemId = text;
                                }
                                break;
                        }
                        eventType = parser.next();
                    }
                } catch (XmlPullParserException e1) {
                    e1.printStackTrace();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }

                status = false;
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("Failure", Integer.toString(statusCode));
            }
            @Override
            public void onRetry() {
                // called when request is retried
                Log.d("Retry", "asdf");
            }
        });
    }

    public void add(AmazonProduct input, Integer quantity){
        final AmazonProduct product = input;
        final Integer number = quantity;
        if (quantity > 0) {
            Thread thread = new Thread() {
                public void run() {
                    try {
                        if (products.size() == 0) {
                            createCart(product, number);
                        } else {
                            addProduct(product, number);
                        }
                        while (status == true) {
                            Thread.sleep(200);
                        }
                    } catch (InterruptedException e) {
                        Log.d("Interrupted", e.toString());
                    }
                }
            };
            thread.start();
            try {
                thread.join();
                status = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            checkout = checkout.replace("&amp;", "%26");
            input.quantity = quantity;
            products.add(input);
        }
    }

    protected void addProduct(final AmazonProduct input, Integer quantity) {
        if (quantity > 0) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("CartId", cartID);
            map.put("HMAC", HMAC);
            map.put("Operation", "CartAdd");
            map.put("Item.1.ASIN", input.ASIN);
            map.put("Item.1.Quantity", quantity.toString());

            SignedRequestsHelper url = SignedRequestsHelper.getInstance();
            final String query = url.sign(map);
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(query, new AsyncHttpResponseHandler() {
                @Override
                public void onStart() {
                    Log.d("Starting", "add");
                    status = false;
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                    // called when response HTTP status is "200 OK"
                    Log.d("Success", "add");
                    String searchResult = new String(response);
                    Log.d("add", searchResult);
                    int index1, index2;
                    index1 = searchResult.indexOf("<PurchaseURL>") + 13;
                    index2 = searchResult.indexOf("</PurchaseURL>");
                    checkout = searchResult.substring(index1, index2);

                    try {
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(new StringReader(searchResult));
                        int eventType = parser.getEventType();
                        String text = "";
                        int index = products.size() - 1;
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            String tagName = parser.getName();
                            switch (eventType) {
                                case (XmlPullParser.TEXT):
                                    text = parser.getText();
                                    Log.d("TEXT", text);
                                    break;
                                case (XmlPullParser.END_TAG):
                                    if (tagName.equalsIgnoreCase("CartItemId")) {
                                        products.get(index).cartItemId = text;
                                        index--;
                                    }
                                    break;
                            }
                            eventType = parser.next();
                        }
                    } catch (XmlPullParserException e1) {
                        e1.printStackTrace();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    Log.d("Failure", Integer.toString(statusCode));
                }

                @Override
                public void onRetry() {
                    // called when request is retried
                    Log.d("Retry", "asdf");
                }
            });
        }
    }

    public List<AmazonProduct> getProducts() {
        if (products == null) {
            products = new ArrayList<>();
        }
        return products;
    }

    public void reset() {
        if (products == null) {
            products = new ArrayList<>();
        } else {
            products.clear();
        }
        checkout = null;
        cartID = null;
        HMAC = null;
        status = true;
    }


    public String getCheckout() {
        return checkout;
    }

    public String getCartID() {
        return cartID;
    }

    public String getHMAC() {
        return HMAC;
    }
}

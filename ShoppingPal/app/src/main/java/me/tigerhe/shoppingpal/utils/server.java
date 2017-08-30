package me.tigerhe.shoppingpal.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import me.tigerhe.shoppingpal.models.AmazonCart;
import me.tigerhe.shoppingpal.models.AmazonProduct;
import me.tigerhe.shoppingpal.singletons.SignedRequestsHelper;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by kevin on 2017-06-22.
 */

public class server {
    public static void modify(AmazonCart cart, AmazonProduct product, Callback callback) throws IOException{
        OkHttpClient client = SignedRequestsHelper.getInstance().getClient();
        Map<String, String> params = new HashMap<>();
        params.put("CartId", cart.getCartID());
        params.put("Operation", "CartModify");
        params.put("HMAC", cart.getHMAC());
        params.put("Item.1.CartItemId", product.getCartItemId());
        params.put("Item.1.Quantity", String.valueOf(product.quantity));
        String url = SignedRequestsHelper.getInstance().sign(params);
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        if (callback != null) {
            client.newCall(request).enqueue(callback);
        } else {
            client.newCall(request).execute();
        }
    }
}

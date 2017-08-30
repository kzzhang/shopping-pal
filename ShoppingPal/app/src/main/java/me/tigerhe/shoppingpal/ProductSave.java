package me.tigerhe.shoppingpal;

import android.util.Log;

import me.tigerhe.shoppingpal.singletons.SignedRequestsHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by kzhan_000 on 2016-12-27.
 */

class ProductSave {
    private File target;
    private FileOutputStream fout;
    private HashMap<String, String> map;
    private String output = "";

    public ProductSave(File file, HashMap<String, String> input){
        Log.d("initialize", "first");
        map = input;
        target = file;
    }

    public void mapRemove(String key){
        map.remove(key);
    }

    public void mapAdd(String key, String value){
        map.put(key, value);
    }

    public String save(){
        Thread thread = new Thread(){
            @Override
            public void run(){
                try {
                    basicSave();
                    while(output.isEmpty()) {
                        Thread.sleep(200);
                    }
                }catch (InterruptedException e){
                    Log.d("Interrupted Sleep", e.toString());
                }
            }
        };
        thread.start();
        try {
            thread.join();
            return output;
        }catch (InterruptedException e){
            Log.d("Interrupted", e.toString());
        }

        return "error";
    }

    public void basicSave(){
        Log.d("basic", "second");
        //open file output stream
        try {
            fout = new FileOutputStream(target, false);
        }catch (IOException a){
            Log.d("Class", a.toString());
        }

        //sign request
        SignedRequestsHelper url = SignedRequestsHelper.getInstance();
        final String input = url.sign(map);
        AsyncHttpClient client = new AsyncHttpClient();
        //get basic details

        map.put("ResponseGroup", "Offers");
        final String pricequery = url.sign(map);
        this.savePrice(pricequery);

        map.remove("ResponseGroup");
        map.put("ResponseGroup", "Images");
        final String imgurlquery = url.sign(map);
        this.saveImage(imgurlquery);

        client.get(input, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                //Log.d("[HTTP REQUEST]", query);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                int index1, index2;
                String name, rating, ASIN, category, manufacturer, producturl;
                Log.d("Success", "first");
                String searchResult = new String(response);
                Log.d("basicSave", searchResult);

                index1 = searchResult.indexOf("<Title>")+7;
                index2 = searchResult.indexOf("</Title>");
                name = searchResult.substring(index1,index2);
                index1 = searchResult.indexOf("<ASIN>")+6;
                index2 = searchResult.indexOf("</ASIN>");
                ASIN = searchResult.substring(index1,index2);
                index1 = searchResult.indexOf("<ProductGroup>")+14;
                index2 = searchResult.indexOf("</ProductGroup>");
                category = searchResult.substring(index1,index2);
                index1 = searchResult.indexOf("<Manufacturer>")+14;
                index2 = searchResult.indexOf("</Manufacturer>");
                manufacturer = searchResult.substring(index1,index2);
                index1 = searchResult.indexOf("<DetailPageURL>")+15;
                index2 = searchResult.indexOf("</DetailPageURL>");
                producturl = searchResult.substring(index1, index2);
                index1 = searchResult.indexOf("All Customer Reviews</Description><URL>")+39;
                String temp = searchResult.substring(index1);
                index2 = temp.indexOf("</URL>");
                rating = temp.substring(0, index2);
                AsyncHttpClient newClient = new AsyncHttpClient();
                //find star rating
                newClient.get(rating, new AsyncHttpResponseHandler() {
                    @Override
                    public void onStart() {
                        Log.d("Starting", "second");
                        //Log.d("[HTTP REQUEST]", query);
                    }
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        // called when response HTTP status is "200 OK"
                        Log.d("Success", "second");
                        int index1, index2;
                        String searchResult = new String(response);
                        String stars;
                        index1 = searchResult.indexOf("Average Customer Rating:&nbsp;(")+31;
                        index2 = index1+3;
                        stars = searchResult.substring(index1, index2);

                        synchronized (fout) {
                            try {
                                fout.write(("Rating of product: " + stars).getBytes());
                                fout.write(("\n").getBytes());
                            } catch (IOException a) {
                                Log.d("Writing IO Exception", a.toString());
                            }
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        Log.d("Failure", Integer.toString(statusCode));
                        Log.d("BAISCFAILURE", responseBody.toString());
                        output = "Failure";
                    }
                    @Override
                    public void onRetry() {
                        // called when request is retried
                        Log.d("Retry", "asdf");
                    }

                    @Override
                    public void onFinish() {
                        try{
                            Log.d("Closing", "closing file stream");
                            fout.close();
                            FileInputStream fin = null;
                            int length = (int) target.length();
                            byte[] bytes = new byte[length];
                            try {
                                fin = new FileInputStream(target);
                                fin.read(bytes);
                            } catch (IOException e) {
                                Log.d("Reading File", e.toString());
                            } finally {
                                try {
                                    fin.close();
                                } catch (IOException e) {
                                    Log.d("Reading File", e.toString());
                                }
                            }
                            output = new String(bytes);
                        }catch (IOException a){
                            Log.d("Writing IO Exception", a.toString());
                        }
                    }
                });

                synchronized (fout) {
                    try {
                        fout.write(("Name of product: " + name).getBytes());
                        fout.write(("\n").getBytes());
                        fout.write(("ASIN of product: " + ASIN).getBytes());
                        fout.write(("\n").getBytes());
                        fout.write(("Category of product: " + category).getBytes());
                        fout.write(("\n").getBytes());
                        fout.write(("Manufacturer of product: " + manufacturer).getBytes());
                        fout.write(("\n").getBytes());
                        fout.write(("Url of product: " + producturl).getBytes());
                        fout.write(("\n").getBytes());
                    } catch (IOException a) {
                        Log.d("Writing IO exception", a.toString());
                    }
                    Log.d("Done", "First");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("Failure", Integer.toString(statusCode));
                output = "Failure";
            }

            @Override
            public void onRetry() {
                // called when request is retried
                Log.d("Retry", "asdf");
            }
        });
    }

    public void savePrice(String input){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(input, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                //Log.d("[HTTP REQUEST]", pricequery);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                int index1, index2;
                String price, amount;
                String searchResult = new String(response);
                Log.d("priceSave", searchResult);
                index1 = searchResult.indexOf("<TotalNew>")+10;
                index2 = searchResult.indexOf("</TotalNew>");
                amount = searchResult.substring(index1, index2);
                if (Integer.valueOf(amount)==0){
                    amount = "0";
                    price = "0";
                }
                else{
                    index1 = searchResult.indexOf("<LowestNewPrice>");
                    String temp = searchResult.substring(index1);
                    index1 = temp.indexOf("<FormattedPrice>$") + 17;
                    index2 = temp.indexOf("</FormattedPrice>");
                    price = temp.substring(index1, index2);
                }

                synchronized (fout) {
                    try {
                        fout.write(("Price of product: " + price).getBytes());
                        fout.write(("\n").getBytes());
                        fout.write(("Amount of product: " + amount).getBytes());
                        fout.write(("\n").getBytes());
                    } catch (IOException a) {
                        Log.d("Writing IO exception", a.toString());
                    }
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("Failure", Integer.toString(statusCode));
                output = "Failure";
            }
            @Override
            public void onRetry() {
                // called when request is retried
                Log.d("Retry", "asdf");
            }
        });
    }

    public void saveImage(String input){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(input, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                //Log.d("[HTTP REQUEST]", imgurlquery);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                int index1, index2;
                String imgurl;
                Log.d("Success", "fourth");
                String searchResult = new String(response);
                Log.d("imgSave", searchResult);
                index1 = searchResult.indexOf("<LargeImage><URL>")+17;
                String temp = searchResult.substring(index1);
                index2 = temp.indexOf("</URL>");
                imgurl = temp.substring(0, index2);
                try{
                    fout.write(("Imgurl of product: "+imgurl).getBytes());
                    fout.write(("\n").getBytes());
                }catch (IOException a){
                    Log.d("Write to file", a.toString());
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("Failure", Integer.toString(statusCode));
                output = "Failure";
            }
            @Override
            public void onRetry() {
                // called when request is retried
                Log.d("Retry", "asdf");
            }
        });
    }
}

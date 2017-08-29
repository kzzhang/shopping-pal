package me.tigerhe.shoppingpal.models;

import android.util.Log;

/**
 * Created by kzhan_000 on 2016-11-12.
 */


//saves information relevant to amazon product
//always check using isValid() to see if the product was successfully retrieved and populated!!

public class AmazonProduct {
    public String name;
    public Float price;
    public Integer amount;
    public Float rating;
    public String picture; //url
    public String ASIN;
    public String category;
    public String manufacturer;
    public String url;
    public Integer quantity; //amount to purchase

    public static String FAILURE = "@@@FAILURE@@@";

    public AmazonProduct(){
    }

    //populate amazonproduct from parsed string input
    public AmazonProduct(String XmlInput){
        if (XmlInput.equals("Failure")){
            ASIN = FAILURE;
            name = FAILURE;
            price = (float)0;
            amount = 0;
            rating = (float)0;
            picture = FAILURE;
            category = FAILURE;
            manufacturer = FAILURE;
            url = FAILURE;
        }

        else {
            int index1, index2;
            String temp;
            index1 = XmlInput.indexOf("Name of product: ") + 17;
            temp = XmlInput.substring(index1);
            index2 = temp.indexOf("\n");
            name = temp.substring(0, index2);

            index1 = XmlInput.indexOf("Price of product: ") + 18;
            temp = XmlInput.substring(index1);
            index2 = temp.indexOf("\n");
            price = Float.parseFloat(temp.substring(0, index2));

            index1 = XmlInput.indexOf("Amount of product: ") + 19;
            temp = XmlInput.substring(index1);
            index2 = temp.indexOf("\n");
            amount = Integer.parseInt(temp.substring(0, index2));

            index1 = XmlInput.indexOf("Rating of product: ") + 19;
            temp = XmlInput.substring(index1);
            index2 = temp.indexOf("\n");
            rating = Float.parseFloat(temp.substring(0, index2));

            index1 = XmlInput.indexOf("Imgurl of product: ") + 19;
            temp = XmlInput.substring(index1);
            index2 = temp.indexOf("\n");
            picture = temp.substring(0, index2);

            index1 = XmlInput.indexOf("ASIN of product: ") + 17;
            temp = XmlInput.substring(index1);
            index2 = temp.indexOf("\n");
            ASIN = temp.substring(0, index2);

            index1 = XmlInput.indexOf("Category of product: ") + 21;
            temp = XmlInput.substring(index1);
            index2 = temp.indexOf("\n");
            category = temp.substring(0, index2);

            index1 = XmlInput.indexOf("Manufacturer of product: ") + 25;
            temp = XmlInput.substring(index1);
            index2 = temp.indexOf("\n");
            manufacturer = temp.substring(0, index2);

            index1 = XmlInput.indexOf("Url of product: ") + 16;
            temp = XmlInput.substring(index1);
            index2 = temp.indexOf("\n");
            url = temp.substring(0, index2);
        }
    }

    //returns whether product is successfully populated
    public boolean isValid(){
        if (ASIN.equals(FAILURE) || price == 0) return false;
        else return true;
    }

    public void print(){
        Log.d("Name", name);
        Log.d("Price", price.toString());
        Log.d("Amount", amount.toString());
        Log.d("Rating", rating.toString());
        Log.d("ImageURL", picture);
        Log.d("ASIN", ASIN);
        Log.d("Category", category);
        Log.d("Manufacturer", manufacturer);
        Log.d("URL", url);
    }

    public String display(){
        String output = "";
        output += "Name: " + name + "\n";
        output += "Price: " + price.toString() + "\n";
        output += "Amount: " + amount.toString() + "\n";
        output += "Rating: " + rating.toString() + "\n";
        output += "ImageURL: " + picture + "\n";
        output += "ASIN: " + ASIN + "\n";
        output += "Category: " + category + "\n";
        output += "Manufacturer: " + manufacturer + "\n";
        output += "URL: " + url + "\n";
        return output;
    }

    //get functions
    public String getName(){
        return name;
    }
    public Float getPrice(){
        return price;
    }
    public Integer getAmount(){
        return amount;
    }
    public Float getRating(){
        return rating;
    }
    public String getPicture(){
        return picture;
    }
    public String getASIN(){
        return ASIN;
    }
    public String getCategory(){
        return category;
    }
    public String getManufacturer(){
        return manufacturer;
    }
    public String getUrl(){
        return url;
    }

    //set functions
    public void setName(String input){
        name = input;
    }
    public void setPrice(Float input){
        price = input;
    }
    public void setAmount(Integer input){
        amount = input;
    }
    public void setRating(Float input){
        rating = input;
    }
    public void setPicture(String input){
        picture = input;
    }
    public void setASIN(String input){
        ASIN = input;
    }
    public void setCategory(String input){
        category = input;
    }
    public void setManufacturer(String input){
        manufacturer = input;
    }
    public void setUrl(String input){
        url = input;
    }
}

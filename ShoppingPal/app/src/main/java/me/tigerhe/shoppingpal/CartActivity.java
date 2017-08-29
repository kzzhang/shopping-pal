package me.tigerhe.shoppingpal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.icu.text.NumberFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.File;
import java.util.HashMap;

import me.tigerhe.shoppingpal.adapters.ListAdapter;
import me.tigerhe.shoppingpal.models.AmazonCart;
import me.tigerhe.shoppingpal.models.AmazonProduct;

public class CartActivity extends AppCompatActivity {

    private Button cameraButton;
    private Button mCheckoutButton;
    private Context mContext;

    private RecyclerView.LayoutManager mLayoutManager;
    private ListAdapter mAdapter;
    private TextView mEmptyMessage;
    private AmazonCart mCart = CartManagerSingleton.getInstance();
    private AmazonProduct mCurrentProduct;

    // list view and adapter for data
    final int RC_BARCODE_CAPTURE = 9001;
    final int RC_PRODUCT_DISPLAY = 9002;
    String barcodeValue;
    File path, current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        mEmptyMessage = (TextView) findViewById(R.id.empty_list_message);
        mContext = this;

        reset();

        cameraButton = (Button) findViewById(R.id.camera_button);

        path = getApplicationContext().getFilesDir();
        current = new File(path, "currentproduct.txt");

        RecyclerView list = (RecyclerView)findViewById(R.id.list);
        mLayoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(mLayoutManager);
        mAdapter = new ListAdapter(this, mCart.getProducts());
        list.setAdapter(mAdapter);

        mCheckoutButton = (Button) findViewById(R.id.checkout_button);
        mCheckoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String checkout = mCart.getCheckout();
                if (checkout != null && checkout != "") {
                    Uri uri = Uri.parse(checkout);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } else {
                    Toast.makeText(mContext, "Invalid cart", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // hide keyboard if visible
            InputMethodManager inputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);

            if (!cameraButton.hasOnClickListeners()) {
                final Rect cameraButtonBounds = new Rect(cameraButton.getLeft(), cameraButton.getTop(),
                        cameraButton.getRight(), cameraButton.getBottom());
                // never called
                cameraButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // do nothing
                    }
                });
                cameraButton.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        switch (MotionEventCompat.getActionMasked(motionEvent)) {
                            case (MotionEvent.ACTION_DOWN):
                                view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorSecondaryDark));
                                return true;
                            case (MotionEvent.ACTION_UP):
                                view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorSecondary));
                                int x = view.getLeft() + (int) motionEvent.getX();
                                int y = view.getTop() + (int) motionEvent.getY();
                                Log.d("BUTTON", String.valueOf(cameraButtonBounds.left)
                                        + " : " + String.valueOf(cameraButtonBounds.top)
                                        + " : " + String.valueOf(cameraButtonBounds.right)
                                        + " : " + String.valueOf(cameraButtonBounds.bottom));
                                Log.d("X - Y", String.valueOf(x) + " : " + String.valueOf(y));
                                if (cameraButtonBounds.contains(x, y)) {
                                    switchToCamera();
                                }
                                return true;
                            default:
                                return true;
                        }
                    }
                });

                Button resetButton = (Button) findViewById(R.id.reset);
                final Rect resetButtonBounds = new Rect(resetButton.getLeft(), resetButton.getTop(),
                        resetButton.getRight(), resetButton.getBottom());

                resetButton.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        switch (MotionEventCompat.getActionMasked(motionEvent)) {
                            case (MotionEvent.ACTION_DOWN):
                                view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorSecondaryDark));
                                return true;
                            case (MotionEvent.ACTION_UP):
                                view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorSecondary));
                                if (resetButtonBounds.contains(view.getLeft() + (int) motionEvent.getX(),
                                        view.getTop() + (int) motionEvent.getY())) {
                                    reset();
                                }
                                return true;
                            default:
                                return true;
                        }
                    }
                });
            }
        }
    }

    public void reset() {
        CartManagerSingleton.getInstance().reset();
        mCart = CartManagerSingleton.getInstance();
        updatePrice(0f, 0);

        TextView count = (TextView)findViewById(R.id.count_price);
        count.setText("$0.00 : 0 items");
        mEmptyMessage.setVisibility(View.VISIBLE);

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void switchToCamera() {
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Operation","ItemLookup");
        ProductSave saver = new ProductSave(current, map);
        String productData = null;
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    barcodeValue = barcode.displayValue;
                    while (String.valueOf(barcodeValue).length() < 12) {
                        barcodeValue = "0" + barcodeValue;
                    }

                    saver.mapAdd("IdType", "UPC");
                    saver.mapAdd("ItemId", barcodeValue);
                    saver.mapAdd("SearchIndex", "All");

                    productData = saver.save();

                    //prints data to work with
                    Log.d("Product Data", productData);
                    mCurrentProduct = new AmazonProduct(productData);
//                    if (retrieved.isValid()) retrieved.print();
//                    else Log.d("Retrieved", "Invalid!");
//                    if (retrieved.isValid()){
//                        AmazonCart sample = new AmazonCart(retrieved, 1);
//                        Log.d("Checkout URL", sample.checkout);
//                    }

//                    TextView output = (TextView)findViewById(R.id.data_output);
                    if (mCurrentProduct.isValid()){
                        displayObject(mCurrentProduct);
//                        output.setText(retrieved.display());
//                        Button addButton = (Button) findViewById(R.id.add_item);
//                        addButton.setEnabled(true);
//                        final TextView quantity = (TextView)findViewById(R.id.quantity);
//                        quantity.setFocusableInTouchMode(true);
//                        quantity.setFocusable(true);
//
//                        addButton.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                String input = quantity.getText().toString();
//                                try{
//                                    int number = Integer.parseInt(input);
//                                    if (number > 0 && number <= retrieved.getAmount()) {
//                                        Button checkout = (Button) findViewById(R.id.checkout);
//                                        if (mCart == null) {
//                                            mCart = new AmazonCart(retrieved, number);
//                                            checkout.setEnabled(true);
//                                        } else {
//                                            mCart.add(retrieved, number);
//                                        }
//                                        items += number;
//                                        price += number*retrieved.getPrice();
//                                        String outputPrice = new DecimalFormat("#.##").format(price);
//                                        TextView count = (TextView)findViewById(R.id.count_price);
//                                        count.setText("$" + outputPrice + " : "+Integer.toString(items)+" items");
//                                        checkout.setOnClickListener(new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View view) {
//                                                Uri uriUrl = Uri.parse(mCart.checkout);
//                                                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
//                                                startActivity(launchBrowser);
//                                            }
//                                        });
//                                    }
//                                }catch (NumberFormatException e){
//                                    Log.d("Input Error","Input an integer for quantity");
//                                }
//                            }
//                        });
                    }
                    else {
                        Toast.makeText(this, "Error - Could not find the associated product on Amazon", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        else if (requestCode == RC_PRODUCT_DISPLAY){
            onWindowFocusChanged(true);
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    try {
                        Integer quantity = data.getIntExtra("Quantity", 0);
                        mCart.add(mCurrentProduct, quantity);
                        float price = mAdapter.updateData();
                        int items = mAdapter.getNumItems();
                        updatePrice(price, items);
                        if (mCart.getProducts().size() > 0) {
                            mEmptyMessage.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    public void displayObject(AmazonProduct product) {
        Intent intent = new Intent(this, DisplayImageActivity.class);
        intent.putExtra("Price", product.getPrice().toString());
        intent.putExtra("Quantity", product.getAmount().toString());
        intent.putExtra("Rating", product.getRating().toString());
        intent.putExtra("Manufacturer", product.getManufacturer());
        intent.putExtra("Imgurl", product.getPicture());
        intent.putExtra("Url", product.getUrl());
        intent.putExtra("Name", product.getName());
        startActivityForResult(intent, RC_PRODUCT_DISPLAY);
    }

    public void updatePrice(Float total, int items) {
        TextView count = (TextView)findViewById(R.id.count_price);
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        count.setText(formatter.format(total) + " : "+Integer.toString(items)+" items");
    }
}
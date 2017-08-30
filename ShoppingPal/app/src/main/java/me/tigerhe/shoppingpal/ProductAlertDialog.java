package me.tigerhe.shoppingpal;

import android.app.AlertDialog;
import android.content.Context;
import android.icu.text.NumberFormat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.tigerhe.shoppingpal.models.AmazonProduct;
import me.tigerhe.shoppingpal.utils.FontManager;

/**
 * Created by kzhan on 2017-08-28.
 */

public class ProductAlertDialog {
    private AlertDialog mDialog;
    private ProgressBar mProgressBar;
    private TextView mCancelButton;
    private TextView mAcceptButton;
    private Integer mCurrent;

    public ProductAlertDialog(Context context, final AmazonProduct product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(R.layout.dialog_product_view);
        mDialog = builder.create();
        mDialog.show();

        mProgressBar = (ProgressBar) mDialog.findViewById(R.id.dialog_progress_bar);
        mAcceptButton = ((TextView) mDialog.findViewById(R.id.dialog_accept));
        mCancelButton = ((TextView) mDialog.findViewById(R.id.dialog_cancel));

        mAcceptButton.setTypeface(FontManager.getTypeface(
                context, FontManager.FONTAWESOME));
        mCancelButton.setTypeface(FontManager.getTypeface(
                context, FontManager.FONTAWESOME));
        String quantity = String.valueOf(product.quantity);
        final EditText inputBox = (EditText) mDialog.findViewById(R.id.dialog_quantity);
        inputBox.setText(quantity);
        inputBox.setSelection(quantity.length());
        mCurrent = product.quantity;
        ((TextView) mDialog.findViewById(R.id.dialog_product_title)).setText(product.getName());
        final NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String price = formatter.format(product.quantity * product.getPrice());
        ((TextView) mDialog.findViewById(R.id.dialog_price)).setText(price);

        inputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString();
                if (!input.equals("")) {
                    mCurrent = Integer.parseInt(input);
                    product.quantity = mCurrent;
                    String price = formatter.format(mCurrent * product.getPrice());
                    ((TextView) mDialog.findViewById(R.id.dialog_price)).setText(price);
                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });
    }

    public void setPositive(View.OnClickListener positive) {
        mAcceptButton.setOnClickListener(positive);
    }

    public void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    public int getQuantity() {
        return mCurrent;
    }

    public void dismiss() {
        mDialog.dismiss();
    }
}

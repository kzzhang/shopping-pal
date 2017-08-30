package me.tigerhe.shoppingpal.adapters;

import android.content.Context;
import android.icu.text.NumberFormat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;

import me.tigerhe.shoppingpal.ProductAlertDialog;
import me.tigerhe.shoppingpal.R;
import me.tigerhe.shoppingpal.models.AmazonProduct;

/**
 * Created by kevin on 2017-06-01.
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.listViewHolder>  {
    private List<AmazonProduct> mProducts;
    private Context mContext;
    private listClick mListener;

    public ListAdapter(Context context, List<AmazonProduct> products) {
        mProducts = products;
        mContext = context;
        mListener = (listClick)context;
    }

    public static class listViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public listViewHolder(View view) {
            super(view);
            mView = view;
        }
    }

    @Override
    public listViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.list_card, parent, false);
        listViewHolder viewHolder = new listViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create dialog
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(listViewHolder holder, int position) {
        TextView price = (TextView) holder.mView.findViewById(R.id.purchased_price);
        TextView name = (TextView) holder.mView.findViewById(R.id.purchased_name);
        TextView quantity = (TextView) holder.mView.findViewById(R.id.quantity_purchased);
        final AmazonProduct product = mProducts.get(position);
        name.setText(product.getName());
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String priceString = "Cost: " + formatter.format(product.quantity * product.getPrice());
        String quantityString = "Quantity: " + String.valueOf(product.quantity);
        price.setText(priceString);
        quantity.setText(quantityString);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProductAlertDialog productDialog = new ProductAlertDialog(mContext, product);
                productDialog.setPositive(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        product.quantity = productDialog.getQuantity();
                        mListener.onClick(product, productDialog);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mProducts.size();
    }

    public Float updateData() {
        notifyDataSetChanged();
        float total = 0;
        Iterator iterator = mProducts.listIterator();
        while (iterator.hasNext()) {
            AmazonProduct product = (AmazonProduct)iterator.next();
            total += product.getPrice() * product.quantity;
        }
        return total;
    }

    public int getNumItems() {
        int total = 0;
        Iterator iterator = mProducts.listIterator();
        while (iterator.hasNext()) {
            AmazonProduct product = (AmazonProduct)iterator.next();
            total += product.quantity;
        }
        return total;
    }

    public interface listClick {
        void onClick(AmazonProduct product, ProductAlertDialog dialog);
    }
}

package me.tigerhe.shoppingpal.utils;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by kzhan on 2017-08-28.
 */

public class FontManager {

    public static final String ROOT = "fonts/";
    public static final String FONTAWESOME = ROOT + "fontawesome-webfont.ttf";

    public static Typeface getTypeface(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), font);
    }

}
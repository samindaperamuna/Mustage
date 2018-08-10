package com.fifthgen.mustage;

import android.graphics.Color;

public class Utility {

    /**
     * Check if the color is a valid Android color. Support basic color names and HTML color codes.
     * @param colorString Color string.
     * @return Whether its a valid Android color.
     */
    public static Boolean isValidColor(String colorString) {
        try {
            int color = Color.parseColor(colorString);
            return true;
        } catch (IllegalArgumentException iae) {
            return false;
        }
    }
}

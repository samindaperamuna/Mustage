package com.fifthgen.chips;

import android.widget.ImageView;

/**
 * Copyright © 2017 Tyler Suehr
 *
 * Defines a renderer for chip avatar images.
 *
 * Implementations of this can used to do things like using libraries,
 * such as Picasso or Glide, to load the image for you.
 *
 * @author Tyler Suehr
 * @version 1.0
 */
public interface ChipImageRenderer {
    void renderAvatar(ImageView imageView, Chip chip);
}
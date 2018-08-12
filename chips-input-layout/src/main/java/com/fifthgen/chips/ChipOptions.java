package com.fifthgen.chips;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

/**
 * Copyright © 2017 Tyler Suehr
 * <p>
 * Contains all the mutable properties for this library.
 *
 * @author Tyler Suehr
 * @version 1.0
 */
final class ChipOptions {
    /* Properties pertaining to ChipsEditText */
    ColorStateList mTextColorHint;
    ColorStateList mTextColor;
    CharSequence mHint;

    /* Properties pertaining to ChipView */
    Drawable mChipDeleteIcon;
    ColorStateList mChipDeleteIconColor;
    ColorStateList mChipBackgroundColor;
    ColorStateList mChipTextColor;
    boolean mShowAvatar;
    boolean mShowDetails;
    boolean mShowDelete;
    boolean mEditable;

    /* Properties pertaining to ChipDetailsView */
    ColorStateList mDetailsChipDeleteIconColor;
    ColorStateList mDetailsChipBackgroundColor;
    ColorStateList mDetailsChipTextColor;

    /* Properties pertaining to FilterableRecyclerView */
    ColorStateList mFilterableListBackgroundColor;
    ColorStateList mFilterableListTextColor;
    float mFilterableListElevation;

    int mTextAppearanceIdRes;

    /* Properties pertaining to the ChipsInputLayout itself */
    Typeface mTypeface = Typeface.DEFAULT;
    boolean mAllowCustomChips;
    boolean mHideKeyboardOnChipClick;
    int mMaxRows;

    @NonNull
    ChipImageRenderer mImageRenderer;


    ChipOptions(Context c, AttributeSet attrs, int defStyleAttr) {
        // Set defaults
        mImageRenderer = new DefaultImageRenderer();

        // Set the XML attributes
        TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.ChipsInputLayout);

        // Setup the properties for the ChipEditText
        mTextColorHint = a.getColorStateList(R.styleable.ChipsInputLayout_android_textColorHint);
        mTextColor = a.getColorStateList(R.styleable.ChipsInputLayout_android_textColor);
        mHint = a.getString(R.styleable.ChipsInputLayout_android_hint);
        mEditable = a.getBoolean(R.styleable.ChipsInputLayout_isEditable, true);

        // Setup the properties for the ChipView
        mShowDetails = a.getBoolean(R.styleable.ChipsInputLayout_chip_showDetails, true);
        mShowAvatar = a.getBoolean(R.styleable.ChipsInputLayout_chip_showAvatar, true);
        mShowDelete = a.getBoolean(R.styleable.ChipsInputLayout_chip_showDelete, true);
        mChipDeleteIcon = a.getDrawable(R.styleable.ChipsInputLayout_chip_deleteIcon);
        mChipDeleteIconColor = a.getColorStateList(R.styleable.ChipsInputLayout_chip_deleteIconColor);
        mChipBackgroundColor = a.getColorStateList(R.styleable.ChipsInputLayout_chip_backgroundColor);
        mChipTextColor = a.getColorStateList(R.styleable.ChipsInputLayout_chip_textColor);

        // Setup the properties for the DetailedChipView
        mDetailsChipDeleteIconColor = a.getColorStateList(R.styleable.ChipsInputLayout_details_deleteIconColor);
        mDetailsChipBackgroundColor = a.getColorStateList(R.styleable.ChipsInputLayout_chip_backgroundColor);
        mDetailsChipTextColor = a.getColorStateList(R.styleable.ChipsInputLayout_details_textColor);

        // Setup the properties for the FilterableRecyclerView
        mFilterableListElevation = a.getDimension(R.styleable.ChipsInputLayout_filter_elevation, R.dimen.chip_open_elevation);
        mFilterableListBackgroundColor = a.getColorStateList(R.styleable.ChipsInputLayout_filter_backgroundColor);
        mFilterableListTextColor = a.getColorStateList(R.styleable.ChipsInputLayout_filter_textColor);

        // Setup the properties for the ChipsInput itself
        mAllowCustomChips = a.getBoolean(R.styleable.ChipsInputLayout_allowCustomChips, true);
        mHideKeyboardOnChipClick = a.getBoolean(R.styleable.ChipsInputLayout_hideKeyboardOnChipClick, true);
        mMaxRows = a.getInt(R.styleable.ChipsInputLayout_maxRows, 3);

        a.recycle();

        // Set the styleable attributes
        final int[] styleable = new int[]{android.R.attr.textAppearance};
        a = c.obtainStyledAttributes(attrs, styleable, defStyleAttr, 0);
        mTextAppearanceIdRes = a.getResourceId(0, android.R.attr.textAppearanceMedium);
        a.recycle();
    }
}

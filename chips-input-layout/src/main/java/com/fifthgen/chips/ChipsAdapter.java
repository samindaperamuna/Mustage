package com.fifthgen.chips;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

/**
 * Copyright © 2017 Tyler Suehr
 * <p>
 * Subclass of {@link RecyclerView.Adapter} to adapt the selected chips into views
 * and display an input (EditText) to allow the user to type text in for chips.
 * <p>
 * This adapter should afford the following abilities/features:
 * (1) Allow user to create custom chips, if the options permit it.
 * (2) Allow user to remove any chip by pressing delete on an empty input.
 * (3) Allow the user to see chip details, if the options permit it.
 * <p>
 * This observes changes to {@link ChipDataSource} to update the UI accordingly.
 *
 * @author Tyler Suehr
 * @version 1.0
 */
class ChipsAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ChipsEditText.OnKeyboardListener, ChipDataSource.ChangeObserver {
    private static final int CHIP = 0;
    private static final int INPUT = 1;

    private final ChipDataSource mDataSource;
    private final ChipOptions mOptions;
    private final ChipsEditText mEditText;

    // ChipsInputLayout chip click listener.
    private ChipsInputLayout.OnChipClickListener mChipClickListener;

    ChipsAdapter(ChipDataSource dataSource,
                 ChipsEditText editText,
                 ChipOptions options) {
        mDataSource = dataSource;
        mEditText = editText;
        mOptions = options;
        mEditText.setKeyboardListener(this);

        // Register an observer on the chip data source
        mDataSource.addChangedObserver(this);
    }

    protected ChipsInputLayout.OnChipClickListener getChipClickListener() {
        return mChipClickListener;
    }

    protected void setChipClickListener(ChipsInputLayout.OnChipClickListener mChipClickListener) {
        this.mChipClickListener = mChipClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return position < mDataSource
                .getSelectedChips().size() ? CHIP : INPUT;
    }

    @Override
    public int getItemCount() {
        // Plus 1 for the edit text
        return mDataSource.getSelectedChips().size() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return viewType == CHIP
                ? new ChipHolder(new ChipView(parent.getContext()))
                : new RecyclerView.ViewHolder(mEditText) {
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == CHIP) { // Chips
            // Display the chip information on the chip view
            final ChipHolder ch = (ChipHolder) holder;
            ch.chipView.inflateFromChip(mDataSource.getSelectedChip(position));
        } else { // EditText
            if (mDataSource.getSelectedChips().size() == 0) {
                mEditText.setHint(mOptions.mHint);
            }

            // Resize the edit text to fit in recycler
            autoFitEditText();
        }
    }

    /**
     * Called when the IME_ACTION_DONE option is pressed on a software or
     * physical keyboard.
     *
     * @param text Current text in the EditText
     */
    @Override
    public void onKeyboardActionDone(String text) {
        if (TextUtils.isEmpty(text) || !mOptions.mAllowCustomChips) {
            return;
        }

        // Clear the input before taking chip so we don't need to update UI twice
        mEditText.setText("");

        // This will trigger callback, which calls notifyDataSetChanged()
        mDataSource.addSelectedChip(new DefaultCustomChip(text));
    }

    /**
     * Called when the backspace (KEYCODE_DEL) is pressed on a software or
     * physical keyboard.
     */
    @Override
    public void onKeyboardBackspace() {
        // Only remove the last chip if the input was empty
        if (mDataSource.getSelectedChips().size() > 0
                && mEditText.getText().length() == 0) {
            // Will trigger notifyDataSetChanged()
            mDataSource.replaceChip(mDataSource.getSelectedChips().size() - 1);
        }
    }

    @Override
    public void onChipDataSourceChanged() {
        notifyDataSetChanged();
    }

    private void autoFitEditText() {
        // Set the EditText to a minimum width of its hint length
        ViewGroup.LayoutParams lp = mEditText.getLayoutParams();
        lp.width = (int) mEditText.calculateTextWidth();
        mEditText.setLayoutParams(lp);

        // Listen to changes in the tree
        mEditText.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Get right of recycler and left of edit text
                        final View parent = (View) mEditText.getParent();
                        int right = parent.getRight();
                        int left = mEditText.getLeft();

                        // Calculate the available space left to draw the EditText,
                        // and only readjust the EditText when the available space
                        // is larger than the needed space.
                        //
                        // This will allow the full text hint to always be visible
                        // by ensuring the EditText gets wrapped to the next line
                        // if it can't fit in the available space.
                        final int available = (right - left - Utils.dp(8));
                        ViewGroup.LayoutParams lp = mEditText.getLayoutParams();
                        if (lp.width < available) {
                            lp.width = available;
                            mEditText.setLayoutParams(lp);
                        }

                        // Request focus
                        mEditText.requestFocus();

                        // Remove the tree listener
                        mEditText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
    }

    private void showDetailedChipView(ChipView view, Chip chip, final int position) {
        // Get chip view's location
        int[] coord = new int[2];
        view.getLocationInWindow(coord);

        // Create a detailed chip view to show
        final ChipDetailsView detailedChipView = new ChipDetailsView(view.getContext());
        detailedChipView.setChipOptions(mOptions);
        detailedChipView.inflateWithChip(chip);

        // Setup the location in window of the detailed chip
        setDetailedChipViewPosition(detailedChipView, coord);

        // Remove the detailed chip when delete button is pressed
        detailedChipView.setOnDeleteClicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Will trigger notifyDataSetChanged()
                mDataSource.replaceChip(position);
                detailedChipView.fadeOut();
            }
        });
    }

    private void setDetailedChipViewPosition(final ChipDetailsView detailedChipView, int[] coord) {
        // Window width
        final ViewGroup rootView = (ViewGroup) mEditText.getRootView();
        int windowWidth = Utils.getWindowWidth(rootView.getContext());

        // Chip size
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                Utils.dp(300),
                Utils.dp(100)
        );
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        // Determine the detailed chip's alignment inside the window
        if (coord[0] <= 0) { // Left align
            lp.leftMargin = 0;
            lp.topMargin = coord[1] - Utils.dp(13);
            detailedChipView.alignLeft();
        } else if (coord[0] + Utils.dp(300) > windowWidth + Utils.dp(13)) { // Right align
            lp.leftMargin = windowWidth - Utils.dp(300);
            lp.topMargin = coord[1] - Utils.dp(13);
            detailedChipView.alignRight();
        } else { // Same position as chip
            lp.leftMargin = coord[0] - Utils.dp(13);
            lp.topMargin = coord[1] - Utils.dp(13);
        }

        // Show the detailed chip view
        rootView.addView(detailedChipView, lp);
        detailedChipView.fadeIn();
    }


    /**
     * Nested inner-subclass of {@link RecyclerView.ViewHolder} that stores
     * reference to the a chip view.
     */
    private class ChipHolder extends RecyclerView.ViewHolder implements
            ChipView.OnChipClickListener, ChipView.OnChipDeleteListener {
        ChipView chipView;

        ChipHolder(ChipView chipView) {
            super(chipView);
            this.chipView = chipView;
            this.chipView.setChipOptions(mOptions);
            this.chipView.setOnDeleteClicked(this);
            this.chipView.setOnChipClicked(this);
        }

        @Override
        public void onChipClicked(ChipView v) {
            final int position = getAdapterPosition();
            if (position > -1) {
                final Chip chip = mDataSource.getSelectedChip(position);

                if (mOptions.mShowDetails) {
                    showDetailedChipView(v, chip, position);
                } else {
                    // The ChipsInputLayout handles the event.
                    if (mChipClickListener != null)
                        mChipClickListener.onChipClicked(chip);
                }
            }
        }

        @Override
        public void onChipDeleted(ChipView v) {
            // Will trigger notifyDataSetChanged()
            final int position = getAdapterPosition();
            if (position > -1) {
                mDataSource.replaceChip(position);
            }
        }
    }
}
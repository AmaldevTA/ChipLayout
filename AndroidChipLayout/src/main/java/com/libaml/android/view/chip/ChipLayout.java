

package com.libaml.android.view.chip;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ChipLayout extends ViewGroup implements View.OnClickListener {

    public static int MAX_CHARACTER_COUNT = 20;

    private int mGravity = (isIcs() ? Gravity.START : Gravity.LEFT) | Gravity.TOP;

    private final List<List<View>> mLines = new ArrayList<List<View>>();
    private final List<Integer> mLineHeights = new ArrayList<Integer>();
    private final List<Integer> mLineMargins = new ArrayList<Integer>();

    String autoCompleteTextViewTag = "chip_autoCompleteTextView", imageButtonTag = "chip_imageButton";

    private float textSize, chipTextPadding, chipPadding, chipPaddingLeft, chipPaddingRight,
            chipPaddingTop, chipPaddingBottom, chipTextPaddingLeft, chipTextPaddingRight,
            chipTextPaddingTop, chipTextPaddingBottom;
    private ChipLayout chipLayout;
    private Context context;
    private boolean showDeleteButton;
    private int labelPosition;
    private int textColor, chipColor;
    private String hintText;
    private Drawable deleteIcon, chipDrawable, chipLayoutDrawable;
    private Bitmap deleteIcon_ = null;
    private List<TextWatcher>  listTextWatcher = new ArrayList<>();
    private ArrayAdapter adapter;
    private AdapterView.OnItemClickListener onItemClickListener;
    private OnClickListener onClickListener;
    private OnFocusChangeListener onFocusChangeListener;
    private ChipItemChangeListener chipItemChangeListener;
    private int dropDownWidth = 300;
    private TextWatcher focusedTextWatcher;

    public ChipLayout(Context context) {
        this(context, null);
    }

    public ChipLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChipLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a_ = context.getTheme().obtainStyledAttributes(attrs, R.styleable.chip_layout, defStyle, 0);

        textColor = a_.getColor(R.styleable.chip_layout_textColor_, Color.parseColor("#000000"));
        chipColor = a_.getColor(R.styleable.chip_layout_chipColor_, Color.parseColor("#00FFFFFF"));
        chipDrawable = a_.getDrawable(R.styleable.chip_layout_chipDrawable_);
        deleteIcon = a_.getDrawable(R.styleable.chip_layout_deleteIcon_);
        showDeleteButton = a_.getBoolean(R.styleable.chip_layout_showDeleteButton_, true);
        labelPosition = a_.getInt(R.styleable.chip_layout_labelPosition_, 0);
        chipLayoutDrawable = a_.getDrawable(R.styleable.chip_layout_chipLayoutDrawable_);
        textSize = a_.getDimension(R.styleable.chip_layout_textSize_, 0);
        hintText = a_.getString(R.styleable.chip_layout_hint_);
        chipTextPadding = a_.getDimension(R.styleable.chip_layout_chipTextPadding_, 0);
        chipTextPaddingLeft = a_.getDimension(R.styleable.chip_layout_chipTextPaddingLeft_, 0);
        chipTextPaddingRight = a_.getDimension(R.styleable.chip_layout_chipTextPaddingRight_, 0);
        chipTextPaddingTop = a_.getDimension(R.styleable.chip_layout_chipTextPaddingTop_, 0);
        chipTextPaddingBottom = a_.getDimension(R.styleable.chip_layout_chipTextPaddingBottom_, 0);
        chipPadding = a_.getDimension(R.styleable.chip_layout_chipPadding_, 0);
        chipPaddingLeft = a_.getDimension(R.styleable.chip_layout_chipPaddingLeft_, 0);
        chipPaddingRight = a_.getDimension(R.styleable.chip_layout_chipPaddingRight_, 0);
        chipPaddingTop = a_.getDimension(R.styleable.chip_layout_chipPaddingTop_, 0);
        chipPaddingBottom = a_.getDimension(R.styleable.chip_layout_chipPaddingBottom_, 0);

        if(deleteIcon != null) {
            deleteIcon_ = ((BitmapDrawable) deleteIcon).getBitmap();
        }


        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.chipLayout, defStyle, 0);

        try {
            int index = a.getInt(R.styleable.chipLayout_android_gravity, -1);
            if(index > 0) {
                setGravity(index);
            }
        } finally {
            a.recycle();
        }
        this.context = context;
        chipLayout = this;
        if(chipLayoutDrawable != null){
            setLayoutBackground(chipLayoutDrawable);
        }
        createNewChipLayout(null);
        setOnClickListener();

    }

    public interface ChipItemChangeListener{
        void onChipAdded(int pos, String txt);
        void onChipRemoved(int pos, String txt);
    }

    public void setOnChipItemChangeListener(ChipItemChangeListener l){
        this.chipItemChangeListener = l;
    }

    public ChipItemChangeListener getOnChipItemChangeListener(){
        return this.chipItemChangeListener;
    }

    /**
     * Ask all children to measure themselves and compute the measurement of this
     * layout based on the children.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int width = 0;
        int height = getPaddingTop() + getPaddingBottom();

        int lineWidth = 0;
        int lineHeight = 0;

        int childCount = getChildCount();

        for(int i = 0; i < childCount; i++) {

            View child = getChildAt(i);
            boolean lastChild = i == childCount - 1;

            if(child.getVisibility() == View.GONE) {

                if(lastChild) {
                    width = Math.max(width, lineWidth);
                    height += lineHeight;
                }

                continue;
            }

            measureChildWithMargins(child, widthMeasureSpec, lineWidth, heightMeasureSpec, height);

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int childWidthMode = MeasureSpec.AT_MOST;
            int childWidthSize = sizeWidth;

            int childHeightMode = MeasureSpec.AT_MOST;
            int childHeightSize = sizeHeight;

            if(lp.width == LayoutParams.MATCH_PARENT) {
                childWidthMode = MeasureSpec.EXACTLY    ;
                childWidthSize -= lp.leftMargin + lp.rightMargin;
            } else if(lp.width >= 0) {
                childWidthMode = MeasureSpec.EXACTLY;
                childWidthSize = lp.width;
            }

            if(lp.height >= 0) {
                childHeightMode = MeasureSpec.EXACTLY;
                childHeightSize = lp.height;
            } else if (modeHeight == MeasureSpec.UNSPECIFIED) {
                childHeightMode = MeasureSpec.UNSPECIFIED;
                childHeightSize = 0;
            }

            child.measure(
                    MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
                    MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode)
            );

            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;

            if(lineWidth + childWidth > sizeWidth) {

                width = Math.max(width, lineWidth);
                lineWidth = childWidth;

                height += lineHeight;
                lineHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            } else {
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            }

            if(lastChild) {
                width = Math.max(width, lineWidth);
                height += lineHeight;
            }

        }

        width += getPaddingLeft() + getPaddingRight();

        setMeasuredDimension(
                (modeWidth == MeasureSpec.EXACTLY) ? sizeWidth : width,
                (modeHeight == MeasureSpec.EXACTLY) ? sizeHeight : height);
    }


    /**
     * Position all children within this layout.
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        mLines.clear();
        mLineHeights.clear();
        mLineMargins.clear();

        int width = getWidth();
        int height = getHeight();

        int linesSum = getPaddingTop();

        int lineWidth = 0;
        int lineHeight = 0;
        List<View> lineViews = new ArrayList<View>();

        float horizontalGravityFactor;
        switch ((mGravity & Gravity.HORIZONTAL_GRAVITY_MASK)) {
            case Gravity.LEFT:
            default:
                horizontalGravityFactor = 0;
                break;
            case Gravity.CENTER_HORIZONTAL:
                horizontalGravityFactor = .5f;
                break;
            case Gravity.RIGHT:
                horizontalGravityFactor = 1;
                break;
        }

        for(int i = 0; i < getChildCount(); i++) {

            ViewGroup child = (ViewGroup) getChildAt(i);
            if(child.getVisibility() == View.GONE) {
                continue;
            }

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin ;
            int childHeight = child.getMeasuredHeight() + lp.bottomMargin + lp.topMargin;

            if(lineWidth + childWidth > width) {
                mLineHeights.add(lineHeight);
                mLines.add(lineViews);
                mLineMargins.add((int) ((width - lineWidth) * horizontalGravityFactor) + getPaddingLeft());

                linesSum += lineHeight;

                lineHeight = 0;
                lineWidth = 0;
                lineViews = new ArrayList<>();
            }

            lineWidth += childWidth;
            lineHeight = Math.max(lineHeight, childHeight);
            lineViews.add(child);
        }

        mLineHeights.add(lineHeight);
        mLines.add(lineViews);
        mLineMargins.add((int) ((width - lineWidth) * horizontalGravityFactor) + getPaddingLeft());

        linesSum += lineHeight;

        int verticalGravityMargin = 0;
        switch ((mGravity & Gravity.VERTICAL_GRAVITY_MASK)	) {
            case Gravity.TOP:
            default:
                break;
            case Gravity.CENTER_VERTICAL:
                verticalGravityMargin = (height - linesSum) / 2;
                break;
            case Gravity.BOTTOM:
                verticalGravityMargin = height - linesSum;
                break;
        }

        int numLines = mLines.size();

        int left;
        int top = getPaddingTop();

        for(int i = 0; i < numLines; i++) {

            lineHeight = mLineHeights.get(i);
            lineViews = mLines.get(i);
            left = mLineMargins.get(i);

            int children = lineViews.size();

            for(int j = 0; j < children; j++) {

                View child = lineViews.get(j);

                if(child.getVisibility() == View.GONE) {
                    continue;
                }

                LayoutParams lp = (LayoutParams) child.getLayoutParams();

                if(lp.height == LayoutParams.MATCH_PARENT) {
                    int childWidthMode = MeasureSpec.AT_MOST;
                    int childWidthSize = lineWidth;

                    if(lp.width == LayoutParams.MATCH_PARENT) {
                        childWidthMode = MeasureSpec.EXACTLY;
                    } else if(lp.width >= 0) {
                        childWidthMode = MeasureSpec.EXACTLY;
                        childWidthSize = lp.width;
                    }

                    child.measure(
                            MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
                            MeasureSpec.makeMeasureSpec(lineHeight - lp.topMargin - lp.bottomMargin, MeasureSpec.EXACTLY)
                    );
                }

                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();

                int gravityMargin = 0;

                if(Gravity.isVertical(lp.gravity)) {
                    switch (lp.gravity) {
                        case Gravity.TOP:
                        default:
                            break;
                        case Gravity.CENTER_VERTICAL:
                        case Gravity.CENTER:
                            gravityMargin = (lineHeight - childHeight - lp.topMargin - lp.bottomMargin) / 2 ;
                            break;
                        case Gravity.BOTTOM:
                            gravityMargin = lineHeight - childHeight - lp.topMargin - lp.bottomMargin;
                            break;
                    }
                }

                child.layout(left + lp.leftMargin,
                        top + lp.topMargin + gravityMargin + verticalGravityMargin,
                        left + childWidth + lp.leftMargin,
                        top + childHeight + lp.topMargin + gravityMargin + verticalGravityMargin);

                left += childWidth + lp.leftMargin + lp.rightMargin;

            }

            top += lineHeight;
        }

    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }


    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }


    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p) && p instanceof LayoutParams;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void setGravity(int gravity) {
        if(mGravity != gravity) {
            if((gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
                gravity |= isIcs() ? Gravity.START : Gravity.LEFT;
            }

            if((gravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                gravity |= Gravity.TOP;
            }

            mGravity = gravity;
            requestLayout();
        }
    }

    public int getGravity() {
        return mGravity;
    }


    private static boolean isIcs() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }



    public static class LayoutParams extends MarginLayoutParams {

        public int gravity = -1;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.chipLayout_Layout);

            try {
                gravity = a.getInt(R.styleable.chipLayout_Layout_android_layout_gravity, -1);
            } finally {
                a.recycle();
            }
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

    }

    private AutoCompleteTextView createAutoCompleteTextView(Context context) {
        final LayoutParams lparamsTextView = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lparamsTextView.setMargins(0, 0, 10, 0);
        lparamsTextView.gravity = Gravity.CENTER;
        final AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(context);
        autoCompleteTextView.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        autoCompleteTextView.setLayoutParams(lparamsTextView);
        autoCompleteTextView.setHint(" ");
        autoCompleteTextView.setPadding(10,0,10,10);
        autoCompleteTextView.setSingleLine(true);
        autoCompleteTextView.setTextColor(textColor);
        autoCompleteTextView.setCursorVisible(true);

        return autoCompleteTextView;
    }

    private ImageButton createImageButton(Context context) {

        final LayoutParams lparamsImageButton = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        lparamsImageButton.gravity = Gravity.CENTER;
        lparamsImageButton.setMargins(0, 0, 0, 0);
        final ImageButton imageButton = new ImageButton(context);
        imageButton.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        if(deleteIcon != null){
            imageButton.setImageBitmap(deleteIcon_);
        }else{
            imageButton.setImageResource(android.R.drawable.presence_offline);
        }
        imageButton.setLayoutParams(lparamsImageButton);
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                View chip = (View) view.getParent();
                int pos = chipLayout.indexOfChild(chip);
                removeChipAt(pos);
            }
        });
        imageButton.setVisibility(View.GONE);
        return imageButton;
    }

    private LinearLayout createLinearLayout(Context context) {

        final LayoutParams lparamsLayout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lparamsLayout.setMargins((int)chipPadding+(int)chipPaddingLeft+2, (int)chipPadding+(int)chipPaddingTop+2,
                (int)chipPadding+(int)chipPaddingRight+2, (int)chipPadding+(int)chipPaddingBottom+2);
        lparamsLayout.gravity = Gravity.CENTER;
        final LinearLayout layout = new LinearLayout(context);
        layout.setPadding((int)chipTextPadding+(int)chipTextPaddingLeft, (int)chipTextPadding+(int)chipTextPaddingTop,
                (int)chipTextPadding+(int)chipTextPaddingRight, (int)chipTextPadding+(int)chipTextPaddingBottom);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(lparamsLayout);
        layout.setFocusable(true);

        return layout;
    }


    private ViewGroup createChips(Context context, String val, boolean setText)
    {

        final LinearLayout layout = createLinearLayout(context);
        final AutoCompleteTextView autoCompleteTextView = createAutoCompleteTextView(context);
        autoCompleteTextView.setTag(autoCompleteTextViewTag);
        if(this.getChildCount() < 1 && hintText != null){
            autoCompleteTextView.setHint(hintText);
        }
        final ImageButton imageButton = createImageButton(context);
        imageButton.setTag(imageButtonTag);

        if(labelPosition == 0){
            layout.addView(autoCompleteTextView);
            layout.addView(imageButton);
        }else{
            layout.addView(imageButton);
            layout.addView(autoCompleteTextView);
        }


        Drawable newDrawable = null;
        if(chipDrawable != null){
            newDrawable = chipDrawable.getConstantState().newDrawable();
        }

        TextWatcher textWatcher = new ChipTextWatcher(layout, context, this, textColor, chipColor, newDrawable,
                showDeleteButton, labelPosition, listTextWatcher, setText);
        focusedTextWatcher = textWatcher;
        if(textSize > 0){
            autoCompleteTextView.setTextSize(textSize);
            ((ChipTextWatcher)textWatcher).setTextSize(textSize);
        }
        autoCompleteTextView.addTextChangedListener(textWatcher);
        for (TextWatcher tw: listTextWatcher){
            autoCompleteTextView.addTextChangedListener(tw);
        }

        OnFocusChangeListener focusChangeListener = new ChipOnFocusChangeListener(this, autoCompleteTextView,
                chipDrawable, chipLayoutDrawable, onFocusChangeListener);
        autoCompleteTextView.setOnFocusChangeListener(focusChangeListener);

        autoCompleteTextView.requestFocus();
        autoCompleteTextView.setOnEditorActionListener(new ChipEditorActionListener(autoCompleteTextView));
        autoCompleteTextView.setAdapter(adapter);
        int density = context.getResources().getDisplayMetrics().densityDpi;
        switch(density)
        {
            case DisplayMetrics.DENSITY_LOW:
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                break;
            case DisplayMetrics.DENSITY_HIGH:
                dropDownWidth = 280;
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                dropDownWidth = 300;
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                dropDownWidth = 320;
                break;
            case DisplayMetrics.DENSITY_560:
                dropDownWidth = 360;
                break;
        }
        if(chipLayout.getWidth() < 1){
            ViewTreeObserver vto = this.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < 16) {
                        chipLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        chipLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    if(chipLayout.getWidth() < dropDownWidth){
                        autoCompleteTextView.setDropDownWidth(dropDownWidth);
                    }else{
                        autoCompleteTextView.setDropDownWidth(chipLayout.getWidth());
                    }
                }
            });
        }else{
            if(chipLayout.getWidth() < dropDownWidth){
                autoCompleteTextView.setDropDownWidth(dropDownWidth);
            }else{
                autoCompleteTextView.setDropDownWidth(chipLayout.getWidth());
            }
        }
        autoCompleteTextView.setDropDownVerticalOffset(3);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                autoCompleteTextView.setText(autoCompleteTextView.getText().toString() + ",");
                if(onItemClickListener != null){
                    onItemClickListener.onItemClick(arg0, arg1, arg2, arg3);
                }
            }
        });


        if(val != null){
            autoCompleteTextView.setText(val);
        }
        return layout;
    }


    void createNewChipLayout(String val){
        this.addView(createChips(context, val, false));
    }

    void createNewChipLayout(String val, boolean setText){
        this.addView(createChips(context, val, setText));
    }

    void chipCreated(ViewGroup vg){
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) vg.getChildAt(labelPosition);
        int pos = chipLayout.indexOfChild(vg);

        if(chipItemChangeListener != null){
            if(autoCompleteTextView.getText() != null && autoCompleteTextView.getText().toString().length() > 0){
                chipItemChangeListener.onChipAdded(pos, autoCompleteTextView.getText().toString());
            }else {
                chipItemChangeListener.onChipAdded(pos, "");
            }
        }
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener f) {
        onFocusChangeListener = f;
        if (this.getChildCount() > 0){
            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) ((ViewGroup)this.getChildAt(this.getChildCount()-1)).getChildAt(labelPosition);
            OnFocusChangeListener focusChangeListener = new ChipOnFocusChangeListener(this, autoCompleteTextView,
                    chipDrawable, chipLayoutDrawable, onFocusChangeListener);
            autoCompleteTextView.setOnFocusChangeListener(focusChangeListener);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        onClickListener = l;
    }

    private void setOnClickListener(){
        super.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        onLayoutClick();
        if (onClickListener != null){
            onClickListener.onClick(view);
        }
    }

    private void onLayoutClick(){
        int totalChips = this.getChildCount() - 1;
        if(totalChips < 0){
            createNewChipLayout(null);
        }else{
            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) ((ViewGroup)this.getChildAt(totalChips)).getChildAt(labelPosition);
            if(autoCompleteTextView.isFocusable()){
                autoCompleteTextView.requestFocus();
                InputMethodManager inputMethodManager=(InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(autoCompleteTextView.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            }else{
                createNewChipLayout(null);
            }
        }

    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        this.onItemClickListener = l;
    }

    public AdapterView.OnItemClickListener getOnItemClickListener() {
         return onItemClickListener;
    }

    public void addLayoutTextChangedListener(TextWatcher textWatcher){
        listTextWatcher.add(textWatcher);
        if (this.getChildCount() > 0){
            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) ((ViewGroup)this.getChildAt(this.getChildCount()-1)).getChildAt(labelPosition);
            autoCompleteTextView.addTextChangedListener(textWatcher);
        }
    }

    public void removeLayoutTextChangedListener(TextWatcher textWatcher){
        listTextWatcher.remove(textWatcher);
        if (this.getChildCount() > 0){
            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) ((ViewGroup)this.getChildAt(this.getChildCount()-1)).getChildAt(labelPosition);
            autoCompleteTextView.removeTextChangedListener(textWatcher);
        }
    }

    public void setTextColor(int textColor){
        this.textColor = textColor;
        for (int i = 0; i < this.getChildCount(); i++){
            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) ((ViewGroup)this.getChildAt(i)).getChildAt(labelPosition);
            autoCompleteTextView.setTextColor(textColor);
            ((ChipTextWatcher)focusedTextWatcher).setTextColor(textColor);

        }
    }

    public int getTextColor(){
        return  this.textColor;
    }

    public void setChipColor(int bgColor){
        this.chipColor = bgColor;
        this.chipDrawable = null;
        for (int i = 0; i < this.getChildCount(); i++){
            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) ((ViewGroup)this.getChildAt(i)).getChildAt(labelPosition);
            if(!autoCompleteTextView.isFocusable()){
                View v = this.getChildAt(i);
                v.setBackgroundColor(chipColor);
            }else{
                ((ChipTextWatcher)focusedTextWatcher).setChipColor(chipColor);
                ((ChipTextWatcher)focusedTextWatcher).setChipDrawable(null);
            }
        }
    }

    public int getChipColor(){
        return this.chipColor;
    }

    public void highlightChipAt(int pos, int bgColor, int textColor){
        View v = this.getChildAt(pos);
        v.setBackgroundColor(bgColor);
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) ((ViewGroup)v).getChildAt(labelPosition);
        autoCompleteTextView.setTextColor(textColor);

    }

    public void highlightChipAt(int pos, Drawable bgDrawable, int textColor){
        View v = this.getChildAt(pos);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            v.setBackground(bgDrawable);
        } else{
            v.setBackgroundDrawable(bgDrawable);
        }
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) ((ViewGroup)v).getChildAt(labelPosition);
        autoCompleteTextView.setTextColor(textColor);
    }


    public void setChipDrawable(Drawable bgDrawable){
        this.chipDrawable = bgDrawable;
        for (int i = 0; i < this.getChildCount(); i++){
            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) ((ViewGroup)this.getChildAt(i)).getChildAt(labelPosition);
            if(!autoCompleteTextView.isFocusable()){
                View v = this.getChildAt(i);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                    v.setBackground(chipDrawable);
                } else{
                    v.setBackgroundDrawable(chipDrawable);
                }
            }else {
                ((ChipTextWatcher)focusedTextWatcher).setChipDrawable(chipDrawable);
            }

        }
    }

    public Drawable getChipDrawable(){
        return this.chipDrawable;
    }

    public void setHint(String hint){
        hintText = hint;
        if (this.getChildCount() == 1){
            AutoCompleteTextView textView = (AutoCompleteTextView) this.getChildAt(0).findViewWithTag(autoCompleteTextViewTag);
            textView.setHint(hintText);
        }
    }

    public String getHint(){
        return hintText;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setText(List<String> vals){
        this.removeAllViews();
        for (String str: vals){
            createNewChipLayout(str+",", true);
        }
    }

    public List<String> getText(){
        List<String> textList = new ArrayList<>();
        for (int i = 0; i < this.getChildCount(); i++){
            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) ((ViewGroup)this.getChildAt(i)).getChildAt(labelPosition);
            if(autoCompleteTextView.getText() != null && autoCompleteTextView.getText().toString().length() > 0){
                textList.add(autoCompleteTextView.getText().toString());
            }
        }
        return textList;
    }

    public void removeChipAt(int pos){
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) ((ViewGroup)this.getChildAt(pos)).getChildAt(labelPosition);
        this.removeViewAt(pos);
        if(chipItemChangeListener != null){
            if(autoCompleteTextView.getText() != null && autoCompleteTextView.getText().toString().length() > 0){
                chipItemChangeListener.onChipRemoved(pos, autoCompleteTextView.getText().toString());
            }else{
                chipItemChangeListener.onChipRemoved(pos, "");
            }
        }
        setHint(hintText);
    }

    public void removeAllChips(){
        this.removeAllViews();
        createNewChipLayout(null);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setLayoutBackground(Drawable drawable){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            this.setBackground(drawable);
        } else{
            this.setBackgroundDrawable(drawable);
        }
    }

    public void setAdapter(ArrayAdapter adapter) {
        this.adapter =  adapter;
        if (this.getChildCount() > 0){
            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) ((ViewGroup)this.getChildAt(this.getChildCount()-1)).getChildAt(labelPosition);
            autoCompleteTextView.setAdapter(adapter);
        }
    }

    public ArrayAdapter getAdapter() {
        return this.adapter;
    }

}
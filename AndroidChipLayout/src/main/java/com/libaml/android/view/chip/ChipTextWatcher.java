package com.libaml.android.view.chip;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;


public class ChipTextWatcher implements TextWatcher {

    private ViewGroup chip;
    private Context context;
    private ChipLayout chipLayout;
    private int textColor, chipColor;
    private Drawable chipDrawable;
    private boolean showDeleteButton, setText;
    private int labelPosition;
    private float textSize = 0;
    private List<TextWatcher> listTextWatcher;

    public ChipTextWatcher(ViewGroup chip, Context context,
                           ChipLayout chipLayout, int textColor, int chipColor,
                           Drawable chipDrawable, boolean showDeleteButton,
                           int labelPosition, List<TextWatcher> listTextWatcher,
                           boolean setText) {
        this.chip = chip;
        this.chipLayout = chipLayout;
        this.context = context;
        this.textColor = textColor;
        this.chipColor = chipColor;
        this.chipDrawable = chipDrawable;
        this.showDeleteButton = showDeleteButton;
        this.labelPosition = labelPosition;
        this.listTextWatcher = listTextWatcher;
        this.setText = setText;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void afterTextChanged(Editable editable) {
        String text = editable.toString();
        if(text != null && text.length() > 0){
            if (text.charAt(text.length()-1) == ','){
                EditText editText = (EditText)chip.getChildAt(labelPosition);
                String val = text.substring(0, text.length()-1);
                if(val.length() > ChipLayout.MAX_CHARACTER_COUNT){
                    editText.setText(textToChip(val, true));
                }else {
                    editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    editText.setText(val);
                }
                editText.setClickable(false);
                editText.setCursorVisible(false);
                editText.setFocusable(false);
                editText.setFocusableInTouchMode(false);
                ((AutoCompleteTextView)editText).setAdapter(null);
                ((AutoCompleteTextView)editText).setOnItemClickListener(null);

                if(chipDrawable != null){
                    int currentVersion = Build.VERSION.SDK_INT;
                    if (currentVersion >= Build.VERSION_CODES.JELLY_BEAN){
                        chip.setBackground(chipDrawable);
                    } else{
                        chip.setBackgroundDrawable(chipDrawable);
                    }
                }else{
                    chip.setBackgroundColor(chipColor);
                }
                if(showDeleteButton){
                    int buttonPosition = 1;
                    if(labelPosition == 1){
                        buttonPosition = 0;
                    }
                    ImageButton close = (ImageButton)chip.getChildAt(buttonPosition);
                    close.setVisibility(View.VISIBLE);
                }
                if(!setText){
                    chipLayout.createNewChipLayout(null);
                }
                chipLayout.chipCreated(chip);
            }

        }

    }

    private SpannableStringBuilder textToChip(String val, boolean trim){

        SpannableStringBuilder ssb = new SpannableStringBuilder(val);

        try{
            TextView textView = createAutoCompleteTextView(context);
            if (trim){
                textView.setText(val.substring(0, ChipLayout.MAX_CHARACTER_COUNT)+"..");
            }else {
                textView.setText(val);
            }
            int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            textView.measure(spec, spec);
            textView.layout(0, 0, textView.getMeasuredWidth(),textView.getMeasuredHeight());
            Bitmap b = Bitmap.createBitmap(textView.getWidth(),textView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(b);
            canvas.translate(-textView.getScrollX(), -textView.getScrollY());
            textView.draw(canvas);
            textView.setDrawingCacheEnabled(true);
            Bitmap cacheBmp = textView.getDrawingCache();
            Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
            textView.destroyDrawingCache();
            BitmapDrawable bmpDrawable = new BitmapDrawable(context.getResources(), viewBmp);
            bmpDrawable.setBounds(0, 0, bmpDrawable.getIntrinsicWidth(), bmpDrawable.getIntrinsicHeight());
            ssb.setSpan(new ImageSpan(bmpDrawable), 0, val.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }catch (Exception e){}


        return ssb;
    }

    private TextView createAutoCompleteTextView(Context context)
    {
        final ChipLayout.LayoutParams lparamsTextView = new ChipLayout.LayoutParams(ChipLayout.LayoutParams.WRAP_CONTENT, ChipLayout.LayoutParams.WRAP_CONTENT);
        lparamsTextView.setMargins(0, 0, 0, 0);
        final TextView textView = new AutoCompleteTextView(context);
        textView.setPadding(0, 0, 0, 0);
        textView.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        textView.setLayoutParams(lparamsTextView);
        textView.setSingleLine(true);
        textView.setTextColor(textColor);
        if(textSize > 0){
            textView.setTextSize(textSize);
        }
        return textView;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setChipColor(int chipColor) {
        this.chipColor = chipColor;
    }

    public void setChipDrawable(Drawable chipDrawable) {
        this.chipDrawable = chipDrawable;
    }
}

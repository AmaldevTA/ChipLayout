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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void afterTextChanged(Editable editable) {
        String text = editable.toString();
        if(text != null && text.length() > 0){
            if (text.charAt(text.length()-1) == ','){
                EditText editText = (EditText)chip.getChildAt(labelPosition);
                String val = text.substring(0, text.length()-1);
                if(val.length() > 20){
                    editText.setText(textToChip(val.substring(0, 20), true));
                }else {
                   // editText.setText(textToChip(val, false));
                    editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    editText.setText(val);
                }
                editText.setClickable(false);
                editText.setCursorVisible(false);
                editText.setFocusable(false);
                editText.setFocusableInTouchMode(false);
                ((AutoCompleteTextView)editText).setAdapter(null);
                ((AutoCompleteTextView)editText).setOnItemClickListener(null);

//                editText.removeTextChangedListener(this);
//                for (TextWatcher tw: listTextWatcher){
//                    editText.removeTextChangedListener(tw);
//                }

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
            }

        }

    }

    private SpannableStringBuilder textToChip(String val, boolean trim){

        SpannableStringBuilder ssb = new SpannableStringBuilder(val);
        TextView textView = createAutoCompleteTextView(context);
        if (trim){
            textView.setText(val+"..");
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
        return textView;
    }
}

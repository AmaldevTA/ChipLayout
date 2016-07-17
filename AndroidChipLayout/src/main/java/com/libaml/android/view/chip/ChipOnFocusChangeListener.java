package com.libaml.android.view.chip;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import java.lang.reflect.Method;


public class ChipOnFocusChangeListener implements View.OnFocusChangeListener {

    private ChipLayout chipLayout;
    private EditText editText;
    private Drawable editTextDrawable, chipLayoutDrawable;
    private View.OnFocusChangeListener focusChangeListener;

    public ChipOnFocusChangeListener(ChipLayout chipLayout, EditText editText,
                                     Drawable editTextDrawable, Drawable chipLayoutDrawable,
                                     View.OnFocusChangeListener focusChangeListener) {
        this.chipLayout = chipLayout;
        this.editText = editText;
        this.editTextDrawable = editTextDrawable;
        this.chipLayoutDrawable = chipLayoutDrawable;
        this.focusChangeListener = focusChangeListener;
    }

    @Override
    public void onFocusChange(View view, final boolean b) {

        if(focusChangeListener != null){
            focusChangeListener.onFocusChange(view, b);
        }

        if(chipLayout.getWidth() < 1){
            ViewTreeObserver vto = chipLayout.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < 16) {
                        chipLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        chipLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    changeBackground(b);
                }
            });
        }else{
            changeBackground(b);
        }
    }

    void changeBackground(boolean b){
        try{

            if(chipLayoutDrawable != null && chipLayoutDrawable instanceof StateListDrawable){

                StateListDrawable stateListDrawable = (StateListDrawable) chipLayoutDrawable;
                Method getStateDrawable = StateListDrawable.class.getMethod("getStateDrawable", int.class);
                //int[] currentState = stateListDrawable.getState();
                //Method getStateDrawableIndex = StateListDrawable.class.getMethod("getStateDrawableIndex", int[].class);
                int stateEnabled = 1;
                int statePressed = 1;

                for (int i = 0; i < 4; i++){
                    try{
                        Drawable drawable = (Drawable) getStateDrawable.invoke(stateListDrawable, i);
                        if(drawable != null){
                            if (stateListDrawable.getCurrent() == drawable.getCurrent()){
                                stateEnabled = i;
                            }else {
                                statePressed = i;
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                if(b){
                    Drawable drawable = (Drawable) getStateDrawable.invoke(stateListDrawable, statePressed);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                        chipLayout.setBackground(drawable);
                    } else{
                        chipLayout.setBackgroundDrawable(drawable);
                    }
                    chipLayout.requestFocus();
                }else {
                    Drawable drawable = (Drawable) getStateDrawable.invoke(stateListDrawable, stateEnabled);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                        chipLayout.setBackground(drawable);
                    } else{
                        chipLayout.setBackgroundDrawable(drawable);
                    }
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

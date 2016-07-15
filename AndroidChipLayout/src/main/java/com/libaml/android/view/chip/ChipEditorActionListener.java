package com.libaml.android.view.chip;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by amal on 7/7/16.
 */
public class ChipEditorActionListener implements EditText.OnEditorActionListener {

    private EditText editText;

    public ChipEditorActionListener(EditText editText) {
        this.editText = editText;
    }

    public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (editText.getText() != null && editText.getText().toString().length() > 0){
                editText.setText(editText.getText().toString()+",");
            }
        }
        return true;
    }
}

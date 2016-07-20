package chip.android.lib.chiplayout;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import com.libaml.android.view.chip.ChipLayout;


public class MainActivity extends AppCompatActivity {

    ChipLayout chip;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ChipLayout.MAX_CHARACTER_COUNT = 20;
        chip = (ChipLayout) findViewById(R.id.chipText);

        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("---------------",chip.getText().toString());

            }
        });
        chip.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("---------------",""+i);
            }
        });
        chip.addLayoutTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
               // Log.d("---------------",editable.toString());

            }
        });
        chip.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Log.d("----",String.valueOf(b));
            }
        });
        chip.setOnChipItemChangeListener(new ChipLayout.ChipItemChangeListener() {
            @Override
            public void onChipAdded(int pos, String txt) {
                Log.d(txt,String.valueOf(pos));

            }

            @Override
            public void onChipRemoved(int pos, String txt) {
                Log.d(txt,String.valueOf(pos));
            }
        });


        String[] countries = {"india","australia","austria","indonesia","canada"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,countries);
        chip.setAdapter(adapter);



    }

    public void click(View v){
        Log.d("---------------",chip.getText().toString());

    }
}

ChipLayout
==========

ChipLayout is an opensource Android library. ChipLayout is an layout which create chips from the text you type and also allow you to show drop down(like MultiautocompleteTextView)

Example
-------
![Framed example screenshot](https://github.com/OfficialAmal/ChipLayout/tree/master/img/img1.png)
![Framed example screenshot](https://github.com/OfficialAmal/ChipLayout/tree/master/img/img2.png)
Source code with examples is included in repository.

Usage
-----
```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:custom="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#FFF"
    android:padding="10dp"
    android:orientation="vertical">
    <com.libaml.android.view.chip.ChipLayout
        android:id="@+id/chipText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        custom:textSize_="10dp"
        custom:chipPadding_="4dp"
        custom:chipTextPadding_="2dp"
        custom:textColor_="@android:color/black"
        custom:deleteIcon_="@android:drawable/presence_offline"
        custom:chipDrawable_="@drawable/round_corner_drawable"
        custom:chipLayoutDrawable_="@drawable/edittext_theme_landing_page"
        custom:labelPosition_="left">
    </com.libaml.android.view.chip.ChipLayout>
</LinearLayout>

```
======
Find the View in your Activity or Fragment class.

```java
  ChipLayout chip = (ChipLayout) findViewById(R.id.chipText);
        
        String[] countries = {"india","australia","austria","indonesia","canada"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,countries);
        chip.setAdapter(adapter);

        chip.setOnClickListener(ClickListener);
        chip.setOnItemClickListener(ItemClickListener);
        chip.addLayoutTextChangedListener(TextChangedListener);
        chip.setOnFocusChangeListener(FocusChangeListener);
```


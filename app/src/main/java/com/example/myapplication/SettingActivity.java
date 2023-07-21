package com.example.myapplication;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        MyDatabaseHelper myDB = MyDatabaseHelper.getInstance(this);

        EditText editText = findViewById(R.id.editText);
        editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) }); // Optional: Set maximum character length
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText editText2 = findViewById(R.id.editText2);
        editText2.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) }); // Optional: Set maximum character length
        editText2.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText editText3 = findViewById(R.id.editText3);
        editText3.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) }); // Optional: Set maximum character length
        editText3.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText editText6 = findViewById(R.id.editText6);
        editText6.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) }); // Optional: Set maximum character length
        editText6.setInputType(InputType.TYPE_CLASS_NUMBER);

        //myDB.SettingValues(Integer.parseInt(String.valueOf(editText)),Integer.parseInt(String.valueOf(editText2)),Integer.parseInt(String.valueOf(editText3)),Integer.parseInt(String.valueOf(editText6)));


    }
}
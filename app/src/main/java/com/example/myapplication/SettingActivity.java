package com.example.myapplication;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    private Button buttonSave;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        buttonSave = findViewById(R.id.buttonSave);

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



        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //myDB.SettingValues(Integer.parseInt(String.valueOf(editText)),Integer.parseInt(String.valueOf(editText2)),Integer.parseInt(String.valueOf(editText3)),Integer.parseInt(String.valueOf(editText6)));
                String value1 = editText.getText().toString().trim();
                String value2 = editText2.getText().toString().trim();
                String value3 = editText6.getText().toString().trim();
                String value4 = editText3.getText().toString().trim();

                // Check if the values are not empty before parsing
                if (!value1.isEmpty() && !value2.isEmpty() && !value3.isEmpty() && !value4.isEmpty()) {
                    int intValue1 = Integer.parseInt(value1);  //daily intake
                    int intValue2 = Integer.parseInt(value2);  //daily step
                    int intValue3 = Integer.parseInt(value3);  //step remind
                    int intValue4 = Integer.parseInt(value4);  //minute remind

                    myDB.SettingValues(intValue1, intValue2, intValue3, intValue4);
                } else {
                    // Handle the case where EditText values are empty
                    Toast.makeText(SettingActivity.this, "Please enter all values.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //myDB.SettingValues();
    }


}
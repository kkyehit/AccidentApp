package com.example.accident_alarm;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by 희준 on 2016-08-22.
 */
public class Add_number_dialog extends Activity {
    EditText editText;
    Button button;

    FileOutputStream output_open;
   File file;
    byte [] data = null;
String s = "";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_number);

        editText = (EditText) findViewById(R.id.add_number_edittext);
        button = (Button) findViewById(R.id.add_number_ok);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String s = editText.getText().toString()+"/";
                    try {
                        FileOutputStream Save_file = openFileOutput("number.txt", MODE_APPEND);
                        OutputStreamWriter Save_file_writer = new OutputStreamWriter(Save_file);
                        Save_file_writer.write(s);
                        Save_file_writer.close();
                        Save_file.close();
                        Toast.makeText(getApplication(), "저장되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (java.io.IOException e2) {
                        Toast.makeText(getApplication(), "오류가 발생하였습니다." +
                                " 저장하지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                finish();
            }
        });
    }
}

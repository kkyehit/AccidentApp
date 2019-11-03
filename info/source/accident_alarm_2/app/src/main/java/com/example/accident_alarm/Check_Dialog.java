package com.example.accident_alarm;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.zip.Inflater;

/**
 * Created by 희준 on 2016-05-03.
 */
public class Check_Dialog extends DialogFragment {
    int situation;
    Intent intent;

    Double lattitude;
    Double longitude;

    Button ok,cancel;
    TextView text;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.check_dialog,container,false);
        intent = new Intent(getActivity(), Accident.class);

        ok = (Button) v.findViewById(R.id.Check_ok);
        cancel= (Button) v.findViewById(R.id.Check_cancel);
        text= (TextView) v.findViewById(R.id.Check_text);

        if(situation == 1){
            text.setText("사고상황입니까?");
        }else if(situation == 2){
            text.setText("정차하셧습니까?");
        }
        else{
            text.setText("오류가 있습니다. 다시시도해 주세요.");
        }
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("Extra",situation);
                intent.putExtra("Lattitude",lattitude);
                intent.putExtra("Longitude",longitude);
                startActivity(intent);
                dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return v;
    }
/*
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Inflater inflater = new Inflater();
        inflater.inflate(R.layout.check_dialog,contaner,false);
        intent = new Intent(getActivity(), Accident.class);

        ok = (Button) getActivity().findViewById(R.id.Check_ok);
        cancel= (Button) getActivity().findViewById(R.id.Check_cancel);
        text= (TextView) getActivity().findViewById(R.id.Check_text);
        if(situation == 0){
            text.setText("사고상황입니까?");
        }else if(situation == 1){
            text.setText("정차하셧습니까?");
        }

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("extra",situation);
                startActivity(intent);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return super.onCreateDialog(savedInstanceState);

    }*/
}

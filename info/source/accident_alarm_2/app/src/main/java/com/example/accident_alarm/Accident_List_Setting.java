package com.example.accident_alarm;

import java.util.ArrayList;

/**
 * Created by 희준 on 2016-05-17.
 */
public class Accident_List_Setting {
    ArrayList<String> array;
    Accident_List_Setting(){
        array = new ArrayList<String>();
}
    public void add_array_list(String union_string) {
        String parse_string[] = union_string.split("@");
        int i;
        for(i = array.size() -1 ; i > -1; i--)
            array.remove(i);
        for (i = 0; i < parse_string.length; i++) {
            array.add(parse_string[i]);
        }
    }
    public  ArrayList<String> get_array_list(){
        return array;
    }
}

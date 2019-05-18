package com.record.recordingapp.util;

import com.record.recordingapp.dummy.Phones;
import com.record.recordingapp.dummy.Country;

import org.json.JSONObject;

import java.util.ArrayList;

public class Constant {
    public static JSONObject userData = null;
    public static ArrayList<Country> countries = new ArrayList<Country>();
    public static ArrayList<Phones> phones = new ArrayList<Phones>();
    public static String fileName = "record.3gp";
    public static String userEmail = "";
    public static String userPassword = "";
}

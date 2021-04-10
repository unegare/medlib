package com.example.medlib;

import java.util.ArrayList;

public class DoctorItem {
    private String mProfileName;
    private int mProfileID;

    public DoctorItem(String profileName, int profileID) {
        mProfileName = profileName;
        mProfileID = profileID;
    }

    public String getProfileName() {
        return mProfileName;
    }

    public int getProfileID() {
        return mProfileID;
    }

//    public static int lastDoctorId = 0;
//
//    public static ArrayList<DoctorItem> createDoctorList(int num) {
//        ArrayList<DoctorItem> doctors = new ArrayList<DoctorItem>();
//
//        for (int i = 0; i < num; i++) {
//            doctors.add(new DoctorItem("Doctor #" + (lastDoctorId++)));
//        }
//        return doctors;
//    }
}

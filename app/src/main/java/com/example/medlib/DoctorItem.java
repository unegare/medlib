package com.example.medlib;

import java.util.ArrayList;
import java.util.function.IntConsumer;

public class DoctorItem {
    private String mProfileName;
    private int mProfileID;
    private IntConsumer mOnClick;

    public DoctorItem(String profileName, int profileID, IntConsumer onClick) {
        mProfileName = profileName;
        mProfileID = profileID;
        mOnClick = onClick;
    }

    public String getProfileName() {
        return mProfileName;
    }

    public int getProfileID() {
        return mProfileID;
    }

    public IntConsumer getOnClickHandler() {
        return mOnClick;
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

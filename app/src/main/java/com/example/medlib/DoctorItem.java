package com.example.medlib;

import java.util.ArrayList;
import java.util.function.IntConsumer;

public class DoctorItem {
    private String mProfileName;
    private int mID;
    private IntConsumer mOnClick;
    private IntConsumer mOnLongClick;

    public DoctorItem(String profileName, int ID, IntConsumer onClick, IntConsumer onLongClick) {
        mProfileName = profileName;
        mID = ID;
        mOnClick = onClick;
        mOnLongClick = onLongClick;
    }

    public String getProfileName() {
        return mProfileName;
    }

    public int getID() {
        return mID;
    }

    public IntConsumer getOnClickHandler() {
        return mOnClick;
    }

    public IntConsumer getOnLongClickHandler() {return mOnLongClick;}

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

package com.bignerdranch.android.criminalintent;

import java.sql.Time;
import java.util.Date;
import java.util.UUID;

public class Crime {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private Date mTime;
    private boolean mSolved;
    private String mSuspectId;
    private String mSuspect;
    private String mCallSuspect;

    public Crime(){
        //Generate unique identifier
        this(UUID.randomUUID());
    }

    public Crime(UUID id){
        mId = id;
        mDate = new Date();
        mTime = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setDate(Date date) {mDate = date;}

    public Date getTime() {return mTime;}

    public void setTime(Date time) {mTime = time;}

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public String getSuspectId() {
        return mSuspectId;
    }

    public void setSuspectId(String suspectId) {
        mSuspectId = suspectId;
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }

    public String getPhotoFileName(){
        return "IMG_" + getId().toString() + ".jpg";
    }

    public String getCallSuspect() {
        return mCallSuspect;
    }

    public void setCallSuspect(String callSuspect) {
        mCallSuspect = callSuspect;
    }
}

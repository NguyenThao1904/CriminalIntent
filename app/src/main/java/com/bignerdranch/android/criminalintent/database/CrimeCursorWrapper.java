package com.bignerdranch.android.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.bignerdranch.android.criminalintent.Crime;
import com.bignerdranch.android.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.sql.Date;
import java.util.UUID;

public class CrimeCursorWrapper extends CursorWrapper {
    public CrimeCursorWrapper (Cursor cursor){
        super(cursor);
    }
    public Crime getCrime(){

        // lay gia tri tu column
        String uuidString = getString(getColumnIndex(CrimeTable.Cols.UUID));
        String title = getString(getColumnIndex(CrimeTable.Cols.TITLE));
        long date = getLong(getColumnIndex(CrimeTable.Cols.DATE));
        long time = getLong(getColumnIndex(CrimeTable.Cols.TIME));
        int isSolved = getInt(getColumnIndex(CrimeTable.Cols.SOLVED));
        String suspectId = getString(getColumnIndex(CrimeTable.Cols.SUSPECT_ID));
        String  suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT));
        String  callSuspect = getString(getColumnIndex(CrimeTable.Cols.CALL_SUSPECT));

        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setTitle(title);
        crime.setTime(new Date(time));
        crime.setDate(new Date(date));
        crime.setSolved(isSolved != 0);
        crime.setSuspectId(suspectId);
        crime.setSuspect(suspect);
        crime.setCallSuspect(callSuspect);

        return crime;
    }
}

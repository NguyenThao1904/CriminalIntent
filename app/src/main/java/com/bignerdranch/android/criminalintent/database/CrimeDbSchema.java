package com.bignerdranch.android.criminalintent.database;

import java.util.UUID;

public class CrimeDbSchema {
    public static final class CrimeTable{
        public static final String NAME = "crimes";

// insert columns
        public static final class Cols{
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String TIME = "time";
            public static final String SOLVED = "solved";
            public static final String SUSPECT_ID = "suspect_id";
            public static final String SUSPECT = "suspect";
            public static final String CALL_SUSPECT = "call_suspect";
        }
    }

}

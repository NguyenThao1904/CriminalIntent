package com.bignerdranch.android.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static androidx.core.content.PermissionChecker.checkSelfPermission;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    public static final String FORMAT_DATE = "EEEE, dd MMM yyyy";
    public static final String FORMAT_TIME = "HH:mm";
    private static final int REQUEST_CONTACT = -1;
    private static final int REQUEST_CODE_READ_CONTACTS = -1;
    private static final int REQUEST_PHOTO = 2;
    private static final String DIALOG_PHOTO = "DialogPhoto";


    private Crime mCrime;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallSuspectButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private ViewTreeObserver mPhotoObserver;
    private Callbacks mCallbacks;

    //Required interface for hosting activities
    public interface Callbacks{
        void onCrimeUpdated(Crime crime);
    }

    public static CrimeFragment newInstance(UUID crimeId){
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mCallbacks = (Callbacks)activity;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);

    }

    public  boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_item_delete_crime:
                CrimeLab.get(getActivity()).removeCrime(mCrime);
                Intent i=new Intent(getActivity(),CrimeListActivity.class);
                startActivity(i);

            default:
                return super.onOptionsItemSelected(item);

        }

    }
    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        setHasOptionsMenu(true);
    }

    @Override
    public  void onPause(){
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText)v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //this space intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //this one too
            }
        });
        mDateButton = (Button)v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager,DIALOG_DATE);

            }
        });

        mTimeButton = (Button) v.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getTime());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });

        mSolvedCheckBox = (CheckBox)v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //set the crime's solved property
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use intent builder to create a send action
                // that opens a populated email
                ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setChooserTitle(getString(R.string.send_report))
                        .startChooser();
            }
        });

        final  Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//        pickContact.addCategory(Intent.CATEGORY_HOME);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        if(mCrime.getSuspect() != null){
            mSuspectButton.setText(mCrime.getSuspect());
        }


        mCallSuspectButton = (Button) v.findViewById(R.id.crime_call_suspect);
        //set enable call_suspect button by phone number
        handleCallSuspectButton(mCrime.getCallSuspect());
        mCallSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri number = Uri.parse("tel:" + mCrime.getCallSuspect());
                //create an activity to call this phone number
                Intent i = new Intent(Intent.ACTION_DIAL, number);
                startActivity(i);
            }
        });

        PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null){
            mSuspectButton.setEnabled(false);
            mCallSuspectButton.setEnabled(false);
        }

        // Setup photo taking abilities
        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        mPhotoFile);

                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager()
                        .queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.toString(),
                            uri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    startActivityForResult(captureImage, REQUEST_PHOTO);
                }
            }
        });
        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPhotoFile == null || !mPhotoFile.exists()) {
                    mPhotoView.setImageDrawable(null);
                } else {

                    FragmentManager manager = getFragmentManager();
                    PhotoDetailFragment dialog = PhotoDetailFragment.newInstance(mPhotoFile);
                    dialog.setTargetFragment(CrimeFragment.this, REQUEST_PHOTO);
                    dialog.show(manager, DIALOG_PHOTO);

                }

            }
        });
        updatePhotoView(mPhotoView.getWidth(), mPhotoView.getHeight());
        mPhotoObserver = mPhotoView.getViewTreeObserver();
        mPhotoObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updatePhotoView(mPhotoView.getWidth(), mPhotoView.getHeight());
            }
        });

        return v;
    }

    private void updateCrime(){
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    private void updateTime() {
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        mTimeButton.setText(df.format(FORMAT_TIME, mCrime.getTime()));

    }

    private void updateDate() {
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        mDateButton.setText(df.format(FORMAT_DATE, mCrime.getDate()));
    }

    private String getCrimeReport(){
        String solvedString = null;
        if(mCrime.isSolved()){
            solvedString = getString(R.string.crime_report_solved);
        }else {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        String dateFormat = "EEE, MMM dd";
        String dateString = android.text.format.DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if(suspect == null){
            suspect = getString(R.string.crime_report_no_suspect);
        }else{
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }


private void updatePhotoView(int width, int height) {
    if (mPhotoFile == null || !mPhotoFile.exists()) {
        mPhotoView.setImageDrawable(null);
    } else {
        Bitmap bitmap = PictureUtils.getScaledBitmap(
                mPhotoFile.getPath(), width, height); // change this one
        mPhotoView.setImageBitmap(bitmap);
    }
}

    @Override
    public void  onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if(requestCode == REQUEST_TIME || requestCode == REQUEST_DATE){
            if(requestCode == REQUEST_TIME){
                Date time = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
                mCrime.setTime(time);
                updateTime();
            }
            if(requestCode == REQUEST_DATE){
                Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                mCrime.setDate(date);
                updateDate();
            }
        } else if(requestCode == REQUEST_CONTACT && data != null){
            Uri contactUri = data.getData();
            //Special which fields you want your query to return
            //values for
            String[] queryFields = new String[]{

                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            //Perform your query - the contactUri os like a "where" clause here
            Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);
            try{
                //Double-check that you actually gor results
                if(c.getCount() == 0){
                    return;
                }
                //Pull out the first column of the first row of the data that is your suspect's name
                c.moveToFirst();
                String suspect = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                // Get the ID of the suspect
                String suspectId = c.getString(
                        c.getColumnIndex(ContactsContract.Contacts._ID));
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(mCrime.getSuspect());
                mCrime.setSuspectId(suspectId);
                getContactPhoneNumberWrapper();

            }finally {
                c.close();
            }
        }else if (requestCode == REQUEST_PHOTO) {

            updatePhotoView(mPhotoView.getWidth(), mPhotoView.getHeight());
        }
        updateCrime();
        handleCallSuspectButton(mCrime.getCallSuspect());

    }


    private void getContactPhoneNumberWrapper(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // See: https://developer.android.com/training/permissions/requesting.html

            //Ask user whether allow to access READ_CONTACTS or not
            //To check if the user has already granted your app a particular permission
            int hasReadContactsPermission = checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS);

            //User allow
            if(hasReadContactsPermission != PackageManager.PERMISSION_GRANTED) {
                // See: http://stackoverflow.com/a/33080682
                //Explain why your app needs the permission
                requestPermissions(
                        new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_CODE_READ_CONTACTS
                );
            } else {
                mCrime.setCallSuspect(getContactPhoneNumber());
            }
        } else {
            mCrime.setCallSuspect(getContactPhoneNumber());
        }
        mCallSuspectButton.setEnabled(true);
    }


    private String getContactPhoneNumber(){
        String[] fields = new String[] {
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        Cursor cursor = getActivity().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, fields,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{mCrime.getSuspectId()}, null
        );

        String phoneNumber = "";
        try {
            if(cursor.getCount() == 0) {
                return phoneNumber;
            }
            cursor.moveToFirst();
            phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        } finally {
            cursor.close();
        }
        return phoneNumber;
    }

    //Method phan hoi cho phep permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCrime.setCallSuspect(getContactPhoneNumber());
                    handleCallSuspectButton(mCrime.getCallSuspect());

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    private void handleCallSuspectButton(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            mCallSuspectButton.setText(getString(R.string.crime_call_no_phone_number));
            mCallSuspectButton.setEnabled(false);
        } else {
            mCallSuspectButton.setText(getString(R.string.crime_call_suspect, mCrime.getCallSuspect()));
            mCallSuspectButton.setEnabled(true);
        }
    }


}

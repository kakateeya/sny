package org.hss.sny.sooryanamaskarayagnya;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;

import org.hss.sny.gae.memberDetailsApi.model.MemberDetails;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MemberDetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MemberDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberDetailsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private boolean mRegistered;
    private String mUid;
    private Member mMember;

    private OnFragmentInteractionListener mListener;

    private static final String TAG = "RegistrationActivity";

    EditText mFirstName;
    EditText mLastName;
    EditText mAge;
    RadioButton mIsMale;
    EditText mCity;
    EditText mState;
    Spinner mCountry;
    EditText mZip;
    Button mRegister;
    Context mContext;
    EditText mEmail;
    ProgressDialog mPD;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MemberDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MemberDetailsFragment newInstance(boolean registered, String uid) {
        MemberDetailsFragment fragment = new MemberDetailsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, registered);
        args.putString(ARG_PARAM2, uid);
        fragment.setArguments(args);
        return fragment;
    }

    public MemberDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRegistered = getArguments().getBoolean(ARG_PARAM1);
            mUid = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_registration, container, false);
        mContext = getActivity();
        mFirstName = (EditText)view.findViewById(R.id.firstName);
        mLastName = (EditText)view.findViewById(R.id.lastName);
        mAge = (EditText)view.findViewById(R.id.age);
        mIsMale = (RadioButton)view.findViewById(R.id.male);
        mCity = (EditText)view.findViewById(R.id.city);
        mState = (EditText)view.findViewById(R.id.state);
        mCountry = (Spinner)view.findViewById(R.id.country);
        mZip = (EditText)view.findViewById(R.id.zip);
        mRegister = (Button) view.findViewById(R.id.button);
        mEmail = (EditText) view.findViewById(R.id.email);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Member member = getMemberInfo();
                if (member != null) {
                    if (mUid == null) {
                        RegisterWithServerTask rss = new RegisterWithServerTask();
                        rss.execute(member);
                        mPD = Utils.showProgressDialog(mContext, "Registering...", "Please wait...");
                    } else {
                        //editing existing member
                        UpdateWithServerTask updTask = new UpdateWithServerTask();
                        updTask.execute(mMember);
                        mPD = Utils.showProgressDialog(mContext, "Updating...", "please wait...");
                    }
                }
            }
        });
        if (mUid != null) {
            mRegister.setText("Update");
            PopulateMemberDataTask pmdt = new PopulateMemberDataTask(mUid);
            pmdt.execute();
        } else {
            PopulateDataTask pdt = new PopulateDataTask();
            pdt.execute();
        }

        return view;
    }

    private Member getMemberInfo () {
        String firstName = mFirstName.getText().toString();
        String lastName = mLastName.getText().toString();
        String age = mAge.getText().toString();
        String city = mCity.getText().toString().toUpperCase();
        String state = mState.getText().toString().toUpperCase();
        int countryIdx = mCountry.getSelectedItemPosition();
        String country = mCountry.getSelectedItem().toString();
        String zip = mZip.getText().toString().toUpperCase();
        String email = mEmail.getText().toString();

        if (TextUtils.isEmpty(firstName) ||
                TextUtils.isEmpty(lastName) ||
                TextUtils.isEmpty(age) ||
                TextUtils.isEmpty(city) ||
                TextUtils.isEmpty(state) ||
                (countryIdx == 0) ||
                TextUtils.isEmpty(country) ||
                TextUtils.isEmpty(zip)) {
            Toast.makeText(mContext, "All fields are required", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (firstName.length() < 3 || lastName.length() < 3) {
            Toast.makeText(mContext, "First or Last name too short (<3)", Toast.LENGTH_SHORT).show();
            return null;
        }
        int ageint;
        try {
            ageint = Integer.parseInt(age);
            if (ageint < 2 || ageint > 100) {
                Toast.makeText(mContext, "Invalid age (2-100)", Toast.LENGTH_SHORT).show();
                return null;
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "Invalid age", Toast.LENGTH_SHORT).show();
            return null;
        }        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(mContext, "Invalid Email address", Toast.LENGTH_SHORT).show();
            return null;
        }

        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(mContext, "Invalid Email address", Toast.LENGTH_SHORT).show();
            return null;
        }

        Member member = mMember;
        if (member == null) {
            member = new Member();
            if (mRegistered) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                member.primaryId = sharedPreferences.getLong(Constants.SHARED_PREF_PRIMARY_MEMBER_ID, 0);
            }
        }
        member.firstName = firstName;
        member.lastName = lastName;
        member.age = ageint;
        member.isMale = mIsMale.isChecked();
        member.city = city;
        member.state = state;
        member.country = country;
        member.countryIdx = countryIdx;
        member.zipcode = zip;
        member.email = email;
        return member;
    }

    class RegisterWithServerTask extends AsyncTask<Member, Void, MemberDetails> {
        private boolean error;
        private String errMsg;

        @Override
        protected MemberDetails doInBackground(Member... params) {
            Member member = params[0];
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String uid = UUID.randomUUID().toString();
            member.uid = uid;
            if (TextUtils.isEmpty(member.email)) {
                member.email = sharedPreferences.getString(Constants.SHARED_PREF_REGISTRATION_ID, null);
            }

            MemberDetails md = ServerApi.getMemberDetails(member);
            try {
                md = ServerApi.getMemberDetailsApi().insert(md).execute();
                List<MemberDetails> lmd = new ArrayList<>();
                lmd.add(md);
                postRegistration(mContext, lmd);
            } catch (IOException e) {
                error = true;
                errMsg = e.getLocalizedMessage();
                Log.e(TAG, errMsg);
            }
            return md;
        }

        @Override
        public void onPostExecute(MemberDetails md) {
            mPD.dismiss();
            if (error) {
                mListener.onFailure(errMsg);
            } else {
                mListener.onSuccess();
            }
        }
    }

    public static MemberDetails postRegistration(Context context, List<MemberDetails> members) throws IOException {
        MemberDetails primaryMember = null;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> mus = sharedPreferences.getStringSet(Constants.SHARED_PREF_MEMBERS, null);
        HashSet<String> memberUids = new HashSet<String>();

        if (mus != null) {
            memberUids.addAll(mus);
        }
        Cache cache = Cache.getInstance(context.getApplicationContext());
        for (MemberDetails md : members) {
            Member member = ServerApi.getMember(md);
            if (member.primaryId == member.id || member.primaryId == 0) {
                primaryMember = md;
                member.primaryId = member.id;
            }
            memberUids.add(member.uid);
            FileOutputStream fos = context.openFileOutput(member.uid, Context.MODE_PRIVATE);
            Gson gson = new Gson();
            fos.write(gson.toJson(member).getBytes());
            fos.close();
            cache.putMember(md);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (primaryMember != null) {
            editor.putLong(Constants.SHARED_PREF_PRIMARY_MEMBER_ID, primaryMember.getId());
            editor.putString(Constants.SHARED_PREF_PRIMARY_MEMBER_UID, primaryMember.getUid());
        }
        editor.putStringSet(Constants.SHARED_PREF_MEMBERS, memberUids);
        editor.apply();

        return primaryMember;
    }

    class PopulateDataTask extends AsyncTask<Void, Void, Void> {
        String firstName;
        String lastName;
        String city;
        String state;
        String country;
        String zipcode;
        int countryIdx;

        @Override
        protected Void doInBackground(Void... params) {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            Location lastKnownLocation = null;
            try {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } catch (Exception e) {

            }
            if (lastKnownLocation == null) {
                try {
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                } catch (Exception e) {

                }
            }
            if (lastKnownLocation != null) {
                try {
                    List<Address> addresses = new Geocoder(mContext).getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address addr = addresses.get(0);
                        city = addr.getLocality();
                        state = addr.getAdminArea();
                        country = addr.getCountryName();
                        int idx = 0;
                        for (String c : mContext.getResources().getStringArray(R.array.countries)) {
                            if (c.equalsIgnoreCase(country)) {
                                countryIdx = idx;
                                break;
                            }
                            idx++;
                        }
                        zipcode = addr.getPostalCode();
                    }
                } catch (IOException ioe) {

                }
            }
            Cursor c = mContext.getContentResolver().query(ContactsContract.Profile.CONTENT_URI, new String[]{ContactsContract.Profile.DISPLAY_NAME}, null, null, null);
            if (c.moveToFirst()) {
                String displayName = c.getString(0);
                String names [] = displayName.split("\\s+");
                firstName = names[0];
                lastName = names[names.length - 1];
            }
            c.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if(!mRegistered && !TextUtils.isEmpty(firstName)) {
                mFirstName.setText(firstName);
            }
            if (!TextUtils.isEmpty(lastName)) {
                mLastName.setText(lastName);
            }
            if (!TextUtils.isEmpty(city)) {
                mCity.setText(city);
            }
            if (!TextUtils.isEmpty(state)) {
                mState.setText(state);
            }
            mCountry.setSelection(countryIdx);

            if (!TextUtils.isEmpty(zipcode)) {
                mZip.setText(zipcode);
            }
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            mEmail.setText(sharedPreferences.getString(Constants.SHARED_PREF_REGISTRATION_ID, null));
        }
    }

    class PopulateMemberDataTask extends AsyncTask<Void, Void, Member> {
        String mUid;
        public PopulateMemberDataTask(String json) {
            mUid = json;
        }
        @Override
        protected Member doInBackground(Void... params) {
            return Cache.getInstance(mContext).getMember(mUid);
        }

        @Override
        protected void onPostExecute(Member v) {
            mFirstName.setText(v.firstName);
            mLastName.setText(v.lastName);
            mAge.setText(Integer.toString(v.age));
            if (v.isMale) {
                mIsMale.setChecked(true);
            } else {

            }
            mCity.setText(v.city);
            mState.setText(v.state);
            //mCountry.setText(v.country);
            mCountry.setSelection(v.countryIdx);
            mZip.setText(v.zipcode);
            if (!TextUtils.isEmpty(v.email)) {
                mEmail.setText(v.email);
            }
            mMember = v;
        }
    }

    class UpdateWithServerTask extends AsyncTask<Member, Void, MemberDetails> {
        private boolean error;
        private String errMsg;

        @Override
        protected MemberDetails doInBackground(Member... params) {
            Member member = params[0];
            MemberDetails md = ServerApi.getMemberDetails(member);
            Cache cache = Cache.getInstance(getActivity().getApplicationContext());
            try {
                md = ServerApi.getMemberDetailsApi().update(member.id, md).execute();
                FileOutputStream fos = mContext.openFileOutput(member.uid, Context.MODE_PRIVATE);
                Gson gson = new Gson();
                fos.write(gson.toJson(member).getBytes());
                fos.close();
                cache.putMember(member);
            } catch (IOException e) {
                error = true;
                errMsg = e.getLocalizedMessage();
            }
            return md;
        }

        @Override
        public void onPostExecute(MemberDetails md) {
            mPD.dismiss();
            if (error) {
                Toast.makeText(mContext, errMsg, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext, "Success!", Toast.LENGTH_SHORT).show();
                mListener.onSuccess();
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onSuccess();
        public void onFailure(String errMsg);
    }
}

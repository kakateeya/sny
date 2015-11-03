package org.hss.sny.sooryanamaskarayagnya;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import org.hss.sny.gae.memberDetailsApi.MemberDetailsApi;
import org.hss.sny.gae.memberDetailsApi.model.MemberDetails;
import org.hss.sny.gae.memberDetailsApi.model.MemberDetailsCollection;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class RegistrationActivity extends Activity implements MemberDetailsFragment.OnFragmentInteractionListener {

    private static final String TAG = "RegistrationActivity";

    private Context mContext;
    ProgressDialog mPD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.memberdetailactivity);
        checkPlayServices(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPlayServices(0)) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String registrationId = sharedPreferences.getString(Constants.SHARED_PREF_REGISTRATION_ID, null);
            if (TextUtils.isEmpty(registrationId)) {
                try {
                    GoogleAccountCredential cred = Utils.getBaseCredential(this);
                    startActivityForResult(cred.newChooseAccountIntent(), 0);
                } catch (Exception e) {
                    finish();
                }
            } else {
                startCheckingRegistration(registrationId);
            }
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices(int rc) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, rc).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    private void showErrorDialog (String msg, boolean fatal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setMessage(msg)
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        if (!fatal) {
            builder.setPositiveButton("Try agin", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }

        builder.show();
    }
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getExtras() != null) {
            String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
            if (accountName != null) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                sharedPreferences.edit().putString(Constants.SHARED_PREF_REGISTRATION_ID, accountName).apply();
                return;
            }
        }
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private void startCheckingRegistration(String email) {
        if (mPD != null) {
            mPD.dismiss();
        }
        mPD = Utils.showProgressDialog(this, "Checking registration...", "Please wait...");
        CheckRegistrationTask crt = new CheckRegistrationTask();
        crt.execute(email);
    }

    @Override
    public void onSuccess() {
        success();
    }

    @Override
    public void onFailure(String errMsg) {

    }

    private void success() {
        Toast.makeText(mContext, "Success!", Toast.LENGTH_LONG).show();
        setResult(Activity.RESULT_OK);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private MemberDetailsApi myApi;
    private void initApi() {
        if (myApi == null) {
            myApi = ServerApi.getMemberDetailsApi();
        }
    }

    class CheckRegistrationTask extends AsyncTask<String, Void, MemberDetails> {

        private boolean error;
        private String errMsg;

        @Override
        protected MemberDetails doInBackground(String... params) {
            String email = params[0];
            MemberDetailsCollection mdc = null;
            MemberDetails md = null;
            initApi();
            try {
                mdc = myApi.check(email).execute();
                if (mdc != null) {
                    List<MemberDetails> mds = mdc.getItems();
                    if (mds != null && mds.size() > 0) {
                        md = MemberDetailsFragment.postRegistration(mContext, mdc.getItems());
                    }
                }
            } catch (FileNotFoundException e) {
                error = true;
                errMsg = e.getLocalizedMessage();
                e.printStackTrace();
            } catch (IOException e) {
                error = true;
                errMsg = e.getLocalizedMessage();
                Log.e(TAG, errMsg);
            }

            return md;
        }

        @Override
        public void onPostExecute(MemberDetails memberDetails) {
            mPD.dismiss();
            if (memberDetails == null) {
                if (error) {
                    showErrorDialog(errMsg, true);
                } else {
                    //start the fragment to start registration.
                    Fragment newFragment = MemberDetailsFragment.newInstance(false, null);
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment, newFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            } else {
                Log.d(TAG, "Member found! ");
                success();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPD != null)
            mPD.dismiss();
    }

}
package org.hss.sny.sooryanamaskarayagnya;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;


public class AddMemberActivity extends Activity implements MemberDetailsFragment.OnFragmentInteractionListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memberdetailactivity);

        //start the fragment to start registration.
        Fragment newFragment = MemberDetailsFragment.newInstance(true, null);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onSuccess() {
        finish();
    }

    @Override
    public void onFailure(String errMsg) {
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}

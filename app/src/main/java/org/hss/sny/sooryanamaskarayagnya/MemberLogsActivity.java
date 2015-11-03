package org.hss.sny.sooryanamaskarayagnya;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


public class MemberLogsActivity extends Activity implements MemberLogsFragment.OnFragmentInteractionListener {

    public static final String EXTRA_MEMBER_UID = "EXTRA_MEMBER_UID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_logs);
        Intent intent = getIntent();

        String memberUid = intent.getStringExtra(EXTRA_MEMBER_UID);
        if (savedInstanceState == null) {
            Fragment fragment = MemberLogsFragment.newInstance(memberUid, null);
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

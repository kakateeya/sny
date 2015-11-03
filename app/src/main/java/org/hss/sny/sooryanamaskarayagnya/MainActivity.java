package org.hss.sny.sooryanamaskarayagnya;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Locale;


public class MainActivity extends Activity implements ActionBar.TabListener, MembersFragment.OnFragmentInteractionListener {

    private static final int FRAGMENT_TYPE_MEMBERS = 0;
    private static final int FRAGMENT_TYPE_STATS = 1;
    private static final int FRAGMENT_TYPE_MEDIA = 2;
    //private static final int FRAGMENT_TYPE_MSGS = 2;
    private static final int FRAGMENT_TYPES= FRAGMENT_TYPE_MEDIA + 1;

    StatisticsFragment mStatsFragment;
    MembersFragment mMembersFragment;
    MediaFragment mMediaFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < FRAGMENT_TYPES; i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab().setText(getPageTitle(i)).setTabListener(this).setTag(i));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Long primaryId = sharedPreferences.getLong(Constants.SHARED_PREF_PRIMARY_MEMBER_ID, 0);
        if (primaryId <= 0) {
            startActivity(new Intent(this, RegistrationActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_mem) {
            startActivity(new Intent(this, AddMemberActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        int id = (Integer)tab.getTag();
        switch (id) {
            case FRAGMENT_TYPE_STATS:
                if (mStatsFragment == null) {
                    mStatsFragment = StatisticsFragment.newInstance(null, null);
                    fragmentTransaction.add(android.R.id.content, mStatsFragment);
                } else {
                    fragmentTransaction.attach(mStatsFragment);
                }
                break;

            case FRAGMENT_TYPE_MEDIA:
                if (mMediaFragment == null) {
                    mMediaFragment = MediaFragment.newInstance(null, null);
                    fragmentTransaction.add(android.R.id.content, mMediaFragment);
                } else {
                    fragmentTransaction.attach(mMediaFragment);
                }
                break;
            default:
                if (mMembersFragment == null) {
                    mMembersFragment = MembersFragment.newInstance(null, null);
                    fragmentTransaction.add(android.R.id.content, mMembersFragment);
                } else {
                    fragmentTransaction.attach(mMembersFragment);
                }
                break;
        }

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        int id = (Integer)tab.getTag();
        Fragment fragment = null;
        switch (id) {
            case FRAGMENT_TYPE_STATS:
                fragment = mStatsFragment;
                break;
            case FRAGMENT_TYPE_MEDIA:
                fragment = mMediaFragment;
                break;
            case FRAGMENT_TYPE_MEMBERS:
                fragment = mMembersFragment;
                break;
        }
        if (fragment != null) {
            fragmentTransaction.detach(fragment);
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onFragmentInteraction(Member member) {
        startActivity(new Intent(this, MemberLogsActivity.class)
                .putExtra(MemberLogsActivity.EXTRA_MEMBER_UID, member.uid));
    }

    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case FRAGMENT_TYPE_MEMBERS:
                return getString(R.string.members).toUpperCase(l);
            case FRAGMENT_TYPE_STATS:
                return getString(R.string.stats).toUpperCase(l);
                /*
                case FRAGMENT_TYPE_MSGS:
                    return getString(R.string.messages).toUpperCase(l);
                    */
            case FRAGMENT_TYPE_MEDIA:
                return getString(R.string.media).toUpperCase(l);
        }
        return null;
    }
}

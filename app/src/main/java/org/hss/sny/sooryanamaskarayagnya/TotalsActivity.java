package org.hss.sny.sooryanamaskarayagnya;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.hss.sny.gae.statTotalApi.model.CollectionResponseStatTotal;
import org.hss.sny.gae.statTotalApi.model.StatTotal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


public class TotalsActivity extends Activity {

    private  static final String TAG = "TotalsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_totals);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_totals, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        Spinner mFilter;
        TableLayout mTable;
        ProgressDialog mPD;
        String errMsg;
        Member mMember;
        Activity mContext;

        static final int POS_ALL = 0;
        static final int POS_ZIP = 1;
        static final int POS_CITY = 2;
        static final int POS_STATE = 3;
        static final int POS_COUNTRY = 4;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            mContext = getActivity();
            View rootView = inflater.inflate(R.layout.fragment_totals, container, false);
            mFilter = (Spinner)rootView.findViewById(R.id.spinner);

            mFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mPD = Utils.showProgressDialog(mContext, "Retrieving information...", "Please wait...");
                    GetTotalsTask gtt = new GetTotalsTask();
                    gtt.execute(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    mPD = Utils.showProgressDialog(mContext, "Retrieving information...", "Please wait...");
                    GetTotalsTask gtt = new GetTotalsTask();
                    gtt.execute(0);
                }
            });
            mTable = (TableLayout)rootView.findViewById(R.id.table);
            return rootView;
        }
        class GetTotalsTask extends AsyncTask<Integer, Void, List<StatTotal>> {

            @Override
            public List<StatTotal> doInBackground(Integer...args) {
                List<StatTotal> results = null;
                int pos = args[0];
                if (mMember == null) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    String primaryMemberId = sharedPreferences.getString(Constants.SHARED_PREF_PRIMARY_MEMBER_UID, null);

                    if (primaryMemberId != null) {
                        FileInputStream fis = null;
                        try {
                            fis = mContext.openFileInput(primaryMemberId);
                            mMember = new Gson().fromJson(new JsonReader(new InputStreamReader(fis)), Member.class);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }

                String zip = "";
                String city = "";
                String state = "";
                String country = "";

                if (mMember != null) {
                    switch (pos) {
                        case POS_ZIP:
                            zip = mMember.zipcode;

                        case POS_CITY:
                            city = mMember.city;

                        case POS_STATE:
                            state = mMember.state;

                        case POS_COUNTRY:
                            country = mMember.country;
                            break;
                    }
                } else if (pos > 0) {
                    Toast.makeText(mContext, "Unable to filter", Toast.LENGTH_SHORT).show();
                }
                try {
                    CollectionResponseStatTotal totals = ServerApi.getStatsApi().list(city, country, state, zip).execute();
                    if (totals != null) {
                        results = totals.getItems();
                        if (results != null) {
                            for (StatTotal entry : results) {
                                Log.d(TAG, entry.getMaxAge() + " " + entry.getMales() + " " +
                                        entry.getMcount() + " " + entry.getFemales() + " " + entry.getFcount());
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    errMsg = e.getLocalizedMessage();
                }
                return results;
            }

            @Override
            public void onPostExecute(List<StatTotal> results) {
                mPD.dismiss();
                if (results == null) {
                    Toast.makeText(mContext, "Failed to get info. " + errMsg, Toast.LENGTH_SHORT).show();
                    return;
                }
                int nrows = mTable.getChildCount();
                if (nrows > 1) {
                    mTable.removeViews(1, nrows - 1);
                } else {
                    /*
                    final TableRow tableRow = new TableRow(mContext);
                    tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                    LinearLayout row = (LinearLayout)mContext.getLayoutInflater().inflate(R.layout.table_hdr, tableRow);
                    */
                    LinearLayout row = (LinearLayout)mContext.getLayoutInflater().inflate(R.layout.table_hdr, null);
                    mTable.addView(row);
                }
                int males = 0;
                int females = 0;
                int mtotal = 0;
                int ftotal = 0;
                for (StatTotal total: results) {
                    /*
                    final TableRow tableRow = new TableRow(mContext);
                    tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                    */
                    LinearLayout row = (LinearLayout)mContext.getLayoutInflater().inflate(R.layout.table_row, null);
                    ((TextView)row.findViewById(R.id.age)).setText(Integer.toString(total.getMaxAge())+"+");
                    males += total.getMales();
                    ((TextView)row.findViewById(R.id.males)).setText(Integer.toString(total.getMales()));
                    int mcnt = total.getMcount();
                    mtotal += mcnt;
                    ((TextView)row.findViewById(R.id.mcount)).setText(Integer.toString(mcnt));
                    females += total.getFemales();
                    ((TextView)row.findViewById(R.id.females)).setText(Integer.toString(total.getFemales()));
                    int fcnt = total.getFcount();
                    ftotal += fcnt;
                    ((TextView)row.findViewById(R.id.fcount)).setText(Integer.toString(fcnt));
                    ((TextView)row.findViewById(R.id.total)).setText(Integer.toString(mcnt + fcnt));
                    mTable.addView(row);
                }
                /*
                final TableRow tableRow = new TableRow(mContext);
                tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                */
                LinearLayout row = (LinearLayout)mContext.getLayoutInflater().inflate(R.layout.table_hdr, null);
                ((TextView)row.findViewById(R.id.age)).setText("all");
                ((TextView)row.findViewById(R.id.males)).setText(Integer.toString(males));
                ((TextView)row.findViewById(R.id.mcount)).setText(Integer.toString(mtotal));
                ((TextView)row.findViewById(R.id.females)).setText(Integer.toString(females));
                ((TextView)row.findViewById(R.id.fcount)).setText(Integer.toString(ftotal));
                ((TextView)row.findViewById(R.id.total)).setText(Integer.toString(mtotal + ftotal));
                mTable.addView(row);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                sharedPreferences.edit().putInt(Constants.SHARED_PREF_TOTAL_PARTICIPANTS, males + females)
                        .putInt(Constants.SHARED_PREF_TOTAL_COUNT, mtotal + ftotal).apply();

            }
        }
    }
}

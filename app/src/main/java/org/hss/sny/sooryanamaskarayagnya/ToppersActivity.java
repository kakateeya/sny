package org.hss.sny.sooryanamaskarayagnya;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.hss.sny.gae.memberDetailsApi.model.MemberDetails;
import org.hss.sny.gae.topperApi.model.CollectionResponseTopper;
import org.hss.sny.gae.topperApi.model.Topper;

import java.io.IOException;
import java.util.List;


public class ToppersActivity extends Activity {
    private static final String TAG = "ToppersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toppers);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        ListView mListView;
        List<Topper> mTopperList;
        BaseAdapter mAdapter;

        class ViewHolder {
            TextView rank;
            TextView name;
            TextView address;
            TextView count;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_toppers, container, false);
            mListView = (ListView)rootView.findViewById(R.id.list);
            mAdapter = new BaseAdapter() {
                @Override
                public int getCount() {
                    if (mTopperList == null) return 0;
                    return mTopperList.size();
                }

                @Override
                public Object getItem(int position) {
                    if (mTopperList == null) return null;
                    return mTopperList.get(position);
                }

                @Override
                public long getItemId(int position) {
                    return 0;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    ViewHolder vh;
                    if (convertView == null) {
                        convertView = getActivity().getLayoutInflater().inflate(R.layout.topper_entry, null);
                        vh = new ViewHolder();
                        vh.rank = (TextView) convertView.findViewById(R.id.rank);
                        vh.name = (TextView) convertView.findViewById(R.id.name);
                        vh.address = (TextView) convertView.findViewById(R.id.address);
                        vh.count = (TextView) convertView.findViewById(R.id.count);
                        convertView.setTag(vh);
                    } else {
                        vh = (ViewHolder) convertView.getTag();
                    }
                    Topper topper = mTopperList.get(position);
                    org.hss.sny.gae.topperApi.model.MemberDetails memberDetails = topper.getMemberDetails();
                    vh.rank.setText(Integer.toString(position + 1));
                    String agestr;
                    int age = memberDetails.getAge();
                    if (Constants.ADMIN) {
                        agestr = Integer.toString(age);
                        vh.name.setText(memberDetails.getFirstName() + " " + memberDetails.getLastName() + " (" + agestr + ")");
                        vh.address.setText(memberDetails.getCity() + ", " + memberDetails.getState() + ", " + memberDetails.getCountry() + ", " + memberDetails.getZipcode());
                    } else {
                        age = age - age % 10;
                        agestr = Integer.toString(age) + "+";
                        vh.name.setText(memberDetails.getFirstName() + " " + memberDetails.getLastName().substring(0, 1) + " (" + agestr + ")");
                        vh.address.setText(memberDetails.getState() + ", " + memberDetails.getCountry());
                    }
                    vh.count.setText(Integer.toString(topper.getTotal()));
                    int color;
                    if (memberDetails.getMale()) {
                        color = getResources().getColor(android.R.color.holo_blue_light);
                    } else {
                        color = getResources().getColor(android.R.color.holo_purple);
                    }
                    vh.name.setTextColor(color);
                    return convertView;
                }
            };
            mListView.setAdapter(mAdapter);
            GetToppersTask gtt = new GetToppersTask();
            gtt.execute();
            return rootView;
        }

        class GetToppersTask extends AsyncTask<Void, Void, List<Topper>> {
            ProgressDialog mProgressDialog;
            String mErrorMsg;

            public GetToppersTask() {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.setTitle("Fetching the toppers list");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
            }
            @Override
            protected void onPreExecute ( ) {
                mProgressDialog.show ( ) ;
            }

            @Override
            protected List<Topper> doInBackground(Void... params) {
                try {
                    CollectionResponseTopper responseTopper = ServerApi.getTopperApi().list().execute();
                    List<Topper> toppers = responseTopper.getItems();
                    if (toppers != null) {
                        int rank = 1;
                        for (Topper topper : toppers) {
                            org.hss.sny.gae.topperApi.model.MemberDetails memberDetails = topper.getMemberDetails();
                            Log.d(TAG, rank + " ;" + memberDetails.getFirstName() + " " + memberDetails.getLastName() + " ;" + topper.getTotal() + " ;" + memberDetails.getCity() + "," + memberDetails.getState() + "," + memberDetails.getCountry() + "," + memberDetails.getZipcode() + "; " + (memberDetails.getMale() ? "M" : "F") + "; " + memberDetails.getAge());
                            rank++;
                        }
                    } else {
                        mErrorMsg = "No entries";
                    }
                    return toppers;
                } catch (IOException e) {
                    e.printStackTrace();
                    mErrorMsg = e.getLocalizedMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<Topper> toppers) {
                mProgressDialog.cancel();
                if (toppers == null) {
                    Toast.makeText(getActivity(), mErrorMsg, Toast.LENGTH_SHORT).show();
                    return;
                }
                mTopperList = toppers;
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}

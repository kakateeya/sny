package org.hss.sny.sooryanamaskarayagnya;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.hss.sny.sooryanamaskarayagnya.Cache.CountLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MemberLogsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MemberLogsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberLogsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Member mMember;
    private BaseAdapter mListAdapter;
    private SimpleDateFormat mDateFormat;
    private CountLog mLogs;
    private TextView mTotalView;
    private ListView mLogList;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MemberLogsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MemberLogsFragment newInstance(String param1, String param2) {
        MemberLogsFragment fragment = new MemberLogsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MemberLogsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mMember = Cache.getInstance(getActivity()).getMember(mParam1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
        getActivity().setTitle(mMember.firstName);
        mDateFormat = new SimpleDateFormat("EEE, dd MMM, hh:mm aa", Locale.US);
    }

    public static final class ViewHolder {
        TextView mTimeView;
        TextView mCountView;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_member_logs, container, false);
        mTotalView = (TextView)view.findViewById(R.id.total);
        mLogList = (ListView)view.findViewById(R.id.listView);
        mListAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                if (mLogs != null && mLogs.logs != null) {
                    return mLogs.logs.size();
                }
                return 0;
            }

            @Override
            public Object getItem(int position) {
                Long k = mLogs.logs.firstKey();
                Map.Entry<Long, Integer> entry = mLogs.logs.firstEntry();
                for (int i = 0; i < position; i++) {
                    entry = mLogs.logs.higherEntry(k);
                    k = mLogs.logs.higherKey(k);
                }
                return entry;
            }

            @Override
            public long getItemId(int position) {
                Long k = mLogs.logs.firstKey();
                for (int i = 0; i < position; i++) {
                    k = mLogs.logs.higherKey(k);
                }
                return k;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.log_list_entry, parent, false);
                    holder = new ViewHolder();
                    holder.mTimeView = (TextView) convertView.findViewById(R.id.time);
                    holder.mCountView = (TextView) convertView.findViewById(R.id.count);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                Map.Entry<Long, Integer> entry = (Map.Entry<Long, Integer>)getItem(position);
                holder.mTimeView.setText(mDateFormat.format(new Date(entry.getKey())));
                holder.mCountView.setText(Integer.toString(entry.getValue()));

                return convertView;
            }
        };
        mLogList.setAdapter(mListAdapter);
        mLogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogCountDialog dialog = new LogCountDialog();
                Bundle b = new Bundle();
                b.putLong(LogCountDialog.EXTRA_TIME, id);
                b.putInt(LogCountDialog.EXTRA_COUNT, mLogs.logs.get(id));
                dialog.setArguments(b);
                dialog.setLogEnteredCallback(mLogListener);
                dialog.show(getActivity().getFragmentManager(), "LogCountDialog");
            }
        });

        LogLoader ll = new LogLoader();
        ll.execute();
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_member_logs, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            LogCountDialog dialog = new LogCountDialog();
            dialog.setLogEnteredCallback(mLogListener);
            dialog.show(getActivity().getFragmentManager(), "LogCountDialog");
            return true;
        } else if (id == R.id.action_info) {
            startActivity(new Intent(getActivity(), EditMemberActivity.class).putExtra(MemberLogsActivity.EXTRA_MEMBER_UID, mParam1));
            return true;
        } else if (id == R.id.action_reset) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset_logs_prompt)
                    .setMessage(R.string.reset_logs_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ResetLogsTask rlt = new ResetLogsTask();
                            rlt.execute();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private LogCountDialog.LogEnteredCallback mLogListener = new LogCountDialog.LogEnteredCallback() {
        @Override
        public void onLogEntered(long time, int count) {
            boolean logsChanged = true;
            int oldCount = 0;
            if (mLogs != null && mLogs.logs != null && mLogs.logs.containsKey(time)) {
                oldCount = mLogs.logs.get(time);
            } else if (count <= 0) {
                logsChanged = false;
            }
            if (logsChanged) {
                if (count > 0) {
                    if (mLogs == null) {
                        mLogs = new CountLog(mMember);
                    }
                    mLogs.logs.put(time, count);
                } else if (oldCount > 0) {
                    mLogs.logs.remove(time);
                }
                mLogs.total += count - oldCount;
                LogUpdater lu = new LogUpdater();
                lu.execute();
            }
        }
    };

    public class LogLoader extends AsyncTask<Void, Void, CountLog> {

        @Override
        protected CountLog doInBackground(Void... params) {
            return Cache.getInstance(getActivity().getApplicationContext()).getLog(mMember.id);
        }

        @Override
        protected void onPostExecute(CountLog logs) {
            mLogs = logs;
            updateListView();
        }
    }

    private void updateListView() {
        if (mLogs != null) {
            mTotalView.setText(Integer.toString(mLogs.total));
        } else {
            mTotalView.setText(Integer.toString(0));
        }
        mListAdapter.notifyDataSetChanged();
    }

    public class LogUpdater extends AsyncTask<Void, Void, CountLog> {
        @Override
        protected CountLog doInBackground(Void... params) {
            return Cache.getInstance(getActivity().getApplicationContext()).putLog(mLogs);
        }

        @Override
        protected void onPostExecute(CountLog log) {
            mLogs = log;
            updateListView();
        }
    }
    public class ResetLogsTask extends AsyncTask<Void, Void, CountLog> {

        @Override
        protected CountLog doInBackground(Void... params) {
            return Cache.getInstance(getActivity().getApplicationContext()).resetLogs(mMember.id);
        }

        @Override
        protected void onPostExecute(CountLog logs) {
            mLogs = logs;
            updateListView();
        }
    }
}

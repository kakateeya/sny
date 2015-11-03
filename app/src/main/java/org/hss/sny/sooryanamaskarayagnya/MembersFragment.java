package org.hss.sny.sooryanamaskarayagnya;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class MembersFragment extends ListFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private BaseAdapter mAdapter;

    List<Member> mMembers;
    private OnFragmentInteractionListener mListener;
    private Cache mCache;

    // TODO: Rename and change types of parameters
    public static MembersFragment newInstance(String param1, String param2) {
        MembersFragment fragment = new MembersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MembersFragment() {
    }
    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPrefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(Constants.SHARED_PREF_MEMBERS)) {
                LoadMembersTask lmt = new LoadMembersTask(getActivity());
                lmt.execute();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mCache = Cache.getInstance(getActivity());

        mAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                if (mMembers == null) return 0;
                return mMembers.size();
            }

            @Override
            public Object getItem(int position) {
                return mMembers.get(position);
            }

            @Override
            public long getItemId(int position) {
                return mMembers.get(position).id;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
                }
                Member member = mMembers.get(position);
                String text = member.firstName;
                int total = mCache.getTotal(member.id);
                if (total > 0) {
                    text += " (" + Integer.toString(total) + ")";
                }
                TextView tv = (TextView)convertView.findViewById(android.R.id.text1);
                tv.setText(text);
                int color;
                if (member.isMale) {
                    color = getResources().getColor(android.R.color.holo_blue_light);
                } else {
                    color = getResources().getColor(android.R.color.holo_purple);
                }
                tv.setTextColor(color);
                return convertView;
            }
        };
        setListAdapter(mAdapter);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(mSharedPrefListener);

        LoadMembersTask lmt = new LoadMembersTask(getActivity());
        lmt.execute();
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
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(mSharedPrefListener);
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            //mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
            mListener.onFragmentInteraction(mMembers.get(position));
        }
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
        public void onFragmentInteraction(Member m);
    }

    class LoadMembersTask extends AsyncTask<Void, Void, List<Member>> {
        Context mContext;
        public LoadMembersTask(Context context) {
            mContext = context;
        }
        @Override
        protected List<Member> doInBackground(Void... params) {
            Cache cache = Cache.getInstance(mContext);
            List<Member> members = cache.loadMembers();
            return members;
        }

        @Override
        protected void onPostExecute(List<Member> members) {
            mMembers = members;
            mAdapter.notifyDataSetChanged();
        }
    }
}
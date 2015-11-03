package org.hss.sny.sooryanamaskarayagnya;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.hss.sny.sooryanamaskarayagnya.dummy.DummyContent;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 */
public class StatisticsFragment extends ListFragment {
    private static final String TAG = "StatsFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // TODO: Rename and change types of parameters
    public static StatisticsFragment newInstance(String param1, String param2) {
        StatisticsFragment fragment = new StatisticsFragment();
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
    public StatisticsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Context context = getActivity();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int participants = sharedPreferences.getInt(Constants.SHARED_PREF_TOTAL_PARTICIPANTS, 0);
        if (participants > 0) {
            int count = sharedPreferences.getInt(Constants.SHARED_PREF_TOTAL_COUNT, 0);
            String curText = "Total (" + count + " by " + participants + ")";
            DummyContent.STATS.get(0).content = curText;
        }
        // TODO: Change Adapter to display your content
        setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.STATS));
    }

    @Override
    public void onResume() {
        super.onResume();
        Context context = getActivity();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int participants = sharedPreferences.getInt(Constants.SHARED_PREF_TOTAL_PARTICIPANTS, 0);
        if (participants > 0) {
            int count = sharedPreferences.getInt(Constants.SHARED_PREF_TOTAL_COUNT, 0);
            TextView textview = (TextView)getListView().getChildAt(0);
            if (textview != null) {
                String curText = "Total (" + count + " by " + participants + ")";
                textview.setText(curText);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        switch (position) {
            case 0:
                startActivity(new Intent(getActivity(), TotalsActivity.class));
                break;
            case 1:
                startActivity(new Intent(getActivity(), ToppersActivity.class));
                break;
        }
    }

}

package org.hss.sny.sooryanamaskarayagnya;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class LogCountDialog extends DialogFragment {
    Button mDate;
    Button mTime;
    EditText mCount;
    int mYear;
    int mMonth;
    int mDay;
    int mHour;
    int mMinute;
    long mInputTime;
    private LogEnteredCallback mLogEnteredCb;
    long mMinTime;

    public interface LogEnteredCallback {
        public void onLogEntered(long time, int count);
    }
    public static final String EXTRA_TIME = "EXTRA_TIME";
    public static final String EXTRA_COUNT = "EXTRA_COUNT";

    public static final long MAX_DELTA = DateUtils.DAY_IN_MILLIS * 30;

    DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            DateFormat df = DateFormat.getDateInstance();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            Date time = calendar.getTime();

            long timems = time.getTime();
            long curtime = System.currentTimeMillis();
            if (curtime < timems || mMinTime > timems) {
                Toast.makeText(getActivity(), "Time not valid. Try again.", Toast.LENGTH_SHORT).show();
                return;
            }
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            mDate.setText(df.format(time));
        }
    };

    public void setLogEnteredCallback(LogEnteredCallback cb) {
        mLogEnteredCb = cb;
    }

    TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            DateFormat df = DateFormat.getDateInstance();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, mYear);
            calendar.set(Calendar.MONTH, mMonth);
            calendar.set(Calendar.DAY_OF_MONTH, mDay);
            mHour = hourOfDay;
            mMinute = minute;
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            df = DateFormat.getTimeInstance();
            mTime.setText(df.format(calendar.getTime()));
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        Bundle args = getArguments();
        int count = 0;
        if (args != null) {
            if (args.containsKey(EXTRA_TIME)) {
                mInputTime = args.getLong(EXTRA_TIME);
            }
            if (args.containsKey(EXTRA_COUNT)) {
                count = args.getInt(EXTRA_COUNT);
            }
        }
        View v = inflater.inflate(R.layout.fragment_log_count_dialog, null);
        mDate = (Button)v.findViewById(R.id.date);
        final Calendar calendar = Calendar.getInstance();
        if (mInputTime > 0) {
            calendar.setTimeInMillis(mInputTime);
        }
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
        Calendar calx = Calendar.getInstance();
        calx.set(Calendar.YEAR, 2015);
        calx.set(Calendar.MONTH, Calendar.JANUARY);
        calx.set(Calendar.DAY_OF_MONTH, 14);
        mMinTime = calx.getTimeInMillis();
        if (mInputTime <= 0) {
            mDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatePickerDialog dialog = new DatePickerDialog(getActivity(), mDateSetListener, mYear,
                            mMonth, mDay);
                    dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                    dialog.getDatePicker().setMinDate(mMinTime);
                    dialog.show();
                }
            });
        }
        DateFormat df = DateFormat.getDateInstance();
        mDate.setText(df.format(new Date()));
        mTime = (Button)v.findViewById(R.id.time);
        if (mInputTime <= 0) {
            mTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new TimePickerDialog(getActivity(), mTimeSetListener, mHour, mMinute, true).show();
                }
            });
        }
        df = DateFormat.getTimeInstance();
        mTime.setText(df.format(new Date()));
        mCount = (EditText)v.findViewById(R.id.count);
        if (mInputTime > 0) {
            mCount.setText(Integer.toString(count));
        }
        builder.setView(v)
                .setMessage(R.string.enter_log)
                .setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            int count = Integer.parseInt(mCount.getText().toString());
                            Toast.makeText(getActivity(), "Logging " + count, Toast.LENGTH_SHORT).show();
                            if (mLogEnteredCb != null) {
                                if (mInputTime <= 0) {
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.set(Calendar.YEAR, mYear);
                                    calendar.set(Calendar.MONTH, mMonth);
                                    calendar.set(Calendar.DAY_OF_MONTH, mDay);
                                    calendar.set(Calendar.HOUR_OF_DAY, mHour);
                                    calendar.set(Calendar.MINUTE, mMinute);
                                    calendar.set(Calendar.SECOND, 0);
                                    mInputTime = calendar.getTimeInMillis();
                                }
                                mLogEnteredCb.onLogEntered(mInputTime, count);
                            }
                            dismiss();
                        } catch (Exception e) {

                        }
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}

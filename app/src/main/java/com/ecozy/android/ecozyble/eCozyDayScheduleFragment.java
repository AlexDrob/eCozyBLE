package com.ecozy.android.ecozyble;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AREG on 22.02.2017.
 */

public class eCozyDayScheduleFragment extends Fragment {

    private final int SCHEDULE_FRAGMENT = 3;
    private static final int TOTAL_POINTS = 12;
    // week days
    private final int SUNDAY    = 0;
    private final int SATURDAY  = 1;
    private final int FRIDAY    = 2;
    private final int THURSDAY  = 3;
    private final int WEDNESDAY = 4;
    private final int TUESDAY   = 5;
    private final int MONDAY    = 6;

    private eCozyBluetoothLE mBluetoothLE;

    private int mScheduleDay;
    private float mDrawScale;
    private LineDataSet mDataSet;
    private boolean[] submenu_days;
    private LineChart mWholeSchedule;
    private ArrayList<Entry> mEntries;
    private eCozySchedule meCozySchedule;
    private List<ILineDataSet> mDataSets;

    private AlertDialog mAlert;

    private Spinner mPointHours;
    private Spinner mPointMinutes;
    private Spinner mPointTemperature;

    private CheckBox mMondayCheckBox;
    private CheckBox mTuesdayCheckBox;
    private CheckBox mWednesdayCheckBox;
    private CheckBox mThursdayCheckBox;
    private CheckBox mFridayCheckBox;
    private CheckBox mSaturdayCheckBox;
    private CheckBox mSundayCheckBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true); // show my menu at action bar
        submenu_days = new boolean[] {false, false, false, false, false, false, false};
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.ecozy_main_schedule_fragment, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //для альбомного режима

        meCozySchedule = eCozySchedule.get(getContext()); // create or receive schedule

        mScheduleDay = meCozySchedule.getTargetScheduleDay();
        submenu_days[mScheduleDay] = true;

        mDrawScale = getContext().getResources().getDisplayMetrics().density;

        mWholeSchedule = (LineChart) v.findViewById(R.id.whole_schedule);
        mWholeSchedule.getLegend().setEnabled(false); // disable legend
        Description description = new Description();
        description.setText("");
        mWholeSchedule.setDescription(description); // disable description text
        XAxis xAxis = mWholeSchedule.getXAxis();
        xAxis.setDrawGridLines(true);
        xAxis.setAxisMaximum(96.0f);
        xAxis.setAxisMinimum(0.0f);
        xAxis.setLabelCount(96); // 24*4
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGridColor(getContext().getResources().getColor(R.color.axis_color));
        xAxis.setAxisLineColor(getContext().getResources().getColor(R.color.axis_color));
        YAxis leftAxis = mWholeSchedule.getAxisLeft();
        leftAxis.setAxisMinimum(10.0f);
        leftAxis.setAxisMaximum(38.0f);
        leftAxis.setLabelCount(28);
        leftAxis.setGridColor(getContext().getResources().getColor(R.color.axis_color));
        leftAxis.setAxisLineColor(getContext().getResources().getColor(R.color.axis_color));
        YAxis rightAxis = mWholeSchedule.getAxisRight();
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisLineColor(getContext().getResources().getColor(R.color.axis_color));
        mWholeSchedule.setDoubleTapToZoomEnabled(false); // disable zooming on double tap
        mWholeSchedule.setVisibleXRangeMaximum(30.0f);

        leftAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                if ((v % 2) == 0) {
                    return String.valueOf((int)v);
                }
                return "";
            }
        });

        final String[] xTime = new String[] {"00:00", "01:00", "02:00", "03:00", "04:00", "05:00",
                "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00",
                "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00",
                "22:00", "23:00", "24:00"};
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                if ((v % 4) == 0) {
                    return xTime[(int)(v / 4)];
                }
                return "";
            }
        });

        mWholeSchedule.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent motionEvent, ChartTouchListener.ChartGesture chartGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent motionEvent, ChartTouchListener.ChartGesture chartGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent motionEvent) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent motionEvent) {
                MPPointD point = mWholeSchedule.getTransformer(YAxis.AxisDependency.LEFT).getValuesByTouchPoint(motionEvent.getX(),motionEvent.getY());
                Log.d("1234","DoubleTap on schedule");
                int temperature = Math.round((float)point.y);
                float time = Math.round((float)point.x)/4.0f;
                if ((temperature < 10) || (temperature > 38) || (time < 0) || (time > 96)) {
                    return; // protection write wrong schedule point when tap on axis
                }
                if (mEntries.size() < TOTAL_POINTS) {
                    Log.d("1234", "Temperature: " + temperature);
                    Log.d("1234", "Time: " + time);
                    meCozySchedule.AddPoint(time, temperature, mScheduleDay);
                    RefreshSchedule();
                } else {
                    ShowDialogWindow(); // show dialog that only 10 points per day maximum
                }
            }

            @Override
            public void onChartSingleTapped(MotionEvent motionEvent) {
                MPPointD point = mWholeSchedule.getTransformer(YAxis.AxisDependency.LEFT).getValuesByTouchPoint(motionEvent.getX(),motionEvent.getY());
                Log.d("1234","SingleTap on schedule");
                int temperature = Math.round((float)point.y);
                float time = Math.round((float)point.x)/4.0f;
                // if point is present, remove it
                if (meCozySchedule.IsPointAvailable(time, temperature, mScheduleDay) == true) {
                    meCozySchedule.RemovePoint(time, temperature, mScheduleDay);
                    Log.d("1234","Temperature: " + temperature);
                    Log.d("1234","Time: " + time);
                    RefreshSchedule();
                }
            }

            @Override
            public void onChartFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {

            }

            @Override
            public void onChartScale(MotionEvent motionEvent, float v, float v1) {

            }

            @Override
            public void onChartTranslate(MotionEvent motionEvent, float v, float v1) {

            }
        });

        RefreshSchedule();

        ((eCozyActivity) getActivity()).DisableDrawerIndicator();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ecozy_day_schedule_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.schedule_plus:
                ShowScheduleAlarmDialog();
                return true;
            case R.id.schedule_edit:
                ShowNewPointScheduleDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBluetoothLE.currentConnectionState() == true) {
            for (int i = 0; i < 7; i++) {
                mBluetoothLE.WriteSchedule(i, meCozySchedule.scheduleToByteArray(i));
            }
        }
        if( mAlert != null && mAlert.isShowing() )
        {
            mAlert.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mBluetoothLE = eCozyBluetoothLE.get(getContext(), getActivity());
    }

    private void RefreshSchedule() {
        mDataSets = new ArrayList<ILineDataSet>();

        mEntries = new ArrayList<Entry>();
        mEntries = meCozySchedule.getDayScheduleWithTemperature(mScheduleDay);
        Log.d("123", "Entries: " + mEntries);
        //Collections.sort(mEntries, new EntryXComparator());
        mDataSet = new LineDataSet(mEntries, "Label");
        mDataSet.setCircleHoleRadius(7.0f * mDrawScale + 0.5f); // 7dp
        mDataSet.setCircleRadius(5.0f * mDrawScale + 0.5f); // 5dp
        // now set colors of lines and circles
        int[] colors = meCozySchedule.getColorsForDay(mScheduleDay, mEntries.size());
        mDataSet.setColors(colors);
        mDataSet.setCircleColors(colors);
        mDataSet.setCircleColorHole(getResources().getColor(R.color.colorBackgroundBottom));
        mDataSets.add(mDataSet);

        LineData lineData = new LineData(mDataSets);
        lineData.setValueFormatter(new eCozyDayScheduleFragment.MyValueFormatter());
        lineData.setHighlightEnabled(false);
        mWholeSchedule.setData(lineData);
        mWholeSchedule.invalidate(); // refresh data
    }

    private void ShowScheduleAlarmDialog() {
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.SelectScheduleDayDialogTitle);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.days_schedule_select, null);

        builder.setView(dialogView);

        builder.setPositiveButton(R.string.dialogOkButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // save changes
                if (mMondayCheckBox.isChecked() == true) {
                    meCozySchedule.copySchedule(mScheduleDay, MONDAY);
                }
                if (mTuesdayCheckBox.isChecked() == true) {
                    meCozySchedule.copySchedule(mScheduleDay, TUESDAY);
                }
                if (mWednesdayCheckBox.isChecked() == true) {
                    meCozySchedule.copySchedule(mScheduleDay, WEDNESDAY);
                }
                if (mThursdayCheckBox.isChecked() == true) {
                    meCozySchedule.copySchedule(mScheduleDay, THURSDAY);
                }
                if (mFridayCheckBox.isChecked() == true) {
                    meCozySchedule.copySchedule(mScheduleDay, FRIDAY);
                }
                if (mSaturdayCheckBox.isChecked() == true) {
                    meCozySchedule.copySchedule(mScheduleDay, SATURDAY);
                }
                if (mSundayCheckBox.isChecked() == true) {
                    meCozySchedule.copySchedule(mScheduleDay, SUNDAY);
                }
                // go to main schedule fragment
                ((eCozyActivity) getActivity()).ShowCurrentFragment(SCHEDULE_FRAGMENT);
            }
        });

        builder.setNegativeButton(R.string.dialogCancelButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        mAlert = builder.create();
        mAlert.setCancelable(false);

        mMondayCheckBox = (CheckBox) dialogView.findViewById(R.id.monday);
        if (submenu_days[MONDAY] == true) {
            mMondayCheckBox.setChecked(true);
        }
        mTuesdayCheckBox = (CheckBox) dialogView.findViewById(R.id.tuesday);
        if (submenu_days[TUESDAY] == true) {
            mTuesdayCheckBox.setChecked(true);
        }
        mWednesdayCheckBox = (CheckBox) dialogView.findViewById(R.id.wednesday);
        if (submenu_days[WEDNESDAY] == true) {
            mWednesdayCheckBox.setChecked(true);
        }
        mThursdayCheckBox = (CheckBox) dialogView.findViewById(R.id.thursday);
        if (submenu_days[THURSDAY] == true) {
            mThursdayCheckBox.setChecked(true);
        }
        mFridayCheckBox = (CheckBox) dialogView.findViewById(R.id.friday);
        if (submenu_days[FRIDAY] == true) {
            mFridayCheckBox.setChecked(true);
        }
        mSaturdayCheckBox = (CheckBox) dialogView.findViewById(R.id.saturday);
        if (submenu_days[SATURDAY] == true) {
            mSaturdayCheckBox.setChecked(true);
        }
        mSundayCheckBox = (CheckBox) dialogView.findViewById(R.id.sunday);
        if (submenu_days[SUNDAY] == true) {
            mSundayCheckBox.setChecked(true);
        }
        mAlert.show();
    }

    private void ShowNewPointScheduleDialog() {
        if (mEntries.size() < TOTAL_POINTS) {
            AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.SetPointTitle);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.set_schedule_point, null);

            builder.setView(dialogView);

            builder.setPositiveButton(R.string.dialogOkButton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    float time = Integer.valueOf(mPointHours.getSelectedItem().toString()) +
                            0.25f * mPointMinutes.getSelectedItemPosition();
                    int temperature = Integer.valueOf(mPointTemperature.getSelectedItem().toString());
                    if (meCozySchedule.IsPointAvailable(time, temperature, mScheduleDay) == false) {
                        meCozySchedule.AddPoint(time, temperature, mScheduleDay);
                        RefreshSchedule();
                    }
                }
            });

            builder.setNegativeButton(R.string.dialogCancelButton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            mAlert = builder.create();
            mAlert.setCancelable(false);

            String[] hoursArray = new String[24];
            String[] temperatureArray = new String[29];
            String[] minutesArray = new String[] {"0","15","30","45"};

            ArrayAdapter<String> minutesArrayAdapter = new ArrayAdapter<String>(
                    getContext(), android.R.layout.simple_spinner_item, minutesArray);
            minutesArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );

            mPointMinutes = (Spinner) dialogView.findViewById(R.id.point_minutes);
            mPointMinutes.setAdapter(minutesArrayAdapter);

            for (int i = 0; i < 24; i++) {
                hoursArray[i] = String.valueOf(i);
            }
            ArrayAdapter<String> hoursArrayAdapter = new ArrayAdapter<String>(
                    getContext(), android.R.layout.simple_spinner_item, hoursArray);
            hoursArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );

            mPointHours = (Spinner) dialogView.findViewById(R.id.point_hour);
            mPointHours.setAdapter(hoursArrayAdapter);

            for (int i = 10; i < 39; i++) {
                temperatureArray[i-10] = String.valueOf(i);
            }
            ArrayAdapter<String> temperatureArrayAdapter = new ArrayAdapter<String>(
                    getContext(), android.R.layout.simple_spinner_item, temperatureArray);
            temperatureArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );

            mPointTemperature = (Spinner) dialogView.findViewById(R.id.point_temperature);
            mPointTemperature.setAdapter(temperatureArrayAdapter);

            mAlert.show();
        } else {
            ShowDialogWindow(); // show dialog that only 10 points per day maximum
        }
    }

    // Show dialog window for fail schedule point
    private void ShowDialogWindow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.FailPointDialogTitle)
                .setMessage(R.string.ScheduleFullBody)
                .setCancelable(false)
                .setPositiveButton(R.string.dialogOkButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private class MyValueFormatter implements IValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return String.valueOf((int)entry.getY());
        }
    }
}

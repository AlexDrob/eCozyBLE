package com.ecozy.android.ecozyble;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.github.mikephil.charting.utils.EntryXComparator;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by AREG on 21.02.2017.
 */

public class eCozyMainScheduleFragment extends Fragment
        implements eCozyBluetoothLE.BleMainScheduleListener {

    private final int MAIN_FRAGMENT = 1;
    private final int TOTAL_DAYS = 7;
    // week days
    private final int SUNDAY    = 0;
    private final int SATURDAY  = 1;
    private final int FRIDAY    = 2;
    private final int THURSDAY  = 3;
    private final int WEDNESDAY = 4;
    private final int TUESDAY   = 5;
    private final int MONDAY    = 6;

    private final int DAY_SCHEDULE_FRAGMENT = 4;

    private int mSwitchState;
    private float mDrawScale;
    private LineChart mWholeSchedule;
    private SwitchCompat mSwitch;
    private List<ILineDataSet> mDataSets;
    private eCozySchedule meCozySchedule;

    private eCozyBluetoothLE mBluetoothLE;

    private ViewGroup mRoot;
    private View mView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true); // show my menu at action bar
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.ecozy_main_schedule_fragment, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //для альбомного режима

        meCozySchedule = eCozySchedule.get(getContext()); // create or receive schedule
        meCozySchedule.SetDefault(getContext());

        mDrawScale = getContext().getResources().getDisplayMetrics().density;

        mWholeSchedule = (LineChart) v.findViewById(R.id.whole_schedule);
        mWholeSchedule.getLegend().setEnabled(false); // disable legend
        final Description description = new Description();
        description.setText("");
        mWholeSchedule.setDescription(description); // disable description text
        XAxis xAxis = mWholeSchedule.getXAxis();
        xAxis.setDrawGridLines(true);
        xAxis.setAxisMaximum(24);
        xAxis.setAxisMinimum(0);
        xAxis.setLabelCount(24);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGridColor(getContext().getResources().getColor(R.color.axis_color));
        xAxis.setAxisLineColor(getContext().getResources().getColor(R.color.axis_color));
        YAxis leftAxis = mWholeSchedule.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisLineColor(getContext().getResources().getColor(R.color.axis_color));
        YAxis rightAxis = mWholeSchedule.getAxisRight();
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisLineColor(getContext().getResources().getColor(R.color.axis_color));

        final String[] quarters = new String[] { "Sun", "Sat", "Fri", "Thu", "Wed", "Tue", "Mon" };
        final String[] xTime = new String[] {"00:00", "", "", "03:00", "", "", "06:00", "", "", "09:00",
                "", "", "12:00", "", "", "15:00", "", "", "18:00", "", "", "21:00", "", "", "24:00"};
        leftAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return quarters[(int) v];
            }
        });
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                return xTime[(int) v];
            }
        });

        mWholeSchedule.setDoubleTapToZoomEnabled(false); // disable zooming on double tap

        mWholeSchedule.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent motionEvent, ChartTouchListener.ChartGesture chartGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent motionEvent, ChartTouchListener.ChartGesture chartGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent motionEvent) {
                mRoot = (ViewGroup) getActivity().getWindow().getDecorView().findViewById(android.R.id.content);

                mView = new View(getContext());
                mView.setLayoutParams(new ViewGroup.LayoutParams(1, 1));

                mRoot.addView(mView);

                mView.setX(motionEvent.getX());
                mView.setY(motionEvent.getY());

                PopupMenu popupMenu = new PopupMenu(getContext(), mView, Gravity.CENTER);
                popupMenu.getMenu().add(Menu.NONE, 1, 1, R.string.PopupSave);
                popupMenu.getMenu().add(Menu.NONE, 2, 2, R.string.PopupLoad);
                popupMenu.getMenu().add(Menu.NONE, 3, 3, R.string.PopupApply);

                popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener()
                {
                    @Override
                    public void onDismiss(PopupMenu menu)
                    {
                        mRoot.removeView(mView);
                    }
                });

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case 1: // save schedule to base
                                Log.d("eCozyActivity", "item.getItemId() call " + item.getItemId());
                                return true;
                            case 2: // load schedule from base
                                Log.d("eCozyActivity", "PopupLoad call");
                                return true;
                            case 3: // load schedule to device
                                if (mBluetoothLE.currentConnectionState() == true) {
                                    for (int i = 0; i < 7; i++) {
                                        mBluetoothLE.WriteSchedule(i, meCozySchedule.scheduleToByteArray(i));
                                    }
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popupMenu.show();
            }

            @Override
            public void onChartDoubleTapped(MotionEvent motionEvent) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent motionEvent) {
                MPPointD point = mWholeSchedule.getTransformer(YAxis.AxisDependency.LEFT).
                        getValuesByTouchPoint(motionEvent.getX(),motionEvent.getY());
                int schedule_day = Math.round((float)point.y);
                if ((schedule_day < 0) || (schedule_day > 6)) {
                    return;
                }
                meCozySchedule.setTargetScheduleDay(schedule_day);
                ((eCozyActivity) getActivity()).setTargetScheduleDay(schedule_day);
                ((eCozyActivity) getActivity()).ShowCurrentFragment(DAY_SCHEDULE_FRAGMENT);
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

        RefreshSchedule(); // refresh data on screen

        ((eCozyActivity) getActivity()).DisableDrawerIndicator();

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        mBluetoothLE.SetMainScheduleListener(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        mBluetoothLE = eCozyBluetoothLE.get(getContext(), getActivity());
        mBluetoothLE.SetMainScheduleListener(this);
        if (mBluetoothLE.currentConnectionState() == true) {
            mBluetoothLE.ReadScheduleState();
            mBluetoothLE.ReadScheduleFromDevice();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ecozy_main_schedule_fragment_menu, menu);
        // Get switch widget
        MenuItem item = menu.findItem(R.id.schedule_state);
        mSwitch = (SwitchCompat) MenuItemCompat.getActionView(item);
        // Switch is off
        mSwitch.setChecked(false);
        mSwitch.setText(R.string.ScheduleOff);
        // Change state callback
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    mSwitch.setText(R.string.ScheduleOn);
                    if (mBluetoothLE.currentConnectionState() == true) {
                        mBluetoothLE.WriteScheduleState(1);
                    }
                } else {
                    mSwitch.setText(R.string.ScheduleOff);
                    if (mBluetoothLE.currentConnectionState() == true) {
                        mBluetoothLE.WriteScheduleState(0);
                    }
                }
            }
        });
    }

    public void SetScheduleState(int scheduleState) {
        mSwitchState = scheduleState;
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run()
            {
                if (mSwitchState == 0) {
                    mSwitch.setChecked(false);
                    mSwitch.setText(R.string.ScheduleOff);
                } else if (mSwitchState == 1) {
                    mSwitch.setChecked(true);
                    mSwitch.setText(R.string.ScheduleOn);
                }
            }
        });
    }

    public void SetSchedule(byte[] schedule, int day) {
        Log.d("123", "Received schedule for day " + day);
        meCozySchedule.setScheduleForDay(schedule, day);
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run()
            {
                RefreshSchedule();
            }
        });
    }

    private void RefreshSchedule() {
        mDataSets = new ArrayList<ILineDataSet>();
        for (int i = 0; i < TOTAL_DAYS; i++) {
            ArrayList<Entry> entries = new ArrayList<Entry>();
            entries = meCozySchedule.getDayScheduleWithoutTemperature(i);
            Collections.sort(entries, new EntryXComparator());
            LineDataSet dataSet = new LineDataSet(entries, "Label");
            dataSet.setCircleHoleRadius(7.0f * mDrawScale + 0.5f); // 7dp
            dataSet.setCircleRadius(5.0f * mDrawScale + 0.5f); // 5dp
            dataSet.setLineWidth(10.0f * mDrawScale + 0.5f); // 10dp
            // now set colors of lines and circles
            int[] colors = meCozySchedule.getColorsForDay(i, entries.size());
            dataSet.setColors(colors);
            dataSet.setCircleColors(colors);
            dataSet.setCircleColorHole(getResources().getColor(R.color.colorBackgroundBottom));
            mDataSets.add(dataSet);
        }

        LineData lineData = new LineData(mDataSets);
        lineData.setValueFormatter(new MyValueFormatter());
        lineData.setHighlightEnabled(false);
        mWholeSchedule.setData(lineData);
        mWholeSchedule.invalidate(); // refresh data
    }

    private class MyValueFormatter implements IValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            int temperature = meCozySchedule.getTemperatureByTimeAndDay(entry.getX(), (int)entry.getY());
            return String.valueOf(temperature);
        }
    }
}

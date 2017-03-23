package com.ecozy.android.ecozyble;

import android.content.Context;
import android.util.Log;
import com.github.mikephil.charting.data.Entry;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by AREG on 22.02.2017.
 */

public class eCozySchedule {

    private static final int TOTAL_DAYS = 7;
    private static final int TOTAL_POINTS = 12;
    // week days
    private final int SUNDAY    = 0;
    private final int SATURDAY  = 1;
    private final int FRIDAY    = 2;
    private final int THURSDAY  = 3;
    private final int WEDNESDAY = 4;
    private final int TUESDAY   = 5;
    private final int MONDAY    = 6;
    // default values
    private float[] xArray_w = new float[] {-1, 7, 8.5f, 19, 23, 25};
    private int[] xArray_h   = new int[] {-1, 9, 22, 25};
    private int[] yArray_w = new int[] {21, 21, 16, 22, 17, 17}; // fake is last temperature
    private int[] yArray_h = new int[] {21, 21, 17, 17}; // fake is last temperature
    // Arrays for store schedule
    private static float[][] timeArray = new float[TOTAL_POINTS][TOTAL_DAYS];
    private static int[][] temperatureArray = new int[TOTAL_POINTS][TOTAL_DAYS];

    private int mTargetScheduleDay;

    private static eCozySchedule seCozySchedule;

    private int[] mScheduleColors;

    // class constructor in our case
    public static eCozySchedule get(Context context) {
        if (seCozySchedule == null) {
            seCozySchedule = new eCozySchedule(context);
        }
        return seCozySchedule;
    }

    // get day schedule with fake temperature
    public ArrayList<Entry> getDayScheduleWithoutTemperature(int day) {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < TOTAL_POINTS; i++) {
            entries.add(new Entry(timeArray[i][day], day));
            if (timeArray[i][day] == 25) {
                break;
            }
        }
        return entries;
    }

    // return temperature when we know time and day
    public int getTemperatureByTimeAndDay(float time, int day) {
        for (int i = 0; i < TOTAL_POINTS; i++ ) {
            if (timeArray[i][day] == time) {
                return temperatureArray[i][day];
            }
        }
        return 0;
    }

    // get colors array for day's schedule
    public int[] getColorsForDay(int day, int numb_points) {
        int[] colors = new int[numb_points];
        for (int i = 0; i < numb_points; i++) {
            colors[i] = getColorByTemperature(temperatureArray[i][day]-10);
        }
        colors[0] = colors[colors.length-1];
        return colors;
    }

    // get color by temperature
    private int getColorByTemperature(int temperature) {
        return mScheduleColors[temperature];
    }

    // get day schedule with fake temperature
    public ArrayList<Entry> getDayScheduleWithTemperature(int day) {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < TOTAL_POINTS; i++) {
            entries.add(new Entry(timeArray[i][day]*4, temperatureArray[i][day]));
            if (timeArray[i][day] == 25) {
                break;
            }
        }
        return entries;
    }

    // is schedule point available? True if available, False - otherwise
    public boolean IsPointAvailable(float time, int temperature, int day) {
        int point = 0;
        for (int i = 0; i < TOTAL_POINTS; i++) {
            if (timeArray[i][day] == time) {
                point = i;
                break;
            } else if (timeArray[i][day] == 25) {
                break;
            }
        }
        if (point == 0) {
            return false;
        }
        if (temperatureArray[point][day] == temperature) {
            return true;
        }
        return false;
    }

    // remove schedule point
    public void RemovePoint(float time, int temperature, int day) {
        int point = 0;
        for (int i = 0; i < TOTAL_POINTS; i++) {
            if (timeArray[i][day] == time) {
                point = i;
                break;
            } else if (timeArray[i][day] == 25) {
                break;
            }
        }
        if (point == 0) {
            return; // fail, point is absent
        }
        for (int i = point; i < TOTAL_POINTS - 1; i++) {
            timeArray[i][day] = timeArray[i + 1][day];
            temperatureArray[i][day] = temperatureArray[i + 1][day];
        }
    }

    // add schedule point
    public void AddPoint(float time, int temperature, int day) {
        int point = 0;
        // if time point already present abort
        for (int i = 0; i < TOTAL_POINTS; i++) {
            if (timeArray[i][day] == time) {
                return;
            }
        }
        // get feature time point
        for (int i = 0; i < TOTAL_POINTS; i++) {
            if (timeArray[i][day] < time) {
                Log.d("321", "Current time: "+timeArray[i][day]+" Time: "+time);
                point = i+1;
            }
            if (timeArray[i][day] == 25) {
                temperatureArray[0][day] = temperatureArray[1][day];
                break;
            }
        }
        // check temperature points, if troubles abort
        if ((temperatureArray[point - 1][day] == temperature) ||
                (temperatureArray[point][day] == temperature)) {
            return;
        }
        // shift right all schedule points
        for (int i = TOTAL_POINTS - 1; i > point; i--) {
            timeArray[i][day] = timeArray[i - 1][day];
            temperatureArray[i][day] = temperatureArray[i - 1][day];
        }
        // set current point
        timeArray[point][day] = time;
        temperatureArray[point][day] = temperature;
        // set fake points
        timeArray[TOTAL_POINTS - 1][day] = 25;
        temperatureArray[0][day] = temperatureArray[1][day];
    }

    // copy schedule from one day to other day
    public void copySchedule(int dayFrom, int dayTo) {
        for (int i = 0; i < TOTAL_POINTS; i++) {
            timeArray[i][dayTo] = timeArray[i][dayFrom];
            temperatureArray[i][dayTo] = temperatureArray[i][dayFrom];
        }
    }

    // convert schedule to byte array
    public byte[] scheduleToByteArray(int day) {
        int timeInMinutes;
        byte[] scheduleArray = new byte[30];
        Arrays.fill(scheduleArray, (byte)0xFF);
        for (int i = 0; i < 10; i++) {
            if (timeArray[i+1][day] == 25) {
                break;
            }
            timeInMinutes = (int)(timeArray[i+1][day] * 60);
            scheduleArray[3 * i] = (byte)(timeInMinutes & 0xFF); // low first
            scheduleArray[3 * i + 1] = (byte)((timeInMinutes >> 8) & 0xFF);
            scheduleArray[3 * i + 2] = (byte)temperatureArray[i+1][day];
        }
        return scheduleArray;
    }

    // set schedule for day from byte array
    public void setScheduleForDay(byte[] schedule, int day) {
        int timeInMinutes;
        int temperature;
        for (int i = 0; i < 10; i++) {
            timeInMinutes = ((schedule[3 * i + 1] & 0xFF) << 8) + (schedule[3 * i] & 0xFF);
            temperature = (schedule[3 * i + 2] & 0xFF);
            Log.d("Schedule", "timeInMinutes: "+ timeInMinutes+ " temperature: "+temperature );
            if (timeInMinutes == 0xFFFF) { // schedule finished
                timeArray[i+1][day] = 25.0f;
                temperatureArray[i+1][day] = temperatureArray[i][day];
                break;
            }
            timeArray[i+1][day] = timeInMinutes / 60.0f;
            temperatureArray[i+1][day] = temperature;
        }
        timeArray[11][day] = 25.0f;
        temperatureArray[0][day] = temperatureArray[1][day];
        temperatureArray[11][day] = temperatureArray[10][day];
    }

    // get current schedule day
    public int getTargetScheduleDay() {
        return mTargetScheduleDay;
    }

    // set current schedule day
    public void setTargetScheduleDay(int targetScheduleDay) {
        mTargetScheduleDay = targetScheduleDay;
    }

    public void SetDefault(Context context) {
        for (int i = 0; i < TOTAL_DAYS; i++) {
            switch (i) {
                case SUNDAY:
                case SATURDAY:
                    for (int j = 0; j < xArray_h.length; j++) {
                        timeArray[j][i] = xArray_h[j];
                        temperatureArray[j][i] = yArray_h[j];
                    }
                    break;
                case FRIDAY:
                case THURSDAY:
                case WEDNESDAY:
                case TUESDAY:
                case MONDAY:
                    for (int j = 0; j < xArray_w.length; j++) {
                        timeArray[j][i] = xArray_w[j];
                        temperatureArray[j][i] = yArray_w[j];
                    }
                    break;
            }
        }
        mScheduleColors = context.getResources().getIntArray(R.array.schedule_colors);
    }

    private eCozySchedule(Context context) {
        SetDefault(context);
    }
}

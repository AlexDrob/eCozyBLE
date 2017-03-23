package com.ecozy.android.ecozyble;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by AREG on 17.02.2017.
 */

public class eCozySettingsFragment extends Fragment implements eCozyBluetoothLE.BleSettingsListener {

    private final int HOME_FRAGMENT = 1;

    private eCozyBluetoothLE mBluetoothLE;

    private int mIdentifyProgress;
    private int mLocksProgress;
    private int mDisplayLocksProgress;
    private int mValueBatteryLevel;
    private String mFirmwareVersion;
    private String[] mDisplayedValues = new String[251];
    private float mLocalTemperatureCalibration;

    private AlertDialog mAlert;

    private TextView mBatteryLevel;
    private TextView mIdentifyText;
    private TextView mThermostatVersion;
    private TextView mTemperatureCalibration;
    private TextView mDisplayLocksText;
    private ImageView mBatteryImage;
    private SeekBar mIdentifySeekBar;
    private SeekBar mDisplayLocksSeekBar;
    private LinearLayout mChangeCalibration;
    private NumberPicker mTemperaturePicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true); // show my menu at action bar
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.ecozy_fragment_settings, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //для портретного режима

        mBatteryLevel = (TextView) v.findViewById(R.id.BatteryLevel);
        mBatteryImage = (ImageView) v.findViewById(R.id.battery_image);

        mIdentifyText = (TextView) v.findViewById(R.id.identify_text);
        mIdentifySeekBar = (SeekBar) v.findViewById(R.id.identify_seekBar);

        mDisplayLocksText = (TextView) v.findViewById(R.id.lock_display_text);
        mDisplayLocksSeekBar = (SeekBar) v.findViewById(R.id.lock_display_seekBar);

        mThermostatVersion = (TextView) v.findViewById(R.id.thermostat_version);
        mTemperatureCalibration = (TextView) v.findViewById(R.id.TemperatureCalibration);
        mChangeCalibration = (LinearLayout) v.findViewById(R.id.ChangeCalibration);

        mIdentifySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIdentifyProgress = mIdentifySeekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mIdentifyProgress != mIdentifySeekBar.getProgress()) {
                    mIdentifyProgress = mIdentifySeekBar.getProgress();
                    Log.d("123", "onStopTrackingTouch " + mIdentifyProgress);
                    switch (mIdentifyProgress) {
                        case 0:
                            mIdentifyText.setText(getResources().getString(R.string.IdentifyLevel) + " " +
                                    getResources().getString(R.string.IdentifyOff));
                            break;
                        case 1:
                            mIdentifyText.setText(getResources().getString(R.string.IdentifyLevel) + " " +
                                    getResources().getString(R.string.IdentifyLowLevel));
                            break;
                        case 2:
                            mIdentifyText.setText(getResources().getString(R.string.IdentifyLevel) + " " +
                                    getResources().getString(R.string.IdentifyHighLevel));
                            break;
                    }
                    if (mBluetoothLE.currentConnectionState() == true) {
                        mBluetoothLE.WriteImmediateAlertLevel(mIdentifyProgress);
                    }
                }
            }
        });

        mDisplayLocksSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mDisplayLocksProgress = mDisplayLocksSeekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mDisplayLocksProgress != mDisplayLocksSeekBar.getProgress()) {
                    mDisplayLocksProgress = mDisplayLocksSeekBar.getProgress();
                    if (mBluetoothLE.currentConnectionState() == true) {
                        Log.d("123", "onStopTrackingTouch " + mDisplayLocksProgress);
                        switch (mDisplayLocksProgress) {
                            case 0:
                                mDisplayLocksText.setText(getResources().getString(R.string.LockDisplayLevel) + " " +
                                        getResources().getString(R.string.LockDisplay_0));
                                break;
                            case 1:
                                mDisplayLocksText.setText(getResources().getString(R.string.LockDisplayLevel) + " " +
                                        getResources().getString(R.string.LockDisplay_1));
                                break;
                            case 2:
                                mDisplayLocksText.setText(getResources().getString(R.string.LockDisplayLevel) + " " +
                                        getResources().getString(R.string.LockDisplay_2));
                                break;
                        }
                        if (mBluetoothLE.currentConnectionState() == true) {
                            mBluetoothLE.WriteDisplayLocks(mDisplayLocksProgress);
                        }
                    }
                }
            }
        });

        View.OnClickListener ShowLocalTemperatureCalibrationDialog = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentValue = 0;
                for (int i = -125; i < +126; i++) {
                    mDisplayedValues[i+125] = String.valueOf(i/(float)10.0);
                    if (mDisplayedValues[i+125].equals(mTemperatureCalibration.getText())) {
                        currentValue = i + 125;
                    }
                }

                AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.CalibrationDialogTitle);

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.local_temperature_calibration, null);

                builder.setView(dialogView);

                builder.setPositiveButton(R.string.dialogOkButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int value = mTemperaturePicker.getValue();
                        if (value >= 0) {
                            value -= 125;
                        }
                        mTemperatureCalibration.setText(mDisplayedValues[mTemperaturePicker.getValue()]);
                        if (mBluetoothLE.currentConnectionState() == true) {
                            mBluetoothLE.WriteLocalTemperatureCalibration(value);
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

                mTemperaturePicker = (NumberPicker) dialogView.findViewById(R.id.numberPicker1);
                mTemperaturePicker.setMaxValue(mDisplayedValues.length-1);
                mTemperaturePicker.setMinValue(0);
                mTemperaturePicker.setDisplayedValues(mDisplayedValues);
                mTemperaturePicker.setValue(currentValue);
                mTemperaturePicker.setWrapSelectorWheel(false);

                mAlert.show();
            }
        };

        mChangeCalibration.setOnClickListener(ShowLocalTemperatureCalibrationDialog);
        mBatteryLevel.setText(getResources().getString(R.string.BatteryLevel) + " 100%");
        mIdentifyText.setText(getResources().getString(R.string.IdentifyLevel) + " " +
                getResources().getString(R.string.IdentifyOff));
        mDisplayLocksText.setText(getResources().getString(R.string.LockDisplayLevel) + " " +
                getResources().getString(R.string.LockDisplay_0));

        ((eCozyActivity) getActivity()).EnableDrawerIndicator();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ecozy_settings_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.settings_ready:
                ((eCozyActivity) getActivity()).ShowCurrentFragment(HOME_FRAGMENT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mBluetoothLE = eCozyBluetoothLE.get(getContext(), getActivity());
        mBluetoothLE.SetSettingsListener(this);
        if (mBluetoothLE.currentConnectionState() == true) {
            mBluetoothLE.ReadBatteryLevel();
            mBluetoothLE.ReadFirmwareVersion();
            mBluetoothLE.ReadLocalTemperatureCalibration();
            mBluetoothLE.ReadDisplayLocks();
            SetFindMe(mBluetoothLE.getFindMe());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBluetoothLE.SetSettingsListener(null);
        if( mAlert != null && mAlert.isShowing() )
        {
            mAlert.dismiss();
        }
    }

    public void SetBatteryLevel(int batteryLevel) {
        Log.d("12345", "onCharacteristicChanged received: battery " + batteryLevel);
        mValueBatteryLevel = batteryLevel;
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run()
            {
                mBatteryLevel.setText(getResources().getString(R.string.BatteryLevel) + " "
                        + String.valueOf(mValueBatteryLevel) + "%");
                if (mValueBatteryLevel < 16) {
                    mBatteryImage.setImageResource(R.mipmap.battery_red);
                } else if (mValueBatteryLevel < 31) {
                    mBatteryImage.setImageResource(R.mipmap.battery_yellow);
                } else {
                    mBatteryImage.setImageResource(R.mipmap.battery_green);
                }
            }
        });
    }

    public void SetFirmwareVersion(String firmwareVersion) {
        mFirmwareVersion = firmwareVersion;
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run()
            {
                mThermostatVersion.setText(mFirmwareVersion);
            }
        });
    }

    public void SetLocalTemperatureCalibration(float calibration) {
        mLocalTemperatureCalibration = calibration;
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run()
            {
                mTemperatureCalibration.setText(String.valueOf(mLocalTemperatureCalibration));
            }
        });
    }

    public void SetDisplayLocks(int locks) {
        mLocksProgress = locks;
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run()
            {
                mDisplayLocksSeekBar.setProgress(mLocksProgress);
                switch (mLocksProgress) {
                    case 0:
                        mDisplayLocksText.setText(getResources().getString(R.string.LockDisplayLevel) + " " +
                                getResources().getString(R.string.LockDisplay_0));
                        break;
                    case 1:
                        mDisplayLocksText.setText(getResources().getString(R.string.LockDisplayLevel) + " " +
                                getResources().getString(R.string.LockDisplay_1));
                        break;
                    case 2:
                        mDisplayLocksText.setText(getResources().getString(R.string.LockDisplayLevel) + " " +
                                getResources().getString(R.string.LockDisplay_2));
                        break;
                }
            }
        });
    }

    private void SetFindMe(int level) {
        mIdentifySeekBar.setProgress(level);
        switch (level) {
            case 0:
                mIdentifyText.setText(getResources().getString(R.string.IdentifyLevel) + " " +
                        getResources().getString(R.string.IdentifyOff));
                break;
            case 1:
                mIdentifyText.setText(getResources().getString(R.string.IdentifyLevel) + " " +
                        getResources().getString(R.string.IdentifyLowLevel));
                break;
            case 2:
                mIdentifyText.setText(getResources().getString(R.string.IdentifyLevel) + " " +
                        getResources().getString(R.string.IdentifyHighLevel));
                break;
        }
    }
}

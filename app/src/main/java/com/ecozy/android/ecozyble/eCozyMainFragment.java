package com.ecozy.android.ecozyble;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by AREG on 14.02.2017.
 */

public class eCozyMainFragment extends Fragment implements eCozyBluetoothLE.BleMainListener {

    private final int SETTINGS_FRAGMENT = 2;
    private final int SCHEDULE_FRAGMENT = 3;

    private final int AbsMinTemperature = 10;
    private final int AbsMaxTemperature = 38;

    private eCozyBluetoothLE mBluetoothLE;

    private ImageButton mArrowLeft;
    private ImageButton mArrowRight;
    private TextView mDeviceName;
    private TextView mTargetTemperature;
    private TextView mCurrentTemperature;
    private Button mSettingsButton;
    private Button mScheduleButton;
    private NumberPicker mTemperaturePicker;

    private float temp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true); // show my menu at action bar
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.ecozy_fragment_main, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //для портретного режима

        mArrowLeft  = (ImageButton) v.findViewById(R.id.ArrowLeft);
        mArrowRight = (ImageButton) v.findViewById(R.id.ArrowRight);
        mDeviceName = (TextView) v.findViewById(R.id.deviceName);
        mTargetTemperature  = (TextView) v.findViewById(R.id.target_temperature);
        mCurrentTemperature = (TextView) v.findViewById(R.id.currentTemperature);
        mSettingsButton = (Button) v.findViewById(R.id.settings_button);
        mScheduleButton = (Button) v.findViewById(R.id.schedule_button);

        mDeviceName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.ChangeThermostatName);

                // Set up the input
                final EditText input = new EditText(getContext());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(mDeviceName.getText());
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton(R.string.dialogOkButton,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDeviceName.setText(input.getText());
                    }
                });
                builder.setNegativeButton(R.string.dialogCancelButton,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.show();
                return true;
            }
        });

        mCurrentTemperature.setText(getResources().getString(R.string.current_temperature)
                + " 20.00 " + getResources().getString(R.string.degrees));

        View.OnClickListener MinusButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeTargetTemperature(false);
            }
        };

        View.OnClickListener PlusButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeTargetTemperature(true);
            }
        };

        View.OnLongClickListener UpdateTemperature = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                        R.string.UpdateTemperatureSent,
                        Toast.LENGTH_SHORT);
                TextView t = (TextView) toast.getView().findViewById(android.R.id.message);
                if( t != null) t.setGravity(Gravity.CENTER);
                toast.show();
                if (mBluetoothLE.currentConnectionState() == true) {
                    mBluetoothLE.ReadTemperatures();
                }
                return true;
            }
        };

        View.OnClickListener OpenSettingsButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((eCozyActivity) getActivity()).ShowCurrentFragment(SETTINGS_FRAGMENT);
            }
        };

        View.OnClickListener OpenScheduleButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((eCozyActivity) getActivity()).ShowCurrentFragment(SCHEDULE_FRAGMENT);
            }
        };

        mArrowLeft.setOnClickListener(MinusButton);
        mArrowRight.setOnClickListener(PlusButton);
        mTargetTemperature.setOnLongClickListener(UpdateTemperature);
        mSettingsButton.setOnClickListener(OpenSettingsButton);
        mScheduleButton.setOnClickListener(OpenScheduleButton);

        ((eCozyActivity) getActivity()).EnableDrawerIndicator();

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        mBluetoothLE.SetMainListener(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        mBluetoothLE = eCozyBluetoothLE.get(getContext(), getActivity());
        mBluetoothLE.SetMainListener(this);
        mDeviceName.setText(R.string.ThermostatName);
        if (mBluetoothLE.currentConnectionState() == true) {
            mDeviceName.setText(mBluetoothLE.getMacAddress()); // set mac as device name
            mBluetoothLE.ReadTemperatures();
        }
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ecozy_main_fragment_menu, menu);
        MenuItem GoAway = menu.findItem(R.id.go_away);
        Button GoAwayButton = (Button) GoAway.getActionView();
        GoAwayButton.setText(getResources().getString(R.string.GoAway) + "    ");
        GoAwayButton.setBackgroundResource(R.color.colorBackgroundTop);
        GoAwayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temperature = 10;
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                        R.string.SetGoAwayTemperature,
                        Toast.LENGTH_SHORT);
                TextView t = (TextView) toast.getView().findViewById(android.R.id.message);
                if( t != null) t.setGravity(Gravity.CENTER);
                toast.show();
                mTargetTemperature.setText(String.valueOf(temperature));
                if (mBluetoothLE.currentConnectionState() == true) {
                    mBluetoothLE.WriteTargetTemperature(temperature);
                }
            }
        });
        GoAwayButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ShowSetGoAwayTemperatureDialog();
                return true;
            }
        });
    }

    public void SetTargetTemperature(float temperature) {
        temp = temperature;
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run()
            {
                mTargetTemperature.setText(String.valueOf(Math.round(temp)));
            }
        });
    }

    public void SetCurrentTemperature(float temperature) {
        temp = temperature;
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run()
            {
                mCurrentTemperature.setText(getResources().getString(R.string.current_temperature)
                        + " " + String.valueOf(temp) + " " + getResources().getString(R.string.degrees));
            }
        });
    }

    private void ChangeTargetTemperature(boolean plus) {
        int temperature = Integer.valueOf(mTargetTemperature.getText().toString());
        if (plus == false) {
            if (temperature != AbsMinTemperature) {
                temperature -= 1;
            }
        } else {
            if (temperature != AbsMaxTemperature) {
                temperature += 1;
            }
        }
        mTargetTemperature.setText(String.valueOf(temperature));
        if (mBluetoothLE.currentConnectionState() == true) {
            mBluetoothLE.WriteTargetTemperature(temperature);
        }
    }

    private void ShowSetGoAwayTemperatureDialog() {
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.SetAwayTemperatureDialogTitle);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.local_temperature_calibration, null);

        builder.setView(dialogView);

        builder.setPositiveButton(R.string.dialogOkButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton(R.string.dialogCancelButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alert = builder.create();
        alert.setCancelable(false);

        mTemperaturePicker = (NumberPicker) dialogView.findViewById(R.id.numberPicker1);
        mTemperaturePicker.setMaxValue(AbsMaxTemperature);
        mTemperaturePicker.setMinValue(AbsMinTemperature);
        mTemperaturePicker.setValue(10);
        mTemperaturePicker.setWrapSelectorWheel(false);

        alert.show();
    }
}

package com.ecozy.android.ecozyble;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class eCozyActivity extends ActionBarActivity {

    // navigation drawer title
    private String[] mViewsNames;
    private TextView mDrawerHeaderText;

    private eCozyBluetoothLE mBluetoothLE;
    private PowerManager.WakeLock mWakeLock;

    private int mCurrentFragment;

    private ProgressDialog ProgressDialogBluetoothTurningOn;

    private int mTargetScheduleDay;

    private Drawer mDrawer;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecozy);

        mViewsNames = getResources().getStringArray(R.array.views_array);

        // Handle Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.colorBackgroundTop));

        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withSelectedItem(-1)
                .withActionBarDrawerToggle(true)
                .withHeader(R.layout.drawer_header)
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        ArrayList<String> mStringArrayList;
                        mDrawerHeaderText = (TextView) mDrawer.getHeader().findViewById(R.id.header_text);
                        if (mBluetoothLE.currentConnectionState() == false) {
                            // Show list of found ble devices
                            mDrawerHeaderText.setText(R.string.BleDevices);
                            mStringArrayList = mBluetoothLE.getDeviceList();
                            for (int i = 0; i < mStringArrayList.size(); i++) {
                                mDrawer.addItem(new PrimaryDrawerItem().withName(mStringArrayList.get(i)).withIcon(FontAwesome.Icon.faw_bluetooth));
                            }
                        } else {
                            mDrawerHeaderText.setText(R.string.BleDevice);

                            mStringArrayList = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.views_array)));
                            mDrawer.addItem(new PrimaryDrawerItem().withName(mStringArrayList.get(0)).withIcon(FontAwesome.Icon.faw_home));
                            mDrawer.addItem(new PrimaryDrawerItem().withName(mStringArrayList.get(1)).withIcon(FontAwesome.Icon.faw_cog));
                            mDrawer.addItem(new PrimaryDrawerItem().withName(mStringArrayList.get(2)).withIcon(FontAwesome.Icon.faw_calendar));
                            mDrawer.addItem(new PrimaryDrawerItem().withName(mStringArrayList.get(3)).withIcon(FontAwesome.Icon.faw_times_circle));
                            mDrawer.addItem(new PrimaryDrawerItem().withName(mStringArrayList.get(4)).withIcon(FontAwesome.Icon.faw_sign_out));
                        }

                        InputMethodManager inputMethodManager = (InputMethodManager) eCozyActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(eCozyActivity.this.getCurrentFocus().getWindowToken(), 0);
                    }
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        switch (mCurrentFragment) {
                            case 1:
                                getSupportActionBar().setTitle(mViewsNames[0]);
                                break;
                            case 2:
                                getSupportActionBar().setTitle(mViewsNames[1]);
                                break;
                            case 3:
                                getSupportActionBar().setTitle(mViewsNames[2]);
                                break;
                            case 4: // schedule for day
                                String[] DayName = getResources().getStringArray(R.array.schedule_days_array);
                                getSupportActionBar().setTitle(mViewsNames[2] + " " + DayName[mTargetScheduleDay]);
                                break;
                        }
                        mDrawer.removeAllItems();
                    }
                    @Override
                    public void onDrawerSlide (View drawerView, float slideOffset) {
                    }
                })
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Log.d("DraweronItemClick", "position: " + String.valueOf(position));
                        if (mBluetoothLE.currentConnectionState() == false) {
                            if (position != 0) {
                                // show home screen
                                ShowCurrentFragment(1);
                                // connected to selected device
                                mBluetoothLE.openConnection(eCozyActivity.this, position - 1);
                            }
                        } else {
                            switch (position) {
                                case 1: // home
                                    ShowCurrentFragment(1);
                                    break;
                                case 2: // settings
                                    ShowCurrentFragment(2);
                                    break;
                                case 3: // schedule
                                    ShowCurrentFragment(3);
                                    break;
                                case 4: // disconnect
                                    mBluetoothLE.closeConnection();
                                    mBluetoothLE.scanLeDevice(true);
                                    ShowCurrentFragment(1);
                                    break;
                                case 5: // close
                                    finish();
                                    break;
                                default:
                                    break;
                            }
                        }
                        mDrawer.closeDrawer();
                        return true;
                    }
                })
                .withOnDrawerItemLongClickListener(new Drawer.OnDrawerItemLongClickListener() {
                    @Override
                    // Обработка длинного клика, например, только для SecondaryDrawerItem
                    public boolean onItemLongClick(View view, int position, IDrawerItem drawerItem) {
                        Log.d("NavDrover", "Long click");
                        if (mBluetoothLE.currentConnectionState() == false) {
                            if (position != 0) {
                                PopupMenu popupMenu = new PopupMenu(eCozyActivity.this, view);
                                popupMenu.getMenu().add(R.string.forgetThermostat);
                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        // forget all data from thermostat
                                        Toast toast = Toast.makeText(getApplicationContext(),
                                                R.string.forgetToast, Toast.LENGTH_SHORT);
                                        TextView t = (TextView) toast.getView().findViewById(android.R.id.message);
                                        if( t != null) t.setGravity(Gravity.CENTER);
                                        toast.show();
                                        return true;
                                    }
                                });

                                popupMenu.show();
                            }
                        }
                        return true;
                    }
                })
                .build();

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What to do on back clicked
                Log.d("123","setNavigationOnClickListener() called");
                if ((mCurrentFragment == 1) || (mCurrentFragment == 2)) {
                    mDrawer.openDrawer();
                } else {
                    if (mCurrentFragment == 4) {
                        ShowCurrentFragment(3);
                    } else {
                        ShowCurrentFragment(1);
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        ShowCurrentFragment(1);

        mBluetoothLE = eCozyBluetoothLE.get(eCozyActivity.this, eCozyActivity.this);
        if (mBluetoothLE.BLE_state() == true) {
            mBluetoothLE.scanLeDevice(true);
        } else {
            ShowDialogWindow();
        }

        PowerManager pm = (PowerManager) getSystemService(this.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        mWakeLock.acquire();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mBluetoothLE.currentConnectionState() == false) {
            mBluetoothLE.scanLeDevice(false);
        } else {
            mBluetoothLE.closeConnection();
        }
        mWakeLock.release();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen() == true) {
            mDrawer.closeDrawer();
        } else if (mCurrentFragment == 4) {
            ShowCurrentFragment(3);
        } else if (mCurrentFragment != 1) {
            ShowCurrentFragment(1);
        } else {
            if (mBluetoothLE.currentConnectionState() == true) {
                mBluetoothLE.closeConnection();
            } else {
                mBluetoothLE.scanLeDevice(false);
            }
            super.onBackPressed();
        }
    }

    public void ShowCurrentFragment(int Fragment) {
        mCurrentFragment = Fragment;
        Fragment fragment = new eCozyMainFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (Fragment) {
            case 1: // home
                getSupportActionBar().setTitle(mViewsNames[0]);
                fragment = new eCozyMainFragment();
                break;
            case 2: // settings
                getSupportActionBar().setTitle(mViewsNames[1]);
                fragment = new eCozySettingsFragment();
                break;
            case 3: // schedule
                getSupportActionBar().setTitle(mViewsNames[2]);
                fragment = new eCozyMainScheduleFragment();
                break;
            case 4: // schedule for day
                String[] DayName = getResources().getStringArray(R.array.schedule_days_array);
                getSupportActionBar().setTitle(mViewsNames[2] + " " + DayName[mTargetScheduleDay]);
                fragment = new eCozyDayScheduleFragment();
                break;
        }
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    public void setTargetScheduleDay(int targetScheduleDay) {
        mTargetScheduleDay = targetScheduleDay;
    }

    // Show dialog window for turn on bluetooth
    private void ShowDialogWindow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(eCozyActivity.this);
        builder.setTitle(R.string.BLE_on_header)
                .setMessage(R.string.BLE_on_messageBody)
                .setCancelable(false)
                .setPositiveButton(R.string.dialogOkButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mBluetoothLE.BLE_turn_on();
                                // Show waiting dialog while bluetooth is turning on
                                ProgressDialogBluetoothTurningOn = new ProgressDialog(eCozyActivity.this);
                                ProgressDialogBluetoothTurningOn.setTitle(R.string.Progress_BLE_on_header);
                                ProgressDialogBluetoothTurningOn.setMessage(getResources().getString(R.string.Progress_BLE_on_mBody));
                                ProgressDialogBluetoothTurningOn.setCancelable(false);
                                ProgressDialogBluetoothTurningOn.show();
                                // wait 2 sec while bluetooth is turning on
                                final Handler handler = new Handler();
                                Timer delay_timer = new Timer();
                                delay_timer.schedule(new TimerTask() {
                                    public void run() {
                                        handler.post(new Runnable() {
                                            public void run() {
                                                ProgressDialogBluetoothTurningOn.dismiss();
                                                // start scanning BLE devices
                                                mBluetoothLE.scanLeDevice(true);
                                            }
                                        });
                                    }
                                }, 2000);
                            }
                        })
                .setNegativeButton(R.string.dialogCancelButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                eCozyActivity.this.finish();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void EnableDrawerIndicator() {
        mDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void DisableDrawerIndicator() {
        mDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}

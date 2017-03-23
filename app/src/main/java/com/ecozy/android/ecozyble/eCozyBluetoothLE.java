package com.ecozy.android.ecozyble;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.ecozy.android.ecozyble.eCozyBleRequest.READ_CHARACTERISTIC;

/**
 * Created by AREG on 15.02.2017.
 */

public class eCozyBluetoothLE {

    private static final String TAG = "eCozyBluetoothLE";

    // week days
    private final int SUNDAY    = 0;
    private final int SATURDAY  = 1;
    private final int FRIDAY    = 2;
    private final int THURSDAY  = 3;
    private final int WEDNESDAY = 4;
    private final int TUESDAY   = 5;
    private final int MONDAY    = 6;

    // Services in our thermostat
    private static final UUID GenericAccessServiceUuid = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    private static final UUID DeviceInformationServiceUuid = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    private static final UUID BatteryServiceUuid = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    private static final UUID ImmediateAlertServiceUuid = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    private static final UUID CurrentTimeServiceUuid = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
    private static final UUID ThermostatServiceUuid = UUID.fromString("0000004a-0000-1000-8000-0026bb765291");
    private static final UUID ThermostatSettingsServiceUuid = UUID.fromString("28986E7D-9656-4239-80EC-B6CFD5DFF39A");
    private static final UUID SilabsOtaServiceUuid = UUID.fromString("1d14d6ee-fd63-4fa1-bfa4-8f47b42119f0");

    // Characteristics in our thermostat
    // Generic Access Service
    private static final UUID DeviceNameCharacteristicUuid = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");
    private static final UUID AppearanceCharacteristicUuid = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");
    // Device Information Service
    private static final UUID ManufacturerNameStringCharacteristicUuid = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    private static final UUID FirmwareRevisionStringCharacteristicUuid = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    // Battery Service
    private static final UUID BatteryLevelCharacteristicUuid = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    // Immediate Alert Service
    private static final UUID AlertLevelCharacteristicUuid = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    // Current Time Service
    private static final UUID CurrentTimeCharacteristicUuid = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb");
    // Thermostat Service
    private static final UUID currentHeatingCoolingState = UUID.fromString("0000000F-0000-1000-8000-0026BB765291");
    private static final UUID targetHeatingCoolingState = UUID.fromString("00000033-0000-1000-8000-0026BB765291");
    private static final UUID targetTemperatureCharacteristicUuid = UUID.fromString("00000035-0000-1000-8000-0026bb765291");
    private static final UUID currentTemperatureCharacteristicUuid = UUID.fromString("00000011-0000-1000-8000-0026BB765291");
    private static final UUID heatingThresholdTemperature = UUID.fromString("00000012-0000-1000-8000-0026BB765291");
    // Thermostat Settings Service
    private static final UUID LocalTemperatureCalibration = UUID.fromString("8d80d014-bc22-41e7-a672-8eb95a3690e0");
    private static final UUID DisplayLocks = UUID.fromString("e34d0807-e470-44db-8c03-3a279e6ca622");
    private static final UUID ScheduleEnable = UUID.fromString("3551c6f8-c96f-4749-aff2-7ad48c080626");
    private static final UUID MondaySchedule = UUID.fromString("10f3f87b-2daf-4570-89cb-d9f46d7676e9");
    private static final UUID TuesdaySchedule = UUID.fromString("1ec99150-baf3-4fc7-a9ae-47f0c3400aba");
    private static final UUID WednesdaySchedule = UUID.fromString("561af0ec-0bbc-455f-a7a3-d80f7b29f9f7");
    private static final UUID ThursdaySchedule = UUID.fromString("3241a583-e138-479a-bce8-b7f9314a074c");
    private static final UUID FridaySchedule = UUID.fromString("3ae832db-620a-4ed2-882c-ba491dd80f71");
    private static final UUID SaturdaySchedule = UUID.fromString("86fb2c22-a74b-4341-b1a3-bd1dd46bc4e9");
    private static final UUID SundaySchedule = UUID.fromString("7b0e666c-420f-4170-9a20-859e3a351ebe");
    // Characteristic for setting notifications
    private static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private ArrayList<BluetoothDevice> mBleMACs = new ArrayList<>();

    private static Context context;
    private static Activity activity;

    private static int mFindMe;

    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothDevice mBluetoothDevice;
    private static BluetoothGatt mBluetoothGatt;

    private static BleMainListener sBleMainListener;
    private static BleSettingsListener sBleSettingsListener;
    private static BleMainScheduleListener sBleMainScheduleListener;


    private ProgressDialog mProgressDialogConnecting;

    private Timer mTimer;
    private TimerTask mTimerTask;

    private Queue<eCozyBleRequest> BleRequestQueue = new LinkedList<eCozyBleRequest>();

    private static eCozyBluetoothLE seCozyBluetoothLE;

    // Listener for eCozyMainFragment
    public interface BleMainListener {
        public void SetTargetTemperature(float temperature);
        public void SetCurrentTemperature(float temperature);
    }

    // Listener for eCozySettingsFragment
    public interface BleSettingsListener {
        public void SetBatteryLevel(int batteryLevel);
        public void SetFirmwareVersion(String firmwareVersion);
        public void SetLocalTemperatureCalibration(float calibration);
        public void SetDisplayLocks(int locks);
    }

    // Listener for eCozyMainScheduleFragment
    public interface BleMainScheduleListener {
        public void SetScheduleState(int scheduleState);
        public void SetSchedule(byte[] schedule, int day);
    }

    // Class constructor
    private eCozyBluetoothLE(Context Context, Activity Activity){
        context = Context;
        activity = Activity;
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) Context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public static eCozyBluetoothLE get(Context Context, Activity Activity) {
        context = Context;
        activity = Activity;
        if (seCozyBluetoothLE == null) {
            seCozyBluetoothLE = new eCozyBluetoothLE(Context, Activity);
        }
        return seCozyBluetoothLE;
    }

    public void SetMainListener(BleMainListener listener) {
        this.sBleMainListener = listener;
    }

    public void SetSettingsListener(BleSettingsListener listener) {
        this.sBleSettingsListener = listener;
    }

    public void SetMainScheduleListener(BleMainScheduleListener listener) {
        this.sBleMainScheduleListener = listener;
    }

    // Check bluetooth state. Return True if bluetooth is on, False otherwise
    public boolean BLE_state() {
        return mBluetoothAdapter.isEnabled();
    }

    // Turning on bluetooth
    public void BLE_turn_on() {
        mBluetoothAdapter.enable();
    }

    // Start scanning ble devices; True if start, False if stop
    public void scanLeDevice(boolean enable) {
        if (enable) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // return list of macs selected devices
    public ArrayList<String> getDeviceList() {
        ArrayList<String> mBleMacs = new ArrayList<>();
        for (int i = 0; i < mBleMACs.size(); i++) {
            mBleMacs.add(mBleMACs.get(i).getAddress());
        }
        return mBleMacs;
    }

    // Check if connected to ble devices; return True if connected, False otherwise
    public boolean currentConnectionState() {
        if (mBluetoothGatt == null) {
            return false;
        }
        return true;
    }

    // Open connection with ble devices
    public void openConnection(Context context, int position) {
        scanLeDevice(false); // stop scanning
        this.context = context;
        if (mBleMACs.size() > position) {
            mBluetoothDevice = mBleMACs.get(position);
            // start connection
            mBluetoothGatt = mBluetoothDevice.connectGatt(context, true, mGattCallback);
            // Show waiting dialog while bluetooth is turning on
            mProgressDialogConnecting = new ProgressDialog(this.context);
            mProgressDialogConnecting.setTitle(R.string.Progress_connecting_header);
            mProgressDialogConnecting.setMessage(activity.getResources().getString(R.string.Progress_connecting_mBody));
            mProgressDialogConnecting.setCancelable(false);
            mProgressDialogConnecting.show();
        }
    }

    // Get number of found devices
    public int foundDevicesCount() {
        return mBleMACs.size();
    }

    // Close connection with ble devices
    public void closeConnection() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        mFindMe = 0;
        // Stop timer
        mTimer.cancel();
        mTimer.purge();
    }

    // get device MAC address, use it for find device name
    public String getMacAddress() {
        return mBluetoothDevice.getAddress();
    }

    // Set to queue request to read target and current temperature
    public void ReadTemperatures() {
        eCozyBleRequest meCozyBleRequest = new eCozyBleRequest();

        meCozyBleRequest.setRequestType(READ_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(targetTemperatureCharacteristicUuid);
        BleRequestQueue.add(meCozyBleRequest);

        meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(READ_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(currentTemperatureCharacteristicUuid);
        BleRequestQueue.add(meCozyBleRequest);
    }

    // Set to queue request to read battery level
    public void ReadBatteryLevel() {
        eCozyBleRequest meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(READ_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(BatteryServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(BatteryLevelCharacteristicUuid);
        BleRequestQueue.add(meCozyBleRequest);
    }

    // Set to queue request to read firmware version
    public void ReadFirmwareVersion() {
        eCozyBleRequest meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(READ_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(DeviceInformationServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(FirmwareRevisionStringCharacteristicUuid);
        BleRequestQueue.add(meCozyBleRequest);
    }

    // Set to queue request to read local temperature calibration
    public void ReadLocalTemperatureCalibration() {
        eCozyBleRequest meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(READ_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatSettingsServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(LocalTemperatureCalibration);
        BleRequestQueue.add(meCozyBleRequest);
    }

    // Set to queue request to read display locks
    public void ReadDisplayLocks() {
        eCozyBleRequest meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(READ_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatSettingsServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(DisplayLocks);
        BleRequestQueue.add(meCozyBleRequest);
    }

    // Set to queue request to read schedule state (enable/disable)
    public void ReadScheduleState() {
        eCozyBleRequest meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(READ_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatSettingsServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(ScheduleEnable);
        BleRequestQueue.add(meCozyBleRequest);
    }

    // Set to queue request to read schedule
    public void ReadScheduleFromDevice() {
        eCozyBleRequest meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(READ_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatSettingsServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(MondaySchedule);
        BleRequestQueue.add(meCozyBleRequest);

        meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(READ_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatSettingsServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(TuesdaySchedule);
        BleRequestQueue.add(meCozyBleRequest);

        meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(READ_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatSettingsServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(WednesdaySchedule);
        BleRequestQueue.add(meCozyBleRequest);

        meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(READ_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatSettingsServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(ThursdaySchedule);
        BleRequestQueue.add(meCozyBleRequest);

        meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(READ_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatSettingsServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(FridaySchedule);
        BleRequestQueue.add(meCozyBleRequest);

        meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(READ_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatSettingsServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(SaturdaySchedule);
        BleRequestQueue.add(meCozyBleRequest);

        meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(READ_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatSettingsServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(SundaySchedule);
        BleRequestQueue.add(meCozyBleRequest);
    }

    // Set to queue request to write current schedule
    public void WriteSchedule(int day, byte[] schedule) {
        eCozyBleRequest meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(eCozyBleRequest.WRITE_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatSettingsServiceUuid);
        switch (day) {
            case SUNDAY:
                meCozyBleRequest.setCharacteristicUuid(SundaySchedule);
                break;
            case SATURDAY:
                meCozyBleRequest.setCharacteristicUuid(SaturdaySchedule);
                break;
            case FRIDAY:
                meCozyBleRequest.setCharacteristicUuid(FridaySchedule);
                break;
            case THURSDAY:
                meCozyBleRequest.setCharacteristicUuid(ThursdaySchedule);
                break;
            case WEDNESDAY:
                meCozyBleRequest.setCharacteristicUuid(WednesdaySchedule);
                break;
            case TUESDAY:
                meCozyBleRequest.setCharacteristicUuid(TuesdaySchedule);
                break;
            case MONDAY:
                meCozyBleRequest.setCharacteristicUuid(MondaySchedule);
                break;
        }
        meCozyBleRequest.setPayload(schedule);
        BleRequestQueue.add(meCozyBleRequest);
    }

    // Write time to device
    public void WriteCurrentTime() {
        Date date=new Date();
        int year = date.getYear() + 1900; // year returns like current year minus 1900
        int day_of_week = date.getDay();
        if (day_of_week == 0) {
            day_of_week = 7; // Sunday returns like 0, must be write to device like 7
        }
        byte[] current_time = new byte[10];
        current_time[0] = (byte)(year & 0xFF); // year, low byte first
        current_time[1] = (byte)((year >> 8) & 0xFF); // year, low byte first
        current_time[2] = (byte)date.getMonth(); // month
        current_time[3] = (byte)date.getDate(); // day
        current_time[4] = (byte)date.getHours(); // hour
        current_time[5] = (byte)date.getMinutes(); // minutes
        current_time[6] = (byte)date.getSeconds(); // seconds
        current_time[7] = (byte)day_of_week; // day of week
        current_time[8] = (byte)0; // Fractions256
        current_time[9] = (byte)0; // Adjust Reason
        eCozyBleRequest meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(eCozyBleRequest.WRITE_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(CurrentTimeServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(CurrentTimeCharacteristicUuid);
        meCozyBleRequest.setPayload(current_time);
        BleRequestQueue.add(meCozyBleRequest);
    }

    // Set to queue request to write current schedule state
    public void WriteScheduleState(int scheduleState) {
        eCozyBleRequest meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(eCozyBleRequest.WRITE_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatSettingsServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(ScheduleEnable);
        meCozyBleRequest.setPayload(new byte[]{(byte)scheduleState});
        BleRequestQueue.add(meCozyBleRequest);
    }

    // Set to queue request to write display locks
    public void WriteLocalTemperatureCalibration(int newCalibration) {
        eCozyBleRequest meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(eCozyBleRequest.WRITE_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatSettingsServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(LocalTemperatureCalibration);
        meCozyBleRequest.setPayload(new byte[]{(byte)newCalibration});
        BleRequestQueue.add(meCozyBleRequest);
    }

    // Set to queue request to write display locks
    public void WriteDisplayLocks(int newLocks) {
        eCozyBleRequest meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(eCozyBleRequest.WRITE_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatSettingsServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(DisplayLocks);
        meCozyBleRequest.setPayload(new byte[]{(byte)newLocks});
        BleRequestQueue.add(meCozyBleRequest);
    }

    // Set to queue request to write Immediate Alert Level
    public void WriteImmediateAlertLevel(int newLevel) {
        mFindMe = newLevel;
        eCozyBleRequest meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(eCozyBleRequest.WRITE_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ImmediateAlertServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(AlertLevelCharacteristicUuid);
        meCozyBleRequest.setPayload(new byte[]{(byte)newLevel});
        BleRequestQueue.add(meCozyBleRequest);
    }

    // Set to queue request to write new target temperature
    public void WriteTargetTemperature(int temperature) {
        eCozyBleRequest meCozyBleRequest = new eCozyBleRequest();

        meCozyBleRequest.setRequestType(eCozyBleRequest.WRITE_CHARACTERISTIC);
        meCozyBleRequest.setServiceUuid(ThermostatServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(targetTemperatureCharacteristicUuid);
        meCozyBleRequest.setPayload(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                .putFloat((float)temperature).array());
        BleRequestQueue.add(meCozyBleRequest);
    }

    public static int getFindMe() {
        return mFindMe;
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (device.getName().equals("Thermostat")) {
                        addBleMacToList(device);
                    }
                }
            });
        }
    };

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        // Callback indicating when GATT client has connected/disconnected to/from a remote GATT server.
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mFindMe = 0;
                if (mBluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    mBluetoothDevice.createBond();
                }
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mBluetoothGatt = null;
                Handler refresh = new Handler(Looper.getMainLooper());
                refresh.post(new Runnable() {
                    public void run()
                    {
                        // Show modal dialog about disconnect
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(R.string.FailConnectionTitle)
                                .setMessage(R.string.FailConnectionBody)
                                .setCancelable(false)
                                .setPositiveButton(R.string.dialogOkButton,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                ((eCozyActivity) activity).ShowCurrentFragment(1);
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
            }
        }

        @Override
        // Callback indicating the result of a characteristic write operation.
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                ShowToastMessage(R.string.FailWrite);
            }
        }

        @Override
        // Callback reporting the result of a descriptor read operation.
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "onDescriptorRead called with status " + status);
        }

        @Override
        // Callback indicating the result of a descriptor write operation.
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                ShowToastMessage(R.string.FailWriteDescriptor);
            }
        }

        @Override
        // Callback reporting the result of a characteristic read operation.
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead called with status " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                UpdateTargetOrCurrentTemperature(characteristic);
                UpdateValuesForSettingsFragment(characteristic);
                UpdateValuesForThermostatSettingsFragment(characteristic);
            } else if (status != 137) {
                ShowToastMessage(R.string.FailRead);
            }
        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Handler refresh = new Handler(Looper.getMainLooper());
            refresh.post(new Runnable() {
                public void run()
                {
                    mProgressDialogConnecting.dismiss();
                    mProgressDialogConnecting.hide();
                }
            });
            Log.d(TAG, "onServicesDiscovered received: " + status);
            WriteCurrentTime();
            ReadTemperatures();
            ReadTemperatures();
            SetNotifications();

            mTimer = new Timer(); // Create timer
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    //Log.d(TAG, "Timer schedule");
                    if (BleRequestQueue.size() != 0) {
                        SendNextRequestToDevice();
                    }
                }
            };
            mTimer.schedule(mTimerTask, 10, 300);
        }

        @Override
        // Characteristic notification
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            UpdateTargetOrCurrentTemperature(characteristic);
        }
    };

    // Add MAC address to list with found devices
    private void addBleMacToList(BluetoothDevice newMAC) {
        if (mBleMACs.contains(newMAC) == false) {
            mBleMACs.add(newMAC);
        }
    }

    // Set values to receive notifications
    private void SetNotifications() {
        eCozyBleRequest meCozyBleRequest = new eCozyBleRequest();

        meCozyBleRequest.setRequestType(eCozyBleRequest.SET_CHARACTERISTIC_NOTIFICATION);
        meCozyBleRequest.setServiceUuid(ThermostatServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(targetTemperatureCharacteristicUuid);
        BleRequestQueue.add(meCozyBleRequest);

        meCozyBleRequest = new eCozyBleRequest();
        meCozyBleRequest.setRequestType(eCozyBleRequest.SET_CHARACTERISTIC_NOTIFICATION);
        meCozyBleRequest.setServiceUuid(ThermostatServiceUuid);
        meCozyBleRequest.setCharacteristicUuid(currentTemperatureCharacteristicUuid);
        BleRequestQueue.add(meCozyBleRequest);
    }

    // If received new sate of target or current temperature, send it to main fragment via listener
    // if it is possible
    private void UpdateTargetOrCurrentTemperature(BluetoothGattCharacteristic characteristic) {
        UUID characteristicUUID = characteristic.getUuid();
        if (targetTemperatureCharacteristicUuid.equals(characteristicUUID)) {
            byte val[] = characteristic.getValue();
            float temperature = ByteBuffer.wrap(val).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            Log.d(TAG, "onCharacteristicChanged received: target temperature " + temperature);
            if (sBleMainListener != null) {
                sBleMainListener.SetTargetTemperature(temperature);
            }
        } else if (currentTemperatureCharacteristicUuid.equals((characteristicUUID))) {
            byte val[] = characteristic.getValue();
            float temperature = ByteBuffer.wrap(val).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            Log.d(TAG, "onCharacteristicChanged received: current temperature " + temperature);
            if (sBleMainListener != null) {
                sBleMainListener.SetCurrentTemperature(temperature);
            }
        }
    }

    // If received new sate of battery, firmware version, temperature calibration and display
    // locks, send it to settings fragment  via listener if it is possible
    private void UpdateValuesForSettingsFragment(BluetoothGattCharacteristic characteristic) {
        UUID characteristicUUID = characteristic.getUuid();
        if (BatteryLevelCharacteristicUuid.equals(characteristicUUID)) {
            byte val[] = characteristic.getValue();
            int batteryLevel = (int)(val[0] & 0xFF);
            if (sBleSettingsListener != null) {
                sBleSettingsListener.SetBatteryLevel(batteryLevel);
            }
        } else if (FirmwareRevisionStringCharacteristicUuid.equals(characteristicUUID)) {
            if (sBleSettingsListener != null) {
                sBleSettingsListener.SetFirmwareVersion(new String(characteristic.getValue()));
            }
        } else if (LocalTemperatureCalibration.equals(characteristicUUID)) {
            byte val[] = characteristic.getValue();
            int calibration = (int)(val[0]);
            if (sBleSettingsListener != null) {
                sBleSettingsListener.SetLocalTemperatureCalibration((float)calibration/(float)10.0);
            }
        } else if (DisplayLocks.equals(characteristicUUID)) {
            byte val[] = characteristic.getValue();
            int locks = (int)(val[0]);
            if (sBleSettingsListener != null) {
                sBleSettingsListener.SetDisplayLocks(locks);
            }
        }
    }

    // If received new sate of thermostat settings, send it to settings fragment
    // via listener if it is possible
    private void UpdateValuesForThermostatSettingsFragment(BluetoothGattCharacteristic
                                                                   characteristic) {
        UUID characteristicUUID = characteristic.getUuid();
        if (ScheduleEnable.equals(characteristicUUID)) {
            byte val[] = characteristic.getValue();
            int schedule_state = (int)(val[0] & 0xFF);
            if (sBleMainScheduleListener != null) {
                sBleMainScheduleListener.SetScheduleState(schedule_state);
            }
        } else if (MondaySchedule.equals(characteristicUUID)) {
            SendScheduleToFragment(characteristic, MONDAY);
        }  else if (TuesdaySchedule.equals(characteristicUUID)) {
            SendScheduleToFragment(characteristic, TUESDAY);
        } else if (WednesdaySchedule.equals(characteristicUUID)) {
            SendScheduleToFragment(characteristic, WEDNESDAY);
        } else if (ThursdaySchedule.equals(characteristicUUID)) {
            SendScheduleToFragment(characteristic, THURSDAY);
        } else if (FridaySchedule.equals(characteristicUUID)) {
            SendScheduleToFragment(characteristic, FRIDAY);
        } else if (SaturdaySchedule.equals(characteristicUUID)) {
            SendScheduleToFragment(characteristic, SATURDAY);
        } else if (SundaySchedule.equals(characteristicUUID)) {
            SendScheduleToFragment(characteristic, SUNDAY);
        }
    }

    // Send received schedule to main schedule fragment
    private void SendScheduleToFragment(BluetoothGattCharacteristic characteristic, int day) {
        if (sBleMainScheduleListener != null) {
            sBleMainScheduleListener.SetSchedule(characteristic.getValue(), day);
        }
    }

    // Get next element from queue and send this request to device
    private void SendNextRequestToDevice() {
        BluetoothGattDescriptor descriptor;
        BluetoothGattCharacteristic characteristic;
        eCozyBleRequest meCozyBleRequest = BleRequestQueue.remove();

        if (meCozyBleRequest.getRequestType() == eCozyBleRequest.READ_CHARACTERISTIC) {
            characteristic = mBluetoothGatt.getService(meCozyBleRequest.getServiceUuid())
                    .getCharacteristic(meCozyBleRequest.getCharacteristicUuid());
            mBluetoothGatt.readCharacteristic(characteristic);
        } else if (meCozyBleRequest.getRequestType() == eCozyBleRequest.WRITE_CHARACTERISTIC) {
            characteristic = mBluetoothGatt.getService(meCozyBleRequest.getServiceUuid())
                    .getCharacteristic(meCozyBleRequest.getCharacteristicUuid()); // characteristic != null
            characteristic.setValue(meCozyBleRequest.getPayload());
            mBluetoothGatt.writeCharacteristic(characteristic);
        } else if (meCozyBleRequest.getRequestType() == eCozyBleRequest.SET_CHARACTERISTIC_NOTIFICATION) {
            characteristic = mBluetoothGatt.getService(meCozyBleRequest.getServiceUuid())
                    .getCharacteristic(meCozyBleRequest.getCharacteristicUuid());
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            descriptor = characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    private void ShowToastMessage(int body) {
        Toast toast = Toast.makeText(context, body, Toast.LENGTH_LONG);
        TextView t = (TextView) toast.getView().findViewById(android.R.id.message);
        if( t != null) t.setGravity(Gravity.CENTER);
        toast.show();
    }
}


package com.ecozy.android.ecozyble;

import java.util.UUID;

/**
 * Created by AREG on 17.02.2017.
 */

public class eCozyBleRequest {

    public static int READ_CHARACTERISTIC = 1;
    public static int SET_CHARACTERISTIC_NOTIFICATION = 2;
    public static int WRITE_CHARACTERISTIC = 3;
    public static int READ_DESCRIPTOR = 4;
    public static int WRITE_DESCRIPTOR = 5;

    private int mRequestType;
    private UUID mServiceUuid;
    private UUID mCharacteristicUuid;
    private byte[] mPayload;

    public UUID getCharacteristicUuid() {
        return mCharacteristicUuid;
    }

    public void setCharacteristicUuid(UUID characteristicUuid) {
        mCharacteristicUuid = characteristicUuid;
    }

    public byte[] getPayload() {
        return mPayload;
    }

    public void setPayload(byte[] payload) {
        mPayload = payload;
    }

    public UUID getServiceUuid() {
        return mServiceUuid;
    }

    public void setServiceUuid(UUID serviceUuid) {
        mServiceUuid = serviceUuid;
    }

    public int getRequestType() {
        return mRequestType;
    }

    public void setRequestType(int requestType) {
        mRequestType = requestType;
    }
}

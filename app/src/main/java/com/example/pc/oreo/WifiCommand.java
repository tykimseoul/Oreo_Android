package com.example.pc.oreo;

import org.json.JSONArray;
import org.json.JSONException;

public class WifiCommand {
    private byte[] rawData;
    private WifiCommandCode type;
    private byte[] payload;
    private int[] flightDataPayload;

    public WifiCommand(byte[] rawData) {
        this.rawData = rawData;
        parseData();
    }

    public WifiCommand(int type) {
        for (WifiCommandCode code : WifiCommandCode.values()) {
            if (type == code.getValue()) {
                this.type = code;
                break;
            }
        }
    }

    private void parseData() {
        for (WifiCommandCode code : WifiCommandCode.values()) {
            if (rawData[1] == code.getValue()) {
                type = code;
                break;
            }
        }
        if (type == WifiCommandCode.FLIGHT_DATA) {
            int payloadLength = rawData.length - 2;
            payload = new byte[payloadLength];
            System.arraycopy(rawData, 2, payload, 0, payloadLength);
            try {
                JSONArray arr = new JSONArray(new String(payload));
                flightDataPayload = new int[arr.length()];
                for (int i = 0; i < arr.length(); i++) {
                    flightDataPayload[i] = arr.optInt(i);
                }
            } catch (JSONException e) {
                flightDataPayload = new int[0];
            }
        } else {
            int payloadLength = type.getLength() - 2;
            payload = new byte[payloadLength];
            System.arraycopy(rawData, 2, payload, 0, payloadLength);
        }
    }

    public WifiCommandCode getType() {
        return type;
    }

    public int[] getFlightDataPayload() {
        return flightDataPayload;
    }

    public enum WifiCommandCode {
        REQUEST_CONNECTION(100, 2),
        APPROVE_CONNECTION(101, 2),
        JOYSTICK_CONTROL(102, 14),
        TAKE_OFF(103, 2),
        LAND(104, 2),
        REQUEST_STREAM(105, 2),
        FRAME_DATA(106, 2),
        FLIGHT_DATA(107, 36),
        CAMERA_SETTINGS(108, 10);

        private final byte value;
        private final byte length;

        WifiCommandCode(int value, int length) {
            this.value = (byte) value;
            this.length = (byte) length;
        }

        public byte getValue() {
            return value;
        }

        public byte getLength() {
            return length;
        }
    }
}

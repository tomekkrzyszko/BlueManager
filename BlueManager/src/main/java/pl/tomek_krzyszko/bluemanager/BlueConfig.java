package pl.tomek_krzyszko.bluemanager;

import android.Manifest;
import android.content.pm.PackageManager;

public class BlueConfig {

    public static class Package {

        public static final String[] PERMISSIONS = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        public static final String[] PERMISSIONS_v23 = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        public static final String[] FEATURES = {
                PackageManager.FEATURE_BLUETOOTH_LE
        };
    }

    public static final String BLUE_BROADCAST_ACTION = "blue_action";

    public static final String BLUE_SCAN_TYPE = "blue_type";
    public static final String BLUE_DEVICE_VALUE = "blue_value";

    public static final int BLUE_SCAN_DISCOVERED = 0;
    public static final int BLUE_SCAN_UPDATED = 1;
    public static final int BLUE_SCAN_LOST = 2;
    public static final int BLUE_SCAN_ERROR = 3;

    public static final String BLUE_SCAN_ERROR_CODE = "extra_error_code";



    public BlueConfig() {
    }

}

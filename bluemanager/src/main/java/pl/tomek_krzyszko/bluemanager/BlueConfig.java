package pl.tomek_krzyszko.bluemanager;

import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class BlueConfig {

    public static final String BLUE_BROADCAST_ACTION = "blue_action";

    public static final String BLUE_SCAN_TYPE = "blue_type";
    public static final String BLUE_DEVICE_VALUE = "blue_value";

    public static final int BLUE_SCAN_DISCOVERED = 0;
    public static final int BLUE_SCAN_UPDATED = 1;
    public static final int BLUE_SCAN_LOST = 2;
    public static final int BLUE_SCAN_ERROR = 3;

    public static final String BLUE_SCAN_ERROR_CODE = "extra_error_code";

    public static Builder builder() {
        return new AutoValue_BlueConfig.Builder()
                .setShouldSendBroadcast(false)
                .setScanPeriodMillis(15000)
                .setWaitPeriodMillis(1000)
                .setDiscoveryTimeoutMillis(1000)
                .setScanPeriodMillisLegacy(25000)
                .setWaitPeriodMillisLegacy(1500)
                .setDiscoveryTimeoutMillisLegacy(1500)
                .setWaitPeriodAfterErrorMillis(1000)
                .setServiceDiscoveryTimeoutMillis(15000)
                .setBufferSize(1024)
                .setNotificationsEnablingDescriptorUUID(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
    }

    public abstract boolean getShouldSendBroadcast();
    public abstract long getScanPeriodMillis();
    public abstract long getWaitPeriodMillis();
    public abstract long getDiscoveryTimeoutMillis();
    public abstract long getScanPeriodMillisLegacy();
    public abstract long getWaitPeriodMillisLegacy();
    public abstract long getDiscoveryTimeoutMillisLegacy();
    public abstract long getWaitPeriodAfterErrorMillis();
    public abstract long getServiceDiscoveryTimeoutMillis();
    public abstract int getBufferSize();
    public abstract UUID getNotificationsEnablingDescriptorUUID();
    public abstract UUID getBluetoothClassicServerUUID();

    @AutoValue.Builder
    public static abstract class Builder {

        /**
         * Flag which enable broadcast functionality
         */
        public abstract Builder setShouldSendBroadcast(boolean shouldSendBroadcast);

        /**
         * Time in milliseconds when the scanner scan.
         */
        public abstract Builder setScanPeriodMillis(long scanPeriodMillis);

        /**
         * Time in milliseconds when the scanner does not scan and waits with its worker thread being suspended
         */
        public abstract Builder setWaitPeriodMillis(long waitPeriodMillis);


        /**
         * Time in milliseconds after which a {@link pl.tomek_krzyszko.bluemanager.device.BlueDevice} is being considered lost
         * when not detected again.
         */
        public abstract Builder setDiscoveryTimeoutMillis(long discoveryTimeoutMillis);

        /**
         * Periods for devices with API < 21 should probably be longer due to Bluetooth API being poorly implemented.
         * Scanning may not be as fast as on Androids 5.0+
         */
        public abstract Builder setScanPeriodMillisLegacy(long scanPeriodMillisLegacy);
        public abstract Builder setWaitPeriodMillisLegacy(long waitPeriodMillisLegacy);
        public abstract Builder setDiscoveryTimeoutMillisLegacy(long discoveryTimeoutMillisLEgacy);

        /**
         * Time in milliseconds to wait if error occurs when trying to startScan the scan.
         * Error may be a consequence of bluetooth being disabled in the system or permissions being revoked.
         */
        public abstract Builder setWaitPeriodAfterErrorMillis(long waitPeriodAfterErrorMillis);

        /**
         * Time in milliseconds after which service discovery process is considered to have failed
         */
        public abstract Builder setServiceDiscoveryTimeoutMillis(long serviceDiscoveryTimeoutMillis);

        /**
         * Buffer store for the input stream
         */
        public abstract Builder setBufferSize(int size);

        /**
         * UUID of notification descriptor
         */
        public abstract Builder setNotificationsEnablingDescriptorUUID(UUID uuid);

        /**
         * UUID for bluetooth classic service and connection process
         */
        public abstract Builder setBluetoothClassicServerUUID(UUID uuid);

        abstract BlueConfig autoBuild();

        public BlueConfig build() {
            return autoBuild();
        }
    }


}

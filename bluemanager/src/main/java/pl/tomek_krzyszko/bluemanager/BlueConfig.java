package pl.tomek_krzyszko.bluemanager;

import android.content.Intent;

import com.google.auto.value.AutoValue;

import java.util.UUID;

import pl.tomek_krzyszko.bluemanager.device.BlueDevice;

@AutoValue
public abstract class BlueConfig {

    /**
     * Broadcast action name for {@link android.content.IntentFilter#addAction(String)}
     */
    public static final String BLUE_BROADCAST_ACTION = "blue_action";

    /**
     * Key for {@link Intent#getExtras()} scan type value
     */
    public static final String BLUE_SCAN_TYPE = "blue_type";

    /**
     * Key for {@link Intent#getExtras()} device value
     */
    public static final String BLUE_DEVICE_VALUE = "blue_value";

    /**
     * Value for {@link Intent#getExtras()} scanning key
     * Represent {@link pl.tomek_krzyszko.bluemanager.callback.BlueDeviceScanListener#onDeviceFound(BlueDevice)}
     */
    public static final int BLUE_SCAN_DISCOVERED = 0;

    /**
     * Value for {@link Intent#getExtras()} scanning key
     * Represent {@link pl.tomek_krzyszko.bluemanager.callback.BlueDeviceScanListener#onDeviceUpdate(BlueDevice)}
     */
    public static final int BLUE_SCAN_UPDATED = 1;

    /**
     * Value for {@link Intent#getExtras()} scanning key
     * Represent {@link pl.tomek_krzyszko.bluemanager.callback.BlueDeviceScanListener#onDeviceLost(BlueDevice)}
     */
    public static final int BLUE_SCAN_LOST = 2;

    /**
     * Value for {@link Intent#getExtras()} scanning key
     * Represent {@link pl.tomek_krzyszko.bluemanager.callback.BlueDeviceScanListener#onDeviceScanError(int)}
     */
    public static final int BLUE_SCAN_ERROR = 3;

    /**
     * Key for {@link Intent#getExtras()} scan status value
     */
    public static final String BLUE_SCAN_ERROR_CODE = "extra_error_code";

    public static Builder builder() {
        return new AutoValue_BlueConfig.Builder()
                .setShouldSendBroadcast(false)
                .setAutoRestartService(false)
                .setDiscoveryTimeoutMillis(5000)
                .setWaitPeriodAfterErrorMillis(1000)
                .setServiceDiscoveryTimeoutMillis(15000)
                .setBufferSize(1024)
                .setNotificationsEnablingDescriptorUUID(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
    }

    public abstract boolean getShouldSendBroadcast();
    public abstract boolean getAutoRestartService();
    public abstract long getDiscoveryTimeoutMillis();
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
         * Flag which enable {@link android.app.Service#START_STICKY} functionality
         */
        public abstract Builder setAutoRestartService(boolean autoRestartService);

        /**
         * Time in milliseconds after which a {@link pl.tomek_krzyszko.bluemanager.device.BlueDevice} is being considered lost
         * when not detected again.
         */
        public abstract Builder setDiscoveryTimeoutMillis(long discoveryTimeoutMillis);

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

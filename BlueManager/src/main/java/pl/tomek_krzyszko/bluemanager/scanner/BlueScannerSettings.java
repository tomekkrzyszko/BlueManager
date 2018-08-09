package pl.tomek_krzyszko.bluemanager.scanner;

public class BlueScannerSettings {

    public static final boolean DEFAULT_SEND_BROADCASTS = true;

    /**
     * Time in milliseconds when the scanner scan.
     */
    public static final long DEFAULT_SCAN_PERIOD_MILLIS = 15 * 1000;

    /**
     * Time in milliseconds when the scanner does not scan and waits with its worker thread being suspended
     */
    public static final long DEFAULT_WAIT_PERIOD_MILLIS = 1000;

    /**
     * Time in milliseconds to wait if error occurs when trying to start the scan.
     * Error may be a consequence of bluetooth being disabled in the system or permissions being revoked.
     */
    public static final long DEFAULT_WAIT_PERIOD_AFTER_ERROR_MILLIS = 1000;

    /**
     * Time in milliseconds after which a {@link pl.tomek_krzyszko.bluemanager.device.BlueDevice} is being considered lost
     * when not detected again.
     */
    public static final long DEFAULT_DISCOVERY_TIMEOUT_MILLIS = 1 * 1000;




    /**
     * Periods for devices with API < 21 should probably be longer due to Bluetooth API being poorly implemented.
     * Scanning may not be as fast as on Androids 5.0+
     */
    public static final long DEFAULT_SCAN_PERIOD_MILLIS_LEGACY = 15 * 1000;
    public static final long DEFAULT_WAIT_PERIOD_MILLIS_LEGACY = 1000;
    // RELEASE time
    public static final long DEFAULT_DISCOVERY_TIMEOUT_MILLIS_LEGACY = 1 * 1000;


    private boolean sendBroadcasts;
    private long scanPeriodMillis;
    private long waitPeriodMillis;
    private long waitPeriodAfterErrorMillis;
    private long discoveryTimeoutMillis;

    private BlueScannerSettings() {
    }

    public boolean isSendBroadcasts() {
        return sendBroadcasts;
    }

    public long getScanPeriodMillis() {
        return scanPeriodMillis;
    }

    public long getWaitPeriodMillis() {
        return waitPeriodMillis;
    }

    public long getWaitPeriodAfterErrorMillis() {
        return waitPeriodAfterErrorMillis;
    }

    public long getDiscoveryTimeoutMillis() {
        return discoveryTimeoutMillis;
    }

    public static BlueScannerSettings getDefault() {
        return new Builder().build();
    }

    public static BlueScannerSettings getDefaultLegacy() {
        Builder builder = new Builder();
        builder.setScanPeriodMillis(DEFAULT_SCAN_PERIOD_MILLIS_LEGACY);
        builder.setWaitPeriodMillis(DEFAULT_WAIT_PERIOD_MILLIS_LEGACY);
        builder.setDiscoveryTimeoutMillis(DEFAULT_DISCOVERY_TIMEOUT_MILLIS_LEGACY);
        return builder.build();
    }

    public static class Builder {

        private BlueScannerSettings blueScannerSettings;

        public Builder() {
            blueScannerSettings = new BlueScannerSettings();
            blueScannerSettings.sendBroadcasts = DEFAULT_SEND_BROADCASTS;
            blueScannerSettings.scanPeriodMillis = DEFAULT_SCAN_PERIOD_MILLIS;
            blueScannerSettings.waitPeriodMillis = DEFAULT_WAIT_PERIOD_MILLIS;
            blueScannerSettings.waitPeriodAfterErrorMillis = DEFAULT_WAIT_PERIOD_AFTER_ERROR_MILLIS;
            blueScannerSettings.discoveryTimeoutMillis = DEFAULT_DISCOVERY_TIMEOUT_MILLIS;
        }

        public Builder setSendBroadcasts(boolean sendBroadcasts) {
            blueScannerSettings.sendBroadcasts = sendBroadcasts;
            return this;
        }

        public Builder setScanPeriodMillis(long scanPeriodMillis) {
            blueScannerSettings.scanPeriodMillis = scanPeriodMillis;
            return this;
        }

        public Builder setWaitPeriodMillis(long waitPeriodMillis) {
            blueScannerSettings.waitPeriodMillis = waitPeriodMillis;
            return this;
        }

        public Builder setWaitPeriodAfterErrorMillis(long waitPeriodAfterErrorMillis) {
            blueScannerSettings.waitPeriodAfterErrorMillis = waitPeriodAfterErrorMillis;
            return this;
        }

        public Builder setDiscoveryTimeoutMillis(long discoveryTimeoutMillis) {
            blueScannerSettings.discoveryTimeoutMillis = discoveryTimeoutMillis;
            return this;
        }

        public BlueScannerSettings build() {
            return blueScannerSettings;
        }
    }
}

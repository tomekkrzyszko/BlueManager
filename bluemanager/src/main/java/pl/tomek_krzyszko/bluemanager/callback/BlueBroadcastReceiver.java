package pl.tomek_krzyszko.bluemanager.callback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import pl.tomek_krzyszko.bluemanager.BlueConfig;
import pl.tomek_krzyszko.bluemanager.device.BlueDevice;
import timber.log.Timber;


/**
 * Class responsible for {@link BroadcastReceiver} implementation in library
 */
public abstract class BlueBroadcastReceiver extends BroadcastReceiver implements BlueDeviceScanListener {

    /**
     * Method which return proper {@link IntentFilter}
     *
     * @return proper {@link IntentFilter} for {@link BroadcastReceiver}
     */
    public static IntentFilter getIntentFilter() {
        return new IntentFilter(BlueConfig.BLUE_BROADCAST_ACTION);
    }


    /**
     * Method which implement {@link BroadcastReceiver} onReceive method
     *
     * @param context {@link Context} of the application,
     * @param intent {@link Intent} which was handled by {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        BlueDevice blueDevice = (BlueDevice) intent.getSerializableExtra(BlueConfig.BLUE_DEVICE_VALUE);
        int extraType = intent.getIntExtra(BlueConfig.BLUE_SCAN_TYPE, -1);
        if (blueDevice != null) {
            switch (extraType) {
                case BlueConfig.BLUE_SCAN_DISCOVERED: {
                    onDeviceFound(blueDevice);
                    break;
                }
                case BlueConfig.BLUE_SCAN_UPDATED: {
                    onDeviceUpdate(blueDevice);
                    break;
                }
                case BlueConfig.BLUE_SCAN_LOST: {
                    onDeviceLost(blueDevice);
                    break;
                }
                default:
                    Timber.w("Unsupported EXTRA_TYPE = " + extraType);
                    break;
            }
        } else {
            switch (extraType) {
                case BlueConfig.BLUE_SCAN_ERROR:
                    int errorCode = intent.getIntExtra(BlueConfig.BLUE_SCAN_ERROR_CODE, -1);
                    onDeviceScanError(errorCode);
                    break;
                default:
                    Timber.w("Intent EXTRA_VALUE = null && Unsupported EXTRA_TYPE = " + extraType);
                    break;
            }
        }

    }

}
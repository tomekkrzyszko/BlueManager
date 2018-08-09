package pl.tomek_krzyszko.bluemanager.callback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import pl.tomek_krzyszko.bluemanager.device.BlueDevice;
import pl.tomek_krzyszko.bluemanager.scanner.BlueScanner;


public abstract class BlueBroadcastReceiver extends BroadcastReceiver implements BlueDeviceScanListener {

    public static IntentFilter getIntentFilter() {
        return new IntentFilter(BlueScanner.ACTION_TERMA);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        BlueDevice blueDevice = (BlueDevice) intent.getSerializableExtra(BlueScanner.EXTRA_VALUE);
        int extraType = intent.getIntExtra(BlueScanner.EXTRA_TYPE, -1);
        if (blueDevice != null) {
            switch (extraType) {
                case BlueScanner.EXTRA_TYPE_DISCOVERED:
                    onDeviceDiscovered(blueDevice);
                    break;
                case BlueScanner.EXTRA_TYPE_UPDATED:
                    onDeviceUpdate(blueDevice);
                    break;
                case BlueScanner.EXTRA_TYPE_LOST:
                {
                    onDeviceLost(blueDevice);
                }

                break;
                default:
                    //L.w(BlueBroadcastReceiver.this, "Unsupported EXTRA_TYPE = " + extraType);
                    break;
            }
        } else {
            switch (extraType) {
                case BlueScanner.EXTRA_TYPE_ERROR:
                    int errorCode = intent.getIntExtra(BlueScanner.EXTRA_ERROR_CODE, -1);
                    onDeviceScanError(errorCode);
                    break;
                default:
                   // L.w(BlueBroadcastReceiver.this, "Intent EXTRA_VALUE = null && Unsupported EXTRA_TYPE = " + extraType);
                    break;
            }
        }

    }

}
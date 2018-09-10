package pl.tomek_krzyszko.bluemanager.dagger.modules;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.os.Build;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

import dagger.Module;
import dagger.Provides;
import pl.tomek_krzyszko.bluemanager.callback.BlueDeviceScanListener;
import pl.tomek_krzyszko.bluemanager.dagger.scopes.InstanceScope;
import pl.tomek_krzyszko.bluemanager.device.BlueDevice;
import pl.tomek_krzyszko.bluemanager.scanner.BlueScanner;

@Module
public class TaskModule {

    private BlueScanner blueScanner;

    public TaskModule(BlueScanner blueScanner) {
        this.blueScanner = blueScanner;
    }

    /**
     * Used in API level < 21 for scanning
     * Used in all API levels for managing device's Bluetooth
     */
    @InstanceScope
    @Provides
    public BluetoothAdapter provideBluetoothAdapter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return ((BluetoothManager) blueScanner.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        } else {
            return BluetoothAdapter.getDefaultAdapter();
        }
    }

    /**
     * Used in API level >= 21 for scanning
     */
    @InstanceScope
    @Provides
    public BluetoothLeScanner provideBluetoothLeScanner(BluetoothAdapter bluetoothAdapter){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return bluetoothAdapter.getBluetoothLeScanner();
        } else {
            return null;
        }
    }

    @InstanceScope
    @Provides
    public Set<BlueDeviceScanListener> provideBlueDeviceScanListeners(){
        return new HashSet<>();
    }

    @InstanceScope
    @Provides
    public Map<String, BlueDevice> provideConnectedDevices(){
        return new HashMap<>();
    }

    @InstanceScope
    @Provides
    public Timer provideTimer(){
        return new Timer();
    }
}

package pl.tomek_krzyszko.bluemanager.dagger.modules;

import android.os.Handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dagger.Module;
import dagger.Provides;
import pl.tomek_krzyszko.bluemanager.callback.BlueDeviceScanListener;
import pl.tomek_krzyszko.bluemanager.dagger.scopes.InstanceScope;
import pl.tomek_krzyszko.bluemanager.device.BlueDevice;
import pl.tomek_krzyszko.bluemanager.device.BlueDeviceController;
import pl.tomek_krzyszko.bluemanager.scanner.BlueScanner;
import pl.tomek_krzyszko.bluemanager.scanner.BlueScannerTask;

@Module
public class ScannerModule {

    private BlueScanner blueScanner;

    public ScannerModule(BlueScanner blueScanner) {
        this.blueScanner = blueScanner;
    }

    /**
     * Task run on a worker thread. The task consists of cyclic bluetooth scanning.
     */
    @InstanceScope
    @Provides
    public BlueScannerTask provideBlueScannerTask(){
        return new BlueScannerTask(blueScanner);
    }

    @InstanceScope
    @Provides
    public Handler provideHandler(){
        return new Handler();
    }

    @InstanceScope
    @Provides
    public Set<BlueDeviceScanListener> provideBlueDeviceScanListeners(){
        return new HashSet<>();
    }

    /**
     * Mapping between hardware address and {@link BlueDevice}.
     * {@link BlueDevice} is a model class representing devices discovered in a nearby location.
     */
    @InstanceScope
    @Provides
    public Map<String, BlueDevice> provideDiscoveredDevices(){
        return new HashMap<>();
    }


    @InstanceScope
    @Provides
    public BlueDeviceController provideBlueDeviceController(){
        return new BlueDeviceController(blueScanner);
    }
}

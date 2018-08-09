package pl.tomek_krzyszko.bluemanager.dagger.modules;

import android.os.Build;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import pl.tomek_krzyszko.bluemanager.callback.BlueDeviceScanListener;
import pl.tomek_krzyszko.bluemanager.device.BlueDevice;
import pl.tomek_krzyszko.bluemanager.scanner.BlueScanner;
import pl.tomek_krzyszko.bluemanager.scanner.BlueScannerSettings;
import pl.tomek_krzyszko.bluemanager.scanner.BlueScannerTask;

@Module
public class ScannerModule {

    private BlueScanner blueScanner;

    public ScannerModule(BlueScanner blueScanner) {
        this.blueScanner = blueScanner;
    }

    @Singleton
    @Provides
    public BlueScannerSettings provideBlueScannerSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return BlueScannerSettings.getDefault();
        } else {
            return BlueScannerSettings.getDefaultLegacy();
        }
    }

    @Singleton
    @Provides
    public BlueScannerTask provideBlueScannerTask(){
        return new BlueScannerTask(blueScanner);
    }

    @Singleton
    @Provides
    public Set<BlueDeviceScanListener> provideBlueDeviceScanListeners(){
        return new HashSet<>();
    }

    @Singleton
    @Provides
    public Map<String, BlueDevice> provideConnectedDevices(){
        return new HashMap<>();
    }
}

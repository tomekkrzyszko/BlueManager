package pl.tomek_krzyszko.bluemanager;


import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;

import pl.tomek_krzyszko.bluemanager.dagger.ApplicationScope;
import pl.tomek_krzyszko.bluemanager.dagger.components.BlueManagerComponent;
import pl.tomek_krzyszko.bluemanager.exception.BlueManagerExceptions;
import pl.tomek_krzyszko.bluemanager.scanner.BlueScanner;
import timber.log.Timber;

/**
 * Main manager class used to control scanning and managing scan results.
 */
public class BlueManager{

    private Context mContext;
    private boolean connected = false;
    private BlueManagerComponent component;
    private static BlueManager instance = null;
    private BlueScannerServiceConnection blueScannerServiceConnection;
    private Intent blueScannerIntent;

    public static BlueManager getInstance() {
        if (instance == null) {
            instance = new BlueManager();
        }
        return instance;
    }

    public BlueManager() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    public BlueManagerComponent getComponent() {
        return component;
    }

    /**
     *
     * @param context {@link Context} used to initialize app context in library
     * @param blueConfig {@link BlueConfig} used to change default BlueManager configuration. If null, default config will be used.
     */
    public void initilize(Context context, @Nullable BlueConfig blueConfig){
        this.mContext = context;
        this.component = ApplicationScope.getComponent(mContext,blueConfig);
    }

    /**
     * @return {@link Intent} associated with {@link BlueScanner} service.
     */
    private Intent getServiceIntent() {
        if(blueScannerIntent == null){
            blueScannerIntent = new Intent(mContext, BlueScanner.class);
        }
        return blueScannerIntent;
    }

    /**
     *  Set {@link BlueScannerServiceConnection} instance.
     */
    private void setBlueScannerServiceConnection(BlueScannerServiceConnection blueScannerServiceConnection) {
        this.blueScannerServiceConnection = blueScannerServiceConnection;
    }

    /**
     * Starts {@link BlueScanner} service. If the service is started already, it does nothing.
     */
    public void startService(BlueScannerServiceConnection blueScannerServiceConnection) throws BlueManagerExceptions {
        if(mContext!=null) {
            mContext.startService(getServiceIntent());
            connect(blueScannerServiceConnection);
        }else{
            throw new BlueManagerExceptions("First initilize BlueManager.");
        }
    }

    /**
     * Stops {@link BlueScanner} service.
     */
    public void stopService() {
        disconnect();
        mContext.stopService(getServiceIntent());
    }

    /**
     * Connects {@link BlueManager} to {@link BlueScanner} service.
     * If the service hasn't been started before, it is started now.
     *
     * @param blueScannerServiceConnection {@link BlueScannerServiceConnection} used to notify about service connection status.
     * @return whether or not you have successfully bound the service
     */
    private boolean connect(BlueScannerServiceConnection blueScannerServiceConnection) {
        if (!connected) {
            setBlueScannerServiceConnection(blueScannerServiceConnection);
            connected = mContext.bindService(getServiceIntent(), serviceConnection, Context.BIND_ADJUST_WITH_ACTIVITY);
            return connected;
        }
        return true;
    }

    /**
     * Disconnects {@link BlueManager} from {@link BlueScanner} service.
     */
    private void disconnect() {
        if (connected) {
            connected = false;
            mContext.unbindService(serviceConnection);
        }
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (blueScannerServiceConnection != null) {
                BlueScanner blueScanner = ((BlueScanner.LocalBinder) iBinder).getService();
                blueScannerServiceConnection.onConnected(blueScanner);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (blueScannerServiceConnection != null) {
                blueScannerServiceConnection.onDisconnected();
            }
        }
    };


    public boolean checkLocationIsOn(Context context){
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled=false;
        try {
            if (lm != null) {
                gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }else{
                return false;
            }
        } catch(Exception ex) {
            return false;
        }
        if(gpsEnabled){
            return true;
        }else{
            Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(myIntent);
            return false;
        }
    }


    public boolean checkBluetooth(Context context){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            return false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable :)
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivity(enableBtIntent);
                return false;
            }else{
                return true;
            }
        }
    }
}

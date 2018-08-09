package pl.tomek_krzyszko.bluemanager;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;

import pl.tomek_krzyszko.bluemanager.dagger.ApplicationScope;
import pl.tomek_krzyszko.bluemanager.dagger.components.BlueManagerComponent;
import pl.tomek_krzyszko.bluemanager.exception.BlueManagerExceptions;
import pl.tomek_krzyszko.bluemanager.scanner.BlueScanner;
import timber.log.Timber;

/**
 * Main manager class used to control scanning and managing scan results.
 */
public class BlueManager implements ServiceConnection{

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
    public void setBlueScannerServiceConnection(BlueScannerServiceConnection blueScannerServiceConnection) {
        this.blueScannerServiceConnection = blueScannerServiceConnection;
    }

    /**
     * Starts {@link BlueScanner} service. If the service is started already, it does nothing.
     */
    public void startService() throws BlueManagerExceptions {
        if(mContext!=null) {
            mContext.startService(getServiceIntent());
        }else{
            throw new BlueManagerExceptions("First initilize BlueManager.");
        }
    }

    /**
     * Stops {@link BlueScanner} service.
     */
    public void stopService() {
        mContext.stopService(getServiceIntent());
    }

    /**
     * Connects {@link BlueManager} to {@link BlueScanner} service.
     * If the service hasn't been started before, it is started now.
     *
     * @param blueScannerServiceConnection {@link BlueScannerServiceConnection} used to notify about service connection status.
     * @return whether or not you have successfully bound the service
     */
    public boolean connect(BlueScannerServiceConnection blueScannerServiceConnection) {
        if (!connected) {
            setBlueScannerServiceConnection(blueScannerServiceConnection);
            connected = mContext.bindService(getServiceIntent(), this, Context.BIND_ADJUST_WITH_ACTIVITY);
            return connected;
        }
        return true;
    }

    /**
     * Disconnects {@link BlueManager} from {@link BlueScanner} service.
     */
    public void disconnect() {
        if (connected) {
            connected = false;
            mContext.unbindService(this);
        }
    }


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
}

package pl.tomek_krzyszko.bluemanager.dagger.components;

import android.app.Activity;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;
import pl.tomek_krzyszko.bluemanager.BlueConfig;
import pl.tomek_krzyszko.bluemanager.dagger.modules.BlueManagerModule;
import pl.tomek_krzyszko.bluemanager.dagger.modules.ScannerModule;
import pl.tomek_krzyszko.bluemanager.dagger.modules.TaskModule;

@Singleton
@Component(modules = BlueManagerModule.class)
public interface BlueManagerComponent {
    void inject(Activity activity);

    Context getApplicationContext();
    BlueConfig getBlueConfig();
    ScannerComponent module(ScannerModule scannerModule);
    TaskComponent module(TaskModule scannerModule);

}

package pl.tomekkrzyszko.bluemanager;

import android.app.Application;
import android.content.Context;

import pl.tomek_krzyszko.bluemanager.BlueConfig;
import pl.tomekkrzyszko.bluemanager.dagger.component.AppComponent;
import pl.tomekkrzyszko.bluemanager.dagger.component.DaggerAppComponent;
import pl.tomekkrzyszko.bluemanager.dagger.module.AppModule;


public class BlueManagerApplication extends Application {
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();


        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        appComponent.blueManager().initilize(this,new BlueConfig());
    }

    public static AppComponent getComponent(Context context) {
        return ((BlueManagerApplication) context.getApplicationContext()).appComponent;
    }

    public static BlueManagerApplication get(Context context) {
        return (BlueManagerApplication) context.getApplicationContext();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

}
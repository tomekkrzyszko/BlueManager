package pl.tomekkrzyszko.bluemanager;

import android.app.Application;
import android.content.Context;

import dagger.component.AppComponent;
import dagger.component.DaggerAppComponent;
import dagger.module.AppModule;


public class BlueManagerApplication extends Application {
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();


        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();


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
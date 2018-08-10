package pl.tomekkrzyszko.bluemanager.dagger.module;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import dagger.Module;
import dagger.Provides;
import pl.tomek_krzyszko.bluemanager.BlueManager;
import pl.tomekkrzyszko.bluemanager.dagger.scope.PerApplication;

@Module
public class AppModule {
    Application mApplication;

    public AppModule(Application application) {
        mApplication = application;
    }

    @Provides
    @PerApplication
    Application providesApplication() {
        return mApplication;
    }

    @Provides
    @PerApplication
    Context provideContext() {
        return mApplication.getApplicationContext();
    }

    @PerApplication
    @Provides
    SharedPreferences providesSharedPreferences(Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @PerApplication
    @Provides
    BlueManager provideBlueManager(){
        return BlueManager.getInstance();
    }
}

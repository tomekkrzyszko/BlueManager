package dagger.module;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import dagger.Module;
import dagger.scope.PerApplication;
import dagger.Provides;

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
}

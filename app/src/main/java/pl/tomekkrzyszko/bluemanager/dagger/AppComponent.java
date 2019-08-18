package pl.tomekkrzyszko.bluemanager.dagger;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;
import pl.tomekkrzyszko.bluemanager.BlueManagerApplication;
@Singleton
@Component(
        modules = {
                AndroidSupportInjectionModule.class,
                AppModule.class,
                BuildersModule.class,
        })
public interface AppComponent{
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(BlueManagerApplication application);
        AppComponent build();
    }
    void inject(BlueManagerApplication app);
}
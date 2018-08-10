package pl.tomekkrzyszko.bluemanager.dagger.component;

import android.app.Application;
import android.content.Context;

import pl.tomek_krzyszko.bluemanager.BlueManager;
import pl.tomekkrzyszko.bluemanager.dagger.module.AppModule;
import dagger.Component;
import pl.tomekkrzyszko.bluemanager.dagger.module.MainModule;
import pl.tomekkrzyszko.bluemanager.dagger.scope.PerApplication;

@PerApplication
@Component(modules = {AppModule.class}
)
public interface AppComponent {
    Application application();
    Context context();
    BlueManager blueManager();
    MainComponent module(MainModule splashModule);
}

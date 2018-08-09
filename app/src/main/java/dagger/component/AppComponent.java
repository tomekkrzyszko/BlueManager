package dagger.component;

import android.app.Application;
import android.content.Context;

import dagger.module.AppModule;
import dagger.Component;
import dagger.module.MainModule;
import dagger.scope.PerApplication;

@PerApplication
@Component(modules = {AppModule.class}
)
public interface AppComponent {
    Application application();
    Context context();
    MainComponent module(MainModule splashModule);
}

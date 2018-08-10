package pl.tomekkrzyszko.bluemanager.dagger.component;

import dagger.Subcomponent;
import pl.tomekkrzyszko.bluemanager.dagger.module.MainModule;
import pl.tomekkrzyszko.bluemanager.dagger.scope.PerActivity;
import pl.tomekkrzyszko.bluemanager.MainActivity;


@PerActivity
@Subcomponent(
        modules = {MainModule.class})
public interface MainComponent {
    void inject(MainActivity splashActivity);
}

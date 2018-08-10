package pl.tomek_krzyszko.bluemanager.dagger.components;

import dagger.Component;
import pl.tomek_krzyszko.bluemanager.dagger.modules.BlueManagerModule;
import pl.tomek_krzyszko.bluemanager.dagger.modules.ScannerModule;
import pl.tomek_krzyszko.bluemanager.dagger.modules.TaskModule;
import pl.tomek_krzyszko.bluemanager.dagger.scopes.ApplicationScope;

@ApplicationScope
@Component(modules = BlueManagerModule.class)
public interface BlueManagerComponent {
    ScannerComponent module(ScannerModule scannerModule);
    TaskComponent module(TaskModule scannerModule);
}

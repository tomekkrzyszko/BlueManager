package pl.tomek_krzyszko.bluemanager.dagger.components;

import dagger.Subcomponent;
import pl.tomek_krzyszko.bluemanager.dagger.modules.TaskModule;
import pl.tomek_krzyszko.bluemanager.dagger.scopes.InstanceScope;
import pl.tomek_krzyszko.bluemanager.scanner.BlueScannerTask;

@InstanceScope
@Subcomponent(modules = {TaskModule.class})
public interface TaskComponent {
    void inject(BlueScannerTask blueScannerTask);

}

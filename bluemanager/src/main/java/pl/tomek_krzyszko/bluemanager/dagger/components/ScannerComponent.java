package pl.tomek_krzyszko.bluemanager.dagger.components;

import dagger.Subcomponent;
import pl.tomek_krzyszko.bluemanager.dagger.modules.ScannerModule;
import pl.tomek_krzyszko.bluemanager.dagger.scopes.InstanceScope;
import pl.tomek_krzyszko.bluemanager.scanner.BlueScanner;

@InstanceScope
@Subcomponent(modules = ScannerModule.class)
public interface ScannerComponent {
    void inject(BlueScanner blueScanner);

}

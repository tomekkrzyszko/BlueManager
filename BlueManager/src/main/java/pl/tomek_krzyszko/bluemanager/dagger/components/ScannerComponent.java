package pl.tomek_krzyszko.bluemanager.dagger.components;

import dagger.Subcomponent;
import pl.tomek_krzyszko.bluemanager.dagger.modules.ScannerModule;
import pl.tomek_krzyszko.bluemanager.dagger.scopes.ApplicationScope;

@ApplicationScope
@Subcomponent(modules = {ScannerModule.class})
public interface ScannerComponent {

}

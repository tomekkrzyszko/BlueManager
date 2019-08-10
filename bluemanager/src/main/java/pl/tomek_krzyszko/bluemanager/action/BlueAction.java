package pl.tomek_krzyszko.bluemanager.action;

import java.io.Serializable;
import java.util.UUID;

/**
 * Abstract class which implements how all future action in library should look like and works.
 */
public abstract class BlueAction implements Serializable {

    /**
     * Enum type with all available actions
     */
    public enum ActionType {
        READ, WRITE, NOTIFY
    }

    protected UUID service;
    protected UUID characteristic;

    public BlueAction(UUID service, UUID characteristic) {
        this.service = service;
        this.characteristic = characteristic;
    }

    public BlueAction() {
    }

    public UUID getService() {
        return service;
    }

    public void setService(UUID service) {
        this.service = service;
    }

    public UUID getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(UUID characteristic) {
        this.characteristic = characteristic;
    }

    public abstract ActionType getActionType();

}

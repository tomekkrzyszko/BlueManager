package pl.tomek_krzyszko.bluemanager.action;

import java.util.UUID;

public class WriteAction extends BlueAction {

    protected byte[] value;

    public WriteAction(UUID service, UUID characteristic, byte[] value) {
        super(service, characteristic);
        this.value = value;
    }

    public WriteAction(byte[] value) {
        super();
        this.value = value;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.WRITE;
    }
}

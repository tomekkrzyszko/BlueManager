package pl.tomek_krzyszko.bluemanager.action;

import java.util.UUID;

/**
 * Specific class which implements Write Action
 */
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

    /**
     * Specific class which implements Notify Action
     *
     *@return byte[] containing value of the characteristic
     */
    public byte[] getValue() {
        return value;
    }


    /**
     * Specific class which implements Notify Action
     *
     *@param value of the characteristic
     */
    public void setValue(byte[] value) {
        this.value = value;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.WRITE;
    }
}

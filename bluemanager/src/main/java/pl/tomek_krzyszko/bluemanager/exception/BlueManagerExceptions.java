package pl.tomek_krzyszko.bluemanager.exception;

/**
 * Wrapper class over {@link Exception} class
 * It handles all specific exceptions from the {@link pl.tomek_krzyszko.bluemanager.BlueManager} library
 */
public class BlueManagerExceptions extends Exception {
    public BlueManagerExceptions(String message) {
        super(message);
    }
}

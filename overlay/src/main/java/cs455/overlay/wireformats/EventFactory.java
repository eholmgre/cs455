package cs455.overlay.wireformats;

public class EventFactory {
    private static EventFactory ourInstance = new EventFactory();

    public static EventFactory getInstance() {
        return ourInstance;
    }

    private EventFactory() {
    }
}

package cs455.overlay.events;

public class EventFactory {
    // Comrades, this is _our_ instance!
    private static EventFactory ourInstance = new EventFactory();

    public static EventFactory getInstance() {
        return ourInstance;
    }

    private EventFactory() {
        // We must seize the means of EventFactory production!
    }

    /*
    public Event createEvent(byte[] msg) {
        Event message;

        return message;
    }
    */
}

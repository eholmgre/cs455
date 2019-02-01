package cs455.overlay.node;

import cs455.overlay.events.Event;

public interface Node {
    void onEvent(Event event);
}

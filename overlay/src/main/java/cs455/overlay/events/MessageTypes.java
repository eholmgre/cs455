package cs455.overlay.events;

public enum MessageTypes {
    REGISTER_REQUEST, REGISTER_RESPONSE, DEREGISTER_REQUEST, DEREGISTER_RESPONSE,
    MESSAGING_NODES_LIST, LINK_WEIGHTS, TASK_INITIATE, TASK_COMPLETE,
    PULL_TRAFFIC_SUMMARY, TRAFFIC_SUMMARY, INCOMMING_CONNECTION
}
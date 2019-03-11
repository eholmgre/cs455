Erik Holmgren
cs455 - overlay

Files:
    events/
        DeregisterRequest - message class for requesting to deregister.
        DeregisterResponse - message class for responding to request to deregister.
        Event - interface for message classes
        EventFactory - Singleton class for constructing events from bytes received over socket.
        LinkWeights - message class for sending overlay weights
        MessageTypes - enum describing each message class
        MessagingNodeHandshake - message class for nodes to establish themselves with eachother.
        MessagingNodesList - message class to instruct message nodes to establish connections with eachother
        PullTrafficSummaries - message class to request traffic summaries
        RegisterRequest - message class for asking to register at the registery
        RegisterResponse - message class for responding to a register request
        TaskComplete - message class for informing the registey you've completed the rounds
        TaskIniteate - message class to tell the message nodes to start
        TaskMessage - message class for the communication between messaging nodes during a round
        TrafficRummary - message class for a report of the stats for a task
    node/
        MessagingNode - class for the messaging node, contains main routine for messageing node
        Node - interface for nodes
        Registry - class for regsitry node, contains main routine for regsitry
    routing/
        SubOverlay - class for data strucutre messageing nodes use to keep track of the overlay and find shortest paths
    transport/
        ConnectionManager - class for data structure nodes use to manage sockets and receiver threads communicating with other nodes
        TCPRecieverThread - class describing a threaded routing for receiving tcp traffic from a socket
        TCPSender - class that handles outputing data over a socket
        TCPServerThread - class that describes a threaded routing for accepting new tcp connections
    util/
        Overlay - class for data strucure Registry uses to keep track of the overlay

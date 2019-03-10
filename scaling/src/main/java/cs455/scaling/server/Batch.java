package cs455.scaling.server;

import java.util.LinkedList;

public class Batch implements Task {

    LinkedList<Task> stuffToDo;



    @Override
    public void run() {
        for (Task t : stuffToDo) {
            t.run();
        }

    }
}

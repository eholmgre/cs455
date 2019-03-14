Erik Holmgren
CS455 - HW2-PC
2019-03-14

Files:
	src/main/java/cs455/scaling
	| - client/
		| - Client.java: Code for the client node, including main to run the client program
							Sends messages to server and checks that it receives their hashes
							
		| - ClientStats.java: Class to track and display statistics on the client's side.
							Utilizes a timer to print stats every 20 seconds.
							
	| - server/
		| - Server.java: Code for the server node, including main to run the server program
							Spawns a threadpool to handle NIO events, such as accepting connections, and
							sending and recieving data from clients
							
		| - ServerStats.java: Tracks individual client data processed by the Server
							Tracks connected and disconected channels to handle problems caused by
							accepting connections in the ThreadPool, also tracks sent and received
							messages for each client. Utilizes a timer to print stats every 20 seconds.
							
		| - Task.java: Interface for events that will be processed in the ThreadPool. Contains one method,
							run, which is called by a worker thread to handle the event. Note that all
							implementing classes are anonymous, and declared in Server.java.
							
		| - ThreadPool.java: Class to handle the threadpool, all operations and subclasses are abstracted
							via methods on the threadpool. Spawns worker threads when the start method is called.
							Worker threads wait on a blocking queue of Batch objects, which are a wrapper around
							a linked list of tasks. Batches are assembled by an instance of the Batcher class,
							which adds new tasks to the current batch and decides when to hand the batch off to
							the queue for the worker threads. These tasks are handed to the Batcher by an outside
							object via the add method of the threadpool.
							
		| - util/
			| - Hasher.java - contains static method for hashing a supplied byte array.
			
			| - MessageMaker.java - contains a static method to return a new array of random bytes.
			

Instructions:
	0. Build with gradle build
	1. Start server with the command line arguments specified in the instructions. My implementation tends to do better
		with smaller batch sizes (50 or 100 as opposed to 1000)
	2. Start clients on other machines with command line arguments specified in the instructions.
	3. Proffit
	
	I'm including the start and close script I used to test my implementation, run it on the desired server machine to
		start the server and spawn up n clients on each machine in the machine lists file like ./start.sh n
        ctrl-c ing the server terminal should kill all clients, but i've included the close.sh script that kills all user
        java processes on machines in the machine_list file
	
Sorry about all the anonymous classes

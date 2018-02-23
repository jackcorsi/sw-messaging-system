My messaging system is implemented from scratch using an alternative class structure

#Thread Structure

##Client Side

Each client has three primary threads running at any one time:

###SenderReceiver thread ( _SenderReceiver_ class)
This thread is accessed through a _SenderReceiver_ object and is responsible for receiving data from its equivalent SenderReceiver object on the server side. Hence there is a one-to-one relationship between one server side SenderReceiver and the client's single SenderReceiver.

###Terminal reading thread ( _ClientTerminalInputReader_ class)
This is a very simple thread with the role of continuously and concurrently taking in text input from the command line to be read by the main thread.

###Main thread 
The main thread on the client side alternates between two continuous loops depending on whether the user is logged in yet or not, contained in the methods _mainLoop_ and _loginLoop_

####Login loop
This loop accepts either a *login* or *register* command from the user, and then appropriately attempts to establish a connection to the server and join the messaging system using the supplied username (see notes on __communication protocol__). The loop will restart if the login attempt is rejected by the server, or the client fails to establish a connection the the server. During normal execution, and assuming the user does not quit, the loop will only terminate once the client receives confirmation of a successful login from the server, at which point control is transfered to the main loop

####Main loop
This loop runs continuously whilst the user is logged into the server. It operates in two main parts - first polling for input from the user and handling this by sending the appropriate command to the server, then polling for data from the server received via the SenderReceiver thread. Information received from the server can come in two formats (see __Communication Protocol__) : either messages or events. Messages received from the server will be immediately written to the console, while events will usually result in a specific message being printed or the loop exiting such that the user logs out. In contrast to other approaches to this system where all user input on the client side is simply forwarded to the server, this implementation has processing done on the client side, whereby commands from the user are read by a switch statement resulting in a specific message being sent to the server, and conversely information received from the is interpreted depending on its value rather than simply printed to the console.


##Server Side

On the server side, there are three main types of running thread, operating on data accessible through two main ArrayLists of _ServerUser_ objects which contain, respectively, active (online) users and inactive (offline) users. Active users are defined as having at least one instance of the Client program connected to the server and logged in as that user, while users which do not have any active logged in devices are "inactive"

###Client acceptor thread
This thread is contained within the _ServerClientAcceptor_ class, and runs continuously accepting new incoming connections from clients. On the server side, this thread is the producer of _SenderReceiver_ objects, creating a new one for each new client instance that attempts to make a connection to the server. Once a connection is accepted through the socket, the thread will interpret whether the user is attempting to register as a new user or login as an existing one. If the request is unsuccessful, which may be because the user attempted to register as a user that already exists or log in as one which does not, the thread will send a rejection notification back to the client before closing the connection and restarting its loop ready for the next incoming request. If the user successfully registers as a new client, the thread will generate a new _ServerUser_ object to represent them. In all successful login or registration cases, the SenderReceiver object used to communicate with the running client is passed to the relevant ServerUser object as its new owner, and this ServerUser may be moved into the _active_ ArrayList if they are inactive.

###SenderReceiver threads
As with the client side, all network communication is done through _SenderReceiver_ objects, each of which has its own thread to receive information from its remote counterpart. On the server side, _ServerUser_ objects have a one-to-many relationship with SenderReceivers, as they own one for each logged in instance of a user on a device.


###Main thread
The main server thread runs from the _Server_ and is responsible for receiving input from all logged in clients and responding to them, as well as passing incoming messages on to the recipient client. The thread iterates through all users in the active users ArrayList. If a user has become inactive, they are moved into the inactive list during this loop. For each user, the thread runs its _process_ method which is responsible for reading incoming messages from the client. The process method iterates through all of the devices on which the user is signed in, checking their respective SenderReceivers for incoming messages or commands. If the device has requested a message using the navigation commands, the relevant message is sent to it via its SenderReceiver, or an event indicating that the message does not exist. If the device has attempted to send a message, the message is returned from the process method as a _ServerIncomingMessage_ object to the original loop. The main server loop will then search for a matching recipient for the the incoming message, using the object's _sendRejection_ method to inform the client if their message has not been delivered. Upon finding the correct user to send the message to, the main loop will call its _sendMessage_ method. This method adds the message to the user's internal list of message history, and for all logged in devices will either immediately push the message (if they are currently displaying the latest message) or simply a notification that a new message has been received (if they are browsing through their history and should not be interrupted by a new message)


#Communication Protocol

As previously discussed, all communication between the server and clients is done through SenderReceiver objects which are threaded wrappers around a socket connection. The object continually listens for new messages from the socket and adds them to an internal blocking queue from which they can be read by the SenderReceiver's owner. I originally chose to employ this architecture as I expected that it would be easier to execute the majority of server and client code in a single-threaded manner in order to avoid concurrency errors caused by race conditions and deadlocks, the solutions to which would inevitably slow down code by enforcing blocking and waiting for other threads to finish accessing information. 

As in the sample solution, information is read through the sockets as lines of text. Originally, each line of text was individually added to the SenderReceiver's queue, however this meant that if multiple lines were needed to perform an operation such as sending a message, the code receiving the information would need to either wait for multiple lines to be received, or have some internal state saving the progress of a partially completed operation to be finished on another iteration of a polling loop. This resulted in code that was unnecessarily complex and defeated the purpose of encapsulating the receiving of data from a machine into a single object and thread. My solution to this was to devise a system in which multiple lines of text from the input stream were interpreted as a single unit, like multiple bytes to a network packet. When sending information, the sender sends multiple lines of text followed by a terminating character indicating that the lines should be grouped. I eventually settled on 2 lines as being the greatest number of lines that a unit would need, however the SenderReceiver code is expandable to allow more lines. The SenderReceiver thread waits until all lines of a message have been delivered through the input stream, and then adds them to the queue as an array of Strings rather than a single string, such that code polling for new information from the connection will receive them all as a block representing a single request.

Once this was implemented, it followed that the meaning of received messages could be inferred by the number of lines they contained. A single line sent from server to client represents an event that the client should inform the user of and potentially act upon, and consists of a single character indicating which event it represents. A single line sent from client to server represents a command or request for information such as a next or previous message. Two lines sent between server and client represents a message with a sender and receiver, and is interpreted as such.

All of the single line commands and events used by the system are defined as single characters within the _SharedConst_ class:

###EVENT_MSG_NOTIFICATION 
Sent from the main server thread to a logged in device when they have received a new message but are currently on their most recent message. Causes the client to simply output a notification that a new message is available, rather than confusingly outputting the new message amongst their message history.
###EVENT_INVALID_MSG
Sent from the main server thread to reject messages which could not be delivered to their recipient. Also used for rejecting login attempts
###EVENT_KICKED
Sent to inform the client that it has been disconnected due to illegal behaviour
###EVENT_NO_NEXT_MSG, EVENT_NO_PREVIOUS_MSG, EVENT_NO_MSGS
Indicates that the client has reached the end of their message history or that there are no messages available to respond to their request
###CONNECT_LOGIN_STRING, CONNECT_REGISTER_STRING
Send from the client login loop to to the client acceptor thread, indicating whether the client wishes to login or register
###TAKEN_USERNAME_STRING
Response to a registration attempt as a username that has already been taken
###CONNECT_ACCEPT_STRING
Sent from the client acceptor thread to indicate a successful login. This message causes the client to proceed from the login loop to the main loop
###COMMAND_PREVIOUS, COMMAND_NEXT, COMMAND_DELETE, COMMAND_LATEST, COMMAND_LOGOUT, COMMAND_NEW
Commands for navigating through the user's message history. Message history is stored within each ServerUser object as a doubly linked list of messages. For each logged in device, a pointer is stored to the message that they are currently on. A delete operation removes the message from the list but keeps the same place in it such that the user must then use next or previous depending on which direction they would like to go in. "Latest" is a shortcut to the most recent message in the user's list, and is equivalent to using "next" until the end is reached. "new" causes the server to start from the most recent message and trace back through the first set of consecutive unread messages in the history, stopping once a read message is encountered and pushing all of these messages to the client in chronological order. This was implemented as a way for a user to immediately view all missed messages upon logging in if they wish, and also as another tool to avoid missed messages due to my choice to not push messages to a device which is not at the "head" of the message list. 
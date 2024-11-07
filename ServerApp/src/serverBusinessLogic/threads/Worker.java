package serverBusinessLogic.threads;

import application.Server;
import dataAccess.ServerFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import logicalExceptions.MaxThreadsErrorException;
import logicalExceptions.ServerErrorException;
import logicalExceptions.SignInErrorException;
import logicalExceptions.UserExistErrorException;
import logicalExceptions.UserNotActiveException;
import logicalModel.message.Message;
import logicalModel.message.MessageType;
import logicalModel.model.User;

/**
 * Worker is a thread responsible for handling client-server interactions in the
 * server's business logic layer. It listens to client requests, processes them,
 * and sends back responses according to request type. This class uses an
 * instance of Socket to manage communication with clients, and it handles
 * exceptions relevant to user authentication and server response errors.
 *
 * @author Irati, Elbire, Meylin, Olaia
 */
public class Worker extends Thread {

    private final Socket clientSocket;
    private ObjectInputStream read;
    private ObjectOutputStream write;
    private Logger logger = Logger.getLogger(Worker.class.getName());

    /**
     * Constructs a new Worker with a specified client socket.
     *
     * @param clientSocket the socket connected to the client
     */
    public Worker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * Runs the worker thread, initializing input/output streams and handling
     * requests received from the client socket. This method reads a request
     * message, processes it through, and sends an appropriate response back to
     * the client.
     */
    @Override
    public void run() {
        try {
            //Thread.sleep(60000);
            read = new ObjectInputStream(clientSocket.getInputStream());
            write = new ObjectOutputStream(clientSocket.getOutputStream());
            // Read the customer's message
            Message request = (Message) read.readObject();

            Message response = handleRequest(request);

            //Send the response to the client
            write.writeObject(response);
            logger.info("Response sent to client.");

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Client handling error: {0}", e.getMessage());
        //} catch (InterruptedException ex) {
            //Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                read.close();
                write.close();
                clientSocket.close();
                Server.closeWorker();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles incoming client requests and returns a response message. Based on
     * the message type, it processes the request for sign-in or sign-up, or
     * catches any custom exception types related to authentication and server
     * errors.
     *
     * @param request the message received from the client
     * @return a response message containing the result of the request
     * processing
     */
    private Message handleRequest(Message request) {
        Message response = null;
        try {
            User responseUser = null;
            if (request.getMessage().equals(MessageType.SIGN_IN_REQUEST)) {
                responseUser = ServerFactory.getSignable().signIn(request.getUser());
                response = new Message(responseUser, MessageType.OK_RESPONSE);
            } else if (request.getMessage().equals(MessageType.SIGN_UP_REQUEST)) {
                responseUser = ServerFactory.getSignable().signUp(request.getUser());
                response = new Message(responseUser, MessageType.OK_RESPONSE);
            }
        } catch (UserNotActiveException e) {
            response = new Message(null, MessageType.USER_NOT_ACTIVE);
        } catch (SignInErrorException e) {
            response = new Message(null, MessageType.SIGN_IN_ERROR);
        } catch (UserExistErrorException e) {
            response = new Message(null, MessageType.USER_EXISTS_ERROR);
        } catch (ServerErrorException e) {
            response = new Message(null, MessageType.SERVER_ERROR);
        } catch (MaxThreadsErrorException ex) {
            response = new Message(null, MessageType.MAX_THREADS_ERROR);
        }
        return response;
    }
}

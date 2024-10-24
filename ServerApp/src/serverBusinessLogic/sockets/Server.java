package serverBusinessLogic.sockets;

import dataAccess.DataAccessObject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import logicalExceptions.MaxThreadsErrorException;
import logicalExceptions.ServerErrorException;
import logicalExceptions.SignInErrorException;
import logicalExceptions.UserExistErrorException;
import logicalExceptions.UserNotActiveException;
import logicalModel.message.Message;
import logicalModel.message.MessageType;
import logicalModel.model.User;

/**
 *
 * @author 2dam
 */
public class Server {

    public static void sendRecieveMessage(Message request) throws MaxThreadsErrorException, ServerErrorException, SignInErrorException, IOException {
        ServerSocket serverSocket = null;
        Socket socket = null;
        ObjectInputStream read = null;
        ObjectOutputStream write = null;
        Message response = null;

        try {
            //Pillar las cosas del archivo de configuracion
            ResourceBundle configFile = ResourceBundle.getBundle("config.config");
            int port = Integer.parseInt(configFile.getString("PORT"));
            serverSocket = new ServerSocket(port);

            socket = serverSocket.accept();
            write = new ObjectOutputStream(socket.getOutputStream());
            read = new ObjectInputStream(socket.getInputStream());
            User responseUser = null;
            DataAccessObject dao = new DataAccessObject();
            if (request.getMessage().compareTo(MessageType.SIGN_IN_REQUEST) == 0) {
                try {
                    responseUser = dao.signIn(request.getUser());
                } catch (UserNotActiveException e) {
                    Message m = new Message(responseUser, MessageType.USER_NOT_ACTIVE);
                    write.writeObject(m);
                } catch (SignInErrorException e) {
                    Message m = new Message(responseUser, MessageType.SIGN_IN_ERROR);
                    write.writeObject(m);
                } catch (MaxThreadsErrorException e) {
                    Message m = new Message(responseUser, MessageType.MAX_THREADS_ERROR);
                    write.writeObject(m);
                } catch (ServerErrorException e) {
                    Message m = new Message(responseUser, MessageType.SERVER_ERROR);
                    write.writeObject(m);
                }
                Message m = new Message(responseUser, MessageType.OK_RESPONSE);
                write.writeObject(m);
            } else {
                try {
                    responseUser = dao.signUp(request.getUser());
                } catch (ServerErrorException e) {
                    Message m = new Message(responseUser, MessageType.SERVER_ERROR);
                    write.writeObject(m);
                } catch (UserExistErrorException e) {
                    Message m = new Message(responseUser, MessageType.USER_EXISTS_ERROR);
                    write.writeObject(m);
                }
                Message m = new Message(responseUser, MessageType.OK_RESPONSE);
                write.writeObject(m);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
                if (socket != null) {
                    socket.close();
                }
                if (read != null) {
                    read.close();
                }
                if (write != null) {
                    write.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Fin servidor");
        }
    }

}

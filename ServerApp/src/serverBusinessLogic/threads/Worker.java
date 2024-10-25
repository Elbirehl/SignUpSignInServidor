/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverBusinessLogic.threads;

import dataAccess.DataAccessObject;
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
 *
 * @author 2dam
 */
public class Worker extends Thread {
    private final Socket clientSocket;
    private Logger logger = Logger.getLogger(Worker.class.getName());
    public Worker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (ObjectInputStream read = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream write = new ObjectOutputStream(clientSocket.getOutputStream())) {

            // Leer el mensaje del cliente
            Message request = (Message) read.readObject();
            DataAccessObject dao = new DataAccessObject();
            Message response = handleRequest(request, dao);

            // Enviar la respuesta al cliente
            write.writeObject(response);
            logger.info("Respuesta enviada al cliente.");

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error en el manejo del cliente: {0}", e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Message handleRequest(Message request, DataAccessObject dao) {
        Message response = null;
        try {
            User responseUser = null;
            if (request.getMessage().equals(MessageType.SIGN_IN_REQUEST)) {
                responseUser = dao.signIn(request.getUser());
                response = new Message(responseUser, MessageType.OK_RESPONSE);
            } else if (request.getMessage().equals(MessageType.SIGN_UP_REQUEST)) {
                responseUser = dao.signUp(request.getUser());
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
            response = new Message(null, MessageType.MAX_THREADS_ERROR);        }
        return response;
    }
}

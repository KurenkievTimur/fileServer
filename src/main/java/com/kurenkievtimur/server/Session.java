package com.kurenkievtimur.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Session implements Runnable {
    private final Socket socket;

    public Session(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            String[] params = input.readUTF().split(" ");
            switch (params[0]) {
                case "GET" -> Server.handleGetRequest(output, params);
                case "PUT" -> Server.handlePutRequest(input, output, params);
                case "DELETE" -> Server.handleDeleteRequest(output, params);
                case "exit" -> Server.disabled();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

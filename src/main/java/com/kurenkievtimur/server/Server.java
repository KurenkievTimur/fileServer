package com.kurenkievtimur.server;

import com.kurenkievtimur.common.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final static String ADDRESS = "127.0.0.1";
    private static final int PORT = 23456;
    private final static ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
    private static ServerSocket serverSocket = null;
    public static boolean isExit = false;

    public static void main(String[] args) throws IOException {
        System.out.println("Server started!");
        serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS));
        while (!isExit) {
            EXECUTOR.execute(new Session(serverSocket.accept()));
        }
    }

    public synchronized static void handlePutRequest(DataInputStream input, DataOutputStream output, String[] params) throws IOException {
        String name = "";
        if (params.length == 2) {
            name = "%s.%s".formatted(UUID.randomUUID(), params[1]);
        } else {
            name = params[2];
        }

        int length = input.readInt();
        byte[] message = new byte[length];
        File file = new File(Utils.getServerFilePath(name));

        if (file.exists()) {
            output.writeUTF("403");
        } else {
            ServerFileMap serverFileMap = ServerFileMap.getServerFileMap();

            input.readFully(message, 0, message.length);
            Files.write(file.toPath(), message);

            int id = serverFileMap.put(name);
            output.writeUTF("%s %d".formatted("200", id));
        }
    }

    public synchronized static void handleGetRequest(DataOutputStream output, String[] params) throws IOException {
        String choice = params[1];
        String userInput = params[2];

        ServerFileMap serverFileMap = ServerFileMap.getServerFileMap();

        File file = new File(Utils.getServerFilePath(userInput));
        if (choice.equals("BY_ID")) {
            String name = serverFileMap.get(Integer.parseInt(userInput));
            file = new File(Utils.getServerFilePath(name));
        }

        output.writeUTF(file.exists() ? "200" : "404");

        byte[] message = Files.readAllBytes(file.toPath());

        output.writeInt(message.length);
        output.write(message);
    }

    public synchronized static void handleDeleteRequest(DataOutputStream output, String[] params) throws IOException {
        String choice = params[1];
        String userInput = params[2];

        ServerFileMap serverFileMap = ServerFileMap.getServerFileMap();

        File file = new File(Utils.getServerFilePath(userInput));
        if (choice.equals("BY_ID")) {
            String name = serverFileMap.get(Integer.parseInt(userInput));
            file = new File(Utils.getServerFilePath(name));
        }

        if (file.delete()) {
            serverFileMap.delete(file.getName());
            output.writeUTF("200");
        } else {
            output.writeUTF("404");
        }
    }

    public synchronized static void disabled() throws IOException {
        EXECUTOR.shutdown();
        isExit = true;
        ServerFileMap fileMap = ServerFileMap.getServerFileMap();
        Utils.serialize(fileMap, Utils.getServerFilePath("file_map.data"));
        serverSocket.close();
    }
}

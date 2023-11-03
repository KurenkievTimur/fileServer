package com.kurenkievtimur.client;

import com.kurenkievtimur.common.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 23456;

    public static void main(String[] args) throws IOException {
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                Scanner scanner = new Scanner(System.in);
        ) {
            System.out.print("Enter action (1 - get a file, 2 - save a file, 3 - delete a file): ");
            String action = scanner.nextLine();
            switch (action) {
                case "1" -> handleGetRequest(scanner, input, output);
                case "2" -> handlePutRequest(scanner, input, output);
                case "3" -> handleDeleteRequest(scanner, input, output);
                case "exit" -> handleExitRequest(output);
            }
        }
    }

    public static void handlePutRequest(Scanner scanner, DataInputStream input, DataOutputStream output) throws IOException {
        System.out.print("Enter name of the file: ");
        String fileName = scanner.nextLine();

        File file = new File(Utils.getClientFilePath(fileName));

        if (!file.exists()) {
            System.out.println("File not found!");
            return;
        }

        byte[] bytes = Files.readAllBytes(file.toPath());

        System.out.print("Enter name of the file to be saved on server: ");
        String saveFile = scanner.nextLine();

        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);

        output.writeUTF("%s %s %s".formatted("PUT", fileExtension, saveFile));
        System.out.println("The request was sent.");

        output.writeInt(bytes.length);
        output.write(bytes);

        String[] response = input.readUTF().split(" ");
        if (response[0].equals("403")) {
            System.out.println("The response says that file with name already exists!");
        } else {
            System.out.printf("Response says that file is saved! ID = %s\n", response[1]);
        }
    }

    public static void handleGetRequest(Scanner scanner, DataInputStream input, DataOutputStream output) throws IOException {
        System.out.print("Do you want to get the file by name or by id (1 - name, 2 - id): ");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            System.out.print("Enter name: ");
        } else {
            System.out.print("Enter id: ");
        }
        String userInput = scanner.nextLine();

        output.writeUTF("%s %s %s".formatted("GET", choice.equals("1") ? "BY_NAME" : "BY_ID", userInput));
        System.out.println("The request was sent.");

        String code = input.readUTF();

        if (code.equals("200")) {
            int length = input.readInt();
            byte[] message = new byte[length];

            input.readFully(message, 0, length);

            System.out.print("The file was downloaded! Specify a name for it: ");
            String fileName = scanner.nextLine();

            File file = new File(Utils.getClientFilePath(fileName));
            Files.write(file.toPath(), message);

            System.out.println("File saved on the hard drive!");
        } else {
            System.out.println("The response says that this file is not found!");
        }
    }

    public static void handleDeleteRequest(Scanner scanner, DataInputStream input, DataOutputStream output) throws IOException {
        System.out.print("Do you want to delete the file by name or by id (1 - name, 2 - id): ");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            System.out.print("Enter name: ");
        } else {
            System.out.print("Enter id: ");
        }
        String userInput = scanner.nextLine();

        output.writeUTF("%s %s %s".formatted("DELETE", choice.equals("1") ? "BY_NAME" : "BY_ID", userInput));

        String response = input.readUTF();
        if (response.equals("200")) {
            System.out.println("The response says that this file was deleted successfully!");
        } else {
            System.out.println("The response says that this file is not found!");
        }
    }

    public static void handleExitRequest(DataOutputStream output) throws IOException {
        output.writeUTF("exit");
        System.out.println("The request was sent.");
    }
}

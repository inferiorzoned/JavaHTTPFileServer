// package app;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Client
 */
public class Client implements Runnable{
    static int BUFFER = HTTPServerSkeleton.BUFFER_SIZE;
    static int PORT = HTTPServerSkeleton.PORT;
    private Socket socket; 
    private String fileName;
    // Scanner scn;

    public Client(Socket s, String f) {
        socket = s;
        fileName = f;
    }


    public static void main(String[] args) throws UnknownHostException, IOException {
        // Socket socket = new Socket("localhost", PORT);
        System.out.println("server connection established");
        Scanner scn = new Scanner(System.in);

        System.out.println("How many files?");
        int n = Integer.parseInt(scn.nextLine());
        String[] names = new String[n];
        for (int i = 0; i < n; i++) {
            names[i] = scn.nextLine();
        }
        
        for (int i = 0;  i < n; i++) {
            Client client = new Client(new Socket("localhost", PORT), names[i]);    
            Thread upload = new Thread(client);
            upload.start();
        }

    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        FileInputStream fStream = null;
        
        BufferedReader bReader = null;
        PrintWriter pWriter = null;
        
        BufferedOutputStream bStream = null;
        Scanner scn = new Scanner(System.in);
        try {
            // System.out.println("\nPrompt: UPLOAD 'filepath'");
            // String fileString = scn.nextLine();

            pWriter = new PrintWriter(socket.getOutputStream());
            pWriter.write("UPLOAD " + fileName+"\r\n");
            pWriter.flush();

            File file = new File(fileName);
            // System.out.println(file.exists());
            
            if (!file.exists() && !file.isFile()) {
                // TODO print on client and server;
                System.out.println("Couldn't upload because not a valid file");
                pWriter.write("unsuccessful\r\n");
                pWriter.flush();
                // pWriter.close();
                // return;
            } else {
                pWriter.write("successful\r\n");
                pWriter.flush();

                bStream = new BufferedOutputStream(socket.getOutputStream());
                FileInputStream fileIn = new FileInputStream(file);
                byte[] fileData = new byte[BUFFER];
                int len;
                while((len = fileIn.read(fileData)) > 0) {
                    bStream.write(fileData, 0, len);
                }
                bStream.flush();
                // pWriter.close();
                // bStream.close();
                fileIn.close();

                System.out.println(fileName);
                System.out.println("flushed");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            // try {
            // //     // bStream.close();
            // //     // pWriter.close();
            //     // socket.close();
            // } catch (IOException e) {
            // //     // TODO Auto-generated catch block
            //     e.printStackTrace();
            // }
        }
    }
}
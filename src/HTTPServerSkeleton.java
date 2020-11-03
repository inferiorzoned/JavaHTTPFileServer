// package app;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.StringTokenizer;
// import java.nio.file.Files;
import java.nio.file.Path;
// import java.nio.file.Paths;
import java.util.stream.Stream;

public class HTTPServerSkeleton implements Runnable {
    private Socket socket;
    String logString;
    static final int PORT = 6789;
    static final int BUFFER_SIZE = 4096;

    public HTTPServerSkeleton(Socket s) {
        socket = s;
        logString = "";
    }

    public static String readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return String.valueOf(fileData);
    }

    public String htmlGenerator(File file) throws IOException {

        if(!file.exists()) {
            File temp = new File("404.html");
            FileInputStream fis = new FileInputStream(temp);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while(( line = br.readLine()) != null ) {
                sb.append( line );
                sb.append( '\n' );
            }

            String text = sb.toString();
            return text;
        }

        String text = "<html><head><title>HTTP File Server</title></head><body><ul>";

        // System.out.println(file.list().length);
        text += "<h1>" + file.getName() + "</h1>";
        for (File f : file.listFiles()) {
            // System.out.println("genrating html");
            String link = f.toString().substring(f.toString().indexOf("/") + 1);
            String fname = link.substring(link.lastIndexOf("/") + 1);
            if (f.isDirectory()) {
                text += "<li><b><a href=\"http:////localhost:" + PORT + "/" + link + "/\">" + fname + "</a></b></li>";
            } else if (f.isFile()) {
                text += "<li><a href=\"http:////localhost:" + PORT + "/" + link + "\">" + fname + "</li>";
            }
        }
        text += "</body></html>";
        return text;
    }

    public void appendtoLog(String logstr)
    {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("log.txt",true));
            out.write(logstr);
            out.close();
        } catch (Exception e) {
            //TODO: handle exception
        }
    }

    public void sendResponse(String status, String contentType, String content) {
        try {
            PrintWriter pr = new PrintWriter(socket.getOutputStream());
            // String logString = "";
            logString += "\nRESPONSE:\n";
            pr.write(status + "\r\n");
            logString += status + "\r\n";
            pr.write("Server: Java HTTP Server: 1.0\r\n");
            logString += "Server: Java HTTP Server: 1.0\r\n";
            pr.write("Date: " + new Date() + "\r\n");
            logString += "Date: " + new Date() + "\r\n";
            if (content == null) {
                pr.write("Content-Length: 0\r\n");
                logString += "Content-Length: 0\r\n";
            } else {
                pr.write("Content-Type: " + contentType + "\r\n");
                logString += "Content-Type: " + contentType + "\r\n";
                pr.write("Content-Length: " + content.length() + "\r\n");
                logString += "Content-Length: " + content.length() + "\r\n";
            }
            pr.write("\r\n");
            logString += "\r\n";
            if (content != null)
                pr.write(content);
            appendtoLog(logString);
            pr.flush();
            pr.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(File file) {

        long size = file.length();
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            PrintWriter pr = new PrintWriter(socket.getOutputStream());

            FileInputStream fis = new FileInputStream(file);
            // String logString = "";
            String mimeType = Files.probeContentType(file.toPath());
            
            pr.write("HTTP/1.1 200 OK\r\n");
            pr.write("Server: Java HTTP Server: 1.0\r\n");
            pr.write("Date: " + new Date() + "\r\n");
            pr.write("Content-Type: " + mimeType + "\r\n");
            pr.write("Content-Length: " + size + "\r\n");
            pr.write("Content-Disposition: attachment;\"\r\n");
            pr.write("\r\n");

            logString += "\nRESPONSE:\n";
            logString += "HTTP/1.1 200 OK\r\n";
            logString += "Server: Java HTTP Server: 1.0\r\n";
            logString += "Date: " + new Date() + "\r\n";
            logString += "Content-Type: " + mimeType + "\r\n";
            logString += "Content-Length: " + size + "\r\n";
            logString += "Content-Disposition: attachment;\r\n";
            logString += "\r\n";
            // String encodedFileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8.name()).replace("+","%20");
            // pr.write("Content-Disposition: attachment; filename*=UTF-8''" + encodedFileName + "; filename=" + encodedFileName + "\"\r\n");
            // logMeassage += "Content-Disposition: attachment; filename=\"" + file.getName() + "\"\r\n";
            
            pr.flush();

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            int len;
            while ((len = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, len);
            }
            appendtoLog(logString);
            dos.flush();
            pr.close();
            dos.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {

        ServerSocket serverConnect = new ServerSocket(PORT);
        System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

        BufferedWriter out = new BufferedWriter(new FileWriter("log.txt"));
        out.write("");
        out.close();

        // File file1 = new File(".");
        // System.out.println(file1.getAbsolutePath());
        // File file = new File("generateHtml.html");

        // FileInputStream fis = new FileInputStream(file);
        // System.out.println(file.getAbsolutePath());
        // BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
        // StringBuilder sb = new StringBuilder();
        // String line;
        // while ((line = br.readLine()) != null) {
        // sb.append(line);
        // sb.append('\n');
        // }

        // String content = sb.toString();
        int clientNo = 1;
        while (true) {
            HTTPServerSkeleton myServer = new HTTPServerSkeleton(serverConnect.accept());
            // socket = serverConnect.accept();
            // System.out.println("Client " + clientNo + " has connected with the server");
            clientNo++;
            Thread t = new Thread(myServer);
            t.start();
        }

    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pr = new PrintWriter(socket.getOutputStream());
            BufferedOutputStream bStream = new BufferedOutputStream(socket.getOutputStream());

            // request from the client
            String request = in.readLine();
            // System.out.println(request);
            if (request != null) {
                if(request.startsWith("GET")){
                    if(request != null){
                        logString += "\nREQUEST:\n";
                        logString += request;
                        appendtoLog(logString);
                    }
                    
                    if (request.equals("GET /favicon.ico HTTP/1.1")) {
                        byte[] bytes = Files.readAllBytes(Paths.get("root/favicon.ico"));
        
                        pr.write("HTTP/1.1 200 OK\r\n");
                        pr.write("Server: Java HTTP Server: 1.0\r\n");
                        pr.write("Date: " + new Date() + "\r\n");
                        pr.write("Content-Type: image/x-icon\r\n");
                        pr.write("Content-Length: " + bytes.length + "\r\n");
                        pr.write("Content-Disposition: attachment;\"\r\n");
            
                        pr.write("\r\n");
                        pr.flush();
        
                        logString += "\nRESPONSE:\n";
                        logString +="HTTP/1.1 200 OK\r\n";
                        logString +="Server: Java HTTP Server: 1.0\r\n";
                        logString +="Date: " + new Date() + "\r\n";
                        logString +="Content-Type: image/x-icon\r\n";
                        logString +="Content-Length: " + bytes.length + "\r\n";
                        logString += "Content-Disposition: attachment;\r\n";
                        logString += "\r\n";
                        appendtoLog(logString);
        
                        bStream.write(bytes);
                        bStream.flush();
                        // System.out.println("here");
                    }
        
                    // StringTokenizer parse = new StringTokenizer(request);
                    // String method = parse.nextToken().toUpperCase(); // we get the HTTP method of
                    // the client
                    String requestSplit[] = request.split(" ");
                    String method = requestSplit[0];
                    String fileReqString = requestSplit[1];
        
                    String requestedPath = requestSplit[1].substring(requestSplit[1].indexOf("/") + 1);
                    // System.out.println(requestSplit[1].indexOf("/") + 1);
                    // System.out.println(requestedPath);
    
                    File file1 = new File("root" + File.separator + requestedPath);
                    if (!file1.exists()) {
                        System.out.println("Request: " + request);
                        System.out.println("Response: HTTP/1.1 404 NOT FOUND");
                        String contentString = htmlGenerator(file1);
                        sendResponse("HTTP/1.1 404 NOT FOUND", "text/html", contentString);
                    } else if (fileReqString.equals("/")) {
                        // System.out.println(file1.getAbsolutePath());
                        // System.out.println(file1.exists());
                        // System.out.println(file1.isDirectory());
        
                        String contentString = htmlGenerator(file1);
                        sendResponse("HTTP/1.1 200 OK", "text/html", contentString);
                        // System.out.println(contentString);
                    } else if (fileReqString.endsWith("/")) {
                        String contentString = htmlGenerator(file1);
                        sendResponse("HTTP/1.1 200 OK", "text/html", contentString);
                    } else {
                        sendFile(file1);
                    }
                }
                else if (request.toUpperCase().startsWith("UPLOAD")) {
                    String requestSplit[] = request.split(" ");
                    // String method = requestSplit[0];
                    String fileReqString = requestSplit[1];
                    System.out.println("\n"+request);
    
                    String uploadStatus = in.readLine();
                    if (uploadStatus.toLowerCase().equals("unsuccessful")) {
                        System.out.println("Couldn't upload because not a valid file");
                    }
                    else if (uploadStatus.toLowerCase().equals("successful")) {
                        fileReqString = fileReqString.substring(fileReqString.lastIndexOf("/")+1);
                        System.out.println(fileReqString);
                        File file1 = new File("root" + File.separator + fileReqString);
    
                        BufferedInputStream bIStream = new BufferedInputStream(socket.getInputStream());
                        FileOutputStream fOStream = new FileOutputStream(file1);
    
                        byte [] buffer = new byte[4096];
                        int len = 0;
                        while((len = bIStream.read(buffer)) > 0) {
                            fOStream.write(buffer, 0, len);
                            // System.out.println(len);
                        }
                        fOStream.close();
                        bIStream.close();
                    }
                }
            }
            
            // if(!request.startsWith("GET")) System.out.println(request);
            

            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            // appendtoLog(logString);
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }     
        }     

    }

}

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.*;
import java.net.*;
import java.io.*;


public class Server{
    public static String requestPath;
    public static int statusCode;
    public static String protocol = "HTTP/1.1";
    public static long contentLength = 0;
    public static String response = "";
    public static String connectionStatus;
    public static String extension;
    public static String redirectPath = "";

    public static String checkRedirects(String filePath){
        try{
            File redirectsFile = new File("redirect.defs");
            Scanner redirectIn = new Scanner(redirectsFile);

            if(redirectIn.hasNextLine()){
                String redirect = redirectIn.nextLine();

                String[] tokens = redirect.split(" ");

                if(tokens[0].equals("/"+filePath)){
                    return tokens[1];
                }
            }
        }
        catch(Exception e){
        }
        return null;
    }

    public static boolean checkFilePath(String filePath){

        //add error handling --what if there is no "."
        String[] tokens = filePath.split("\\.");
        System.out.println(tokens[0]);

        if(tokens.length != 1){
        if (tokens[1].equals("html") || tokens[1].equals("htm")){
            extension = "text/html";
        }
        else if (tokens[1].equals("txt")){
            extension = "text/plain";
        }
        else if (tokens[1].equals("pdf")){
            extension = "application/pdf";
        }
        else if (tokens[1].equals("png")){
            extension = "image/png";
        }
        else if (tokens[1].equals("jpeg") || tokens[1].equals("jpg")){
            extension = "image/jpeg";
        }
        else {
            extension = "application/octet-stream";
        }
        }
        else{
            extension = "application/octet-stream";
        }


        try{
            File requestFile = new File(filePath);

            if(requestFile.exists()){
                requestPath = filePath;
                statusCode = 200;
                return true;
            }
            else{
                if((redirectPath = checkRedirects(filePath)) != null){
                    System.out.println(redirectPath);
                    statusCode = 301;
                    return true;
                }
                else{
                    statusCode = 404;
                    return false;
                }
            }
        }
        catch(Exception e){
        }
        return false;
    }


    public static void generateReponse(String filePath, DataOutputStream out) throws Exception{
//        try{
//            //*** error is at requestFile trying to write the response back to the server before we close the connection
//            File responseFile = new File(requestPath);
//
//            contentLength = responseFile.length();
//
//            System.out.println(contentLength);
//
//        }
//        catch(Exception e){
//        }


        java.util.Date date1 = new java.util.Date();
        System.out.println(statusCode);

        String s = protocol + " " + statusCode + " Moved Permanently\n"
                 + "Location: "+redirectPath+"\r\n";
//                 +protocol + " " + statusCode + " OK\n"
//                +date1 + "\n"
//                + "Accept-Ranges: bytes\n"
//                + "Content-Length: "+ contentLength+"\n"
//                //+ "Content-Length: 500000"+"\n"
//                + "Connection: "+ connectionStatus + "\n"
//                + "Content-Type: "+ extension+"\r\n";


        out.write(s.getBytes());
        out.write("\r\n".getBytes());

//        out.write(b);
        System.out.println(s);

        try{
            //*** error is at requestFile trying to write the response back to the server before we close the connection
//            File responseFile = new File(requestPath);
//            Scanner responseScan = new Scanner(responseFile);
            //HTML FILES   TEXT TOO
//            while(responseScan.hasNextLine()){
//                String response = responseScan.nextLine();
//                out.writeBytes(response);
//                out.writeBytes("\r\n");
//            }


            //WORKS PDF PART 1 USE THIS ONE!!!!!!
//            byte[] buf = new byte[(int)contentLength];
//            InputStream is = new FileInputStream(responseFile);
//
//            int c =0;
//            while ((c = is.read(buf, 0, buf.length)) > 0){
//                out.write(buf);
//            }

//            is.close();

            //WORKS PDF PART 2
//            byte [] buffer = new byte[8192];
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//            int bytesRead;
//
//            InputStream is = new FileInputStream(responseFile);
//            while ((bytesRead = is.read(buffer))!= -1){
//                baos.write(buffer, 0, bytesRead);
//            }
//
//           out.write(baos.toByteArray());
//           out.write("\r\n".getBytes());
//           is.close();
//           baos.close();



            //*****IMAGES JPG*******
            //BufferedImage responseScan = ImageIO.read(responseFile);
            // ImageIO.write(responseScan, "jpg", out);

        }
        catch(Exception e){
        }
    }



    public static void processRequest(String request, DataOutputStream out) throws Exception{
        String[] requestTokens = request.split(" ");

        if(requestTokens[0].equals("GET") || requestTokens[0].equals("HEAD")){
            //System.out.println("in GET/HEAD statement");
            java.util.Date date1 = new java.util.Date();
            //System.out.println("REQUEST TOCKEN "+requestTokens[1]);

            if(checkFilePath(requestTokens[1].substring(1))){
                generateReponse(requestPath, out);
            }

        }
        else{
            statusCode = 403;
            //send the response
        }
    }

    public static void main(String[] args) throws Exception{
        //right now dealing with non persistent connections so connection status is close
        connectionStatus = "close";
        //checking for the port syntax
        if((args.length != 1) || (!args[0].contains("--port="))){
            System.out.println("Specify the command in \"java Server \'--port=port\' \" format");
            return;
        }


        ServerSocket echoConnection = null;
        Socket echoClient = null;
       // PrintWriter out = null;

        DataOutputStream out  = null;
        BufferedReader in = null;



        try{
            int equalIndex = args[0].indexOf('=');
            int portNumber = Integer.parseInt(args[0].substring(equalIndex + 1));

            //start the server and accept the client
            echoConnection = new ServerSocket(portNumber);
            System.out.println("Server is started");
            echoClient = echoConnection.accept();

            //open the input and output stream  , had to get rid of you true


            out = new DataOutputStream(echoClient.getOutputStream());
            in = new BufferedReader(new InputStreamReader(echoClient.getInputStream()));


            //got request from client
            String request = "";
            String requestLine = "";
            while(!(requestLine = in.readLine()).equals("")){
                request = request + requestLine;
                // System.out.println(request);
            }

            //parse the request to check the type. process only GET and HEAD requests
            //if not GET/HEAD -->  send 403 (Forbidden)
            //get the path of the file
            //check for the file in the directory
            //check if there is a redirect for the file in redirect.defs
            //if it is redirect, send a redirect response with 301
            //else if it is not present send status code 404
            //else we have send back the file in the data, status code 200

            processRequest(request,out);


        }
        catch(NumberFormatException e){
            System.out.println("Please enter a valid port number");
            System.exit(1);
        }
        catch(IOException e){
            System.out.println("Unable to read while listening on the port.");
        }
        finally{
            try{
                echoConnection.close();
                echoClient.close();
                out.close();
                in.close();
            }
            catch(IOException e){
                System.out.println("Unable to close the connection");
            }
        }
    }

}


import java.util.*;
import java.net.*;
import java.io.*;


public class Server{
	public static String requestPath = "";
	public static String redirectPath = "";
    	public static String response = "";
    	public static String connectionStatus;

    	public static String checkRedirects(String filePath) throws IOException{

            	File redirectsFile = new File("redirect.defs");
            	Scanner redirectIn = new Scanner(redirectsFile);

            	if(redirectIn.hasNextLine()){
                	String redirect = redirectIn.nextLine();

                	String[] tokens = redirect.split(" ");

                	if(tokens[0].equals("/" + filePath)){
                    		return tokens[1];
                	}
            	}
        
        	return null;
    	}


    	public static int checkFilePath(String filePath) throws IOException{

     		File requestFile = new File(filePath);

            	if(requestFile.exists()){
                	requestPath = filePath;
        
                	return 200;
            	}
            	else{
                	if((redirectPath = checkRedirects(filePath)) != null){
                    		return 301;
                	}
                	else{
                    		return 404;
                	}
            	}
    	}

	public static boolean isValidRequest(String requestHeader){
		return (requestHeader.equals("GET") || requestHeader.equals("HEAD"));
	}

	public static Response processRequest(Socket client) throws IOException{
		//open the input stream
		BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		
		//got request from client
		String request = "";
		String requestLine = "";
		
		while(!(requestLine = in.readLine()).equals("")){
			request = request + requestLine;
			// System.out.println(request);
		}

        	String[] requestTokens = request.split(" ");
		String requestType = requestTokens[0];
		String filePath = requestTokens[1].substring(1);

		Response response = new Response();

        	if(isValidRequest(requestType)){

            		int statusCode = checkFilePath(filePath);

			//add error handling --what if there is no "."
			String[] tokens = filePath.split("\\.");
			System.out.println(tokens[0]);
	
			if(tokens.length != 1){
				response.setContentType(tokens[1]);
			}
			
			response.setStatusCode(statusCode);

			response.setResponsePath(requestPath);
			response.setRedirectPath(redirectPath);
			response.setRequestType(requestType);

        	}
        	else{
            		response.setStatusCode(403);
			response.setRequestType(requestType);
      		}
	
		return response;
    	}

	public static int getPortNumber(String command) throws NumberFormatException{
		int equalIndex = command.indexOf('=');
		int portNumber = Integer.parseInt(command.substring(equalIndex + 1));

		return portNumber;
	}

	public static void sendInternalServerError(Response response, Socket client) throws IOException{
	        response.setStatusCode(500);
		response.sendResponse(client);
	}

	public static void main(String[] args) throws IOException{
        	//right now dealing with non persistent connections so connection status is close
        	//connectionStatus = "close";
        	//checking for the port syntax
        	if((args.length != 1) || (!args[0].contains("--port="))){
            		System.out.println("Specify the command in \"java Server \'--port=port\' \" format");
            		return;
        	}

 			int portNumber = getPortNumber(args[0]);

                        //start the server and accept the client
                       ServerSocket echoConnection = new ServerSocket(portNumber);

			Socket echoClient = null;
			Response response  = null; 
        	try{
            		System.out.println("Server is started");

			//connection is closed currently after each request from the client
            		//while((echoClient = echoConnection.accept()) != null){
			while(true){
				echoClient = echoConnection.accept();	
				System.out.println(System.currentTimeMillis());
				System.out.println(echoConnection.getSoTimeout());
            			//parse the request to check the type. process only GET and HEAD requests
            			//if not GET/HEAD -->  send 403 (Forbidden)
            			//get the path of the file
            			//check for the file in the directory
            			//check if there is a redirect for the file in redirect.defs
            			//if it is redirect, send a redirect response with 301
            			//else if it is not present send status code 404
            			//else we have send back the file in the data, status code 200

            			response = processRequest(echoClient);
				response.setConnectionStatus("close");
				response.sendResponse(echoClient);

				echoClient.close();
			}
		}
		catch(NullPointerException e){
			sendInternalServerError(response, echoClient);
			System.out.println("Server timed out!");
		}
		catch(NumberFormatException e){
			sendInternalServerError(response, echoClient);
			System.out.println("Please enter a valid port number");
			System.exit(1);
		}
        	catch(IOException e){
			sendInternalServerError(response, echoClient);
			System.out.println("Unable to read while listening on the port.");
		}
		finally{
			try{	
				echoConnection.close();
			}
			catch(IOException e){
				System.out.println("Unable to close the connection");
			}
        	}
    	}

}


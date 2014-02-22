import java.util.*;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Response{

	private int statusCode;
	private String protocol;
	private String contentType;
	private String responsePath;
	private String redirectPath;
	private String requestType;
	private String connectionStatus;
	
	public Response(){
		this.protocol = "HTTP/1.1";
		this.contentType = "application/octet-stream";
	}

	public void setConnectionStatus(String connectionStatus){
		this.connectionStatus = connectionStatus;
	}

	public void setStatusCode(int statusCode){
		this.statusCode = statusCode;
	}

	public void setResponsePath(String responsePath){
		this.responsePath = responsePath;
	}

	public void setRedirectPath(String redirectPath){
		this.redirectPath = redirectPath;
	}

	public void setRequestType(String requestType){
		this.requestType = requestType;
	}	

	public void setContentType(String extension){
		if (extension.equals("html") || extension.equals("htm")){
            		this.contentType = "text/html";
        	}               
        	else if (extension.equals("txt")){
            		this.contentType = "text/plain";
        	}
        	else if (extension.equals("pdf")){
            		this.contentType = "application/pdf";
        	}
        	else if (extension.equals("png")){
            		this.contentType = "image/png";
        	}
        	else if (extension.equals("jpeg") || extension.equals("jpg")){
            		this.contentType = "image/jpeg";
        	}
        	else {
            		this.contentType = "application/octet-stream";
        	}
	}

	private void generateResponse(Socket client, String respPath) throws IOException{
		//*** error is at requestFile trying to write the response back to the server before we close the connection
                File responseFile = new File(respPath);

                long contentLength = responseFile.length();

                System.out.println(contentLength);

		DataOutputStream out = new DataOutputStream(client.getOutputStream());

		if(!((this.requestType).equals("HEAD"))){
			System.out.println("Not a head");
			if((this.contentType).equals("image/jpeg") || (this.contentType).equals("image/png")){
				BufferedImage image = ImageIO.read(responseFile);
				if((this.contentType).equals("image/jpeg")){
					ImageIO.write(image,"jpg",out);
				}
				else{
					ImageIO.write(image,"png",out);
				}
			}
			else{
				byte[] buffer = new byte[(int)contentLength];
                		InputStream in = new FileInputStream(responseFile);
			
				while(in.read(buffer,0,buffer.length) > 0){
					out.write(buffer);
				}
			
				in.close();
			}
		}

		out.close();

	}
	
	private void generateResponseHeader(Socket client, String code) throws IOException{
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		System.out.println("Please be in th header");
		String header = protocol + " " + this.statusCode + " " + code + "\n";
		if (this.statusCode == 301){
			header += "Location: " + redirectPath + "\r\n";
		}
		
		header += "Date: " + (new java.util.Date()) + "\n"
                        + "Accept-Ranges: bytes\n"
                        //+ "Content-Length: " + contentLength +"\n"
                        + "Connection: " + this.connectionStatus + "\n"
                        + "Content-Type: " + this.contentType + "\r\n";

		out.write(header.getBytes());
		out.write("\r\n".getBytes());
	}


	public void sendResponse(Socket client) throws IOException{

		if (this.statusCode == 200){
			generateResponseHeader(client, "OK");
			generateResponse(client, this.responsePath);
		}
		else if (this.statusCode == 404){
			this.contentType = "text/html";
			generateResponseHeader(client, "Not Found");
			generateResponse(client, "NotFound.html");
		}
		else if (this.statusCode == 301){
			generateResponseHeader(client, "Moved Permanently");
		}
		else if (this.statusCode == 500){
			this.contentType = "text/html";
			generateResponseHeader(client, "Internal Server Error");
			generateResponse(client, "InternalError.html");
		}
		else if (this.statusCode == 403){
			System.out.println("Yay in response");
			this.contentType = "text/html";
			generateResponseHeader(client, "Forbidden");
			generateResponse(client, "Forbidden.html");
		}
	   }

}

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
	public static final int PORT = 80;
	
	boolean canExit = false;
	ServerSocket serverSocket;
	
	public static void main(String[] args) {			
		App app = new App();
		new Thread(app.new ListenExit()).start();
		app.runServer();		
	}
	
	
	private void runServer() {
		System.out.println("Running server");
		while(canExit == false) {
			try {
				serverSocket = new ServerSocket(PORT);
				Socket acceptSocket = serverSocket.accept();				
				new Thread(new ServerThread(acceptSocket)).start();
				serverSocket.close();
				
			}catch(IOException e) {
				System.out.println(e);
			}
		}
		
		System.out.println("Done!");
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Thread class for each accept socket
	 */
	private class ServerThread implements Runnable {
		int id = 0;
		Socket acceptSocket;
		
		public ServerThread(Socket acceptSocket) {
			this.acceptSocket = acceptSocket;
		}
		
		
		@Override
		public void run() {
			System.out.println("Running thread: " + id);
			try {
				String firstRequest = getFirstRequest(acceptSocket.getInputStream());
				String theRequest = "";
				if(firstRequest != null) {
					theRequest = getRequstForPage(firstRequest);
				}
				
				System.out.println("Done reading inputStream");
				
				loadMsgToClient(theRequest, acceptSocket.getOutputStream());
				System.out.println("Done writing outputStream");
				acceptSocket.close();
				
				System.out.println("Success!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
		
		
		private String getFirstRequest(InputStream inputStream) throws IOException{
			//InputStreamReader is a character StreamReader, reads character by character
			InputStreamReader streamReader = new InputStreamReader(inputStream); 
			//BufferedReader is stored in ram, faster program
			BufferedReader inputReader = new BufferedReader(streamReader);
			String line = inputReader.readLine();
			return line;
		}
		
		
		private String getRequstForPage(String fullRequest) {
			String theRequest = "";
			System.out.println(fullRequest);
			String pattern = "(?<=/)([a-zA-Z\\._/]+|\\s+)";
			Pattern r = Pattern.compile(pattern);
			Matcher match = r.matcher(fullRequest);
			
			if(match.find()) {
				theRequest = match.group();
			}
			System.out.println(theRequest);
			
			if(theRequest.equals(" ")) {
				return "index";
			}
			return theRequest;
		}
		
		
		private void loadMsgToClient(String request, OutputStream outputStream) {
			PrintWriter msgToClient = new PrintWriter(outputStream);
			String htmlFilePath = getHTMLFilePath(request);
			System.out.println("The path: " + htmlFilePath);
			File htmlFile = new File(htmlFilePath);
			if(htmlFile.exists()) {
				msgToClient.println("HTTP/1.1 200 OK");
				msgToClient.println("Content-Length: " + htmlFile.length());
				msgToClient.println("Content-Type: text/html");
				msgToClient.println("");
				
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(htmlFile));
					String line = "";
					while((line = br.readLine()) != null) {
						msgToClient.println(line);
					}
				}catch(IOException e) {
					e.printStackTrace();
					msgToClient.flush();
				}
				msgToClient.flush();
			}
			else {
				loadFileNotFoundPage(msgToClient);
			}
			msgToClient.flush();
		}
		
		
		private String getHTMLFilePath(String request) {
			if(request.indexOf('.') < 0) {
				return "HTML_files/" + request + ".html";
			}
			return request;
		}
		
		
		private void loadErrorPage(PrintWriter msgToClient) {
			msgToClient.println("HTTP/1.1 404 Not Found");
			msgToClient.println("Content-Length: 56");
			msgToClient.println("Content-Type: text/html");
			msgToClient.println("");			
			msgToClient.println("<html>\r\n" + 
								"<body>\r\n" + 
								"<h1>ERROR</h1>\r\n" + 
								"</body>\r\n" + 
								"</html>");
			msgToClient.flush();
		}
		
		
		private void loadFileNotFoundPage(PrintWriter msgToClient) {
			msgToClient.println("HTTP/1.1 500 Internal Server Error");
			msgToClient.println("Content-Length: 58");
			msgToClient.println("Content-Type: text/html");
			msgToClient.println("");			
			msgToClient.println("<html>\r\n" + 
								"<body>\r\n" + 
								"<h1>File not found error...</h1>\r\n" + 
								"</body>\r\n" + 
								"</html>");
			msgToClient.flush();
		}
		
	}
	
	
	/*
	 * Thread that listens for exit condition
	 */
	private class ListenExit implements Runnable{
		@Override
		public void run() {
			Scanner sc = new Scanner(System.in);
			String exitCond = sc.nextLine();
			
			while(!exitCond.equals("exit")) {
				exitCond = sc.nextLine();
			}
			System.out.println("canExit = true");
			canExit = true;
			sc.close();
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(canExit);
		}
	}
}
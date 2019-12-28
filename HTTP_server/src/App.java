import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	public static final String INDEX = "index.html";
	public static final String ERROR = "";
	public static final String OM_MEG = "om_meg.html";
	//public static final Inet4Address ADDRESS = new Inet4Address(127.0.0.1);
	
	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("Need argument: <number of threads>");
			return;
		}
		
		int numOfThreads = Integer.parseInt(args[0]);
		App app = new App();
		app.runThreads(numOfThreads);
		
	}
	
	
	private void runThreads(int numOfThreads) {
		//new Thread(new ListenExit()).start();
		System.out.println("Running server");
		while(true) {
			try {
				ServerSocket serverSocket = new ServerSocket(PORT);
				Socket acceptSocket = serverSocket.accept();				
				new Thread(new ServerThread(acceptSocket, serverSocket)).start();
				serverSocket.close();
			}catch(IOException e) {
				System.out.println(e);
			}
		}
	}
	
	
	/*
	 * Thread class for each accept socket
	 */
	private class ServerThread implements Runnable {
		int id = 0;
		Socket acceptSocket;
		ServerSocket serverSocket;
		
		public ServerThread(Socket acceptSocket, ServerSocket serverSocket) {
			this.acceptSocket = acceptSocket;
			this.serverSocket = serverSocket;
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
			StringBuilder result = new StringBuilder();
			//InputStreamReader is a character StreamReader, reads character by character
			InputStreamReader streamReader = new InputStreamReader(inputStream); 
			//BufferedReader is stored in ram, faster program
			BufferedReader inputReader = new BufferedReader(streamReader);
			String line = inputReader.readLine();
			return line;
		}
		
		
		private void loadMsgToClient(String request, OutputStream outputStream) {
			PrintWriter msgToClient = new PrintWriter(outputStream);
			switch(request) {
				case " ":
					loadPage(msgToClient, INDEX);
					break;
				case "om_meg":
					loadPage(msgToClient, OM_MEG);
					break;
				default:
					loadErrorPage(msgToClient);
					
			}
		}
		
		
		private String getRequstForPage(String fullRequest) {
			String theRequest = "";
			System.out.println(fullRequest);
			String pattern = "(?<=/)(\\s|\\w+)";
			Pattern r = Pattern.compile(pattern);
			Matcher match = r.matcher(fullRequest);
			
			if(match.find()) {
				theRequest = match.group();
			}
			System.out.println(theRequest);
			
			return theRequest;
		}
		
		
		private void loadPage(PrintWriter msgToClient, String filePath) {
			File filee = new File(filePath);
			if(filee.exists()) {
				System.out.println("File Exists");
				long fileLength = filee.length();
				msgToClient.println("HTTP/1.1 200 OK");
				msgToClient.println("Content-Length: " + fileLength);
				msgToClient.println("Content-Type: text/html");
				msgToClient.println("");
				
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(filee));
					String line = "";
					while((line = br.readLine()) != null) {
						msgToClient.println(line);
					}
				} catch(IOException e) {
					e.printStackTrace();
					msgToClient.flush();
				}				
			}
			else {
				loadFileNotFoundPage(msgToClient);
			}
			msgToClient.flush();
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
			msgToClient.println("Content-Length: 80");
			msgToClient.println("Content-Type: text/html");
			msgToClient.println("");			
			msgToClient.println("<html>\r\n" + 
								"<body>\r\n" + 
								"<h1>Index file not found!</h1>\r\n" + 
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
			System.out.println("Yoello?");
			sc.close();
		}
	}
}
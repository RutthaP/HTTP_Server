import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Stream;

public class App {
	public static final int PORT = 80;
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
		System.out.println("Running server");
		while(true) {
			try {
				ServerSocket serverSocket = new ServerSocket(PORT);
				Socket acceptSocket = serverSocket.accept();				
				new Thread(new ServerThread(acceptSocket)).start();
				serverSocket.close();
			}catch(IOException e) {
				System.out.println(e);
			}
		}
	}
	
	
	
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
				getInputStreamContent(acceptSocket.getInputStream());
				System.out.println("Done reading inputStream");
				
				returnMsgToClient(acceptSocket.getOutputStream());
				System.out.println("Done writing outputStream");
				acceptSocket.close();
				
				System.out.println("Success!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		private String getInputStreamContent(InputStream inputStream) throws IOException{
			StringBuilder result = new StringBuilder();
			//inputstreamreader is a character stream reader, reads character by character
			InputStreamReader streamReader = new InputStreamReader(inputStream); 
			//bufferedreader is stored in ram, faster program
			BufferedReader inputReader = new BufferedReader(streamReader);
			String line = inputReader.readLine();
			return line;
			/*String line = inputReader.readLine();
			while(line != null) {
				result.append(line);
				result.append(System.lineSeparator());
				System.out.println(line);
				line = inputReader.readLine();
			}
			return result.toString();*/
		}
		
		
		private void returnMsgToClient(OutputStream outputStream) {
			PrintWriter msgToClient = new PrintWriter(outputStream);
			msgToClient.println("HTTP/1.1 200 OK");
			msgToClient.println("Content-Length: 56");
			msgToClient.println("Content-Type: text/html");
			msgToClient.println("");			
			msgToClient.println("<html>\r\n" + 
								"<body>\r\n" + 
								"<h1>Hello, World!</h1>\r\n" + 
								"</body>\r\n" + 
								"</html>");
			msgToClient.flush();
		}
		
	}
}
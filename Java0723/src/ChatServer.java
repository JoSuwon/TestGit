import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

// ChatServer 클래스 : 채팅서버
public class ChatServer {
	ServerSocket server;
	
	Socket child;
	
	static final int PORT = 5000;
	
	HashMap<String, ObjectOutputStream> hm;
	
	
	public ChatServer() {
		try {
			server = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("**** 채팅 서버 OPEN ****");
		System.out.println("서버는 클라이언트 요청 대기중....");
		
		hm = new HashMap<String, ObjectOutputStream>();
		
		try {
			while(true) {
				child = server.accept();
				
				ChatServerThread cst = new ChatServerThread(child, hm);
				Thread th = new Thread(cst);
				th.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	public static void main(String[] args) {
		new ChatServer();
	}
}

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

// 클라이언트 접속을 유지하면서, 데이터 송수신
// 클라이언트가 서버에 접속할때 마다 해당객체 생성
public class ChatServerThread implements Runnable {

	Socket child;
	
	ObjectInputStream ois;
	ObjectOutputStream oos;
	
	String user_id;
	HashMap<String, ObjectOutputStream> hm;
	
	public ChatServerThread() {}
	public ChatServerThread(Socket s, HashMap hm) {
		// 클라이언트 접속 IP 주소를 출력(서버확인)
		child = s;
		this.hm = hm;
		
		System.out.println(child.getInetAddress()+"로 부터 연결 요청 받음!!");
		
		try {
			ois = new ObjectInputStream(child.getInputStream());
			oos = new ObjectOutputStream(child.getOutputStream());
			
			// 클라이언트가 가장먼저 보내는 데이터가 아이디 값
			user_id = (String)ois.readObject();
			
			// 서버에 접속되어있는(방에있는) 모든 클라이언트에게 전달(브로드캐스트)
			// "XXXX 님이 접속 하셨습니다."
			broadcast(user_id+"님이 들어왔습니다.");
			
			// 서버 확인용 출력
			System.out.println("접속한 클라이언트 아이디 : " + user_id);
			
			// 여러 클라이언트에게 공유되는 데이터를 동기화 처리
			// 서버가 접속하는 클라이언트 정보를 저장하는 공간
			synchronized (hm) {
				// 해쉬맵에 아이디/출력스트림 저장
				// 모든 접속된 클라이언트가 공유해야하는 값이기 때문에 동기화 처리가 필요하다.
				hm.put(user_id, oos);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void run() {
		String receiveData;
		
		try {
			while(true) {
				// 클라이언트로 부터 메세지 수신
				receiveData = (String)ois.readObject();
				
				// 받은 메세지를 모든 클라이언트한테 전달 (브로드 캐스트)
				broadcast(receiveData);
			}
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println(" 클라이언트 요청 종료 (강제 종료)");
		} finally {
			// 사용자 종료시 hm저장된 정보를 제거
			synchronized (hm) {
				// 사용자 ID를 사용해서 저장된 정보를 제거
				hm.remove(user_id);
			}
			
			// 사용자가 채팅방에서 나간사실을 알려주기 (브로드캐스트)
			broadcast(user_id+"님이 나갔습니다.");
			// 서버에서 확인용
			System.out.println(user_id+"님이 나갔습니다.");
			
			try {
				if(child != null) child.close();
			} catch(Exception e) {
				System.out.println("클라이언트 자원해제 실패");
			}
		}
		
	}
	
	// broadcast : 방송 : 메세지를 전달받아서 모든 클라이언트에 채팅방에 전달
	private void broadcast(String msg) {
		// 사용자의 정보를 저장하는 HashMap 동기화 해서
		// 출력정보를 사용해서 메세지 전달
		synchronized (hm) {
			Iterator<String> keys = hm.keySet().iterator();
			while(keys.hasNext()) {
				String key = keys.next();
				try {
					hm.get(key).writeObject(user_id + " : " + msg);
					hm.get(key).flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

// 멀티 캐스트 채팅을 위한 클라이언트
public class GUIChatClient extends JFrame implements ActionListener {
	// 카드 레이아웃
	CardLayout card;
	
	// 화면 정보 - 접속창
	JButton btn_connect; // 접속버튼
	JTextField txt_server_ip; // ip입력창
	JTextField txt_name; // 접속할 이름(아이디)
	
	// 화면정보 - 채팅창
	JButton btn_exit; // 종료버튼
	JButton btn_send; // 전송버튼
	JTextField txt_input; //입력메세지
	JTextArea txt_list; //메세지 출력되는 곳
	JScrollPane scroll_pane;
	
	/////////////////////////////////////////////
	
	// 채팅(통신)을 하기위한 정보
	
	// 접속할 서버의 IP주소
	String IPAddress;
	// 포트번호
	static final int PORT = 5000;
	// 클라이언트 소켓
	Socket client = null;
	// 데이터 입출력 스트림 객체
	ObjectInputStream ois;
	ObjectOutputStream oos;
	// 아이디
	String user_id;
	// 서버에서 보낸 메세지를 받기위한 쓰레드 객체
	ReceiveDataThread rdt;
	
	
	public GUIChatClient() {
		super("채팅 클라이언트");
		launchFrame();
	}
	
	private void init() throws IOException {
		IPAddress = txt_server_ip.getText();
		user_id = txt_name.getText();
		
		client = new Socket(IPAddress, PORT);
		
		oos = new ObjectOutputStream(client.getOutputStream());
		ois = new ObjectInputStream(client.getInputStream());
		
		oos.writeObject(user_id);
		oos.flush();
		
		card.show(this.getContentPane(), "채팅창");
		txt_input.requestFocus();
		// 서버가 보낸 메세지를 받기(수신)
		rdt = new ReceiveDataThread();
		Thread th = new Thread(rdt);
		th.start();
	}
	
	// 버튼 클릭 이벤트 처리
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("채팅 서버 접속")) {
			try {
				init();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(e.getActionCommand().equals("전송") || e.getSource() == txt_input) {
			try {
				if(!txt_input.getText().equals("")) {
					oos.writeObject(txt_input.getText());
					oos.flush();
					txt_input.setText("");
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
		if(e.getActionCommand().equals("종료")) {
			System.exit(1);
		}
	}
	
	// 서버가 보낸 메세지를 받는 클래스 구현(내부클래스-함수처럼 사용됨)
	class ReceiveDataThread implements Runnable {
		// 서버에서 보낸 메세지
		String receiveData;
		@Override
		public void run() {
			try {
				while(true){
					receiveData = (String)ois.readObject();
					txt_list.append(receiveData+"\n");
					txt_list.setCaretPosition(txt_list.getDocument().getLength());
				} 
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	public void launchFrame() {
		card = new CardLayout();
		setLayout(card);
		
		// 접속화면
		JPanel connect = new JPanel();
		connect.setBackground(Color.GREEN);
		connect.setLayout(new BorderLayout());
		
		// 접속창 상단
		connect.add(new JLabel(" 다중 채팅 접속창 ", JLabel.CENTER), BorderLayout.NORTH);
		
		// 접속창 센터
		JPanel connect_sub = new JPanel(); // FlowLayout 기본설정
		connect_sub.setBackground(Color.YELLOW);
		
		connect_sub.add(new JLabel("서버 아이피 :    "));
		txt_server_ip = new JTextField("127.0.0.1", 15);
		connect_sub.add(txt_server_ip);
		connect_sub.add(new JLabel("접속 아이디 :    "));
		txt_name = new JTextField("guest", 15);
		connect_sub.add(txt_name);
		
		connect.add(connect_sub, BorderLayout.CENTER);
		
		// 접속창 하단
		btn_connect = new JButton("채팅 서버 접속");
		btn_connect.addActionListener(this);
		connect.add(btn_connect, BorderLayout.SOUTH);
		
		//this.add(connect);
		
		//-------------------------------------------------------------
		
		// 채팅창화면
		JPanel chat = new JPanel();
		chat.setLayout(new BorderLayout());
		chat.setBackground(Color.LIGHT_GRAY);
		
		// 채팅창 상단
		chat.add(new JLabel("클라이언트 채팅창", JLabel.CENTER), BorderLayout.NORTH);
		
		// 채팅창 센터
		txt_list = new JTextArea();
		scroll_pane = new JScrollPane(txt_list);
		chat.add(scroll_pane, BorderLayout.CENTER);
		
		// 채팅창 하단
		JPanel chat_sub = new JPanel();
		txt_input = new JTextField(13);
		txt_input.addActionListener(this);
		btn_send = new JButton("전송");
		btn_exit = new JButton("종료");
		btn_send.addActionListener(this);
		btn_exit.addActionListener(this);
		chat_sub.add(txt_input);
		chat_sub.add(btn_send);
		chat_sub.add(btn_exit);
		chat.add(chat_sub, BorderLayout.SOUTH);
		
		//this.add(chat);
		
		// 접속화면, 채팅화면을 카드레이아웃에 추가
		add(connect, "접속창");
		add(chat, "채팅창");
		
		// 카드 레이아웃의 초기화면 지정
		card.show(this.getContentPane(), "접속창");
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		//setSize(300,300);
		setBounds(700, 300, 300, 300);
		setVisible(true);
		
	}

	
	public static void main(String[] args) {
		new GUIChatClient();
	}
}

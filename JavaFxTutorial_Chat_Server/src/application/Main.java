package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class Main extends Application {
	
	//여러 클라이언트가 접속했을 때 효과적으로 여러개의 스레드를 관리하기 위함
	//threadpool은 thread에 제한을 두기 때문에 갑작스러운 트래픽 초과로 인한 서버 장애를 방지할 수 있음.
	public static ExecutorService threadPool;
	
	//쉽게 사용할 수 있는 배열 vector
	public static Vector<Client> clients = new Vector<Client>();
	
	ServerSocket serverSocket;
	
	//서버를 구동시켜서 클라이언트의 연결을 기다리는 메서드
	public void startServer(String IP, int port) {
		try {
			//서버 소켓을 호출한다
			serverSocket = new ServerSocket();
			//서버컴퓨터 역할을 수행하는 컴퓨터가 특정한 클라이언트의 접속을 기다림
			serverSocket.bind(new InetSocketAddress(IP, port));
		}catch (Exception e) {
			e.printStackTrace();
			//서버소켓이 닫혀있지 않다면 서버를 닫아준다.
			if(!serverSocket.isClosed()) {
				stopServer();
			}
		}
		//클라이언트가 접속할 때까지 계속 기다리는 쓰레드
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						System.out.println("[클라이언트 접속] " + socket.getRemoteSocketAddress() + " : " + Thread.currentThread().getName());
					}catch (Exception e) {
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
			}
		};
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}
	
	//서버의 작동을 중지시키는 메서드
	public void stopServer() {
		try {
			//현재 작동 중인 모든 소켓 닫기
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			//서버 소켓 객체 닫기
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			//쓰레드 풀 종료하기
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	//UI를 생성하고, 실질적으로 프로그램을 동작시키는 메서드
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("나눔고딕", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("시작하기");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1,0,0,0));
		root.setBottom(toggleButton);
		String IP = "127.0.0.1";
		int port = 9876;
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("시작하기")) {
				startServer(IP, port);
				//바로 출력하는 것이 아니라 GUI 요소를 기다리고 출력해줘야한다.
				Platform.runLater(() ->{
					String message = String.format("[서버 시작]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("종료하기");
					});
				}else {
					stopServer();
					Platform.runLater(() ->{
						String message = String.format("[서버 종료]\n", IP, port);
						textArea.appendText(message);
						toggleButton.setText("시작하기");
						});
				}
		});
		Scene scene = new Scene(root, 400,400);
		primaryStage.setTitle("채팅 서버");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	
	
	//프로그램의 진입점입니다.
	public static void main(String[] args) {
		launch(args);
	}
}

package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

//chat server와 한명의 클라이언트가 통신
public class Client {
	Socket socket;
	public Client(Socket socket) {
		this.socket = socket;
		receive();
	}
	//클라이언트로부터 메시지를 전달 받는 메서드
	public void receive() {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						//read(byte[] b) 메서드는 입력 스트림으로부터 읽은 바이트들을 매게값으로 주어진 바이트 배열 b에 저장하고 실제로 읽은 바이트 수를 리턴합니다.
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[512];
						int length = in.read(buffer);
						while(length == -1) throw new IOException();
						//메시지가 온 곳의 소켓 어드레스와 스레드 이름을 print한다.
						System.out.println("[메시지 수신 성공]" + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName());
						String message = new String(buffer, 0, length, "UTF-8");
						//전달받은 메시지를 다른 클라이언트에게도 전송
						for(Client client : Main.clients) {
							client.send(message);
						}
					}
				}catch(Exception e) {
					try {
						System.out.println("[메시지 수신 실패] " + socket.getRemoteSocketAddress() + " : " + Thread.currentThread().getName());
					}catch (Exception e2) {
						e2.printStackTrace();
					}
				}
				
			}
		};
		//threadpool에 thread 제출
		Main.threadPool.submit(thread);
	}
	//클라이언트에게 메시지를 전송하는 메서드
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("이건 언제실행돼?");
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				}catch (Exception e) {
					try {
						System.out.println("[메시지 송신 실패] " + socket.getRemoteSocketAddress() + " : " + Thread.currentThread().getName());
						//클라이언트가 서버에 접속이 실패한 경우이기 때문에 클라이언트 목록에서 현재 클라이언트를 제거해준다.
						Main.clients.remove(Client.this);
						socket.close();
					}catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);
	}
}

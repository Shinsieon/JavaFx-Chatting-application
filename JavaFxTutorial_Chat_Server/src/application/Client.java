package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

//chat server�� �Ѹ��� Ŭ���̾�Ʈ�� ���
public class Client {
	Socket socket;
	public Client(Socket socket) {
		this.socket = socket;
		receive();
	}
	//Ŭ���̾�Ʈ�κ��� �޽����� ���� �޴� �޼���
	public void receive() {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						//read(byte[] b) �޼���� �Է� ��Ʈ�����κ��� ���� ����Ʈ���� �Ű԰����� �־��� ����Ʈ �迭 b�� �����ϰ� ������ ���� ����Ʈ ���� �����մϴ�.
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[512];
						int length = in.read(buffer);
						while(length == -1) throw new IOException();
						//�޽����� �� ���� ���� ��巹���� ������ �̸��� print�Ѵ�.
						System.out.println("[�޽��� ���� ����]" + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName());
						String message = new String(buffer, 0, length, "UTF-8");
						//���޹��� �޽����� �ٸ� Ŭ���̾�Ʈ���Ե� ����
						for(Client client : Main.clients) {
							client.send(message);
						}
					}
				}catch(Exception e) {
					try {
						System.out.println("[�޽��� ���� ����] " + socket.getRemoteSocketAddress() + " : " + Thread.currentThread().getName());
					}catch (Exception e2) {
						e2.printStackTrace();
					}
				}
				
			}
		};
		//threadpool�� thread ����
		Main.threadPool.submit(thread);
	}
	//Ŭ���̾�Ʈ���� �޽����� �����ϴ� �޼���
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("�̰� ���������?");
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				}catch (Exception e) {
					try {
						System.out.println("[�޽��� �۽� ����] " + socket.getRemoteSocketAddress() + " : " + Thread.currentThread().getName());
						//Ŭ���̾�Ʈ�� ������ ������ ������ ����̱� ������ Ŭ���̾�Ʈ ��Ͽ��� ���� Ŭ���̾�Ʈ�� �������ش�.
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

/*
 * copyright 下午3:25:53
 *
 */
package server;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * 为每个客户端连接创建对应的信息处理线程
 * 
 * @author jdl
 * 
 *         date: 2018年9月23日
 *
 */
public class ClientConnection extends Thread {

	// 保存连接套子节
	private Socket socket;
	private PrintWriter writer;
	private Scanner sc;
	// 标志是否持续接受消息
	private volatile boolean flag;

	/**
	 * @param socket
	 */
	public ClientConnection(Socket socket) {
		super();
		this.socket = socket;
		try {
			this.writer = new PrintWriter(socket.getOutputStream());
			sc = new Scanner(socket.getInputStream());
			flag = true;
		} catch (IOException e) {
			e.printStackTrace();
			flag = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	/**
	 * 监听消息，收到则发送到所有连接池中
	 */
	@Override
	public void run() {
		try {
			// 消息临时存储
			String message;
			// 循环监听消息，并群发消息
			System.out.println("开始等待接受客户端消息");
			writer.println("start to chat");
			writer.flush();
			while (flag && (message = sc.nextLine()) != null) {
				if (message.equals("quit")) {
					flag = false;
					break;
				}
				System.out.println(message);
				Application.sendMessage(message, this);
			}
			System.out.println("quit");
			// 异常处理s
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		} finally {
			// 关闭连接
			try {
				release();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("一个连接关闭");
	}

	public void print(String message) {
		writer.println(message);
		writer.flush();
	}

	public void close() {
		flag = false;
	}

	private void release() throws IOException {
		// 关闭输入输出流
		try {
			sc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 关闭套子节
		socket.close();
		Application.remove(this);
	}
}

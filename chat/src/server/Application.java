package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Application 就是一个只包含静态元素的类,利用java静态类的加载机制来运行，不能创建对象
 * 
 * 
 * @author jdl
 * 
 *         date: 2018年9月23日
 *
 */
public class Application extends Thread {

	private final static int port = 1045;
	/**
	 * 客户端连接监听器
	 */
	private static ServerSocket serverSocket;

	/**
	 * 客户端连接保存列表
	 */
	private static ArrayList<ClientConnection> connList;

	/**
	 * 服务器处理中心
	 */
	private static Service service;

	/**
	 * 标记服务器是否继续运行
	 */
	private static volatile boolean flag = true;

	/**
	 * 初始化服务器
	 */
	static {
		try {
			// 初始化服务器变量，服务接受线程
			serverSocket = new ServerSocket(port);
			connList = new ArrayList<ClientConnection>();
			service = new Service();
			// 服务器开始运行
			service.start();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	/**
	 * 核心服务类，用来不断接受客户端连接，并创建客户端处理线程来运行
	 * 
	 * @author jdl
	 * 
	 *         date: 2018年9月23日
	 *
	 */
	private static class Service extends Thread {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {

			// 临时存储客户端连接
			ClientConnection conn;

			// 服务器开启
			System.out.println("服务器开启" + new Date(System.currentTimeMillis()));

			while (flag) {
				try {
					// 接受客户端连接
					conn = new ClientConnection(serverSocket.accept());
					// 保存客户端连接
					synchronized (conn) {
						// 主要是serverSocket怎么主动关闭啊
						if (!flag) {
							conn.close();
							break;
						}
						connList.add(conn);
					}
					// 开始处理处理客户端连接
					conn.start();
					System.out.println("开启了一个连接");
				} catch (SocketException e) {
					// 监听异常结束服务器
					e.printStackTrace();
					break;
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("error");
					break;
				}
			}
			release();
			System.out.println("服务器结束");
		}
	}

	// 防止创建实例
	private Application() {
	};

	public static void close() {
		flag = false;
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void release() {
		synchronized (connList) {
			ClientConnection client;
			for (int i = 0; i < connList.size(); i++) {
				client = connList.get(i);
				client.close();
				connList.remove(client);
			}
		}
	}

	public static void sendMessage(String message, ClientConnection fromClient) {
		// 如果数据不对直接返回
		if (message == null || message.length() == 0 || !flag || connList.size() == 0)
			return;
		// 临时变量，存储连接和输出流
		ClientConnection connTmp;
		for (int i = 0; i < connList.size(); i++) {
			// 在去除之前被移除的话就停止
			try {
				connTmp = connList.get(i);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
				break;
			}

			// 如果是发送者的化就不用传消息过去啊
			if (connTmp == fromClient)
				continue;

			// 如果意外关闭的话，就关闭连接
			try {
				connTmp.print(message);
			} catch (Exception e) {
				e.printStackTrace();
				connTmp.close();
			}
		}
	}

	/**
	 * 开始初始化static成员变量，和方法
	 * 
	 * date: 2018年9月23日 下午5:42:25
	 */
	public static void begin() {
		// 不需要方法体，只要有一个方法被调用，则类就开始被加载到虚拟机中，并且static{}方法自动被调用且只调用一次
	}

	/**
	 * 
	 * 从服务器列表移除连接
	 * 
	 * @param conn
	 *            需要移除的连接
	 *
	 *            date: 2018年9月23日 下午5:42:36
	 */
	public static void remove(ClientConnection conn) {
		connList.remove(conn);
	}
}

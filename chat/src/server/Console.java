package server;

import java.util.Scanner;

public class Console extends Thread {

	private boolean flag = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// 这个是控制台的输入没必要关闭吧,但为什么每次都提出警告
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		String message;
		System.out.println("控制台开始了");
		while (flag) {
			
			message = sc.nextLine();
			
			if (message == null) {
				System.out.println("夭寿啦，控制台输入竟然为null");
				flag = false;
				break;
			}
			
			//输入quit退出
			if (message.equals("quit")) {
				flag = false;
				break;
			}
			Application.sendMessage(message, null);
		}
		Application.close();
		System.out.println("控制台退出了");
		super.run();
	}

	public static void main(String[] args) {
		Application.begin();
		new Console().start();
	}
}

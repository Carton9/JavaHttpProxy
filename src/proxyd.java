import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import edu.npu.proxy.HttpProxyMainThread;

public class proxyd {

	public static void main(String[] args) {
		
		if (args.length > 0 && args[0].equals("-port")) {
			int port = Integer.parseInt(args[1]);
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(port);
				System.out.println("�ڶ˿�" + port + "�������������\n");
				while (true) {
					Socket socket = null;
					try {
						socket = serverSocket.accept();
						new HttpProxyMainThread(socket).start();//��һ�����������һ���߳�
					} catch (Exception e) {
						System.out.println("�߳�����ʧ��");
					}
				}
			} catch (IOException e1) {
				System.out.println("proxyd����ʧ��\n");
			}finally{
				try {
					serverSocket.close();
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
		}else{
			System.out.println("��������");
		}
	}
	
}

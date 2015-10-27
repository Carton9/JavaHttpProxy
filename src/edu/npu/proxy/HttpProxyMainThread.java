package edu.npu.proxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import edu.npu.utils.URL;

public class HttpProxyMainThread extends Thread {
	static public int CONNECT_RETRIES = 5; // ������Ŀ���������Ӵ���
	static public int CONNECT_PAUSE = 5; // ÿ�ν������ӵļ��ʱ��
	static public int TIMEOUT = 50; // ÿ�γ������ӵ����ʱ��
	
	protected Socket csocket;// ��ͻ������ӵ�Socket

	public HttpProxyMainThread(Socket cs) {
		this.csocket = cs;
	}

	public void run() {
		String firstLine = ""; // http����ͷ��һ��
		String urlStr = ""; // �����url
		Socket ssocket = null;//��Ŀ����������ӵ�socket
		// cisΪ�ͻ�����������sisΪĿ������������
		InputStream cis = null, sis = null;
		// cosΪ�ͻ����������sosΪĿ�����������
		OutputStream cos = null, sos = null;
		try {
			csocket.setSoTimeout(TIMEOUT);
			cis = csocket.getInputStream();
			cos = csocket.getOutputStream();
			while (true) {
				int c = cis.read();
				if (c == -1)
					break; // -1Ϊ��β��־
				if (c == '\r' || c == '\n')
					break;// �����һ������,���л�ȡĿ������url
				firstLine = firstLine + (char) c;
			}
			urlStr = extractUrl(firstLine);
			System.out.println(urlStr);
			URL url = new URL(urlStr);//��url��װ�ɶ������һϵ��ת������,����getIP��ʵ����dns����
			firstLine = firstLine.replace(url.getScheme()+"://"+url.getHost(), "");//��һ������Ҫ��������ͷ�ľ���·���������·��
			int retry = CONNECT_RETRIES;
			while (retry-- != 0) {
				try {
					ssocket = new Socket(url.getIP(), url.getPort()); // ���Խ�����Ŀ������������
					System.out.println("+++++��Ŀ�������("+url.getIP()+":"+url.getPort()+")(host:"+url.getHost()+")�������ӳɹ�+++++,������Դ("+url.getResource()+")");
					break;
				} catch (Exception e) {
					System.out.println("-----��Ŀ�������("+url.getIP()+":"+url.getPort()+")(host:"+url.getHost()+")��������ʧ��-----");
				}
				// �ȴ�
				Thread.sleep(CONNECT_PAUSE);
			}
			if (ssocket != null) {
				ssocket.setSoTimeout(TIMEOUT);
				sis = ssocket.getInputStream();
				sos = ssocket.getOutputStream();
				sos.write(firstLine.getBytes()); // ������ͷд��
				pipe(cis, sis, sos, cos); // ����ͨ�Źܵ�
			}
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			try {
				csocket.close();
				cis.close();
				cos.close();
			} catch (Exception e1) {
			}
			try {
				ssocket.close();
				sis.close();
				sos.close();
			} catch (Exception e2) {
			}
		}
	}
	/**
	 * ��http����ͷ�ĵ�һ����ȡ�����url
	 * @param firstLine http����ͷ��һ��
	 * @return url
	 */
	public String extractUrl(String firstLine) {
		String[] tokens = firstLine.split(" ");
		String URL = "";
		for (int index = 0; index < tokens.length; index++) {
			if (tokens[index].startsWith("http://")) {
				URL = tokens[index];
				break;
			}
		}
		return URL;
	}

	/**
	 * Ϊ�ͻ�����Ŀ�����������ͨ�Źܵ�
	 * @param cis �ͻ���������
	 * @param sis Ŀ������������
	 * @param sos Ŀ�����������
	 * @param cos �ͻ��������
	 */
	public void pipe(InputStream cis, InputStream sis, OutputStream sos, OutputStream cos) {
		Client2ServerThread c2s = new Client2ServerThread(cis, sos);
		Server2ClientThread s2c = new Server2ClientThread(sis, cos);
		c2s.start();
		s2c.start();
		try {
			c2s.join();
			s2c.join();
		} catch (InterruptedException e1) {
			
		}
	}
}

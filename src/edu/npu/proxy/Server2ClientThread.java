package edu.npu.proxy;

import java.io.InputStream;
import java.io.OutputStream;

public class Server2ClientThread extends Thread{
	private InputStream sis;
    private OutputStream cos;

    public Server2ClientThread(InputStream sis, OutputStream cos) {
        this.sis = sis;
        this.cos = cos;
    }

    public void run() {
    	int length;
    	byte bytes[] = new byte[1024];
		while(true){
			try {
				if ((length = sis.read(bytes)) > 0) {
					cos.write(bytes, 0, length);//��http����ͷд��Ŀ������
					cos.flush();
				} else if (length < 0)
					break;
			} catch (Exception e) {
				//System.out.println("\nRequest Exception:");
			}
		}
    }
}

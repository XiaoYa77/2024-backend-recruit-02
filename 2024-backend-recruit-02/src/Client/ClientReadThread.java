package Client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import static System.LogBack.LOGGER;

public class ClientReadThread extends Thread {
    Socket socket;
    //构造器用于获取socket对象
    public ClientReadThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            //从socket通信管道中获取一个字节输入流
            InputStream is = socket.getInputStream();
            //将字节输入流包装成数据输入流
            DataInputStream dis = new DataInputStream(is);
            while (true)
            {
                try {
                    //读取数据并存储在msg变量中
                    String msg = dis.readUTF();
                    System.out.println(msg);
                } catch (IOException e) {
                    dis.close();
                    socket.close();
                    break;
                }
            }
        } catch (IOException e) {
            LOGGER.error("出现了IOExpctption类型的异常，请及时处理！");
        } catch (Exception e){
            LOGGER.error("出现了其他类型异常，请及时排查并处理！");
        }
    }
}

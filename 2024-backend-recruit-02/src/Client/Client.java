package Client;

import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

import static System.LogBack.LOGGER;

public class Client {
    public static void main(String[] args){
        try {
            //创建一个配置文件变量用于获取端口号
            Properties p = new Properties();
            //读取文件
            p.load(new FileReader("src/Server/Port.properties"));
            //获取文件的端口号并保存在port变量中
            int port = Integer.parseInt(p.getProperty("port"));

            //创建一个socket对象同时请求与服务端的连接
            Socket socket = new Socket(InetAddress.getLocalHost(), port);
            //从socket通信管道中获取一个字节输出流
            OutputStream os = socket.getOutputStream();
            //将字节输出流包装为数据输出流
            DataOutputStream dos = new DataOutputStream(os);

            Scanner sc = new Scanner(System.in);

            new ClientReadThread(socket).start();

            while (true)
            {
                Thread.sleep(10);
                System.out.print(socket.getInetAddress().getHostAddress() + ">");
                String command = sc.nextLine();
                if (command.equals("exit")) {
                    System.out.println("系统退出...");
                    break;
                }
                //输出命令
                dos.writeUTF(command);
            }
        } catch (IOException e) {
            LOGGER.error("出现了IOExpctption类型的异常，请及时处理！");
        } catch (InterruptedException e) {
            LOGGER.error("出现了InterruptedException类型的异常，请及时处理！");
        } catch (Exception e){
            LOGGER.error("出现了其他类型异常，请及时排查并处理！");
        }
    }
}

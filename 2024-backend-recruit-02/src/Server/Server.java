package Server;

import java.io.FileReader;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import static System.LogBack.LOGGER;

public class Server {
    public static void main(String[] args){
        try {
            //创建一个配置文件变量用于获取端口号
            Properties p = new Properties();
            //读取文件
            p.load(new FileReader("src/Server/Port.properties"));
            //获取文件的端口号并保存在port变量中
            int port = Integer.parseInt(p.getProperty("port"));

            //创建一个ServerSocket对象并为其赋初始端口号
            ServerSocket serverSocket = new ServerSocket(port);

            System.out.println("----------服务器启动成功----------");

            //实现多线程接收
            while (true) {
                //等待客户端的连接
                Socket socket = serverSocket.accept();
                //通过日志记录连接程序的ip
                LOGGER.info("连接的ip:{}", socket.getInetAddress().getHostAddress());
                //开启线程以实现多线程
                new ServerReadThread(socket).start();
            }
        }  catch (BindException e){
            LOGGER.error("该端口已被占用！请关闭相关程序再重新启动！");
        } catch (IOException e) {
            LOGGER.error("出现了IOException类型异常，请及时处理！");
        } catch (Exception e){
            LOGGER.error("出现了其他类型异常，请及时排查并处理！");
        }
    }
}

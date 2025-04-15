package Server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import System.Command;

import static System.LogBack.LOGGER;

public class ServerReadThread extends Thread{
    Socket socket;
    //构造器用于获取socket对象
    public ServerReadThread(Socket socket) {
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
                    //将msg以一个空格为分割符号拆分为多个字符串
                    String[] arr = msg.trim().split(" ");
                    //开启命令线程用于执行命令
                    if (arr.length == 3)
                        new Command(socket,arr[0],arr[1],arr[2]).start();
                    else if (arr.length == 4){
                        if (IsNumber(arr[2]) && IsNumber(arr[3])) {
                            int num1 = Integer.parseInt(arr[2]);
                            int num2 = Integer.parseInt(arr[3]);
                            new Command(socket,arr[0], arr[1], num1, num2).start();
                        }
                        else
                            new Command(socket,arr[0],arr[1],arr[2], arr[3]).start();
                    }else if (arr.length == 2)
                        new Command(socket,arr[0],arr[1]).start();
                    else if (arr.length == 1)
                        new Command(socket,arr[0]).start();
                } catch (IOException e) {
                    System.out.println("ip地址为：" + socket.getInetAddress().getHostAddress() + " 结束访问程序");
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

    public static boolean IsNumber(String str){
        return str != null && str.chars().allMatch(Character::isDigit);
    }
}

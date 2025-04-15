package System;

import java.io.*;
import java.net.Socket;
import java.security.Key;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;

import static System.LogBack.LOGGER;

public class Command extends Thread {
    String command;
    String key;
    String value;
    String field;

    int start;
    int end;

    Socket socket;

    //构造器用于获取命令与键值
    public Command(Socket socket, String command, String key, String value) {
        this.socket = socket;
        this.command = command;
        this.key = key;
        this.value = value;
        this.field = value;
    }

    public Command(Socket socket, String command, String key, int start, int end) {
        this.socket = socket;
        this.command = command;
        this.key = key;
        this.start = start;
        this.end = end;
    }

    public Command(Socket socket, String command, String key) {
        this.socket = socket;
        this.command = command;
        this.key = key;
    }

    public Command(Socket socket, String command) {
        this.socket = socket;
        this.command = command;
    }

    public Command(Socket socket, String command, String key ,String field,String value) {
        this.socket = socket;
        this.command = command;
        this.key = key;
        this.field = field;
        this.value = value;
    }

    String[] allCommandHelp = {"RPOP [KEY]",
            "LEN [KEY]",
            "RANGE [KEY] [START] [END]",
            "HELP [COMMAND]",
            "SET [KEY] [VALUE]",
            "DEL [KEY] [KEY1][KEY2]",
            "RPUSH [KEY] [VALUE]",
            "LPOP [KEY]",
            "LDEL [KEY]",
            "GET [KEY]",
            "LPUSH [KEY] [VALUE]",
            "HSET [KEY] [FIELD] [VALUE]",
            "HGET [KEY] [FIELD]",
            "HDEL [KEY] [FIELD]",
            "HDEL [KEY]"};
    //定义一个命令列表但不赋值
    String[] allCommand = new String[allCommandHelp.length];

    //使用static使其唯一，否则将会导致数据错乱。
    static HashMap<String, LinkedList<String>> linkedListMap = new HashMap<>();
    static HashMap<String, HashMap<String,String>> linkedHashMap = new HashMap<>();

    @Override
    public void run() {
        try {
            //给命令列表赋值，通过获取帮助列表中的第一个字符来赋值，因此添加命令只需在帮助列表中增加
            for (int i = 0; i < allCommandHelp.length;i++)
                allCommand[i] = allCommandHelp[i].split(" ")[0].toLowerCase();

            //从socket通信管道中获取一个字节输出流
            OutputStream os = socket.getOutputStream();
            //将字节输出流包装为数据输出流
            DataOutputStream dos = new DataOutputStream(os);

            //创建属性文件变量
            Properties stringProperties = new Properties();

            //加载属性文件的键值对
            stringProperties.load(new FileReader("src/Server/String-key-value.properties"));

            switch (command) {
                case "help":
                    if (value == null) {
                        if (key == null) {
                            for (var item : allCommandHelp)
                                dos.writeUTF(item);
                        } else {
                            //定义局部变量判断目标指令是否存在
                            boolean found = false;
                            for (int i = 0; i < allCommand.length; i++) {
                                //如果输入的键（忽略大小写）与allCommand[i]一致说明存在
                                if (key.equalsIgnoreCase(allCommand[i])) {
                                    dos.writeUTF(allCommandHelp[i]);
                                    found = true;
                                }
                            }
                            if (!found)
                                dos.writeUTF("未知指令！");
                        }
                    }
                    else
                        dos.writeUTF("未知指令！");
                    break;
                case "ping":
                    dos.writeUTF("PONG");
                    break;
                case "set":
                    if (key == null)
                        dos.writeUTF("Key 不能为空");
                    else {
                        if (value == null)
                            dos.writeUTF("Value 不能为空");
                        else {
                            //将对应键值对保存在配置文件中
                            stringProperties.setProperty(key, value);
                            //修改配置文件
                            stringProperties.store(new FileWriter("src/Server/String-key-value.properties"), "Store all string key value");
                            dos.writeUTF("成功将 " + key + " 的值设置为 " + value);
                        }
                    }
                    break;
                case "get":
                    if (key == null)
                        dos.writeUTF("Key 不能为空");
                    else {
                        //从配置文件中获取对应键值对
                        String property = stringProperties.getProperty(key);
                        dos.writeUTF(Objects.requireNonNullElse(property, "null"));
                    }
                    break;
                case "del":
                    if (key == null)
                        dos.writeUTF("Key 不能为空");
                    else {
                        //从配置文件中删除对应键
                        stringProperties.remove(key);
                        //修改配置文件
                        stringProperties.store(new FileWriter("src/Server/String-key-value.properties"), "Store all string key value");
                        dos.writeUTF("成功删除了 " + key + " 的值");
                    }
                    break;
                case "lpush":
                    if (key == null)
                        dos.writeUTF("Key 不能为空");
                    else {
                        if (value == null)
                            dos.writeUTF("Value 不能为空");
                        else {
                            //判断是否含有该链表，没有则新建一个链表
                            if (!linkedListMap.containsKey(key))
                                linkedListMap.put(key, new LinkedList<>());
                            //在链表的第一位添加值
                            linkedListMap.get(key).addFirst(value);
                            dos.writeUTF("成功将 " + value + " 放置于 " + key + " 的最左端");
                        }
                    }
                    break;
                case "rpush":
                    if (key == null)
                        dos.writeUTF("Key 不能为空！");
                    else {
                        if (value == null)
                            dos.writeUTF("Value 不能为空");
                        else {
                            //判断是否含有该链表，没有则新建一个链表
                            if (!linkedListMap.containsKey(key))
                                linkedListMap.put(key, new LinkedList<>());
                            //在链表的最后一位添加值
                            linkedListMap.get(key).addLast(value);
                            dos.writeUTF("成功将 " + value + " 放置于 " + key + " 的最右端");
                        }
                    }
                    break;
                case "range":
                    //判断是否存在此链表
                    if (linkedListMap.containsKey(key)) {
                        for (int i = start; i <= end; i++) {
                            dos.writeUTF(linkedListMap.get(key).get(i) + " ");
                        }
                    }else
                        dos.writeUTF("查无" + key + "链表");
                    break;
                case "len":
                    //判断是否存在此链表
                    if (linkedListMap.containsKey(key)) {
                        dos.writeUTF(String.valueOf(linkedListMap.get(key).size()));
                    }else
                        dos.writeUTF("查无" + key + "链表");
                    break;
                case "lpop":
                    //判断是否存在此链表
                    if (linkedListMap.containsKey(key)) {
                        linkedListMap.get(key).removeFirst();
                        dos.writeUTF("成功删除最左端的 " + key);
                    }else {
                        dos.writeUTF("查无" + key + "链表");
                    }
                    break;
                case "rpop":
                    //判断是否存在此链表
                    if (linkedListMap.containsKey(key)) {
                        linkedListMap.get(key).removeLast();
                        dos.writeUTF("成功删除最右端的 " + key);
                    }else {
                        dos.writeUTF("查无" + key + "链表");
                    }
                    break;
                case "ldel":
                    //判断是否存在此链表
                    if (linkedListMap.containsKey(key)) {
                        //清除该链表
                        linkedListMap.get(key).clear();
                        dos.writeUTF("成功删除 " + key + " 链表");
                    }else {
                        dos.writeUTF("查无" + key + "链表");
                    }
                    break;
                //如果要实现数据长期保存应使用XML来保存数据而不是.properties
                case "hset":
                    if(key == null)
                        dos.writeUTF("Key 不能为空");
                    else {
                        if (field == null)
                            dos.writeUTF("Field 不能为空");
                        else {
                            if (value == null)
                                dos.writeUTF("Value 不能为空");
                            else {
                                //判断是否存在此链表
                                if (!linkedHashMap.containsKey(key))
                                    linkedHashMap.put(key, new HashMap<>());
                                linkedHashMap.get(key).put(field, value);
                                dos.writeUTF("成功将 " + value + " 设置给 " + key + "对应的 " + field);
                            }
                        }
                    }
                    break;
                case "hget":
                    if(key == null)
                        dos.writeUTF("Key 不能为空");
                    else {
                        if (field == null)
                            dos.writeUTF("Field 不能为空");
                        else {
                            try {
                                dos.writeUTF(linkedHashMap.get(key).get(field));
                            }
                            catch (NullPointerException e) {//数据为null时必报错所以用try-catch拦截并说明该值为空
                                dos.writeUTF("null");
                            }
                        }
                    }
                    break;
                case "hdel":
                    if(key == null)
                        dos.writeUTF("Key 不能为空");
                    else {
                        if (field != null) {
                            if (linkedHashMap.get(key).containsKey(field)) {
                                linkedHashMap.get(key).remove(field);
                            }else {
                                dos.writeUTF("查无" + field + "链表");
                            }
                        }
                        else{
                            if (linkedHashMap.containsKey(key))
                                linkedHashMap.remove(key);
                            else
                                dos.writeUTF("查无" + key);
                        }
                        System.out.println(linkedHashMap.get(key));
                        dos.writeUTF("成功删除 " + key + " 链表中的所有值");
                    }
                    break;
                default:
                    dos.writeUTF("未知指令！");
            }
        } catch (IOException e) {
            LOGGER.error("出现了IOExpctption类型的异常，请及时处理！");
        } catch (NullPointerException e) {
            LOGGER.error("出现了NullPointerException类型的异常，请及时处理！");
        } catch (Exception e){
            LOGGER.error("出现了其他类型异常，请及时排查并处理！");
        }
    }
}

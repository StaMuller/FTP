package client;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * FTP客户端传输文件类
 * ASCLL传输：txt、html等
 * BINARY传输（default）：exe、图片、视频、音频、压缩文件等
 */

public class ClientDataConnection{

    // 数据传输设置相关
    String ip;
    int port;
    boolean on;
    String type = "";
    ServerSocket socket;       // 客户端数据传输端口
    Socket serverSocket;       // 链接服务器的端口
    BufferedInputStream is;   // 服务器输入流:binary
    BufferedOutputStream os;        // 服务器输出流:binary
    BufferedReader bufferedReader;   // 服务器输入流:ascii
    PrintWriter printWriter;  // 服务器输出流:ascii

    // 主动模式
    ClientDataConnection(int port) throws IOException {
        this.port = port;
        this.on = true;
        this.socket = new ServerSocket(port);
        this.serverSocket = socket.accept();    // 等待客户端来连接
        this.is = new BufferedInputStream(serverSocket.getInputStream());
        this.os = new BufferedOutputStream(serverSocket.getOutputStream());
        this.bufferedReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        this.printWriter = new PrintWriter(serverSocket.getOutputStream(), true);
    }

    // 被动模式
    ClientDataConnection(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
        this.on = true;
        this.serverSocket = new Socket(InetAddress.getByName(ip), port);
        this.is = new BufferedInputStream(serverSocket.getInputStream());
        this.os = new BufferedOutputStream(serverSocket.getOutputStream());
        this.bufferedReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        this.printWriter = new PrintWriter(serverSocket.getOutputStream(), true);
    }

    public void setType(String type){
        this.type = type;
    }

    // 上传文件
    public boolean uploadDirectory(File[] files, PrintWriter sendToServer){
        // 传输file文件夹中的所有文件
        for (File temp : files) {
            if(type.equalsIgnoreCase("ascii")) {
                if(!uploadAscii(temp.getPath(), sendToServer)){
                    return false;
                }
            }else{
                // 默认二进制传输
                if(!uploadBinary(temp.getPath(), sendToServer)){
                    return false;
                }
            }
        }
        sendToServer.println("over");
        return true;
    }

    public boolean uploadFile(File file, PrintWriter sendToServer){
        if(type.equalsIgnoreCase("ascii")) {
            return uploadAscii(file.getPath(), sendToServer);
        }else{
            // 默认二进制传输
            return uploadBinary(file.getPath(), sendToServer);
        }
    }

    // ASCII传输
    public boolean uploadAscii(String pathname, PrintWriter sendToServer){
        try{
            File file = new File(pathname);
            sendToServer.println("ready");
            sendToServer.println(file.getName());
            BufferedReader fileBufferedReader = new BufferedReader(new FileReader(file));
            String temp;
            while((temp = fileBufferedReader.readLine()) != null){
                printWriter.println(temp);
            }
            fileBufferedReader.close();
            return true;
        }catch (IOException e){
            sendToServer.println("stop");
            return false;
        }
    }

    // 二进制传输
    public boolean uploadBinary(String pathname, PrintWriter sendToServer){
        try{
            sendToServer.println("ready");
            sendToServer.println(new File(pathname).getName());
            BufferedInputStream fileIs = new BufferedInputStream(new FileInputStream(pathname));
            int content;
            while ((content = fileIs.read()) != -1){
                os.write(content);
            }
            os.flush();
            fileIs.close();
            return true;
        }catch (IOException e){
            sendToServer.println("stop");
            return false;
        }
    }

    // 下载文件
    public boolean download(BufferedReader receiveFromServer) throws IOException {
        boolean judge;
        String path = "Client Files/Download/";
        String info = receiveFromServer.readLine();
        if(info.equals("ready")){
            // 传输特定文件
            String filename = receiveFromServer.readLine();
            if(type.equalsIgnoreCase("ascll")){
                downloadAscii(path + filename);
            }else{
                // 默认二进制传输
                downloadBinary(path + filename, receiveFromServer);
            }
            judge = true;
        }else if(info.equals("stop")){
            // 异常停止
            judge = false;
        }else{
            // 传输文件夹及内部文件
            File file = new File(path + info);
            if(!file.exists()){
                file.isDirectory();
                file.mkdir();
            }
            path += info + "/";
            while(!(info = receiveFromServer.readLine()).equals("over")){
                if(info.equals("ready")){
                    // 传输特定文件
                    String filename = receiveFromServer.readLine();
                    if (type.equalsIgnoreCase("ascll")) {
                        downloadAscii(path + filename);
                    }else{
                        // 默认二进制传输
                        downloadBinary(path + filename, receiveFromServer);
                    }
                }else if(info.equals("stop")){
                    // 异常停止
                    return false;
                }
            }
            judge = true;
        }
        return judge;
    }

    // ASCII传输
    public void downloadAscii(String filepath) throws IOException {
        File file = new File(filepath);
        PrintWriter filePrintWriter = new PrintWriter(file);
        String temp;
        while((temp = bufferedReader.readLine()) != null){
            filePrintWriter.println(temp);
        }
        filePrintWriter.flush();
        filePrintWriter.close();
    }

    // 二进制传输
    public void downloadBinary(String filepath, BufferedReader receiveFromServer) throws IOException{
        FileOutputStream fos = new FileOutputStream(filepath);
        BufferedOutputStream fileOs = new BufferedOutputStream(fos);
        int length;
        while((length = is.read()) != -1){
            fileOs.write(length);   // 将文件下载到客户端
        }
        fileOs.flush();
        fos.close();
    }

    public void close() throws IOException{
        this.type = "";
        this.on = false;
        if(this.socket != null){
            this.socket.close();
        }
        if(this.serverSocket != null) {
            this.serverSocket.close();
        }
    }
}

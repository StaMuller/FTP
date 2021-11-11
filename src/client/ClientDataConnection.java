package client;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * FTP客户端传输文件类
 * ASCLL传输：txt、html等
 * BINARY传输（default）：exe、图片、视频、音频等
 */

public class ClientDataConnection{

    int port;
    ServerSocket socket;       // 客户端数据传输端口
    Socket serverSocket;       // 链接服务器的端口
    PrintWriter sendToServer;
    BufferedReader receiveFromServer;

    ClientDataConnection(int port) throws IOException {
        this.port = port;
        this.socket = new ServerSocket(port);
        this.serverSocket = socket.accept();    // 等待客户端来连接
        this.sendToServer = new PrintWriter(serverSocket.getOutputStream(), true);
        this.receiveFromServer = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
    }

    public void upload(String pathname) throws IOException {
        FileInputStream lengthCheck = new FileInputStream(pathname);
        int fileLength = 0;
        int content;
        while((content = lengthCheck.read()) != -1){
            ++fileLength;
        }
        sendToServer.println(fileLength);      // 获取文件大小上传至服务器
        lengthCheck.close();
        OutputStream os = serverSocket.getOutputStream();   // 获取服务器的输出流
        FileInputStream is = new FileInputStream(pathname);
        while ((content = is.read()) != -1){
            os.write(content);
        }
        is.close();
    }

    public void download(String pathname){
        try{
            String[] str = pathname.split("/");    // 解析以/为分割线的文件路径
            if(str.length == 1){
                str = pathname.split("\\\\");      // 解析以\\为分割线的文件路径
            }
            String filename = str[str.length - 1];        // 最后一个数据是文件名
            int fileLength = Integer.parseInt(receiveFromServer.readLine());
            InputStream is = serverSocket.getInputStream();  // 获取输入流
            FileOutputStream fos = new FileOutputStream("Client Files/Download/" + filename);
            for(int i = 0; i < fileLength; ++i){
                fos.write(is.read());   // 将文件下载到客户端
            }
            is.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        socket.close();
        serverSocket.close();
    }

}

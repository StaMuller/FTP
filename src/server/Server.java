package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * FTP服务器
 */

public class Server {
    static String ftpPath = "./Share";
    static Authority serverAuthority = new Authority();

    // 初始化FTP服务器
    public ServerSocket init(){      // 服务器创建通信ServerSocket，创建Socket接收客户端信息
        int port = 6500;
        ServerSocket serverSocket = null;
        try{
            serverSocket = new ServerSocket(port);   // 服务器绑定于port端口上
            System.out.println("The FTP is started successfully!");
            File uploadDirectory = new File(ftpPath + "/Upload");
            if(!uploadDirectory.exists()){
                uploadDirectory.isDirectory();
                uploadDirectory.mkdir();
            }
            File downloadDirectory = new File(ftpPath + "/Download");
            if(!downloadDirectory.exists()){
                downloadDirectory.isDirectory();
                downloadDirectory.mkdir();
            }
        }catch (IOException e){
            System.out.println("port " + port + " has been used");
        }
        return serverSocket;
    }

    // 运行FTP服务器
    public void run(ServerSocket serverSocket) throws Exception {
        boolean judge = true;
        while(judge){
            // 接收多个客户端链接
            try{
                Socket client = serverSocket.accept();    // 建立与客户端的链接
                Thread thread = new Thread(new ConnectClient(client, Server.ftpPath));
                thread.start();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        serverSocket.close();
    }
}

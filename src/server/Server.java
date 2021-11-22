package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * FTP服务器链接客户端
 */

public class Server implements Runnable{

    String ftpPath;
    ServerSocket serverSocket;   // 服务器套接字
    Socket clientSocket;     // FTP客户端套接字
    BufferedReader receiveFromClient;    // 接受客户端的信息输入流
    PrintWriter sendToClient;     // 服务端发送给客户端的信息输出流

    Server(String ftpPath) throws IOException {
        this.serverSocket = new ServerSocket(6500);
        this.clientSocket = this.serverSocket.accept();
        this.ftpPath = ftpPath;
        System.out.println("The FTP is started successfully!");
    }

    @Override
    public void run() {
        try{
            // 与客户端的输入输出流连接
            InputStream is = clientSocket.getInputStream();
            OutputStream os = clientSocket.getOutputStream();
            receiveFromClient = new BufferedReader(new InputStreamReader(is));
            sendToClient = new PrintWriter(os, true);

            ServerDataConnection dataConnection = null;    // 用于等待客户端的数据链接
            ServerDataConnection dataConnectionB = null;   // 辅助数据传输
            ConnectClient connectClient = new ConnectClient(sendToClient, receiveFromClient);
            while(true){
                String text = receiveFromClient.readLine();
                System.out.println("command: " + text);
                if(text.equals("quit")){
                    System.out.println("Client logout!");
                    sendToClient.println("FTP logout successfully!");
                    break;
                }else if(text.equals("login")){
                    try{
                        if(connectClient.login()){
                            sendToClient.println("legal");
                            System.out.println("Client Login!");
                        }else{
                            sendToClient.println("illegal");
                            System.out.println("illegal user");
                        }
                    }catch (IOException e){
                        System.out.println("login failed");
                    }
                }else if(text.equals("port")){
                    // 等待接收待连接的IP地址与端口号，服务器主动建立数据连接
                    try{
                        dataConnection = connectClient.port();
                        dataConnectionB = new ServerDataConnection(dataConnection.ip, dataConnection.port + 1);
                        System.out.println("Data connection is finished, client port is " + dataConnection.port);
                        System.out.println("Assistant port is " + dataConnectionB.port);
                        sendToClient.println("command \"" + text + "\" is done.");
                    }catch (IOException e){
                        sendToClient.println("Data connection failed");
                        System.out.println("Data connection failed");
                        continue;
                    }
                    sendToClient.println("System will open another different port automatically to accelerate data transfer.");
                }else if(text.startsWith("pasv") || text.startsWith("PASV")){
                    // 服务器开启一个端口供客户端连接
                    try{
                        dataConnection = connectClient.pasv(dataConnection);
                        dataConnectionB = new ServerDataConnection(dataConnection.port + 1);
                        System.out.println("Data connection is finished, server port is " + dataConnection.port);
                        System.out.println("Assistant port is " + dataConnectionB.port);
                        sendToClient.println("command \"" + text + "\" is done.");
                    }catch (IOException e){
                        sendToClient.println("Data connection failed");
                        System.out.println("Data connection failed");
                        continue;
                    }
                    sendToClient.println("System will open another different port automatically to accelerate data transfer.");
                }else if(text.startsWith("type") || text.startsWith("TYPE")){
                    if(dataConnection!=null){
                        String type = receiveFromClient.readLine();
                        dataConnection.setType(type);
                        sendToClient.println("current data transmission type: " + type);
                        System.out.println("get type successfully!");
                        System.out.println("current type: " + type);
                    }else{
                        System.out.println("Data connection failed");
                    }
                }else if(text.startsWith("mode") || text.startsWith("MODE")){
                    String mode = receiveFromClient.readLine();
                    sendToClient.println("current file transfer mode: " + mode);
                }else if(text.startsWith("stru") || text.startsWith("STRU")){
                    String stru = receiveFromClient.readLine();
                    sendToClient.println("current file transfer stru: " + stru);
                }else if(text.startsWith("retr") || text.startsWith("RETR")){
                    // 下载文件到客户端
                    if(dataConnection != null && dataConnection.on){
                        if(connectClient.downloadToClient(text, dataConnection)){
                            String check = receiveFromClient.readLine();
                            if(check.equals("continue")){
                                sendToClient.println("command \"" + text + "\" is done.");
                            }
                        }
                    }else{
                        System.out.println("nonexistent data connection");
                    }
                }else if(text.startsWith("stor") || text.startsWith("STOR")){
                    // 上传文件
                    if(dataConnection != null && dataConnection.on){
                        if(connectClient.uploadFromClient(dataConnection, dataConnectionB)){
                            sendToClient.println("command \"" + text + "\" is done.");
                        }
                    }else{
                        System.out.println("nonexistent data connection");
                    }
                }else if(text.startsWith("noop") || text.startsWith("NOOP")){
                    sendToClient.println("no operation");
                }else{
                    System.out.println("unknown command");
                }
            }
            receiveFromClient.close();
            sendToClient.close();
            clientSocket.close();
            serverSocket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

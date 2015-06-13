package client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JProgressBar;
import javax.swing.JTextArea;

/**
 * Main class operate queries for client ftp
 *
 * @author Pawel Jaroch
 * @version 1.0
 */
public class Client{
    protected ServerSocket dataSocket = null;
    protected Socket passiveSocket = null;
    protected PrintWriter toServer;
    protected BufferedReader fromServer, stdIn;
    protected int clientPort;
    protected int passiveModePort;
    protected String passiveModeIP;
    protected boolean logged = false, passiveMode = false, connected = false;
    protected String command, response;
    protected JTextArea textOut = null;
    protected Thread transferThread = null;
    protected JProgressBar progressBar;
    
    public Client(JTextArea textArea, JProgressBar progress){
        textOut = textArea;
        progressBar = progress;
    }
    
    /**
     * Method that connects to the FTP server
     * 
     * @param host hostname
     * @throws UnknownHostException
     * @throws IOException
     */
    public void connect(String host) throws UnknownHostException, IOException{
        Socket server = new Socket(host, 21);
        toServer = new PrintWriter(server.getOutputStream(), true);
        stdIn = new BufferedReader(new InputStreamReader(System.in));
        fromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
        setResponse(fromServer.readLine());
        while(getResponse().charAt(3) == '-'){
            textOut.append(getResponse() + "\n");
            setResponse(fromServer.readLine());
        }
        textOut.append(getResponse() + "\n");
        connected = true;
    }
    /**
     * Method login to ftp server
     *
     * @param username user login in ftp
     * @param password user password in ftp
     * @return true if logged in
     * @throws IOException
     */
    public boolean login(String username, String password) throws IOException{
        toServer.println("USER " + username);
        textOut.append("USER " + username + "\n");
        setResponse(fromServer.readLine());
        textOut.append(getResponse() + "\n");
        if(!response.startsWith("331")){
            return false;
        }
        toServer.println("PASS " + password);
        textOut.append("PASS " + "\n");
        setResponse(fromServer.readLine());
        textOut.append(getResponse() + "\n");
        if(getResponse().startsWith("221")){
            connected = false;
            return false;
        }
        else if(getResponse().startsWith("230")){
            logged = true;
            return true;
        }
        else return false;
    }
    
    /**
     * Method that sends a PASV command and opens a socket to handle the connection
     *
     * @return true on success
     * @throws IOException
     */
    private boolean initializePassiveMode() throws IOException{
        toServer.println("PASV");
        textOut.append("PASV\n");
        setResponse(fromServer.readLine());
        textOut.append(getResponse() + "\n");
        if(getResponse().startsWith("227")){
            int firstBraceIndex = getResponse().indexOf("(");
            String ip = getResponse().substring(firstBraceIndex+1,getResponse().length()-1);
            System.out.println("ip : "+ ip);
            String[] splitAddress = ip.split(",");
            String ip1,ip2,ip3,ip4;
            int p1,p2;
            ip1 = splitAddress[0];
            ip2 = splitAddress[1];
            ip3 = splitAddress[2];
            ip4 = splitAddress[3];
            p1 = Integer.parseInt(splitAddress[4]);
            p2 = Integer.parseInt(splitAddress[5]);
            passiveModeIP = ip1 + "." + ip2 + "." + ip3 + "." + ip4;
            passiveModePort = (p1 << 8) + p2;
            passiveSocket = new Socket(passiveModeIP, passiveModePort);
            return true;
        }
        else return false;
    }
    /**
     * Method downloads a file from FTP server
     *@param path path to save the file on local
     * @param filename name of the file to send to client ftp
     * @param serverPath path to the file
     * @throws IOException
     */
    public void getFile(String path, String filename, String serverPath) throws IOException{
        if(!initializePassiveMode()) return;
        toServer.println("RETR " + serverPath + filename);
        textOut.append("RETR " + serverPath + filename + "\n");
        setResponse(fromServer.readLine());
        textOut.append(getResponse() + "\n");
        if(getResponse().startsWith("150")){ 
            try (RandomAccessFile file = new RandomAccessFile(path + File.separator + filename,"rw"); 
                    DataInputStream dataIn = new DataInputStream(passiveSocket.getInputStream())) {

                int offset;
                byte[] data = new byte[1024];
                while( (offset = dataIn.read(data)) != -1){
                    file.write(data, 0, offset);
                }
            }
            setResponse(fromServer.readLine()); // transfer complete
            textOut.append(getResponse() + "\n");
        }
        else if(getResponse().startsWith("221")){
            connected = false;
        }
    }
    /**
     * Method uploads a file to FTP server
     *
     * @param path path to the file
     * @param filename name of local file
     * @param serverPath path to save the file on server
     * @throws IOException
     */
    public void putFile(String path, String filename, String serverPath) throws IOException{
        if(!initializePassiveMode()) return;
        toServer.println("STOR " + serverPath + filename);
        textOut.append("STOR " + serverPath + filename + "\n");
        setResponse(fromServer.readLine());
        textOut.append(getResponse() + "\n");
        if(getResponse().startsWith("150")){ 
            transferThread = new FileThread(passiveSocket, path, filename, fromServer, textOut, progressBar);
            transferThread.start();
        }
        else if(getResponse().startsWith("221")){
            connected = false;
        }
    }
    
    /**
     * Deletes a file from FTP server
     *
     * @param filename file to delete
     * @return true on success
     * @throws IOException
     */
    public boolean deleteFile(String filename) throws IOException{
        toServer.println("DELE " + filename);
        textOut.append("DELE " + filename + "\n");
        setResponse(fromServer.readLine());
        textOut.append(getResponse() + "\n");
        if(getResponse().startsWith("221")){
            connected = false;
            return false;
        }
        return getResponse().startsWith("250");
    }
    /**
     * Creates a directory on the server
     *
     * @param name name of the directory
     * @return true on success
     * @throws IOException
     */
    public boolean makeDirectory(String name) throws IOException{
    	System.out.println("katalog : " + name);
        toServer.println("MKD " + name);
        textOut.append("MKD " + name + "\n");
        setResponse(fromServer.readLine());
        textOut.append(getResponse() + "\n");
        new File(name).mkdir();
        if(getResponse().startsWith("221")){
            connected = false;
            return false;
        }
        return getResponse().startsWith("257");
    }
    /**
     * Removes the directory on the server
     *
     * @param name name of the directory
     * @return true on success
     * @throws IOException
     */
    public boolean removeDirectory(String name) throws IOException{
        toServer.println("RMD " + name);
        textOut.append("RMD " + name + "\n");
        setResponse(fromServer.readLine());
        textOut.append(getResponse() + "\n");
        if(getResponse().startsWith("221")){
            connected = false;
            return false;
        }
        File f =  new File(name);
        deleteFolder(f);
        return getResponse().startsWith("250");
    }
    
    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { 
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
    /**
     * Changes the current working directory on server
     *
     * @param name name of the directory to change to
     * @return true on success, false on failures
     * @throws IOException
     */
    public boolean changeDirectory(String name) throws IOException{
        toServer.println("CWD " + name);
        textOut.append("CWD " + name + "\n");
        setResponse(fromServer.readLine());
        textOut.append(getResponse() + "\n");
        if(getResponse().startsWith("221")){
            connected = false;
            return false;
        }
        return getResponse().startsWith("250");
    }
    /**
     * Method shows a current directory
     *
     * @return reply current directory
     * @throws IOException
     */
    public String currentDirectory() throws IOException{
        toServer.println("PWD");
        textOut.append("PWD\n");
        setResponse(fromServer.readLine());
        textOut.append(getResponse() + "\n");
        if(getResponse().startsWith("221")){
            connected = false;
            return null;
        }
        return getResponse();
    }
    /**
     * Prints the list of files of the current folder
     *
     * @return true on success
     * @throws IOException
     */
    public boolean list() throws IOException{
        if(!initializePassiveMode()) return false;
        toServer.println("LIST");
        setResponse(fromServer.readLine());
        if(getResponse().startsWith("150")){ // file found
            try (PrintStream stdOut = new PrintStream(System.out); 
                    DataInputStream dataIn = new DataInputStream(passiveSocket.getInputStream())) {

                int offset;
                byte[] data = new byte[1024];
                while( (offset = dataIn.read(data)) != -1){
                    stdOut.write(data, 0, offset);
                }
            }
            setResponse(fromServer.readLine()); // transfer complete
            return getResponse().startsWith("226");
        }
        else return false;
    }
    /**
     * Retreives the list of files of current folder to string and returns it
     *
     * @return string containing list of files on server
     * @throws IOException
     */
    public String listToString() throws IOException{
        if(!initializePassiveMode()) return null;
        String list;
        toServer.println("LIST");
        textOut.append("LIST\n");
        setResponse(fromServer.readLine());
        textOut.append(getResponse() + "\n");
        if(getResponse().startsWith("150")){ // file found
            try (DataInputStream dataIn = new DataInputStream(passiveSocket.getInputStream())) {

                list = dataIn.readUTF(); 
                
            }
            setResponse(fromServer.readLine()); // transfer complete
            textOut.append(getResponse() + "\n");
            passiveSocket.close();
            if(getResponse().startsWith("226")){
                return list;
            }
            else if(getResponse().startsWith("221")){
                connected = false;
                return null;
            }
            else return null;
        }
        return null;
    }
    
    /**
     * Sends a QUIT command
     *
     * @throws IOException
     */
    public void quit() throws IOException{
        textOut.append("QUIT\n");
        toServer.println("QUIT");
        setResponse(fromServer.readLine());
        textOut.append(getResponse() + "\n");
        connected = false;
    }
    /**
     * Sends a NOOP command
     *
     * @throws IOException
     */
    public void noop() throws IOException{
        textOut.append("NOOP\n");
        toServer.println("NOOP");
        setResponse(fromServer.readLine());
        textOut.append(getResponse() + "\n");
    }
    /**
     * Returns the status of client login
     *
     * @return true if logged in
     */
    public boolean isLogged(){
        return logged;
    }
    
    /**
     * Returns the status of client connection
     *
     * @return true if logged in
     */
    public boolean isConnected(){
        return connected;
    }
    

    /**
     * @return the response
     */
    public String getResponse(){
        return response;
    }

    /**
     * @param response the response to set
     */
    public void setResponse(String response){
        this.response = response;
    }
}
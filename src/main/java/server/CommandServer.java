package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;

import javax.swing.JTextArea;

/**
 * 
 * Class operate commands send throw FTP client
 * 
 * @author Pawel Jaroch
 * @see server.AddUser AddUser
 * @version     1.0
 * 
 *
 */
public class CommandServer implements Runnable{

	protected Socket c_socket = null; 
	protected Socket c_passivesocket = null;
	protected String s_text = null;
	protected ServerSocket s_passivesocket = null;
	protected long lefttime;
	protected String passiveIP;
	protected int passivePort;
	protected String currentDir;
	protected User user;
	protected boolean logged = false;
	protected JTextArea textOut = null;

	/**
	 * @param c_socket socket that will operate
	 * @param textArea server answers, which will be put in text area in {@link server.ServerGUI}
	 */
	public CommandServer(Socket c_socket, JTextArea textArea) {
		this.c_socket = c_socket;
		Calendar cal = Calendar.getInstance();
		lefttime = cal.getTimeInMillis();
		currentDir = "./";
		textOut =  textArea;
		 
	}
	@Override
	/**
	 * Start thread, override method
	 * In if-else operate next commends
	 * {@inheritDoc}
	 */
	public void run() {
		long t;
		String request = "";
		String lastRequest = "";
		String response;
		BufferedReader in = null;
		PrintWriter out = null;
		try{
			in = new BufferedReader(new InputStreamReader(c_socket.getInputStream()));
			out = new PrintWriter(c_socket.getOutputStream(), true); 

			out.println("220 Hello on my FTP server, if you will inactive for one minute, you'll be disconnected");

			while(true){

				request = in.readLine();
				System.out.println("Request: " + request);
				textOut.append("Request: " + request + "\n");
				t = Calendar.getInstance().getTimeInMillis();
				if((t - lefttime) > 60000){
					logged = false;
					response = "221 Connection timed out";
					out.println(response);
					System.out.println(response);
					textOut.append(response + "\n");
					lefttime = t;
				}
				else{
					lefttime = t;
					
					if(request.startsWith("USER ")){
						if(logged){
							response = "230 Already logged in";
							out.println(response);
							System.out.println(response);
							textOut.append(response + "\n");
						}
						else{
							System.out.println("Logging user " + request.substring(5));
							textOut.append("Logging user " + request.substring(5) + "\n");
							response = "331 Password required";
							out.println(response);
							System.out.println(response);
							textOut.append(response + "\n");
						}
						lastRequest = request;
					}
					else if(request.startsWith("PASS ")){
						if(lastRequest.startsWith("USER ") && !logged){
							System.out.println("user " + lastRequest.substring(5) + " pass " + request.substring(5));
							textOut.append("user " + lastRequest.substring(5) + " pass " + request.substring(5) + "\n");
							Database db = new Database();
							
							if(db.checkUser(lastRequest.substring(5), request.substring(5))){
								System.out.println("asdadadfdsdew");
								response = "230 User logged in";
								user = db.getUser(lastRequest.substring(5));
								logged = true;
							}
							else{
								
								response = "430 Invalid username or password";
							}
							out.println(response);
						}
						else{
							response = "503 Bad sequence of commands";
							out.println(response);
						}
						lastRequest = request;
					}
					
					else if(request.toUpperCase().equals("LIST")){
                        File dir = new File(currentDir);
                        File[] files = dir.listFiles();
                        c_passivesocket = s_passivesocket.accept();
                        out.println("150 Opening connection");
                        try (DataOutputStream dataOut = new DataOutputStream(c_passivesocket.getOutputStream())) {
                            String list = "";
                            Database db = new Database();
                            for(File f : files){
                                String type;
                                String acc = "";
                                String owner = "";
                                String  group = "";
                                if(f.isDirectory()) type="dir ";
                                else {
                                    type="file ";
                                    acc = db.checkAccess(f) + " ";
                                    owner = db.getOwner(f) + " ";
                                    group = db.getGroup(f) + " ";
                                }
                                
                                list = list + type + acc + owner + group + f.getName() +"\n";
                            }
                            dataOut.writeUTF(list);
                            out.println("226 Transfer complete");
                        }
                    }
					else if(request.toUpperCase().equals("PASV")){
                        if(logged){
                            int serverPort = 0;
                            s_passivesocket = new ServerSocket(serverPort);
                            passiveIP = s_passivesocket.getInetAddress().getHostAddress();
                            passiveIP = "127.0.0.1"; // local loopback
                            String[] passiveSplitIP = passiveIP.split("\\.");
                            passivePort = s_passivesocket.getLocalPort();
                            System.out.println(passivePort);
                            int p1 = passivePort >> 8;
                            System.out.println("p1 = "+p1);
                            int p2 = passivePort % 256;
                            System.out.println("p2 = "+p2);
                            response = "227 Entering Passive Mode ("+passiveSplitIP[0]+","+passiveSplitIP[1]+","+passiveSplitIP[2]+","+passiveSplitIP[3]+","+p1+","+p2+")";
                            out.println(response);
                            lastRequest = request;
                        }
                        else{
                            response = "530 Not logged in";
                            out.println(response);
                            lastRequest = request;
                        }
                    }
					
                    else if(request.toUpperCase().startsWith("STOR ")){
                        if(lastRequest.toUpperCase().equals("PASV")){
                            String filename = request.substring(5);
                            System.out.println("filename : " + filename);
                            textOut.append("filename : " + filename + "\n");
                            c_passivesocket = s_passivesocket.accept();
                            out.println("150 Opening connection");
                            File f = new File(filename);
                            if(!f.exists() || (f.exists() && user.canWrite(f))){
                            	//try with resources, automatic close rosources, add in java 1.7
                                try (RandomAccessFile file = new RandomAccessFile((filename),"rw");
                                    DataInputStream dataIn = new DataInputStream(c_passivesocket.getInputStream())) {
                                	byte[] data = new byte[1024];
                                    int offset;
                           
                                    while( (offset = dataIn.read(data)) != -1){
                                        file.write(data, 0, offset);
                                    }
                                    Database db = new Database();
                                    db.addFile(filename, user.getId(), user.getGroupId());
                                    out.println("226 Transfer complete");
                                } 
                                c_passivesocket.close();
                            }
                            else{
                                out.println("550 File don't exist (no permission to overwrite)");
                            }
                        }
                    }
                    else if(request.equals("NOOP")){
                        response = "200 Command successful";
                        out.println(response);
                        lastRequest = request;
                    }
                    else if(request.equals("QUIT")){
                        logged = false;
                        response = "221 Bye";
                        out.println(response);
                        c_socket.close();
                        return;
                    }
                    else{
                        response = "502 Command don't exist";
                        out.println(response);
                        lastRequest = request;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
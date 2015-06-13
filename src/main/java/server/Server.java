package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JTextArea;

/**
 * Main class for FTP server, set and operate threads, Thread pool method use {@link java.util.concurrent.ExecutorService}
 * 
 * @author Pawel Jaroch
 * @version 1.0
 *
 */

public class Server implements Runnable{
	
	private static final int s_port = 21;
    protected Thread thread = null;
    protected ExecutorService threadPool = Executors.newFixedThreadPool(10);
    protected ServerSocket s_socket = null;
    protected boolean run = false; 
    protected JTextArea textOut = null;
    
/**
 * Server's constructor 
 * 
 * @param textArea queries server add to command field in {@link server.ServerGUI}
 */
    public Server(JTextArea textArea){
    	textOut = textArea;
    }
    private synchronized boolean isStopped() {
        return this.run;
    }

    /**
     * Method set and operate on threads
     * 
     * {@inheritDoc}
     */
    @Override
    public void run(){
        synchronized(this){
            this.thread = Thread.currentThread();
        }
        try {
            this.s_socket = new ServerSocket(this.getsPort());
            System.out.println("Server running");
            textOut.append("Server running\n");
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        while(!isStopped()){
            Socket c_socket = null;
            try {
                c_socket = this.s_socket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server doesn't run.") ;
                    textOut.append("Server doesn't run.\n");
                    return;
                }
                System.out.println("Accept failed");
                textOut.append("Accept failed\n");
                System.exit(-1);
            }
    
            this.threadPool.execute(new CommandServer(c_socket, textOut));
        }
        
        this.threadPool.shutdown();
        System.out.println("Server doesn't run.") ;
        textOut.append("Server doesn't run.\n");
    }

	public int getsPort() {
		return s_port;
	}
	
	/**
	 * Synchronized method which stopping server
	 * 
	 */
    public synchronized void stop(){
        System.out.println("Stopping Server");
        textOut.append("Stopping Server\n");
        this.run = true;
        try {
            this.s_socket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

}


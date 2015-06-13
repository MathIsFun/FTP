package server;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextArea;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JScrollPane;

import client.Client;

/**
 * Class create gui for server, start server, stop server, adding users
 * 
 * @author Pawel Jaroch
 *@version 1.0
 */

public class ServerGUI {

	private JFrame frmFtpServer;
	private JTextArea StatusArea;
	private Server server;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerGUI window = new ServerGUI();
					window.frmFtpServer.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ServerGUI() {
		initialize();
		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmFtpServer = new JFrame();
		frmFtpServer.setTitle("FTP Server");
		frmFtpServer.setBounds(100, 100, 585, 318);
		frmFtpServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmFtpServer.getContentPane().setLayout(null);
		
		JButton btnStartServ = new JButton("Start Server");
		btnStartServ.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					btnStartServActionPerformed(e);
				}
			});
		
		btnStartServ.setBounds(23, 43, 114, 38);
		frmFtpServer.getContentPane().add(btnStartServ);
		
		JButton btnAddUser = new JButton("AddUser");
		btnAddUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AddUser add = new AddUser();
				add.setVisible(true);
			}
		});
		btnAddUser.setBounds(23, 92, 114, 32);
		frmFtpServer.getContentPane().add(btnAddUser);
		
		JButton btnStopServ = new JButton("Stop Server");
		btnStopServ.setBounds(23, 135, 114, 38);
		frmFtpServer.getContentPane().add(btnStopServ);
		btnStopServ.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnStopServActionPerformed(e);
			}
		});
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(172, 27, 373, 213);
		frmFtpServer.getContentPane().add(scrollPane);
		
		StatusArea = new JTextArea();
		StatusArea.setEditable(false);
		scrollPane.setViewportView(StatusArea);
	}
	private void btnStartServActionPerformed(ActionEvent e) {
		if(server == null)
		{
		server = new Server(StatusArea);
        new Thread(server).start();

        }   
		else StatusArea.append("Server already running!");

    }
	private void btnStopServActionPerformed(ActionEvent e) {
	if(server != null)
	{
		server.stop();
		server = null;
	}
	else StatusArea.append("Server not running\n");
	
}
}

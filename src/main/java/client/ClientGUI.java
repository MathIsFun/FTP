package client;

import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class create a gui for client ftp
 * 
 * @author Pawel Jaroch
 * @version 1.0
 *
 */
public class ClientGUI {

	private JFrame frmFtpClient;
	private JTextField HostField;
	private JButton DisconnectButton;
	private JScrollPane scrollPane_1;
	private JTextArea Command;
	private JTextField serverDirectory;
	private JTextField clientDirectory;
	private JPasswordField passwordField;
	private JTextField UserField;
	private JProgressBar progressBar;
	private JList serverList; 
	private JList clientList;
	
	private String localDir;
    private String serverDir;
    private final Client client;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientGUI window = new ClientGUI();
					window.frmFtpClient.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ClientGUI() {
		this.serverDir = System.getProperty("user.dir");
    	this.localDir = "D:\\FTP CLIENT DIRECTORY";
       // this.serverDir = "./";
		initialize();
		
        this.client = new Client(Command, progressBar);
        
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmFtpClient = new JFrame();
		frmFtpClient.setTitle("FTP client");
		frmFtpClient.setBounds(100, 100, 755, 606);
		frmFtpClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmFtpClient.getContentPane().setLayout(null);
		
		HostField = new JTextField();
		HostField.setBounds(66, 11, 109, 20);
		frmFtpClient.getContentPane().add(HostField);
		HostField.setColumns(10);
		HostField.setText("127.0.0.1");
		
		JButton ConnectButton = new JButton("Po\u0142\u0105cz");
		ConnectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 ConnectButtonActionPerformed(e);
			}
		});
		ConnectButton.setBounds(613, 102, 89, 23);
		frmFtpClient.getContentPane().add(ConnectButton);
		
		DisconnectButton = new JButton("Roz\u0142\u0105cz");
		DisconnectButton.setBounds(613, 152, 89, 23);
		frmFtpClient.getContentPane().add(DisconnectButton);
		DisconnectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 DisconnectButtonActionPerformed(e);
			}
		});
		
		scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(21, 86, 539, 110);
		frmFtpClient.getContentPane().add(scrollPane_1);
		
		Command = new JTextArea();
		scrollPane_1.setViewportView(Command);
		Command.setEditable(false);
		
		serverDirectory = new JTextField();
		serverDirectory.setBounds(334, 245, 231, 20);
		frmFtpClient.getContentPane().add(serverDirectory);
		serverDirectory.setColumns(10);
		serverDirectory.setEditable(false);
        serverDirectory.setText(serverDir);
		
		clientDirectory = new JTextField();
		clientDirectory.setColumns(10);
		clientDirectory.setBounds(21, 245, 231, 20);
		frmFtpClient.getContentPane().add(clientDirectory);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(21, 287, 231, 237);
		frmFtpClient.getContentPane().add(scrollPane_2);
		
		Object elements[][] = { { new Font("Courier", Font.BOLD, 16), Color.YELLOW, "This" },
		        { new Font("Helvetica", Font.ITALIC, 8), Color.DARK_GRAY, "Computer" } };
		clientList = new JList();
		scrollPane_2.setViewportView(clientList);
		clientDirectory.setEditable(false);
		clientList.setModel(new ClientListModel(localDir));
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientList.setCellRenderer(new ComplexCellRenderer());
        clientDirectory.setText(localDir);
        clientList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                clientListMouseClicked(e);
            }
        });
        
		JScrollPane scrollPane_3 = new JScrollPane();
		scrollPane_3.setBounds(334, 287, 231, 237);
		frmFtpClient.getContentPane().add(scrollPane_3);
		
		serverList = new JList();
		scrollPane_3.setViewportView(serverList);
		serverList.setCellRenderer(new ServerSetIcon(true));
		serverList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            	serverListMouseClicked(e);
            }
        });
		
		progressBar = new JProgressBar();
		progressBar.setBounds(208, 549, 170, 14);
		frmFtpClient.getContentPane().add(progressBar);
		
		JButton SendButton = new JButton("Wy\u015Blij na serwer");
		
		SendButton.setBounds(596, 302, 133, 20);
		frmFtpClient.getContentPane().add(SendButton);
		SendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 SendButtonActionPerformed(e);
			}
		});
		
		JButton UploadButton = new JButton("Odbierz z serwera");
		UploadButton.setBounds(596, 333, 143, 20);
		frmFtpClient.getContentPane().add(UploadButton);
		UploadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 UploadButtonActionPerformed(e);
			}
		});
		
		JButton mkdirButton = new JButton("Stw\u00F3rz katalog");
		mkdirButton.setBounds(596, 367, 133, 23);
		frmFtpClient.getContentPane().add(mkdirButton);
		mkdirButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 MkdirButtonActionPerformed(e);
			}
		});
		
		JButton rdirButton = new JButton("Usu\u0144 katalog");
		rdirButton.setBounds(596, 401, 133, 23);
		frmFtpClient.getContentPane().add(rdirButton);
		rdirButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 rdirButtonActionPerformed(e);
			}
		});
		
		JButton DeleteFileButton = new JButton("Usu\u0144 plik");
        DeleteFileButton.setBounds(596, 440, 133, 20);
        frmFtpClient.getContentPane().add(DeleteFileButton);
        DeleteFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 DeleteFileButtonActionPerformed(e);
			}
		});
        
        JButton RefreshButton = new JButton("Od\u015Bwie\u017C");
        RefreshButton.setBounds(598, 244, 104, 21);
        frmFtpClient.getContentPane().add(RefreshButton);
        RefreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 RefreshButtonActionPerformed(e);
			}
		});
        
		JLabel clientLabel = new JLabel("Klient");
		clientLabel.setBounds(105, 220, 33, 14);
		frmFtpClient.getContentPane().add(clientLabel);
		
		JLabel serverLabel = new JLabel("Serwer");
		serverLabel.setBounds(424, 220, 46, 14);
		frmFtpClient.getContentPane().add(serverLabel);
		
		JLabel commandLabel = new JLabel("Pole komend");
		commandLabel.setBounds(21, 61, 89, 14);
		frmFtpClient.getContentPane().add(commandLabel);
		
		JLabel hostLabel = new JLabel("Host");
		hostLabel.setBounds(21, 14, 35, 17);
		frmFtpClient.getContentPane().add(hostLabel);
		
		JLabel userLabel = new JLabel("U\u017Cytkownik");
		userLabel.setBounds(208, 13, 73, 18);
		frmFtpClient.getContentPane().add(userLabel);
		
		JLabel passLabel = new JLabel("Has\u0142o");
		passLabel.setBounds(459, 13, 35, 18);
		frmFtpClient.getContentPane().add(passLabel);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(504, 11, 123, 20);
		frmFtpClient.getContentPane().add(passwordField);
		
		JLabel actionLabel = new JLabel("Akcje");
		actionLabel.setBounds(641, 277, 33, 14);
		frmFtpClient.getContentPane().add(actionLabel);
		
		UserField = new JTextField();
		UserField.setColumns(10);
		UserField.setBounds(291, 11, 123, 20);
		frmFtpClient.getContentPane().add(UserField);
		
		JLabel progressLabel = new JLabel("Pasek post\u0119pu");
		progressLabel.setBounds(105, 549, 93, 14);
		frmFtpClient.getContentPane().add(progressLabel);
		
	}

	private void ConnectButtonActionPerformed(ActionEvent e){
	 try {
         serverDir = System.getProperty("user.dir");
         client.connect(HostField.getText());
         client.login(UserField.getText(), passwordField.getText());
         serverList.setModel(new ServerListModel(serverDir,client));
         DisconnectButton.setEnabled(true);
     } catch (UnknownHostException ex) {
         Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
     } catch (IOException ex) {
         Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
     }
}

	private void clientListMouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && !e.isConsumed()) {
            e.consume();
            if(clientList.getSelectedValue() != null){
                if(((File) clientList.getSelectedValue()).isDirectory()){
                    localDir = localDir + File.separator + ((File) clientList.getSelectedValue()).getName();
                    System.out.println(localDir);
                    clientDirectory.setText(localDir);
                    clientList.setModel(new ClientListModel(localDir));
                }
            }
       }
    }
	

    private void serverListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_serverListMouseClicked
        if (evt.getClickCount() == 2 && !evt.isConsumed()) {
            evt.consume();
            if(serverList.getSelectedValue() != null){
                if(((ServerFile) serverList.getSelectedValue()).toString().equals("..")){
                    if(!serverDir.substring(0, serverDir.lastIndexOf(File.separator)).isEmpty()){
                        serverDir = serverDir.substring(0, serverDir.lastIndexOf(File.separator));
                        serverDir = serverDir.substring(0, serverDir.lastIndexOf(File.separator)+1);
                        if(serverDir.equals("")) serverDir = "/";
                        System.out.println(serverDir);
                        serverDirectory.setText(serverDir);
                        serverList.setModel(new ServerListModel(serverDir, client));
                    }
                }
                else if(((ServerFile) serverList.getSelectedValue()).isDir()){
                    serverDir = serverDir + ((ServerFile) serverList.getSelectedValue()) + File.separator;
                    System.out.println(serverDir);
                    serverDirectory.setText(serverDir);
                    serverList.setModel(new ServerListModel(serverDir, client));
                }
            }
       }
    }


    private void DisconnectButtonActionPerformed(ActionEvent e) {
        try {
            serverList.setListData(new Object[0]);
            client.quit();
            DisconnectButton.setEnabled(false);
            serverDirectory.setText("/");
        } catch (IOException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void MkdirButtonActionPerformed(ActionEvent e) {
        String dirName = serverDir + "\\" +
                JOptionPane.showInputDialog("Name of the directory:").replaceAll("/", "");
        try {
            client.makeDirectory(dirName);
            serverList.setModel(new ServerListModel(serverDir,client));
        } catch (IOException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void rdirButtonActionPerformed(ActionEvent e){
    	 String dirName = serverDir + "\\" +
                 JOptionPane.showInputDialog("Name of the directory:").replaceAll("/", "");
         try {
             client.removeDirectory(dirName);
             serverList.setModel(new ServerListModel(serverDir,client));
         } catch (IOException ex) {
             Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
    	
    
    private void SendButtonActionPerformed(ActionEvent e) {
        System.out.println(localDir);
        String file = clientList.getSelectedValue().toString();
        if(new File(localDir + File.separator + file).isDirectory()){
            showMessageDialog(null, "Sending directories not supported");
            return;
        }
        try {
        	serverDir += "\\";
            client.putFile(localDir, file, serverDir);
        } catch (IOException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void UploadButtonActionPerformed(ActionEvent e) {
        System.out.println(serverDir);
        ServerFile file = (ServerFile) serverList.getSelectedValue();
        if(file.isDir()){
            showMessageDialog(null, "Sending directories not supported");
            return;
        }
        try {
        	serverDir += "\\";
            client.getFile(localDir, file.toString(), serverDir);
        } catch (IOException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void DeleteFileButtonActionPerformed(ActionEvent e) {
        String filename = serverList.getSelectedValue().toString();
        ServerFile file = (ServerFile) serverList.getSelectedValue();
        if(file.isDir()){
            showMessageDialog(null, "U¿yj Usuñ katalog");
            return;
        }
        try {
            client.deleteFile(serverDir + filename);
        } catch (IOException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void RefreshButtonActionPerformed(ActionEvent e) {
        if(client.isLogged()){
            try {
                client.noop();
                serverList.setModel(new ServerListModel(serverDir,client));
            } catch (IOException ex) {
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

class ComplexCellRenderer implements ListCellRenderer {
	  protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	  private final Border padBorder = new EmptyBorder(3,3,3,3);
	  public Component getListCellRendererComponent(JList list, Object value, int index,
	      boolean isSelected, boolean cellHasFocus) {
	    Font theFont = null;
	    Color theForeground = null;
	    String theText = null;

	    JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
	        isSelected, cellHasFocus);

	    if (value instanceof Object[]) {
	      Object values[] = (Object[]) value;
	      theFont = (Font) values[0];
	      theForeground = (Color) values[1];
	      theText = (String) values[2];
	    } else {
	      theFont = list.getFont();
	      theForeground = list.getForeground();
	      theText = "";
	    }
	    if (!isSelected) {
	      renderer.setForeground(theForeground);
	    }
	    File f = (File)value;
	    renderer.setText(f.getName());
	    renderer.setFont(theFont);
	    renderer.setBorder(padBorder);
	    renderer.setIcon(FileSystemView.getFileSystemView().getSystemIcon(f));
	    return renderer;
	  }
	}

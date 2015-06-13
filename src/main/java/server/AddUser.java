package server;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPasswordField;

/**
 * Helpful class to open a new window which add users
 * 
 * @author Pawel Jaroch
 *@version 1.0
 */
public class AddUser extends JFrame {

	private JPanel contentPane;
	private JTextField userText;
	private JButton btnDodaj;
	private JPasswordField passField;
	private Database db;
	private JTextField groupField;
	private JLabel lblGroup;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AddUser frame = new AddUser();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public AddUser() {
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		userText = new JTextField();
		userText.setBounds(76, 64, 105, 20);
		contentPane.add(userText);
		userText.setColumns(10);
		
		
		passField = new JPasswordField();
		passField.setBounds(283, 64, 112, 20);
		contentPane.add(passField);
		
		groupField = new JTextField();
		groupField.setBounds(155, 115, 105, 23);
		contentPane.add(groupField);
		groupField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("User");
		lblNewLabel.setBounds(20, 67, 46, 14);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Password");
		lblNewLabel_1.setBounds(205, 67, 68, 17);
		contentPane.add(lblNewLabel_1);
		
		btnDodaj = new JButton("Dodaj");
		btnDodaj.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String username = userText.getText();
				db = new Database();
				if(!db.checkUser(username, passField.getText())){
					
					if(db.checkGroup(groupField.getText())){
						System.out.println("makarnema");
						db.addUser(username, passField.getText());
						db.addtoGroup(username, groupField.getText());
					}
				}
				
				
			}
		});
		btnDodaj.setBounds(155, 198, 89, 23);
		contentPane.add(btnDodaj);
		
		
		
		lblGroup = new JLabel("Group");
		lblGroup.setBounds(91, 119, 46, 14);
		contentPane.add(lblGroup);
	
	}
}

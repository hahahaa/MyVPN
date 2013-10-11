package vpn;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * main
 *
 */
public class MyVPN {

	public static final String HOSTIP = "127.0.0.1";
	public static final String DEFAULTPORT = "7777";
	
	// GUI components
	public static JFrame mainFrame;
	public static JPanel panel;
	public static JPanel optPanel;
	public static JTextArea logArea;
	public static JScrollPane scrollingTextArea;
	public static JTextField ipField;
	public static JTextField portField;
	public static JTextField sendDataField;
	public static JButton sendButton;
	public static JButton contButton;
	public static JButton serverButton;
	public static JButton clientButton;
	public static JButton disconnectButton;
	
	// TCP components
	public static ServerSocket hostServer = null;
	public static Socket socket = null;
	public static BufferedReader in = null;
	public static PrintWriter out = null;
	
	public static void initPanel() {
		
		// set up log panel
		JPanel logPanel = new JPanel(new BorderLayout());
		
		sendDataField = new JTextField(40);
		sendButton = new JButton("Send");
		sendButton.setEnabled(false);
		
		logArea = new JTextArea(10, 20);
		logArea.setLineWrap(true);
		logArea.setWrapStyleWord(true);
		logArea.setEditable(false);
		logArea.setRows(20);
		scrollingTextArea = new JScrollPane(logArea);
		
		JPanel sendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		sendPanel.add(sendDataField);
		sendPanel.add(sendButton);
		logPanel.add(sendPanel, BorderLayout.NORTH);
		logPanel.add(logArea, BorderLayout.CENTER);
		
		//-------------------------------------------------------------
		// set up option panel
		//-------------------------------------------------------------
		optPanel = new JPanel(new GridLayout(6, 1));
		
		// IP panel
		JPanel ipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ipPanel.add(new JLabel("Host IP:"));
		ipField = new JTextField(10); 
		ipField.setText(HOSTIP);
		ipPanel.add(ipField);
		optPanel.add(ipPanel);
		
		// port panel
		JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		portPanel.add(new JLabel("Port:"));
		portField = new JTextField(10); 
		portField.setText(DEFAULTPORT);
		portPanel.add(portField);
		optPanel.add(portPanel);
		
		// disconnect button
		JPanel buttonPanel3 = new JPanel(new GridLayout(1, 1));
		disconnectButton = new JButton("Disconnect");
		disconnectButton.setEnabled(false);
		buttonPanel3.add(disconnectButton);
		optPanel.add(buttonPanel3);
		
		// server and client button
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
		serverButton = new JButton("Create server");
		clientButton = new JButton("Connect as client");	
		
		buttonPanel.add(serverButton);
		buttonPanel.add(clientButton);
		optPanel.add(buttonPanel);
		
		// continue button
		JPanel buttonPanel2 = new JPanel(new GridLayout(1, 1));
		contButton = new JButton("Continue");
		contButton.setEnabled(false);
		buttonPanel2.add(contButton);
		optPanel.add(buttonPanel2);
		
		//-------------------------------------------------------------
		// set up the main panel
		//-------------------------------------------------------------
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(logPanel, BorderLayout.CENTER);
		panel.add(optPanel, BorderLayout.WEST);
	}
	
	
	public static void initGUI() {
		
		initPanel();
		
		mainFrame = new JFrame("MyVPN");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setContentPane(panel);
		mainFrame.setSize(800, 500);
		mainFrame.setLocation(100, 100);
		mainFrame.pack();
		mainFrame.setVisible(true);	
	}
	
	
	public static void main(String args[]) {
		initGUI();
	}
}

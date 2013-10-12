package vpn;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

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
public class MyVPN implements Runnable {

	public static final String HOSTIP = "127.0.0.1";
	public static final int DEFAULTPORT = 7777;
	public static final int TEXTFIELDSIZE = 15;
	public static final int FRAME_HEIGHT = 500;
	public static final int FRAME_WIDTH = 800;
	public static final int DISCONNECTED = 0;
	public static final int CREATINGSERVER = 1;
	public static final int WAITINGSERVER = 2;
	public static final int CONNECTED = 3;
	public static int status = DISCONNECTED;
	
	// TCP components
	public static ServerSocket hostServer = null;
	public static Socket socket = null;
	public static Scanner in = null;
	public static PrintWriter out = null;
	public final static MyVPN myVPN = new MyVPN();
		
	// GUI components
	public static JFrame mainFrame;
	public static JPanel panel;
	public static JPanel optPanel;
	public static JTextArea logArea;
	public static JScrollPane scrollingTextArea;
	public static JTextField ipField;
	public static JTextField portField;
	public static JTextField pinField;
	public static JTextField sendDataField;
	public static JButton sendButton;
	public static JButton contButton;
	public static JButton serverButton;
	public static JButton clientButton;
	public static JButton disconnectButton;
	public static JLabel statusLabel;
	
	//public static ActionAdapter getButtonListener()
	public static void initPanel() {
		
		// set up log panel
		JPanel logPanel = new JPanel(new BorderLayout());
		
		sendDataField = new JTextField(40);
		sendButton = new JButton("Send");
		sendButton.setEnabled(false);
		
		class SendButtonListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				String msg = sendDataField.getText();
				if(msg != null && msg.length() != 0) {
					msg = msg + "\n";
					out.print(msg);
					out.flush();
				}
			}
		}
		
		ActionListener sendButListener = new SendButtonListener();
		sendButton.addActionListener(sendButListener);
		
		
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
		optPanel = new JPanel(new GridLayout(7, 1));
		
		// IP panel
		JPanel ipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ipPanel.add(new JLabel("Host IP:"));
		ipField = new JTextField(TEXTFIELDSIZE); 
		ipField.setText(HOSTIP);
		ipPanel.add(ipField);
		optPanel.add(ipPanel);
		
		// port panel
		JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		portPanel.add(new JLabel("Port     :"));
		portField = new JTextField(TEXTFIELDSIZE); 
		portField.setText(Integer.toString(DEFAULTPORT));
		portPanel.add(portField);
		optPanel.add(portPanel);
		
		// PIN panel
		JPanel pinPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pinPanel.add(new JLabel("PIN       :"));
		pinField = new JTextField(TEXTFIELDSIZE); 
		pinPanel.add(pinField);
		optPanel.add(pinPanel);
		
		// status label
		statusLabel = new JLabel();
		JPanel statusPanel = new JPanel();
		statusPanel.add(statusLabel);
		optPanel.add(statusPanel);
		
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
		
		class ServerButtonListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				serverButton.setEnabled(false);
				clientButton.setEnabled(false);
				contButton.setEnabled(true);
				disconnectButton.setEnabled(true);
				statusLabel.setText("I am a server!");
				status = CREATINGSERVER;
			}
		}
		
		class ClientButtonListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				serverButton.setEnabled(false);
				clientButton.setEnabled(false);
				contButton.setEnabled(true);
				disconnectButton.setEnabled(true);
				statusLabel.setText("I am a client!");
				status = WAITINGSERVER;
			}
		}
		
		ActionListener serButListener = new ServerButtonListener();
		serverButton.addActionListener(serButListener);
		
		ActionListener cliButListener = new ClientButtonListener();
		clientButton.addActionListener(cliButListener);
			
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
		mainFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		mainFrame.setLocation(100, 100);
		mainFrame.pack();
		mainFrame.setVisible(true);	
	}
	
	public static void doDisconnected() {
		//TODO not sure if it is done
		try {
			if(in != null)
				in.close();
			if(out != null) {
				out.flush();
				out.close();
			}
			if(hostServer != null)
				hostServer.close();	
			if(socket != null)
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void creatServer() {
		try {
			hostServer = new ServerSocket(Integer.parseInt(portField.getText()));
			logArea.append("Server: Waiting for a client......\n");
			socket = hostServer.accept();
			logArea.append("Server: Client connected\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static boolean connectAsClient() {
		//TODO authentication
		try {
			socket = new Socket(ipField.getText(), Integer.parseInt(portField.getText()));
		} catch (ConnectException e) {
			e.printStackTrace();
			return false;
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
			
	@Override
	public void run() {
		if(status == DISCONNECTED) {
			disconnectButton.setEnabled(false);
			contButton.setEnabled(false);
			serverButton.setEnabled(true);
			clientButton.setEnabled(true);
			sendButton.setEnabled(false);
		}
		else if(status == CREATINGSERVER) {
			disconnectButton.setEnabled(true);
			contButton.setEnabled(false);
			serverButton.setEnabled(false);
			clientButton.setEnabled(false);
			sendButton.setEnabled(false);
		}
		else if(status == WAITINGSERVER) {
			disconnectButton.setEnabled(true);
			contButton.setEnabled(false);
			serverButton.setEnabled(false);
			clientButton.setEnabled(false);
			sendButton.setEnabled(false);
		}
		else if(status == CONNECTED) {
			disconnectButton.setEnabled(true);
			contButton.setEnabled(true);
			serverButton.setEnabled(false);
			clientButton.setEnabled(false);
			sendButton.setEnabled(true);
		}
		
		//mainFrame.repaint();
	}
	
	public static void main(String args[]) {
		initGUI();
		
		int prevStatus = -1;
		while(true) {
			prevStatus = status;
			if(status == DISCONNECTED) {
				doDisconnected();
			}
			else if(status == CREATINGSERVER) {
				creatServer();
				status = CONNECTED;
				try {
					in = new Scanner(socket.getInputStream());
					out = new PrintWriter(socket.getOutputStream(), true);
				} catch (IOException e) {
					e.printStackTrace();	
				}
			}
			else if(status == WAITINGSERVER) {
				if(connectAsClient()) {
					logArea.append("Client: Connected to server\n");
					status = CONNECTED;
					try {
						in = new Scanner(socket.getInputStream());
						out = new PrintWriter(socket.getOutputStream(), true);
					} catch (IOException e) {
						e.printStackTrace();	
					}
				}
				else {
					logArea.append("Client: Could not connect\n");
					status = DISCONNECTED;
				}
			}
			else if(status == CONNECTED) {
				// read
				while(in.hasNext()) {
					String msg = in.nextLine();
					if(msg != null && msg.length() != 0)
						logArea.append(msg + "\n");	
				}
			}
			
			if(prevStatus != status)
				myVPN.run();
		}
		
		
	}
	
}

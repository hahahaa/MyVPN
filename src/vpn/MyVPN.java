package vpn;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * main
 *
 */
public class MyVPN implements Runnable {

	public static final String HOSTIP = "127.0.0.1";
	public static final int DEFAULTPORT = 7777;
	public static final String key = "hahahaha";
	public static final int TEXTFIELDSIZE = 15;
	public static final int FRAME_HEIGHT = 500;
	public static final int FRAME_WIDTH = 800;
	public static final int DISCONNECTED = 0;
	public static final int CREATINGSERVER = 1;
	public static final int WAITINGSERVER = 2;
	public static final int CONNECTED = 3;
	public static final int AUTHENTICATING = 4;
	public static int status = DISCONNECTED;
	public static boolean isHost = false;
	public static boolean isConButPressed = false;
	public static DESEncoder encoder = null;
	
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
					sendMessage(msg);
					logArea.append("OUT: " + msg + "\n");
					sendDataField.setText("");
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
		logPanel.add(scrollingTextArea, BorderLayout.CENTER);
		
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
		pinField.setText(key);
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
		
		class DiscButtonListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				doDisconnected();
				terminate("Disconnected\nBye Bye");
			}
		}
		
		ActionListener discButListener = new DiscButtonListener();
		disconnectButton.addActionListener(discButListener);
		
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
				statusLabel.setText("Waiting for client");
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
				statusLabel.setText("Finding server");
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
		
		class ContinueButtonListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				isConButPressed = true;
			}
		}
		
		ActionListener contButListener = new ContinueButtonListener();
		contButton.addActionListener(contButListener);
		
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
		try {
			if (hostServer != null) {
				hostServer.close();
				hostServer = null;
			}
		}
		catch (IOException e) { 
			hostServer = null; 
		}	
		try {
			if (socket != null) {
				socket.close();
				socket = null;
			}
		}
		catch (IOException e) { 
			socket = null; 
		}	
		if (in != null) {
			in.close();
			in = null;
		}		
		if (out != null) {
			out.close();
			out = null;
		}
	}
	
	public static void creatServer() {
		try {
			hostServer = new ServerSocket(Integer.parseInt(portField.getText()));
			logArea.append("Server: Waiting for a client......\n");
			socket = hostServer.accept();
			logArea.append("Server: Client connected\n");
			isHost = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static boolean connectAsClient() {
		try {
			isHost = false;
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
	
	public static void terminate(String msg) {
		JOptionPane.showMessageDialog(null, msg);
		mainFrame.setVisible(false);
		System.exit(0);
	}
	
	public static void sendMessage(String msg) {
		msg = encoder.encrypt(msg);
		msg = msg + "\n";
		logArea.append("OUT(ciphertext): " + msg);
		out.print(msg);
		out.flush();
	}
	//Ben
	public static void sendAuthenticationMessage(String msg){
		msg = msg + "\n";
		logArea.append("Authentication Out: " + msg);
		out.print(msg);
		out.flush();
	}
	
	public static String readMessage() {
		String msg = null;
		while(in.hasNext()) {
			msg = in.nextLine();
//			if(msg != null && msg.length() != 0)
//				logArea.append("IN :" + msg + "\n");
			logArea.append("IN(ciphertext): " + msg + "\n");
			msg = encoder.decrypt(msg);
			return msg;
		}
		return msg;
	}
	//Ben
	public static String readAuthenticationMessage(){
		String msg = null;
		while(in.hasNext()) {
			msg = in.nextLine();
//			if(msg != null && msg.length() != 0)
//				logArea.append("IN :" + msg + "\n");
			logArea.append("Authentication In: " + msg + "\n");
			return msg;
		}
		return msg;
	}
	//Ben
	public static void doAuthentication(boolean isHost){
		boolean hasChallenge_FromClient = false;
		boolean hasChallenge_FromServer = false;
		boolean hasAuthenString_FromServer = false;
		boolean hasAuthenString_FromClient = false;
		String clientChallenge = null;
		String serverChallenge = null;
		String encryptedAuthenStringServer = null;
		String encryptedAuthenStringClient = null;
		String clientID = "Alice";
		String serverID = "Bob";
		int i = 0;
		//if it's a server
		if(isHost){
			//
			//stage one for server
			//
			logArea.append("AuthenticationStage-Server\n");
			//wait to verify a client
			while(!hasChallenge_FromClient)
			{
				//wait for client to send out a challenge
				clientChallenge = readAuthenticationMessage();
				if(i == 1){
					hasChallenge_FromClient = true;
					logArea.append("Challenge String Received from Client\n");
					logArea.append("Challenge String: "+clientChallenge+ "\n");
				}
				i++;
			}
			
			//
			//stage two for server
			//
			//generating a encode string using 
			//the symmetric key, the client's challenge and the server's identity
			
			//Xor the ServerID and the challenge and send it back to client for verification
			waitForContinue();
			sendAuthenticationMessage(encoder.encrypt(XORencode(serverID,clientChallenge)));
			
			//send out a challenge string
			//the server's challenge can be changed
			sendAuthenticationMessage("Cocacola");
			
			//
			//stage three for server
			//
			//waiting for the authentication string from the client
			while(!hasAuthenString_FromClient){
				encryptedAuthenStringClient = readAuthenticationMessage();
				hasAuthenString_FromClient = true;
				logArea.append("Received the encrypted authentication string from the client\n");
				logArea.append("The encrypted authentication string is :" + encryptedAuthenStringClient + "\n");
			}
			waitForContinue();
			//decrypted the authentiction string and verify the client's identity
			if(!XORdecode(encoder.decrypt(encryptedAuthenStringClient),"Cocacola").equals(clientID)){
				terminate("Fail to verify the client, the program is terminated...");
			}
			else{
				logArea.append("The client's identity is successfully verified...\n");
			}
			
			
		}
		//if its a client
		else{
			//
			//stage one for client
			//
			logArea.append("AuthenticationStage-Client\n");
			//send out first message to claim the client's identity
			//this string may vary...
			waitForContinue();
			sendAuthenticationMessage("I'am Alice");
			//send out the challenge word to the server
			//the Client's Challenge can be changed
			sendAuthenticationMessage("BubbleTea");
			logArea.append("First Authentication Message Sent\n");
			
			//
			//stage two for client
			//
			//waiting for the encrypted authentication string from the server
			while(!hasAuthenString_FromServer){
				encryptedAuthenStringServer = readAuthenticationMessage();
				hasAuthenString_FromServer = true;
				logArea.append("Received the encrypted authentication string from the server\n");
				logArea.append("The encrypted authentication string is :" + encryptedAuthenStringServer + "\n");
			}
			while(!hasChallenge_FromServer){
				serverChallenge = readAuthenticationMessage();
				hasChallenge_FromServer = true;
				logArea.append("Received the challenge string from the server\n");
				logArea.append("The challenge string is :" + serverChallenge + "\n");
				
			}
			waitForContinue();
			//decrypted the authentiction string and verify the server's identity
			if(!XORdecode(encoder.decrypt(encryptedAuthenStringServer),"BubbleTea").equals(serverID)){
				terminate("Fail to verify the server, the program is terminated...");
			}
//			else{
//				logArea.append("The server's identity is successfully verified...\n");
//			}
			
			//
			//stage three for client
			//
			//generate a encrypted authentication string to the server
			sendAuthenticationMessage(encoder.encrypt(XORencode(clientID,serverChallenge)));
			logArea.append("The server's identity is successfully verified...\n");
		}
		
	}
	
	
	//Ben XOR
	public static String XORencode(String s, String key) {
        return base64Encode(xorWithKey(s.getBytes(), key.getBytes()));
    }

    public static String XORdecode(String s, String key) {
        return new String(xorWithKey(base64Decode(s), key.getBytes()));
    }

    private static byte[] xorWithKey(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i%key.length]);
        }
        return out;
    }

    private static byte[] base64Decode(String s) {
        try {
            BASE64Decoder d = new BASE64Decoder();
            return d.decodeBuffer(s);
        } catch (IOException e) {throw new RuntimeException(e);}
    }

    private static String base64Encode(byte[] bytes) {
        BASE64Encoder enc = new BASE64Encoder();
        return enc.encode(bytes).replaceAll("\\s", "");

    }
	
	
	/*
	public static void doAuthentication(boolean isHost) {
		//TODO
		boolean hasMsg = false;
		if(isHost) {	
			// server
			while(!hasMsg) {
				// wait for Alice
				String msg = readMessage();
				if(msg != null && msg.length() != 0) {
					waitForContinue();
					logArea.append("IN:" + msg + "\n");	
					if(msg.equals("I'm Alice")) {
						waitForContinue();
						// send "Bob"
						sendMessage("Bob");
						logArea.append("OUT:" + "Bob" + "\n");	
						hasMsg = true;
					}
					else {
						terminate("not Alice 1");
					}
				}
			}
			hasMsg = false;
			while(!hasMsg) {
				// wait for 3rd line
				String msg = readMessage();
				if(msg != null && msg.length() != 0) {
					waitForContinue();
					logArea.append("IN:" + msg + "\n");	
					if(msg.equals("Alice")) {
						hasMsg = true;
						logArea.append("Authenticated: This is Alice\n");
					}
					else {
						terminate("not Alice 3");
					}
				}
			}
		}
		else {
			// client
			waitForContinue();
			sendMessage("I'm Alice");
			logArea.append("OUT:" + "I'm Alice" + "\n");	
			while(!hasMsg) {
				String msg = readMessage();
				if(msg != null && msg.length() != 0) {
					waitForContinue();
					logArea.append("IN:" + msg + "\n");	
					if(msg.equals("Bob")) {
						waitForContinue();
						sendMessage("Alice");
						logArea.append("OUT:" + "Alice" + "\n");	
						hasMsg = true;
						logArea.append("Authenticated: This is Bob\n");
					}
					else {
						terminate("not Bob 2");
					}
				}
			}	
		}
	}
	*/
	public static void waitForContinue() {
		while(!isConButPressed);
		isConButPressed = false;
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
		else if(status == AUTHENTICATING) {
			disconnectButton.setEnabled(true);
			contButton.setEnabled(true);
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
		
		mainFrame.repaint();
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
				encoder = new DESEncoder(pinField.getText());
				creatServer();
				status = AUTHENTICATING;
				try {
					in = new Scanner(socket.getInputStream());
					out = new PrintWriter(socket.getOutputStream(), true);
				} catch (IOException e) {
					e.printStackTrace();	
				}
			}
			else if(status == WAITINGSERVER) {
				encoder = new DESEncoder(pinField.getText());
				if(connectAsClient()) {
					logArea.append("Client: Connected to server\n");
					status = AUTHENTICATING;
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
			else if(status == AUTHENTICATING) {
				if(isHost)
					statusLabel.setText("AUTHENTICATING - Server side");
				else
					statusLabel.setText("AUTHENTICATING - Client side");
				mainFrame.repaint();
				doAuthentication(isHost);
				status = CONNECTED;
				if(isHost)
					statusLabel.setText("I am Server(Bob)!");
				else
					statusLabel.setText("I am Client(Alice)!");
			}
			else if(status == CONNECTED) {
				// read
//				while(in.hasNext()) {
//					String msg = in.nextLine();
//					if(msg != null && msg.length() != 0)
//						logArea.append("IN:" + msg + "\n");	
//				}
				String msg = readMessage();
				if(msg != null && msg.length() != 0) 
					logArea.append("IN:" + msg + "\n");	
			}
			
			if(prevStatus != status)
				myVPN.run();
		}
		
		
	}
	
}

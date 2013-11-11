package main;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class EZSMSRecieverMain {
	ServerSocket server = null;
	Socket client = null;
	int portNumber = 21131;
	ObjectInputStream inputStream = null;
	ObjectOutputStream outputStream = null;
	Map<String, String> contacts = new HashMap<String, String>();
	Map<String, Conversation> conversations = new HashMap<String, Conversation>();
	Map<String, MessageWindow> windows = new HashMap<String, MessageWindow>();
	
	private boolean guiOnly = false;
	
	public EZSMSRecieverMain() { 
		if(!guiOnly) { 
			createMain();
		}
	}
	
	public void createMain() { 
		try {
			setUpServer();
		} catch (IOException e) {
			System.exit(0);
			e.printStackTrace();
		}
		
		/*
		 * Read inputStream to get all contacts
		 */
		while(true) { 
			try {
				Contact c = (Contact) inputStream.readObject();
				if(c.getName().equals("Last")) { 
					break;
				}
				contacts.put(c.getNumber(), c.getName());
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				break;
			}
		}
		
		System.out.println("Contacts Imported.");
		
		/*
		 * Main thread to read incoming messages
		 */
		while(true) { 
			try { 
				Message c = (Message) inputStream.readObject();
				System.out.println("-----Message Received-----");
				System.out.println(contacts.get(c.getRecipient()) + " says: " + c.getMessage());
				if(!conversations.containsKey(contacts.get(c.getRecipient()))) { 
					Conversation conversation = new Conversation(c.getRecipient());
					conversation.getMessages().add(c);
					conversations.put(contacts.get(c.getRecipient()), conversation);
					windows.put(contacts.get(c.getRecipient()), new MessageWindow(c.getRecipient(), conversation));
					windows.get(contacts.get(c.getRecipient())).setVisible(true);
				} else { 
					conversations.get(contacts.get(c.getRecipient())).getMessages().add(c);
					windows.get(contacts.get(c.getRecipient())).setVisible(true);
				}
			} catch(ClassNotFoundException | IOException e) { 
				e.printStackTrace();
				break;
			}
		}
	}
	
	public void setUpServer() throws IOException { 
		server = new ServerSocket(portNumber);
		client = server.accept();
		outputStream = new ObjectOutputStream(client.getOutputStream());
		outputStream.flush();
		inputStream = new ObjectInputStream(client.getInputStream());
		System.out.println("Connection Esablished @ " + client.getLocalPort());
	}
	
	public static void main(String[] args) { 
		new EZSMSRecieverMain();
	}
}

class Conversation { 
	private String recipient;
	private List<Message> messages = new ArrayList<Message>();
	
	public Conversation(String recipient) { 
		this.recipient = recipient;
	}
	
	public String getRecipient() { return recipient; }
	public List<Message> getMessages() { return messages; }
}

class MessageWindow extends JFrame { 
	private Conversation conversation;
	JScrollPane scrollPane;
	
	public MessageWindow(String recipient, Conversation conversation) { 
		super(recipient);
		this.conversation = conversation;
		scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		for(Message m : conversation.getMessages()) { 
			JTextPane jtp = new JTextPane();
			jtp.setText(m.getMessage());
			if(m.getStatus() == Message.Status.INCOMING) {
				jtp.setAlignmentX(JTextPane.LEFT_ALIGNMENT);
			} else { 
				jtp.setAlignmentX(JTextPane.RIGHT_ALIGNMENT);
			}
			scrollPane.getViewport().add(jtp);
		}
		this.add(scrollPane);
		this.setSize(200, 600);
	}
	
	public void updateMessages() { 
		for(Message m : conversation.getMessages()) { 
			JTextPane jtp = new JTextPane();
			jtp.setText(m.getMessage());
			if(m.getStatus() == Message.Status.INCOMING) {
				jtp.setAlignmentX(JTextPane.LEFT_ALIGNMENT);
			} else { 
				jtp.setAlignmentX(JTextPane.RIGHT_ALIGNMENT);
			}
			scrollPane.getViewport().add(jtp);
		}
		this.update(getGraphics());
	}
}

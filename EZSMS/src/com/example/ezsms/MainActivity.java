package com.example.ezsms;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import main.Contact;

import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	List<Contact> contactList = new ArrayList<Contact>();
	private Button connectToHostButton;
	private TextView statusTextView;
	private EditText hostIPAddress;
	private ProgressDialog connectionProgress;
	private Handler connectionThreadHandler = new Handler();
	public static Context context;
	private MenuItem quit;
	private MenuItem changePortNumber;
	public boolean JSONConnection = false;
	private int portNumber = 21131;
	
	public static InputStream is = null;
	public static OutputStream os = null;
	
	public static Socket socket = null;
	public static ObjectInputStream inputStream = null;
	public static ObjectOutputStream outputStream = null;
	
	@Override
	public void onPostCreate(Bundle savedInstanceState) { 
		super.onPostCreate(savedInstanceState);
		getContacts(this.getContentResolver());
		statusTextView.setText(R.string.post_contact_population);
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, MyService.class));
        context = this;
        quit = (MenuItem) this.findViewById(R.id.menu_quit);
        connectToHostButton = (Button) this.findViewById(R.id.connectButton);
        statusTextView = (TextView) this.findViewById(R.id.statusTextView);
        hostIPAddress = (EditText) this.findViewById(R.id.hostIP);
        
        OnClickListener connectToHostListener = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				connectionProgress = new ProgressDialog(MainActivity.this);
				connectionProgress.setCancelable(true);
				connectionProgress.setMessage("Connecting to Host IP");
				connectionProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				connectionProgress.setIndeterminate(true);
				connectionProgress.show();
				Runnable r = new Runnable() { 
					@Override
					public void run() { 
						try {
							connectToServer();
							connectionThreadHandler.post(new Runnable() { 
								public void run() { 
									Toast.makeText(MainActivity.this, "Connection Esablished", Toast.LENGTH_LONG).show();
								}
							}); 
							for(Contact c : contactList) { 
								outputStream.writeObject(c);
								outputStream.flush();
							}
							Contact last = new Contact();
							last.setName("Last");
							last.setNumber("Last");
							outputStream.writeObject(last);
							outputStream.flush();
						} catch (UnknownHostException e) {
							connectionThreadHandler.post(new Runnable() { 
								public void run() { 
									Toast.makeText(MainActivity.this, "Error: Unkown Host Exception", Toast.LENGTH_LONG).show();
								}
							});
							e.printStackTrace();
						} catch (IOException e) {
							connectionThreadHandler.post(new Runnable() { 
								public void run() { 
									Toast.makeText(MainActivity.this, "Error: IO Exception", Toast.LENGTH_LONG).show();
								}
							});
							e.printStackTrace();
						}
						connectionThreadHandler.post(new Runnable() { 
							@Override
							public void run() { 
								connectionProgress.dismiss();
							}
						});
					}
				};
				Thread t = new Thread(r);
				t.start();
			}
        };
        connectToHostButton.setOnClickListener(connectToHostListener);
    }
    
    public void connectToServer() throws UnknownHostException, IOException { 
    	Log.d("SERVER CONNECTION", hostIPAddress.getText().toString());
    	socket = new Socket(hostIPAddress.getText().toString(), portNumber);
    	Log.d("SERVER CONNECTION", "Socket Created");
    	outputStream = new ObjectOutputStream(socket.getOutputStream());
    	Log.d("SERVER CONNECTION", "OutputStream Established");
    	outputStream.flush();
    	inputStream = new ObjectInputStream(socket.getInputStream());
    	Log.d("SERVER CONNECTION", "InputStream Established");
    }
    
    public void getContacts(ContentResolver cr) {
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
            	Contact contact = new Contact();
            	// read id
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                // read names 
                String displayName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                contact.setName(displayName);
                // Phone Numbers 
                Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
                while (pCur.moveToNext()) {
                    String number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if(number.startsWith("+1")) { 
                    	number = number.replace("+1", "");
                    }
                    if(number.contains("-")) { 
                    	number = number.replace("-", "");
                    }
                    contact.setNumber(number);
                    @SuppressWarnings("unused")
					String typeStr = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                }
                if(contact.getName() != null && contact.getNumber() != null) {
                	boolean hasSameName = false;
                	for(Contact c : contactList) { 
                		if(c.getName().equals(contact.getName())) { 
                			hasSameName = true;
                		}
                	}
                	if(!hasSameName) {
                		contactList.add(contact);
                	}
                }
                pCur.close();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) { 
    	switch(item.getItemId()) { 
    	case R.id.menu_quit:
    		System.exit(0);
    		return true;
    	case R.id.menu_change_port:
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle("Change Port Number");

    		// Set up the input
    		final EditText input = new EditText(this);
    		// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
    		input.setInputType(InputType.TYPE_CLASS_TEXT);
    		builder.setView(input);

    		// Set up the buttons
    		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
    		    @Override
    		    public void onClick(DialogInterface dialog, int which) {
    		    	try {
    		    		int tempPortNum = Integer.parseInt(input.getText().toString());
    		    		if(tempPortNum >= 1024 && tempPortNum <= 65535) { 
    		    			portNumber = tempPortNum;
    		    			Toast.makeText(getApplicationContext(), 
    		    					"Port Number Changed To " + Integer.toString(portNumber), 
    		    					Toast.LENGTH_LONG).show();
    		    		} else { 
    		    			Toast.makeText(getApplicationContext(), 
    		    					"Invalid Port Number", 
    		    					Toast.LENGTH_LONG).show();
    		    		}
    		    	} catch (NumberFormatException e) { 
    		    		Toast.makeText(getApplicationContext(), 
    		    				"Invalid Port Number", 
    		    				Toast.LENGTH_LONG).show();
    		    	}
    		    }
    		});
    		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    		    @Override
    		    public void onClick(DialogInterface dialog, int which) {
    		        dialog.cancel();
    		    }
    		});
    		builder.show();
    		return true;
		default:
    		return super.onOptionsItemSelected(item);	
    	}
    }
    
    public static void makeToast(String s) { 
    	Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
    
}

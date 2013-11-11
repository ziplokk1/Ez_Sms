package com.example.ezsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import main.Message;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {
	final SmsManager sms = SmsManager.getDefault();
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		final Bundle bundle = arg1.getExtras();
		try { 
			if(bundle != null) { 
				final Object[] pdusObj = (Object[]) bundle.get("pdus");
				for(int i = 0; i < pdusObj.length; i++) { 
					SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
		            String phoneNumber = currentMessage.getDisplayOriginatingAddress();
		             
		            String senderNum = phoneNumber;
		            String message = currentMessage.getDisplayMessageBody();
		 
		            Log.i("SmsReceiver", "senderNum: "+ senderNum + "; message: " + message);
		            Message sendMessage = new Message(senderNum, message, Message.Status.INCOMING);
		            MainActivity.outputStream.writeObject(sendMessage);
				}
			}
		} catch (Exception e) {
			Log.e("SMSReciever", "Exception: " + e.getLocalizedMessage());
		}
	}
}

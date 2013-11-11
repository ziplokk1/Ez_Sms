package com.example.ezsms;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MyService extends Service {
	
	@Override
	public void onDestroy() { 
		Log.i("MyService", "Service Destroyed");
	}
	
	@Override
	public void onCreate() {
		Log.i("MyService", "Service Created");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) { 
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}

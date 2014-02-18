package cn.cloudchain.yboxserver;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.util.Log;
import cn.cloudchain.yboxserver.helper.WifiApManager;
import cn.cloudchain.yboxserver.server.TcpServer;
import cn.cloudchain.yboxserver.server.UdpServer;

public class MyApplication extends Application {
	final String TAG = MyApplication.class.getSimpleName();
	private static MyApplication instance;
	public int battery = -1;
	public boolean isBatteryLow = false;

	public int signalStrength = -1;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		Log.i(TAG, "onCreate");

		new Thread(new Runnable() {

			@Override
			public void run() {
				openHotspot();
			}
		}).start();

		Intent service = new Intent(this, TcpServer.class);
		startService(service);

		Intent udp = new Intent(this, UdpServer.class);
		startService(udp);

		// IntentFilter timeTickFilter = new
		// IntentFilter(Intent.ACTION_TIME_TICK);
		// registerReceiver(new TimeTickBroadcastReceiver(), timeTickFilter);

		// IntentFilter batteryFilter = new IntentFilter();
		// batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		// batteryFilter.addAction(Intent.ACTION_BATTERY_LOW);
		// registerReceiver(new BatteryInfoBroadcastReceiver(), batteryFilter);
		//
		// TelephonyManager telManager = (TelephonyManager)
		// getSystemService(Context.TELEPHONY_SERVICE);
		// telManager.listen(new PhoneStateMonitor(telManager),
		// PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}

	public static Context getAppContext() {
		return instance;
	}

	public static MyApplication getInstance() {
		return instance;
	}

	private void openHotspot() {
		WifiApManager wifiApManager = new WifiApManager(this);
		WifiConfiguration config = wifiApManager.getWifiApConfiguration();
		if (config == null) {
			config = new WifiConfiguration();
			config.SSID = "INIT";
			config.preSharedKey = "11111111";
		}
		config.hiddenSSID = false;
		boolean result = wifiApManager.setWifiApEnabled(config, true);
		Log.i(TAG, "enable result = " + result + ";pass = "
				+ config.preSharedKey + ";ssid = " + config.SSID);
	}

}
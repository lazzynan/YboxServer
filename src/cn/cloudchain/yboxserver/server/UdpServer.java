package cn.cloudchain.yboxserver.server;

import java.io.IOException;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.IBinder;
import android.util.JsonWriter;
import android.util.Log;
import cn.cloudchain.yboxserver.MyApplication;
import cn.cloudchain.yboxserver.helper.Helper;
import cn.cloudchain.yboxserver.helper.PhoneManager;

public class UdpServer extends Service {
	final String TAG = UdpServer.class.getSimpleName();
	private PhoneManager phoneManager;
	private ScheduledExecutorService executor;
	private DatagramSocket socket;
	private final int PORT = 12345;
	private final String defaultHost = "192.168.43.255";
	// private boolean lastEthernet = MyApplication.getInstance().isEthernet;

	private MulticastLock multiLock;

	@Override
	public void onCreate() {
		super.onCreate();
		phoneManager = new PhoneManager(this);
		executor = Executors.newScheduledThreadPool(1);

		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		multiLock = wifi.createMulticastLock(TAG);
		multiLock.acquire();
		Log.i(TAG, "onCreate");
		Helper.getInstance().getBroadcastAddress();
	}

	@Override
	public void onDestroy() {
		if (executor != null) {
			executor.shutdownNow();
			executor = null;
		}

		if (multiLock != null) {
			multiLock.release();
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");
		executor.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				sendUdpBroadcast();
			}
		}, 0, 3, TimeUnit.SECONDS);
		return START_STICKY;
	}

	private void generateSocket() {
		Log.i(TAG, "generate socket");
		// InetAddress broadcastAddress = null;
		// InetAddress broadcastAddress = Helper.getInstance()
		// .getBroadcastAddress();

		try {
			socket = new DatagramSocket(PORT);
			socket.setReuseAddress(true);
			socket.setBroadcast(true);
			SocketAddress address = new InetSocketAddress(defaultHost, PORT);
			socket.connect(address);
		} catch (SocketException e) {
			e.printStackTrace();
		}

	}

	private void sendUdpBroadcast() {
		if (socket == null) {
			generateSocket();
		}
		try {
			String message = generateContent();
			byte[] data = message.getBytes();
			DatagramPacket pack = new DatagramPacket(data, data.length);
			socket.send(pack);

			Log.i(TAG, "send udp broadcast");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 生成UDP广播的内容
	 * 
	 * @return
	 */
	private String generateContent() {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name("conn");
			if (MyApplication.getInstance().isEthernet) {
				jWriter.value(1);
			} else if (!MyApplication.getInstance().isSIMReady) {
				jWriter.value(0);
			} else if (phoneManager.isMobileDataEnabled()) {
				jWriter.value(2);
			} else {
				jWriter.value(3);
			}
			if (MyApplication.getInstance().isBatteryLow) {
				jWriter.name("battery_low").value(true);
			}
			jWriter.name("clients_update_time").value(
					MyApplication.getInstance().clientUpdateTime);
			jWriter.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sw.toString();
	}

}

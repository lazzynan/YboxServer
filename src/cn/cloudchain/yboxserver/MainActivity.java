package cn.cloudchain.yboxserver;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Intent service = new Intent(this, UdpServer.class);
		// startService(service);
	}

}

package cn.cloudchain.yboxserver.server;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import cn.cloudchain.yboxcommon.bean.DeviceInfo;
import cn.cloudchain.yboxcommon.bean.Types;
import cn.cloudchain.yboxserver.helper.PreferenceHelper;
import cn.cloudchain.yboxserver.helper.WifiApManager;

import com.ybox.hal.BSPSystem;

/**
 * 获取当前的连接设备个数，如果个数为0，则停止服务； 若当前自动休眠模式为AUTO_SLEEP_OFF，则停止服务
 * 所以每次在启动该服务前必须先stopService
 * 
 * @author lazzy
 * 
 */
public class TimetickServer extends Service {
	private final String TAG = TimetickServer.class.getSimpleName();
	private Timer timer;
	private TimerTask timerTask;
	private long delay = 0L;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		List<DeviceInfo> devices = (new WifiApManager(this))
				.getDeviceList(Types.DEVICES_UNBLOCK);
		delay = 0;
		if (devices == null || devices.size() == 0) {
			int autoSleepType = PreferenceHelper.getInt(
					PreferenceHelper.AUTO_SLEEP, Types.AUTO_SLEEP_OFF);
			switch (autoSleepType) {
			case Types.AUTO_SLEEP_FOR_10:
				delay = 10 * 60 * 1000;
				break;
			case Types.AUTO_SLEEP_FOR_30:
				delay = 30 * 60 * 1000;
				break;
			}
		}

		if (delay > 0) {
			startTimer();
		} else {
			endTimer();
			this.stopSelf();
		}
		return onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		endTimer();
		super.onDestroy();
	}

	private void startTimer() {
		Log.i(TAG, "start time ticker");
		if (timer == null) {
			timer = new Timer();
		}
		if (timerTask == null) {
			timerTask = new TimerTask() {

				@Override
				public void run() {
					new BSPSystem(TimetickServer.this).goToSleep();
				}
			};
		}
		timer.schedule(timerTask, delay);
	}

	private void endTimer() {
		Log.i(TAG, "end time ticker");
		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
	}
}

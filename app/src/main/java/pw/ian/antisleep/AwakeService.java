package pw.ian.antisleep;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ian on 10/11/14.
 */
public class AwakeService extends Service {
    private final IBinder binder = new AwakeBinder();
    private SensorManager sensorManager;
    private LegMovementDetector detector;
    private Timer timer = new Timer();

    private long lastActivity = System.currentTimeMillis();
    private boolean alarming = false;

    public class AwakeBinder extends Binder {
        AwakeService getService() {
            return AwakeService.this;
        }
    }

    @Override
    public void onCreate() {
        Toast.makeText(AwakeService.this.getApplicationContext(), "serviuce ready", Toast.LENGTH_SHORT);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        detector = new LegMovementDetector(sensorManager);
        detector.addListener(new LegMovementDetector.ILegMovementListener() {
            @Override
            public void onLegActivity(int activity) {
                if (activity > 0) {
                    lastActivity = System.currentTimeMillis();
                    if (alarming) {
                        // send the alarm stop
                        Intent intent = new Intent("AwakeAlarmStop");
                        LocalBroadcastManager.getInstance(AwakeService.this.getApplicationContext()).sendBroadcast(intent);
                        alarming = false;
                    }
                }
            }
        });
        detector.startDetector();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - lastActivity > 10000 && !alarming) { // # seconds
                    // send the alarm
                    Intent intent = new Intent("AwakeAlarm");
                    LocalBroadcastManager.getInstance(AwakeService.this.getApplicationContext()).sendBroadcast(intent);
                    alarming = true;
                }
            }
        }, 2000L,  500L);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}

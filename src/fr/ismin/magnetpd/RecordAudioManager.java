package fr.ismin.magnetpd;

import java.io.File;

import org.puredata.core.PdBase;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;


public class RecordAudioManager {
	
	private static MainActivity mainActivity;

	private static boolean recording = false;
	private static float countRecord;
	private static SharedPreferences sharedPreferences;
	private static File file;
	
	public RecordAudioManager(MainActivity mA) {
		mainActivity = mA;
		init();
	}
	
	public static void init() {
		
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
		//preferences are used to fill field, if any default value is used.
		countRecord = sharedPreferences.getFloat("count_record", (float)0);
		
		file = new File("/mnt/sdcard/pd_record/");
		file.mkdir();	
	}
	
	
	public void record() {
		if (recording) {
			countRecord++;
			sharedPreferences.edit().putFloat("count_record", countRecord).commit();
			PdBase.sendBang("stop_record");
			Toast.makeText(mainActivity, R.string.info_msg, Toast.LENGTH_LONG).show();
			recording = !recording;
		} else {
			PdBase.sendFloat("countRecord", countRecord);
			PdBase.sendBang("start_record");
			recording = !recording;        	
		}
	}	
}

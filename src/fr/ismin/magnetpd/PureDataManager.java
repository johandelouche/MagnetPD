package fr.ismin.magnetpd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import org.puredata.android.service.PdPreferences;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;
import org.puredata.core.utils.IoUtils;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class PureDataManager implements
		SharedPreferences.OnSharedPreferenceChangeListener {

	private MainActivity mainActivity;
	private static final String TAG = "PureDataManager";

	private PdService pdService = null;
	private int openedPatch;
	private AssetManager assetMgr;
	private ArrayList<String> patchList;
	private PdReceiver receiver;

	private final ServiceConnection pdConnection;

	protected PureDataManager(MainActivity mA) {
		super();
		mainActivity = mA;
		pdConnection = initPdConnection();
		initPDFunctions();
	}

	/********************************************************************/
	/** Pure Data *************************************************/
	/********************************************************************/

	protected void initPDFunctions() {
		// *
		assetMgr = mainActivity.getAssetManager();
		patchList = mainActivity.getPatchList();
		// */
		initPdReceiver();
		PdPreferences.initPreferences(mainActivity.getApplicationContext());
		PreferenceManager.getDefaultSharedPreferences(
				mainActivity.getApplicationContext())
				.registerOnSharedPreferenceChangeListener(this);
		mainActivity.bindService(new Intent(mainActivity, PdService.class),
				pdConnection, Context.BIND_AUTO_CREATE);
		PdBase.sendFloat("vol", 0.5f);
	}

	protected void loadPatch(String path) {
		File patchFile = null;
		try {
			InputStream in = assetMgr.open("patches/" + path);
			patchFile = IoUtils.extractResource(in, "theremin.pd",
					mainActivity.getCacheDir());
			PdBase.closePatch(openedPatch);
			openedPatch = PdBase.openPatch(patchFile);
			startAudio();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			mainActivity.finish();
		} finally {
			if (patchFile != null)
				patchFile.delete();
		}
	}

	private void initPd() {
		File patchFile = null;
		try {
			PdBase.setReceiver(receiver);
			PdBase.subscribe("android");
			PdBase.subscribe("min");
			PdBase.subscribe("sec");
			InputStream in = assetMgr.open("patches/" + patchList.get(0));
			patchFile = IoUtils.extractResource(in, "theremin.pd",
					mainActivity.getCacheDir());
			openedPatch = PdBase.openPatch(patchFile);
			startAudio();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			mainActivity.finish();
		} finally {
			if (patchFile != null)
				patchFile.delete();
		}
	}

	protected void startAudio() {
		String name = mainActivity.getResources().getString(R.string.app_name);
		try {

			pdService.initAudio(-1, -1, -1, -1); // negative values will be
													// replaced with
													// defaults/preferences
			pdService.startAudio(new Intent(mainActivity, MainActivity.class),
					R.drawable.icon, name, "Return to " + name + ".");
		} catch (IOException e) {
			mainActivity.toast(e.toString());
		}
	}

	protected void cleanup() {
		try {
			mainActivity.unbindService(pdConnection);
		} catch (IllegalArgumentException e) {
			// already unbound
			pdService = null;
		}
	}

	private void initPdReceiver() {
		receiver = new PdReceiver() {

			private void pdPost(String msg) {
				mainActivity.toast("Pure Data says, \"" + msg + "\"");
			}

			@Override
			public void print(String s) {
				mainActivity.post(s);
			}

			@Override
			public void receiveBang(String source) {
				pdPost("bang");

			}

			@Override
			public void receiveFloat(String source, float x) {

				if (source.equalsIgnoreCase("min")) {
					mainActivity.updateMin((int) x);
				} else if (source.equalsIgnoreCase("sec")) {
					mainActivity.updateSec((int) x);
				} else {
					pdPost("float: " + x);
				}

			}

			@Override
			public void receiveList(String source, Object... args) {
				pdPost("list: " + Arrays.toString(args));
			}

			@Override
			public void receiveMessage(String source, String symbol,
					Object... args) {
				pdPost("message: " + Arrays.toString(args));
			}

			@Override
			public void receiveSymbol(String source, String symbol) {
				pdPost("symbol: " + symbol);
			}
		};
	}

	private ServiceConnection initPdConnection() {
		return new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				pdService = ((PdService.PdBinder) service).getService();
				initPd();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// this method will never be called
			}
		};
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		startAudio();
	}

}

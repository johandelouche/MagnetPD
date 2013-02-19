package fr.ismin.magnetpd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.puredata.core.PdBase;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class LayoutFragment extends Fragment implements OnItemSelectedListener {
	
	private MainActivity mainActivity;
	private View view;
	
	private Toast toast = null;
	
	
	private TextView magnStrengthTextView;
	private TextView count;
	private TextView logs;
	private CheckBox mute;
	
	private AssetManager assetMgr;
	private ArrayList<String> patchList;
	private Spinner patchSelector;
	private ArrayAdapter<String> spinnerAdapter;
	
	private static final String TAG = "LayoutFragment";
	
	
	   @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
	        Bundle savedInstanceState) {
		   mainActivity = (MainActivity) getActivity();
		   view = inflater.inflate(R.layout.layout_fragment, container, false);
		   initGUI();
	        // Inflate the layout for this fragment
	        return view;
	    }
	   
	   public void initGUI(){

			
			magnStrengthTextView = ((TextView) view.findViewById(R.id.magn_strength));
			count = ((TextView) view.findViewById(R.id.count));
			mute = ((CheckBox) view.findViewById(R.id.mute));
			logs = ((TextView) view.findViewById(R.id.logs));
			logs.setMovementMethod(new ScrollingMovementMethod());
			
			patchSelector = ((Spinner) view.findViewById(R.id.patchSelector));
			patchSelector.setOnItemSelectedListener(this);
			assetMgr = mainActivity.getAssets(); 
			patchList = (ArrayList<String>) displayFiles(assetMgr,"patches");
			spinnerAdapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_spinner_dropdown_item, patchList);
			patchSelector.setAdapter(spinnerAdapter);
			
		}
	   
		public void redraw(double magneticStrength){
			magnStrengthTextView.setText("Magnetic Strength: "+ magneticStrength);
		}
		
		public void mute() {
			if (mute.isChecked()) {
				PdBase.sendFloat("mute", 0);
			} 
			else {
				PdBase.sendFloat("mute", 1);
			}
		}
		
		protected void updateCount(int min, int sec) {
			/*
			 * Need to be improve using android.widget.Chronometer class that extends android.widget.TextView class.
			 */
			String time;
			if (min < 10) {
				time = "0"+min+":";
			} else {
				time = min+":";
			}
			if (sec < 10) {
				time += "0"+sec+"";
			} else {
				time += sec+"";
			}
			final String t = time;
			mainActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					count.setText(t);
				}
			});
		}
		
		protected void toast(final String msg) {
			mainActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (toast == null) {
						toast = Toast.makeText(mainActivity.getApplicationContext(), "", Toast.LENGTH_SHORT);
					}
					toast.setText(TAG + ": " + msg);
					toast.show();
				}
			});
		}
		
		protected void post(final String s) {

			mainActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					logs.append(s + ((s.endsWith("\n")) ? "" : "\n"));
				}
			});
			
		}

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			post("opening : "+patchList.get(pos));
			//pureDataManager.loadPatch(patchList.get(pos));
			mainActivity.loadPatch(patchList.get(pos));
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}


		private List<String> displayFiles (AssetManager mgr, String path) {
		    List<String> patchList = null;
			try {
		        String list[] = mgr.list(path);
		        patchList = new ArrayList<String>(Arrays.asList(list));
		        if (list != null)
		            for (int i=0; i<list.length; ++i)
		                {
		                    Log.v("Assets:", path +"/"+ list[i]);
		                    displayFiles(mgr, path + "/" + list[i]);
		                }
		    } catch (IOException e) {
		        Log.v("List error:", "can't list" + path);
		    }
			return patchList;
		}
		
		protected ArrayList<String> getPatchList(){
			return patchList;
		}

		protected AssetManager getAssetManager(){
			return assetMgr;
		}
	   

}

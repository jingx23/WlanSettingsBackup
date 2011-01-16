package de.snertlab.wlanconfigsaver;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ConfigSaver extends Activity {
    /** Called when the activity is first created. */
	
	private static final String WPA_SUPPLICANT_FILENAME = "wpa_supplicant.conf";
	private static final String WPA_SUPPLICANT_PATH = "/data/misc/wifi/" + WPA_SUPPLICANT_FILENAME;
	private static final String BACKUP_FILENAME = "wpa_supplicant.conf.bak";
	private static final String BACKUP_PATH = "/sdcard/" + BACKUP_FILENAME;
	
	private String catpath;
	
	private TextView txtViewInfo;
	private boolean catPathFound;
	private boolean isRoot;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        txtViewInfo = (TextView) findViewById(R.id.TextView01);
        doInit();
        checkAllOk();
    }
    
    private void checkAllOk(){
    	String errMessage = null;
    	if(!isRoot){
    		errMessage = "No root permission!";
    	}else if(!catPathFound){
    		errMessage = "No cat path found!";
    	}
    	if(errMessage!=null) {
    		Dialog dialog = Common.createErrorDialog(this, errMessage);
    		dialog.show();
    	}
    }
    
    private void doInit(){
    	catpath 		  = Common.findCatPath();
    	catPathFound 	  = (catpath != null);
    	isRoot 			  = Common.hazIGotRoot();
    	boolean isAllOk	  = catPathFound && isRoot;
    	Button btnBackup  = (Button) findViewById(R.id.Button01);
    	Button btnRestore = (Button) findViewById(R.id.Button02);
    	
    	btnBackup.setEnabled(isAllOk);
    	btnRestore.setEnabled(isAllOk);    	
    }
    
    public void btnClickHandlerBackup(View view) throws IOException, InterruptedException {
    	Common.runAsRoot(catpath + " " + WPA_SUPPLICANT_PATH + " > " + BACKUP_PATH + "\n");
    	File file = new File(BACKUP_PATH);
    	txtViewInfo.setText("backup " + (file.exists() ? "successful " + BACKUP_PATH : "failed"));
    }
    
    public void btnClickHandlerRestore(View view) throws IOException, InterruptedException {
    	File file = new File(BACKUP_PATH);
    	if(!file.exists()){
    		Common.createAlertDialog(this, "Backup file not found: " + BACKUP_PATH).show();
    		return;
    	}
    	Common.runAsRoot(catpath + " " + BACKUP_PATH + " > " + WPA_SUPPLICANT_PATH + "\n");
    	txtViewInfo.setText("restore successful"); //TODO: Richtige Prüfung einbauen ob successful oder nicht, z.B. ueber Datum
    }

}
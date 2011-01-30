package de.snertlab.wlanconfigsaver;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class ConfigSaver extends Activity {
    /** Called when the activity is first created. */
	
	private static final String TAG = "wlanSettingsBackup.ActivityConfigSaver";
	private static final String PACKAGE = "de.snertlab.wlanconfigsaver";
	
	private static final String WPA_SUPPLICANT_FILENAME = "wpa_supplicant.conf";
	private static final String WPA_SUPPLICANT_PATH = "/data/misc/wifi/" + WPA_SUPPLICANT_FILENAME;
	private static final String BACKUP_FILENAME = "wpa_supplicant.conf.bak";
	private static final String BACKUP_PATH = "/sdcard/" + BACKUP_FILENAME;
	
	private String catpath;
	
	private TextView txtViewInfo;
	private boolean catPathFound;
	private boolean isRoot;
	private CheckBox checkBackupSendMail;
	
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
    		Log.w(TAG, errMessage);
    		Dialog dialog = Common.createErrorDialog(this, errMessage);
    		dialog.show();
    	}
    }
    
    private void doInit(){
    	Log.d(TAG, "doInit start");
    	catpath 		  = Common.findCatPath();
    	catPathFound 	  = (catpath != null);
    	isRoot 			  = Common.hazIGotRoot();
    	String version 	  = getVersionInfo();
    	boolean isAllOk	  = catPathFound && isRoot;
    	Button btnBackup  = (Button) findViewById(R.id.Button01);
    	Button btnRestore = (Button) findViewById(R.id.Button02);
    	checkBackupSendMail = (CheckBox) findViewById(R.id.checkBox1);
    	
    	btnBackup.setEnabled(isAllOk);
    	btnRestore.setEnabled(isAllOk);

    	this.setTitle( this.getTitle() + " v" + version);
    	
    	Log.i(TAG, "Version: " + version);
    	Log.i(TAG, "Cat path: " + catpath);
    	Log.d(TAG, "doInit end");
    }
    
    public void btnClickHandlerBackup(View view) throws IOException, InterruptedException {
    	Log.d(TAG, "btnClickHandlerBackup start");
    	Common.runAsRoot(catpath + " " + WPA_SUPPLICANT_PATH + " > " + BACKUP_PATH + "\n");
    	File file = new File(BACKUP_PATH);
    	String backupInfoMessage = "backup " + (file.exists() ? "successful " + BACKUP_PATH : "failed");
    	txtViewInfo.setText(backupInfoMessage);
    	if(checkBackupSendMail.isChecked() && file.exists()){
    		Intent emailIntent = Common.createMailIntent(new String[]{""}, "Wlan Backup & Restore", "", file);
    		startActivity(Intent.createChooser(emailIntent, "Wlan Backup & Restore")); 
    	}
    	Log.i(TAG, backupInfoMessage);
    	Log.d(TAG, "btnClickHandlerBackup end");
    }
    
    public void btnClickHandlerRestore(View view) throws IOException, InterruptedException {
    	Log.d(TAG, "btnClickHandlerRestore start");
    	File file = new File(BACKUP_PATH);
    	if(!file.exists()){
    		Common.createAlertDialog(this, "Backup file not found: " + BACKUP_PATH).show();
    		return;
    	}
    	Common.runAsRoot(catpath + " " + BACKUP_PATH + " > " + WPA_SUPPLICANT_PATH + "\n");
    	txtViewInfo.setText("restore successful"); //TODO: Richtige Prüfung einbauen ob successful oder nicht, z.B. ueber Datum
    	Log.d(TAG, "btnClickHandlerRestore end");
    }
    
    private String getVersionInfo(){
    	PackageInfo pInfo  = null;
    	String versionName = null;
    	try {
    		pInfo = getPackageManager().getPackageInfo(PACKAGE, PackageManager.GET_META_DATA);
    		versionName = pInfo.versionName;
    	} catch (NameNotFoundException e) {
    		Log.e(TAG, "", e);
    	}
    	return versionName;
    }

}
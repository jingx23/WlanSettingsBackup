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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class ConfigSaver extends Activity {
    /** Called when the activity is first created. */
	
	public static final String TAG = "wlanSettingsBackup.ActivityConfigSaver";
	private static final String PACKAGE = "de.snertlab.wlanconfigsaver";
	
	private static final String WPA_SUPPLICANT_FILENAME = "wpa_supplicant.conf";
	private static final String WPA_SUPPLICANT_PATH = "/data/misc/wifi/" + WPA_SUPPLICANT_FILENAME;
	private static final String BACKUP_FILENAME = "wpa_supplicant.conf.bak";
	private static final String BACKUP_PATH = "/sdcard/" + BACKUP_FILENAME;
	
	private String catpath;
	
	private boolean catPathFound;
	private boolean isRoot;
	private CheckBox checkBackupSendMail;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        doInit();
        checkAllOk();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		 switch (item.getItemId()) {
		    case R.id.menuQuit:
		        quit();
		        return true;
		    default:
		        return super.onOptionsItemSelected(item);
		    }
	}
	
	private void quit(){
		this.finish();
		System.exit(0);
	}

    
    private void checkAllOk(){
    	String errMessage = null;
    	if(!isRoot){
    		errMessage = getString(R.string.errorNoRoot);
    	}else if(!catPathFound){
    		errMessage = getString(R.string.errorNoCatPath);
    	}
    	if(errMessage!=null) {
    		Log.w(TAG, errMessage);
    		Dialog dialog = Common.createAlertDialog(this, getString(R.string.errorTitleMsg), errMessage);
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
    	File file = new File(BACKUP_PATH);
    	file.delete();
    	Common.runAsRoot(catpath + " " + WPA_SUPPLICANT_PATH + " > " + BACKUP_PATH + "\n");
    	String backupInfoMessage = file.exists() ? getString(R.string.backupSuccessful) : getString(R.string.backupFailed); 
    	Toast.makeText(this, backupInfoMessage, Toast.LENGTH_LONG).show();
    	if(checkBackupSendMail.isChecked() && file.exists()){
    		sendMailWithBackupFile(file);
    	}
    	Log.i(TAG, backupInfoMessage);
    	Log.d(TAG, "btnClickHandlerBackup end");
    }
    
    public void btnClickHandlerRestore(View view) throws IOException, InterruptedException {
    	Log.d(TAG, "btnClickHandlerRestore start");
    	File file = new File(BACKUP_PATH);
    	if(!file.exists()){
    		Common.createAlertDialog(this, getString(R.string.backupFileNotFound) + " " + BACKUP_PATH).show();
    		return;
    	}
    	Common.runAsRoot(catpath + " " + BACKUP_PATH + " > " + WPA_SUPPLICANT_PATH + "\n");
    	Toast.makeText(this, getString(R.string.restoreSuccessful), Toast.LENGTH_LONG).show(); //TODO: Richtige Prüfung einbauen ob successful oder nicht, z.B. ueber Datum
    	Log.d(TAG, "btnClickHandlerRestore end");
    }
    
    private void sendMailWithBackupFile(File backupFile){
    	String appName = getString(R.string.app_name);
		Intent emailIntent = Common.createMailIntent(new String[]{""}, appName, "", backupFile);
		startActivity(Intent.createChooser(emailIntent, appName)); 
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
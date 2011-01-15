package de.snertlab.wlanconfigsaver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ConfigSaver extends Activity {
    /** Called when the activity is first created. */
	
	private static final String WPA_SUPPLICANT_FILENAME = "wpa_supplicant.conf";
	private static final String WPA_SUPPLICANT_PATH = "/data/misc/wifi/" + WPA_SUPPLICANT_FILENAME;
	private static final String BACKUP_FILENAME = "wpa_supplicant.conf.bak";
	private static final String BACKUP_PATH = "/sdcard/" + BACKUP_FILENAME;
	
	private String catpath;
	
	private TextView txtViewInfo;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        txtViewInfo = (TextView) findViewById(R.id.TextView01);
        doInit();
    }
    
    private void doInit(){
    	boolean catPathFound = findCatPath();
    	StringBuilder sb = new StringBuilder();
    	sb.append("Init Status:\n");
    	sb.append("cat path found: " + catPathFound + "\n");
    	sb.append("is root: " + hazIGotRoot() + "\n");
    	txtViewInfo.setText(sb.toString());
    }
    
    public void btnClickHandlerBackup(View view) throws IOException, InterruptedException {
    	runAsRoot(catpath + " " + WPA_SUPPLICANT_PATH + " > " + BACKUP_PATH + "\n");
    	File file = new File(BACKUP_PATH);
    	txtViewInfo.setText("backup " + (file.exists() ? "successful " + BACKUP_PATH : "failed"));
    }
    
    public void btnClickHandlerRestore(View view) throws IOException, InterruptedException {
    	File file = new File(BACKUP_PATH);
    	if(!file.exists()){
    		txtViewInfo.setText("backup file :" + BACKUP_PATH + " not found");
    		return;
    	}
    	runAsRoot(catpath + " " + BACKUP_PATH + " > " + WPA_SUPPLICANT_PATH + "\n");
    	txtViewInfo.setText("restore successful"); //TODO: Richtige Prüfung einbauen ob successful oder nicht
    }
    
    public boolean findCatPath() {
    	String[] arrayOfString = new String[4];
    	arrayOfString[0] = "/system/bin/cat";
    	arrayOfString[1] = "/system/xbin/cat";
    	arrayOfString[2] = "/data/busybox/cat";
    	arrayOfString[3] = "/system/xbin/bb/cat";
    	for (int i = 0; i < arrayOfString.length; i++) {
    		String path = arrayOfString[i];
    		File localFile = new File(path);
    		if ((localFile.exists()) || (localFile.isFile())){
    			catpath = path;
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean hazIGotRoot(){
    	return runAsRoot("");
    }
    
    private boolean runAsRoot(String command){
    	try{
    	Runtime r=Runtime.getRuntime();
    	Process p2 = r.exec("su");
    	DataOutputStream d=new DataOutputStream(p2.getOutputStream());
    	d.writeBytes(command);
    	d.writeBytes("exit\n");
    	d.flush();
    	int retval = p2.waitFor();
    	return (retval==0);
    	}catch(Exception e){
    		//TODO: logging
    		return false;
    	}
    }
}
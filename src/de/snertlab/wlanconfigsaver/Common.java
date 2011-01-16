package de.snertlab.wlanconfigsaver;

import java.io.DataOutputStream;
import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

public class Common {
	
	private static final String TAG = "wlanSettingsBackup.Common";
	
    public static boolean hazIGotRoot(){
    	return runAsRoot("");
    }
    
    public static boolean runAsRoot(String command){
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
    		Log.e(TAG, "runAsRoot", e);
    		return false;
    	}
    }
    
    public static String findCatPath() {
    	String[] arrayOfString = new String[4];
    	arrayOfString[0] = "/system/bin/cat";
    	arrayOfString[1] = "/system/xbin/cat";
    	arrayOfString[2] = "/data/busybox/cat";
    	arrayOfString[3] = "/system/xbin/bb/cat";
    	for (int i = 0; i < arrayOfString.length; i++) {
    		String path = arrayOfString[i];
    		File localFile = new File(path);
    		if ((localFile.exists()) || (localFile.isFile())){
    			return path;
    		}
    	}
    	return null;
    }

    public static Dialog createErrorDialog(Context context, String message){
    	return createAlertDialog(context, "Error", message);
    }
    
    public static Dialog createAlertDialog(Context context, String message){
    	return createAlertDialog(context, null, message);
    }
    
    public static Dialog createAlertDialog(Context context, String title, String message){
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setTitle(title);
    	builder.setIcon(0);
    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	builder.setMessage(message);
    	builder.setCancelable(false);
    	AlertDialog alert = builder.create();
    	return alert;
    }

}

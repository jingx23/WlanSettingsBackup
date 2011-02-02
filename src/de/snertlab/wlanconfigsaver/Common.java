package de.snertlab.wlanconfigsaver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class Common {
	
	private static final String TAG = ConfigSaver.TAG;
	private static final SimpleDateFormat SDF_FILE_CHANGE = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
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
    
    public static List<String> runAsRootWithRet(String cmd){
    	List<String> listRetVal = new ArrayList<String>();
    	try{
    		Process psProcess = Runtime.getRuntime().exec("su");
    		OutputStream os = psProcess.getOutputStream();
    		InputStream is = psProcess.getInputStream();
    		writeLine( os, null, cmd );
    		writeLine( os, null, "exit");
    		try{
    			psProcess.waitFor();
    		}catch( InterruptedException interruptedException ){
    			Log.e( TAG, "While trying to read process", interruptedException);
    			return listRetVal;
    		}
    		String procStringLine = readString( is, null, false );
    		Log.d(TAG, "Read process line as " + procStringLine );
    		if( procStringLine == null || procStringLine.trim().length() == 0 ){
    			Log.d( TAG, "Attempt to read process did not return anything");
    			return listRetVal;
    		}else{
    			StringTokenizer tokenizer = new StringTokenizer( procStringLine, "\n", false );
    			int columnCount = tokenizer.countTokens();
    			for( int index = 0; index < columnCount; index++ ){
    				listRetVal.add(tokenizer.nextToken());
    			}
    			return listRetVal;
    		}
    	}catch( IOException e ){
    		Log.e(TAG, "While trying to read process", e);
    		return listRetVal;
    	}
    }

    public static String readString(InputStream is, PrintWriter logWriter, boolean block) throws IOException
    {
            if( !block && is.available() == 0 )
            {
                    //Caller doesn't want to wait for data and there isn't any available right now
                    return null;
            }
            byte firstByte = (byte)is.read(); //wait till something becomes available
            int available = is.available();
            byte[] characters = new byte[available + 1];
            characters[0] = firstByte;
            is.read( characters, 1, available );
            String string = new String( characters );
            if( logWriter != null )
            {
                    logWriter.println( string );
            }
            return string;
    }

    public static void writeLine(OutputStream os, PrintWriter logWriter, String value) throws IOException
    {
            String line = value + "\n";
            os.write( line.getBytes() );
            if( logWriter != null )
            {
                    logWriter.println( value );
            }
    }

    public static int getFileSize(String path){
    	String tagSize = "Size:";
    	String tagBlocks = "Blocks:";
    	List<String> listStrings = Common.runAsRootWithRet("stat " + path);
    	if( ! listStrings.isEmpty() ){
    		for (String string : listStrings) {
    			int indexOfSize = string.indexOf(tagSize);
    			if(indexOfSize!=-1){
    				String size = string.substring(indexOfSize + tagSize.length(), string.indexOf(tagBlocks));
    				return Integer.parseInt(size.trim());
    			}
			}
    	}
    	return 0;
    }
    
    public static Date getFileChanged(String path) throws ParseException{
    	String tagChange = "Change:";
    	List<String> listStrings = Common.runAsRootWithRet("stat " + path);
    	if( ! listStrings.isEmpty() ){
    		for (String string : listStrings) {
    			int indexOfChange = string.indexOf(tagChange);
    			if(indexOfChange!=-1){
    				String sDate = string.substring(indexOfChange + tagChange.length());
    				sDate = sDate.trim();
    				Date d = SDF_FILE_CHANGE.parse(sDate);
    				return d;
    			}
			}
    	}
    	return null;
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
    
    public static Intent createMailIntent(String[] recipient, String subject, String body, File attachment){
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipient);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
		emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(attachment));
		return emailIntent; 
    }

}

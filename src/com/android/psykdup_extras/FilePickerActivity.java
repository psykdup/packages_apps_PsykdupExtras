package com.android.psykdup_extras;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.psykdup_extras.R;
import com.android.psykdup_extras.ShellInterface;

public class FilePickerActivity extends ListActivity {
	private ArrayAdapter<String> directoryList;
	private Context context;
	private File currentDirectory;
	private boolean allowBrowsing;
	String selectedFileString;
	String action;
	ArrayList<String> directoryEntries = new ArrayList<String>(); 
		@Override
		public void onCreate(Bundle savedInstanceState) {
			this.context = this;
			super.onCreate(savedInstanceState);
			setContentView(R.layout.filemanager);
			Bundle bundle = this.getIntent().getExtras();
			String path = bundle.getString("path");
			action = bundle.getString("action");
			allowBrowsing = !(action.equals("backup")||action.equals("restore")||action.equals("delete")); 
			currentDirectory = new File(path);
			browseTo(currentDirectory);
			
}

		private void browseTo(final File aDirectory){
		    //if we want to browse directory
		    if (aDirectory.isDirectory()){
		      //fill list with files from this directory
		      this.currentDirectory = aDirectory;
		      fill(aDirectory.listFiles());}
		    }
		 private void fill(File[] files) {
	    	    //clear list
	    	    this.directoryEntries.clear();  
	    	    ArrayList<String> directoryfiles = new ArrayList<String>();
	    	    ArrayList<String> directoryfolders = new ArrayList<String>();
	    	    if (allowBrowsing)
	    	    if (this.currentDirectory.getParent() != null)
	    	    	this.directoryEntries.add(new String(".."));
	    	      
	    	     
	    	    //add every file into list
	    	    for (File file : files) {
	    	      //this.directoryEntries.add(file.getAbsolutePath());
	    	      if (file.getName().contains(".zip"))directoryfiles.add(new String(file.getName()));
	    	      if (file.isDirectory())directoryfolders.add(new String(file.getName()));
	    	    }
	    	    Collections.sort(directoryfiles,String.CASE_INSENSITIVE_ORDER);
	    	    Collections.sort(directoryfolders,String.CASE_INSENSITIVE_ORDER);
	    	    this.directoryEntries.addAll(directoryfiles);
	    	    this.directoryEntries.addAll(directoryfolders);
	    	    //create array adapter to show everything
	    	    directoryList = new ArrayAdapter<String>(this, R.layout.file_row, this.directoryEntries);
	    	    this.setListAdapter(directoryList);
	    	  }
		 
		 @Override
		  protected void onListItemClick(ListView l, View v, int position, long id) {
		    //get selected file name
			 int selectionRowID = position;
			    selectedFileString = this.directoryEntries.get(selectionRowID);
			 if (allowBrowsing)
			 {
		    
		    
		    //if we select ".." then go upper
		    if(selectedFileString.equals("..")){
		      this.upOneLevel();
		    } else {
		      //browse to clicked file or directory using browseTo()
		      File clickedFile = null;
		      clickedFile = new File(currentDirectory.getName()+"/"+selectedFileString);
		      if (clickedFile != null)
		    	  if (clickedFile.isFile()&&clickedFile.getName().contains(".zip"))
		    		  show_confirmation_dialog();
		    	  else
		        this.browseTo(clickedFile);
		    }
			 }
			 else
			 {
				 show_confirmation_dialog();
				
			 };
		  }
		 private void show_confirmation_dialog() {
		    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
		    	builder.setTitle("Please Confirm");
		    	if (action.equals("restore"))
		    		restore(builder);
		    	if (action.equals("delete"))
		    		delete(builder);
		    	if (action.equals("update"))
		    		update(builder);
		builder.setNegativeButton("No", null);
				AlertDialog alert = builder.create();
				
				alert.show();
			
		}

		private void restore(AlertDialog.Builder builder) {
			builder.setMessage("Are you sure you want to restore:\n" + selectedFileString + "?");
			builder.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				StringBuilder command = new StringBuilder("echo 'restore_rom(\"/sdcard/clockworkmod/backup/");
				command.append(String.valueOf(selectedFileString));
				command.append("\");' > /cache/recovery/extendedcommand");
				if(ShellInterface.isSuAvailable())
						try {
							//runOnUiThread(runInUIThread);
							ShellInterface.runCommand(command.toString());
							ShellInterface.runCommand("reboot recovery");
							}
					catch (IOException e){
					//	Toast.makeText(this, "Failed!", Toast.LENGTH_LONG);
					}
			}
});
		}
		private void update(AlertDialog.Builder builder) {
			builder.setMessage("Are you sure you want to flash:\n" + selectedFileString + "?");
			builder.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				String path = currentDirectory.getAbsolutePath();
				path = path.replace("/mnt/sdcard", "/sdcard");
				StringBuilder command = new StringBuilder("echo 'install_zip(\""+path+"/");
				command.append(String.valueOf(selectedFileString));
				command.append("\");' > /cache/recovery/extendedcommand");
				if(ShellInterface.isSuAvailable())
						try {
							//runOnUiThread(runInUIThread);
							ShellInterface.runCommand(command.toString());
							ShellInterface.runCommand("reboot recovery");
							}
					catch (IOException e){
					//	Toast.makeText(this, "Failed!", Toast.LENGTH_LONG);
					}
			}
});
		}
		private void delete(AlertDialog.Builder builder) {
			builder.setMessage("Are you sure you want to delete:\n" + selectedFileString + "?");
			builder.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				File f = new File("/mnt/sdcard/clockworkmod/backup/"+ selectedFileString);
				removeDirectory(f);
				finish();
			}
});
		}
		private void upOneLevel(){
			    if(this.currentDirectory.getParent() != null) {
			      this.browseTo(this.currentDirectory.getParentFile());
			    }
			  }
		public static boolean removeDirectory(File directory) {

			  // System.out.println("removeDirectory " + directory);

			  if (directory == null)
			    return false;
			  if (!directory.exists())
			    return true;
			  if (!directory.isDirectory())
			    return false;

			  String[] list = directory.list();

			  // Some JVMs return null for File.list() when the
			  // directory is empty.
			  if (list != null) {
			    for (int i = 0; i < list.length; i++) {
			      File entry = new File(directory, list[i]);

			      //        System.out.println("\tremoving entry " + entry);

			      if (entry.isDirectory())
			      {
			        if (!removeDirectory(entry))
			          return false;
			      }
			      else
			      {
			        if (!entry.delete())
			          return false;
			      }
			    }
			  }

			  return directory.delete();
			}

		 		 }

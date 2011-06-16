package com.android.psykdup_extras;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.psykdup_extras.Extras;
import com.android.psykdup_extras.R;
import com.android.psykdup_extras.ShellInterface;

public class DpiSliderActivity extends Activity{
	private int dpi;
	Context ctx;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.uitweaks_main);
		final SeekBar dpiSB = (SeekBar) findViewById(R.id.SeekBar01);
		final TextView dpiTV = (TextView) findViewById(R.id.ValueTV);
		Button abutton = (Button) findViewById(R.id.Button01);
		Button rbutton = (Button) findViewById(R.id.Button02);
		String tmp = null;
		this.ctx = getApplicationContext();
		if(ShellInterface.isSuAvailable()) { try {
			ShellInterface.runCommand("nitrality remount");
		} catch (IOException e) {
			Log.e(Extras.getTag(), "Couldnt remount rw");
			e.printStackTrace();
		}}
		if(ShellInterface.isSuAvailable()) { tmp = ShellInterface.getProcessOutput("nitrality dpi");}
	
		if (tmp!=null){
		dpi = (tmp.charAt(tmp.length()-1)==(' '))? Integer.parseInt(tmp.substring(0, tmp.length()-1)) : Integer.parseInt(tmp) ;
		dpiTV.setText(String.valueOf(dpi));
		}
		else dpiTV.setText("ERR");
		
		dpiSB.setProgress(prog_value(dpi));
		
		abutton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				change_dpi();
				
			}
		});
		rbutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				
				try {
					if(ShellInterface.isSuAvailable()) { 
						ShellInterface.runCommand("nitrality dpi 240");
						}; 
						dpiSB.setProgress(0);
				} catch (IOException e) {
					Log.e(Extras.getTag(), "Couldnt exec: "+e.getMessage());
					e.printStackTrace();
				}
			}
		});
		dpiSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			dpiTV.setText(String.valueOf(dpi = dpi_value(6-progress)));
			}
		});}
	
	private int prog_value(int dpi2) {
		if (dpi2==182) return 3;
		int val = (240-dpi2)/10;
		return val;
	}

	private int dpi_value(int a)
	{
		int val = 180+a*10;
		if (val==180)
			return 182;
		return val;
	}

	private void change_dpi() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.confirm_dpi_1) + String.valueOf(dpi)+ getString(R.string.confirm_dpi_2));
		builder.setTitle("WARNING");
		builder.setPositiveButton("Yes", new OnClickListener(){

			public void onClick(DialogInterface dialog, int which) {
				try {
					if(ShellInterface.isSuAvailable()) {ShellInterface.runCommand("nitrality dpi "+String.valueOf(dpi));
					ShellInterface.runCommand("setprop qemu.sf.lcd_density " + String.valueOf(dpi));
					ShellInterface.runCommand("killall system_server");
					ShellInterface.runCommand("busybox killall system_server");
					}
				} catch (IOException e) {
					Log.e(Extras.getTag(), "Couldnt exec: "+e.getMessage());
					e.printStackTrace();
				}
				
			};});
		builder.setNegativeButton(R.string.cancel, new OnClickListener(){

			public void onClick(DialogInterface dialog, int which) {
			}});
		AlertDialog alert = builder.create();

		alert.show();
	}
}

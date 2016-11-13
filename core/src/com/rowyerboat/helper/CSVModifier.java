package com.rowyerboat.helper;

import java.io.BufferedReader;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class CSVModifier {
	FileHandle loc;
	
	public CSVModifier(FileHandle handle) {
		loc = handle;
	}
	
	public boolean cleanupCSV() {
		String[] splits = loc.readString().split("\n");
		String newStr = "";
		for (int i = 0; i < splits.length; ++i) {
			String[] parts = splits[i].split(",");
			newStr += String.format(Locale.US, "%.1f", Float.parseFloat(parts[3]));
			newStr += ",";

			newStr += String.format(Locale.US, "%.1f", Float.parseFloat(parts[2]));
			newStr += ",";
			
			if (parts[4].equals("NaN"))
				newStr += "0";
			else
				newStr += parts[4];
			newStr += ",";
			
			if (parts[5].equals("NaN"))
				newStr += "0";
			else
				newStr += parts[5];
			newStr += "\n";
			
			if (i % 50 == 0)
				System.out.print("\r" + "Progress: " + 
						String.format(Locale.US, "%.2f", (float)(i / splits.length * 100f)));
		}
		FileHandle write = Gdx.files.local(loc.path() + ".clean");
		write.writeString(newStr, false);
		
		System.out.println();
		
		return false;
	}
}

package com.rowyerboat.helper;

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
		StringBuilder newStr = new StringBuilder();
		for (int i = 0; i < splits.length; ++i) {
			String[] parts = splits[i].split(",");
			newStr.append(String.format(Locale.US, "%.1f", Float.parseFloat(parts[3])));
			newStr.append(",");

			newStr.append(String.format(Locale.US, "%.1f", Float.parseFloat(parts[2])));
			newStr.append(",");
			
			if (parts[4].equals("NaN"))
				newStr.append("0");
			else
				newStr.append(parts[4]);
			newStr.append(",");
			
			if (parts[5].equals("NaN"))
				newStr.append("0");
			else
				newStr.append(parts[5]);
			newStr.append("\n");
			
			if (i % 50 == 0)
				System.out.print("\r" + "Progress: " + 
						String.format(Locale.US, "%.2f", i / (float)splits.length * 100f));
		}
		System.out.println("\n");
		FileHandle write = Gdx.files.local(loc.path() + ".clean");
		write.writeString(newStr.toString(), false);
		
		return false;
	}
}

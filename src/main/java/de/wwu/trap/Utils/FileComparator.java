package de.wwu.trap.Utils;

import java.io.File;
import java.util.Comparator;

public class FileComparator<T> implements Comparator<File> {

	private boolean inversed = false;
	
	public FileComparator() {

	}

	public FileComparator(boolean inversed) {
		this.inversed = inversed;
	}

	@Override
	public int compare(File o1, File o2) {
		if(!inversed){
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());				
		}else{
			return String.CASE_INSENSITIVE_ORDER.compare(o2.getName(), o1.getName());
		}
	}

}

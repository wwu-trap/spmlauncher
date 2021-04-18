package de.wwu.trap.SpmLauncher.Utils;

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
		NaturalOrderComparator<String> noc = new NaturalOrderComparator<>();
		if(!inversed){
			return noc.compare(o1.getName().toLowerCase(), o2.getName().toLowerCase());	
		}else{
			return noc.compare(o2.getName().toLowerCase(), o1.getName().toLowerCase());
		}
	}

}

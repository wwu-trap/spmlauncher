package Utils;

import java.io.File;
import java.util.Comparator;

public class FileComparator<T> implements Comparator<T> {

	private boolean inversed = false;
	
	public FileComparator() {

	}

	public FileComparator(boolean inversed) {
		this.inversed = inversed;
	}

	@Override
	public int compare(T o1, T o2) {
		Class<?> fileClass = null;
		try {
			fileClass = Class.forName("java.io.File");
		} catch (ClassNotFoundException e) {

		}
		if (fileClass != null && fileClass.isInstance(o1) && fileClass.isInstance(o2)) {
			File f1 = (File) o1;
			File f2 = (File) o2;
			if(!inversed){
				return String.CASE_INSENSITIVE_ORDER.compare(f1.getName(), f2.getName());				
			}else{
				return String.CASE_INSENSITIVE_ORDER.compare(f2.getName(), f1.getName());
			}

		} else {
			return 0;
		}
	}

}

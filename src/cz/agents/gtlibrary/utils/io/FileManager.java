package cz.agents.gtlibrary.utils.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileManager<T extends Object> {
	
	public void saveObject(T object, String fileName) {
		try {
			ObjectOutputStream s = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(fileName))));

			s.writeObject(object);
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public T loadObject(String fileName) throws IOException {
		T object = null;
		ObjectInputStream s = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(fileName))));

		try {
			object = (T) s.readObject();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
		return object;
	}
}

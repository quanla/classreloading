package qj.util;

import java.io.*;
import java.util.Properties;

public class PropertiesUtil {

	public static Properties loadPropertiesFromFile(String fileName) {
        return loadPropertiesFromFile(new File(fileName));
	}

	/**
	 * 
	 * @param file
	 * @return
	 */
    public static Properties loadPropertiesFromFile(File file) {
    	if (!file.exists()) {
    		return null;
    	}
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			return load(fis);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
    }
	
	
    /**
     * Load a Properties object from inputstream. Close the stream afterward
     * @param is
     * @return
     */
	public static Properties load(InputStream is) {
		Properties properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
	        try {
		        is.close();
	        } catch (IOException e) {
		        ;
	        }
        }
		return properties;
	}
}

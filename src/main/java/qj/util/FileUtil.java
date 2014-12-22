package qj.util;

import qj.util.funct.F1;
import qj.util.funct.F2;
import qj.util.funct.Fs;
import qj.util.funct.P2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("UnusedDeclaration")
public class FileUtil {

	/**
	 * 
	 * @param fileToRead
	 * @return
	 * @throws IOException
	 */
	public static byte[] readFileToBytes(File fileToRead) {
		try {
			return IOUtil.readData(new FileInputStream(fileToRead));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

    public static FileInputStream fileInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static FileInputStream fileInputStream(String file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

	
    public static void eachFile(File path, P2<File, String> f) {
        eachFile(path, f, null);
    }
	
    public static void eachFile(File path, P2<File, String> f, F1<File, Boolean> exclude) {
    	eachFile(path, Fs.f2(f, true), exclude);
    }
	
    public static void eachFile(File path, F2<File, String,Boolean> f, F1<File, Boolean> exclude) {

        ArrayList<String> relPath = new ArrayList<>();

        if (path.isFile()) {
            f.e(path, Cols.join(relPath, File.separator));
        } else {
            if (!eachFileInDir(path, f, relPath, exclude)) return;
        }
    }
	
    private static boolean eachFileInDir(File path, F2<File, String,Boolean> f, ArrayList<String> relPath, F1<File, Boolean> exclude) {
        if (!path.exists() || !path.isDirectory()) {
            throw new RuntimeException("Invalid path: " + path);
        }
        for (File child : path.listFiles()) {
            if (exclude != null && exclude.e(child)) {
//            	System.out.println("Excluded " + child);
                continue;
            }
//        	System.out.println("Accepted " + child);

            if (child.isFile()) {
                if (!f.e(child, Cols.join(relPath, File.separator))) return false;
            } else {
                relPath.add(child.getName());
                if (!eachFileInDir(child, f, relPath, exclude)) return false;
                relPath.remove(relPath.size() - 1);
            }
        }
        return true;
    }
}

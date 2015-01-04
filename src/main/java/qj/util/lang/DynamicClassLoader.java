package qj.util.lang;

import qj.util.FileUtil;
import qj.util.IOUtil;
import qj.util.funct.F1;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class DynamicClassLoader extends AggressiveClassLoader {
	LinkedList<F1<String, byte[]>> loaders = new LinkedList<>();

	public DynamicClassLoader(String... paths) {
		for (String path : paths) {
			File file = new File(path);

			F1<String, byte[]> loader = loader(file);
			if (loader == null) {
				throw new RuntimeException("Path not exists " + path);
			}
			loaders.add(loader);
		}
	}

	@SuppressWarnings("UnusedDeclaration")
	public DynamicClassLoader(Collection<File> paths) {
		for (File file : paths) {
			F1<String, byte[]> loader = loader(file);
			if (loader == null) {
				throw new RuntimeException("Path not exists " + file.getPath());
			}
			loaders.add(loader);
		}
	}
	

	public static F1<String, byte[]> loader(File file) {
		if (!file.exists()) {
			return null;
		} else if (file.isDirectory()) {
			return dirLoader(file);
		} else {
			try {
				final JarFile jarFile = new JarFile(file);

				return jarLoader(jarFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static File findFile(String filePath, File classPath) {
		File file = new File(classPath, filePath);
		return file.exists() ? file : null;
	}

	public static F1<String, byte[]> dirLoader(final File dir) {
		return filePath -> {
			File file = findFile(filePath, dir);
			if (file == null) {
				return null;
			}

			return FileUtil.readFileToBytes(file);
		};
	}

	private static F1<String, byte[]> jarLoader(final JarFile jarFile) {
		return new F1<String, byte[]>() {
			public byte[] e(String filePath) {
				ZipEntry entry = jarFile.getJarEntry(filePath);
				if (entry == null) {
					return null;
				}
				try {
					return IOUtil.readData(jarFile.getInputStream(entry));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			protected void finalize() throws Throwable {
				IOUtil.close(jarFile);
				super.finalize();
			}
		};
	}
	
	@Override
	protected byte[] loadNewClass(String name) {
//		System.out.println("Loading class " + name);
		for (F1<String, byte[]> loader : loaders) {
			byte[] data = loader.e(AggressiveClassLoader.toFilePath(name));
			if (data!= null) {
				return data;
			}
		}
		return null;
	}
}

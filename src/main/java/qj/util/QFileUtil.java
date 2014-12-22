package qj.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import qj.util.col.Tree;
import qj.util.funct.F0;
import qj.util.funct.F1;

public class QFileUtil {

	public static class QFile extends Tree<QFileContent> {
		public static F1<String, Tree<QFileUtil.QFileContent>> CONSTRUCTOR = new F1<String, Tree<QFileUtil.QFileContent>>() {public Tree<QFileUtil.QFileContent> e(String name) {
			return new QFileUtil.QFile(name);
		}};

		public QFile(final File file) {
			super(file.getName(), new QFileContent() {
				public InputStream getInputStream() {
					return FileUtil.fileInputStream(file);
				}
			});
		}
		
		public QFile(final String name, final F0<InputStream> inputStreamF) {
			super(name, new QFileContent() {
				public InputStream getInputStream() {
					return inputStreamF.e();
				}
			});
		}
		
		public static QFileContent toValue(final byte[] content) {
			return new QFileContent() {
				@Override
				public InputStream getInputStream() {
					return new ByteArrayInputStream(content);
				}
			};
		}

		public QFile(String name) {
			super(name, null);
		}
		public QFile(String name, QFile... children) {
			this(name);
			downs.addAll(Arrays.asList(children));
		}
	
		public String getName() {
			return name;
		}
	
		public LinkedList<QFile> listFiles() {
			LinkedList<QFile> ret = new LinkedList<QFile>();
			for (Tree<QFileContent> child : downs) {
				QFile child2 = (QFile) child;
				if (child2.isFile()) {
					ret.add(child2);
				}
			}
			return ret;
		}
	
		public LinkedList<QFile> listDirs() {
			LinkedList<QFile> ret = new LinkedList<QFile>();
			for (Tree<QFileContent> child : downs) {
				QFile child2 = (QFile) child;
				if (!child2.isFile()) {
					ret.add(child2);
				}
			}
			return ret;
		}
	
		public QFile getFile(String path) {
			return (QFile) getDown(path);
		}
	
		public boolean exists(String path) {
			Tree<QFileContent> down = getDown(path);
			return down!=null && down != this;
		}
	
		public InputStream getInputStream() {
			return value.getInputStream();
		}
	
		public boolean isFile() {
			return Cols.isEmpty(downs);
		}
		
		public String toString() {
			return "QFile[" + name + "]";
		}
	}

	public static interface QFileContent {
		public InputStream getInputStream();
		
	}
}

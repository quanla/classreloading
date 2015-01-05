package qj.util;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Quan on 22/12/2014.
 */
public class IOUtil {
	
	/**
	 * Read the stream into byte array
	 * @param inputStream
	 * @return
	 * @throws java.io.IOException
	 */
    public static byte[] readData(InputStream inputStream) {
        try {
			return readDataNice(inputStream);
		} finally {
        	close(inputStream);
		}
    }

    public static byte[] readDataNice(InputStream inputStream) {
		ByteArrayOutputStream boTemp = null;
        byte[] buffer = null;
        try {
            int read;
			buffer = new byte[8192];
            boTemp = new ByteArrayOutputStream();
            while ((read=inputStream.read(buffer, 0, 8192)) > -1) {
                boTemp.write(buffer, 0, read);
            }
            return boTemp.toByteArray();
        } catch (IOException e) {
			throw new RuntimeException(e);
        }
	}
	
	
    /**
     * Close streams (in or out)
     * @param stream
     */
    public static void close(Closeable stream) {
        if (stream != null) {
            try {
                if (stream instanceof Flushable) {
                    ((Flushable)stream).flush();
                }
                stream.close();
            } catch (IOException e) {
                // When the stream is closed or interupted, can ignore this exception
            }
        }
    }

	public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // When the conn is closed or interupted, can ignore this exception
			}
        }
		
	}

    public static void close(ResultSet rs) {
    	if (rs != null) {
    		try {
    			rs.close();
    		} catch (SQLException e) {
    			// When the file is closed already, can ignore this exception
    		}
    	}
    }
    public static void close(PreparedStatement ps) {
    	if (ps != null) {
    		try {
    			ps.close();
    		} catch (SQLException e) {
    			// When the file is closed already, can ignore this exception
    		}
    	}
    }
	
	/**
	 * Will close stream
	 * @param in
	 * @param charSet
	 * @return
	 */
	public static String toString(InputStream in, String charSet) {
		return inputStreamToString_force(in, charSet);
	}
	
	/**
	 * Will close stream
	 * @param in
	 * @param charSet
	 * @return
	 */
	public static String inputStreamToString_force(InputStream in, String charSet) {
		try {
			return inputStreamToString(in, charSet);
		} catch (IOException e) {
			return null;
		}
	}

    /**
     * Reads in whole input stream and returns as a string<br>
     * Will close stream
     * @param in The input stream to read in, will be closed 
     * 				by this method at finish
     * @param charSet charset to convert the input bytes into string
     * @return the result string
     * @throws IOException
     */
	public static String inputStreamToString(InputStream in, String charSet) throws IOException {
		InputStreamReader inputStreamReader = null;
		try {
			inputStreamReader = charSet == null? new InputStreamReader(in) : new InputStreamReader(in, charSet);

			return toString(inputStreamReader);
		} catch (UnsupportedEncodingException e1) {
			throw new RuntimeException(e1);
		} finally {
			close(in);
		}
	}
	
    /**
     * Reads in whole input stream and returns as a string
     * @param reader The input reader to read in, will be closed 
     * 				by this method at finish
     * @return the result string
     * @throws IOException
     */
	public static String toString(Reader reader) {
		try {
			StringBuilder sb = new StringBuilder();
			char[] buffer = new char[4096];
			for (int read; (read = reader.read(buffer)) > -1;) {
				sb.append(buffer, 0, read);
			}
			return sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			close(reader);
		}
	}

	/**
	 * Read the input stream and write to output stream
	 * @param inputStream
	 * @param out
	 * @return
	 * @throws IOException
	 */
    public static long connect(InputStream inputStream, OutputStream out) throws IOException {
        try {
            return dump(inputStream, out);
        } finally {
            close(inputStream);
        }
    }
    
    private static long dump(InputStream inputStream, OutputStream out) throws IOException {
        long total = 0;
        int read;
        int bufferSize = 8192;
        byte[] buffer = new byte[bufferSize];
        while ((read=inputStream.read(buffer, 0, bufferSize)) > -1) {
            out.write(buffer, 0, read);
            total+=read;
        }
        out.flush();
        return total;
    }

}

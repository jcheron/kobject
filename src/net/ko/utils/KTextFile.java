package net.ko.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import javax.swing.event.EventListenerList;

import net.ko.creator.KClassCreator;
import net.ko.events.EventFile;
import net.ko.events.EventFileListener;

public class KTextFile {
	private String fileName;
	private String text;

	public KTextFile(String fileName, String text) {
		super();
		this.fileName = fileName;
		this.text = text;
	}

	private final EventListenerList listeners = new EventListenerList();

	public void addFileListener(EventFileListener listener) {
		listeners.add(EventFileListener.class, listener);
	}

	public void removeFileListener(EventFileListener listener) {
		listeners.remove(EventFileListener.class, listener);
	}

	public EventFileListener[] getFileListeners() {
		return listeners.getListeners(EventFileListener.class);
	}

	protected boolean fireFileExist(File aFile) {
		boolean doit = true;
		EventFile event = null;
		for (EventFileListener eFile : getFileListeners()) {
			if (event == null)
				event = new EventFile(aFile, doit);
			eFile.fileExist(event);
			doit = doit && event.doit;
		}
		return doit;
	}

	public static String open(String fileName) {
		String resultat = new String("");
		try {
			File file = new File(fileName);
			int size = (int) file.length();
			int chars_read = 0;
			FileReader in = new FileReader(file);
			char[] data = new char[size];
			while (in.ready()) {
				chars_read += in.read(data, chars_read, size - chars_read);
			}
			resultat = new String(data, 0, chars_read);
			in.close();
		} catch (IOException e) {
			String f = fileName.replace(net.ko.utils.KApplication.getRootPath(KClassCreator.class) + "/", "");
			resultat = openRessource(f);
		}
		return resultat;
	}

	public static InputStream openStreamRessource(String fileName) {
		// byte[] data;
		DataInputStream dis = new DataInputStream(ClassLoader.getSystemResourceAsStream(fileName));
		InputStream result = null;
		try {
			dis.readUTF();
			return dis;
			// data = new byte[dis.available()];
			// dis.readFully(data);
			// result=dis;
		} catch (Exception e) {
			URL url = null;
			try {
				url = new URL(fileName);
				result = url.openStream();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return result;
	}

	public static String openRessource(String filename) {
		// byte[] data;
		try {
			DataInputStream dis = new DataInputStream(ClassLoader.getSystemResourceAsStream(filename));
			String s = dis.readUTF();
			// data = new byte[dis.available()];
			// dis.readFully(data);
			dis.close();
			// return (new String(data));
			return s;
		} catch (Exception ex1) {
			try {
				StringBuffer buffer = new StringBuffer();
				URL url = new URL(filename);
				InputStream is = url.openStream();
				// FileInputStream fis = new FileInputStream(path);
				InputStreamReader isr = new InputStreamReader(is, "UTF8");
				Reader in = new BufferedReader(isr);
				int ch;
				while ((ch = in.read()) > -1) {
					buffer.append((char) ch);
				}
				in.close();
				return buffer.toString();
				// data=new byte[in.available()];
				// in.read(data);
				// return (new String(data));
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
		}
		return "";
	}

	public boolean save() {
		File file = new File(fileName);
		// FileWriter out;
		boolean doit = true;
		try {
			if (file.exists())
				doit = fireFileExist(file);
			if (doit) {
				FileOutputStream fos = new FileOutputStream(fileName, false);
				Writer out = new OutputStreamWriter(fos, "UTF8");
				// out = new FileWriter(file);
				out.write(text);
				out.close();
			}
		} catch (IOException e) {
			doit = false;
			e.printStackTrace();
		}
		return doit;
	}

	public static void save(String fileName, String text) {
		KTextFile kt = new KTextFile(fileName, text);
		kt.save();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void addText(String text) {
		if (this.text != null) {
			this.text += text;
		} else
			this.text = text;
	}

	public String getFileName() {
		return fileName;
	}
}

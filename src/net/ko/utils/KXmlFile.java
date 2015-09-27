package net.ko.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class KXmlFile {
	private Document document = null;
	private Element xmlObject = null;
	private String fileName;

	public Document getDocument() {
		return document;
	}

	public void loadFromFile(String fileName) throws ParserConfigurationException, SAXException {
		DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
		fabrique.setValidating(false);
		// fabrique.setIgnoringElementContentWhitespace(true);
		DocumentBuilder constructeur = fabrique.newDocumentBuilder();
		// fileName=KFileUtils.getPathName(fileName);
		File xml = new File(fileName);
		try {
			this.fileName = fileName;
			document = constructeur.parse(xml);
		} catch (IOException e) {
			String f = fileName.replace(net.ko.utils.KApplication.getRootPath(net.ko.creator.KClassCreator.class) + "/", "");
			try {
				InputStream is = KTextFile.openStreamRessource(f);
				document = constructeur.parse(is);
				is.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		xmlObject = document.getDocumentElement();
	}

	public NodeList getChildNodes(Node node) {
		return node.getChildNodes();
	}

	public NodeList getChildNodes() {
		return xmlObject.getChildNodes();
	}

	public String getAttribute(Element element, String attributeName) {
		return element.getAttribute(attributeName);
	}

	public String getContent() {
		String stringResult = "";
		DOMSource domSource = new DOMSource(document);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = tf.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			transformer.transform(domSource, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stringResult = writer.toString();
		return stringResult;
	}

	public Element getXmlObject() {
		return xmlObject;
	}

	public void saveAs(String fileName) {
		writeXmlFile(document, fileName);

	}

	public boolean save() {
		if (fileName != null) {
			saveAs(fileName);
			return true;
		}
		return false;
	}

	public static void writeXmlFile(Document doc, String filename) {
		try {
			// Prepare the DOM document for writing
			Source source = new DOMSource(doc);

			// Prepare the output file
			File file = new File(filename);
			Result result = new StreamResult(file);

			// Write the DOM document to the file
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.setOutputProperty(OutputKeys.INDENT, "yes");
			xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			xformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
		} catch (TransformerException e) {
		}
	}

	public String getFileName() {
		return fileName;
	}
}

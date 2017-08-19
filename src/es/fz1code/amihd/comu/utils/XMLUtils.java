package es.fz1code.amihd.comu.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XMLUtils {

	private static Logger log = new Logger(XMLUtils.class);
	public static final String CHARSET_ENCODING="UTF-8";
	
	public static void guardarXML(Document doc, String rutaXML) throws Exception {
		try {
			File file = new File(rutaXML);
			TransformerFactory xformFactory= TransformerFactory.newInstance();
		    Transformer transformer= xformFactory.newTransformer();
			transformer.transform(new DOMSource(doc), new StreamResult(file));			
		} 
		catch (Exception e) {
			log.error("Se ha producido un error en la función guardarXML", e);
			throw e;
		}
	}
	
	public static Document leerXML(String rutaXML) throws Exception {
		Document ficheroDoc = null;
		try {
			FileReader reader = new FileReader(rutaXML);
			ficheroDoc = XMLUtils.getDocumentFromXML(reader);
		} 
		catch (Exception e) {
			log.error("Se ha producido un error en la función leerXML", e);
			throw e;
		}
		
		return ficheroDoc;
	}
	
	/**
	 * Método que se encarga de devolver un Objeto Document a partir de un XML
	 * que se le pasa como parámetro.
	 * 
	 * @param xml String
	 * @return Document
	 * @throws Exception
	 */
	public static Document getDocumentFromXML(String xml) throws Exception {
        return getDocumentFromXML(new StringReader(xml));	
	}
	
	public static Document getDocumentFromXML(Reader reader) throws Exception {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(reader));

        return document;	
	}
	
	public static String getStringXML(Document doc) throws Exception {
		return transformarXML(doc, null);
	}
	
	public static String transformarXML(Document doc, String strXSL) throws Exception {
		return transformarXML(doc, strXSL, CHARSET_ENCODING);
	}

	public static String getStringXMLFromBatch(Document doc) throws Exception {
		return transformarXMLFromBatch(doc, null);
	}
	
	public static String transformarXMLFromBatch(Document doc, String strXSL) throws Exception {
		Properties props = System.getProperties();
		String lDbf = props.getProperty("javax.xml.parsers.DocumentBuilderFactory");
		String lTf  = props.getProperty("javax.xml.transform.TransformerFactory");
		props.put("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		props.put("javax.xml.transform.TransformerFactory"  , "org.apache.xalan.processor.TransformerFactoryImpl");				
		
		String result = transformarXML(doc, strXSL, CHARSET_ENCODING);
		
		if (lDbf!=null) {
			props.put("javax.xml.parsers.DocumentBuilderFactory", lDbf);
		}
		if (lTf!=null) {
			props.put("javax.xml.transform.TransformerFactory"  , lTf);
		}		
		
		return result;
	}
	
	public static String transformarXML(Document doc, String strXSL, String encoding) throws Exception {
		return transformarXML(doc,strXSL,encoding,false);
	}
		
	public static String transformarXML(
			Document doc, 
			String strXSL, 
			String encoding,
			boolean indent) throws Exception {
		
		try {
			long iniTime = System.currentTimeMillis();
			
			// Instanciamos y obtenemos un Transformer para el XSL
			TransformerFactory tf = TransformerFactory.newInstance();

			Transformer t=null;
			if (strXSL != null) {
				// Obtenemos el XSL de transformación
				StreamSource strSource = new StreamSource(new File(strXSL));
				t = tf.newTransformer(strSource);
			}
			else {
				t = tf.newTransformer();
			}
			
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			StreamResult result = new StreamResult ( new OutputStreamWriter (bao, encoding));
		      			
			// Obtenemos la transformación del XML
			DOMSource source = new DOMSource(doc);
			if(indent){
				t.setOutputProperty(OutputKeys.INDENT, "yes");
			}
			t.transform(source, result);
			
			String lRet = bao.toString(encoding);
			
			log.info("Transformacion aplicada con XSL [" + strXSL + "] en [" + (System.currentTimeMillis()-iniTime) + "] ms.");
			return lRet;
		} 
		catch (Exception e) {
			log.error("Se ha producido un error en la función transformarXML", e);
			throw e;
		}
	}
	
	/**
	 * Método que devuelve el valor de un nodo contenido en una lista. Tanto la lista como 
	 * el nombre del Nodo se pasan como parámetro.
	 * 
	 * @param nodes NodeList
	 * @param nodeName String
	 * @return String
	 */
	public static String getValueFromNodeName(NodeList nodes, String nodeName) {
		String value = null;
		
		if(nodes != null) {
			int len = nodes.getLength();
			for(int i=0 ; i<len ; i++) {
				Node node = nodes.item(i);
				value = getValueFromNodeName(node, nodeName);
				if (value!=null) break;
			}
		}
		
		return value;
	}
	
	public static String getValueFromNode(NodeList nodes) {
		String value = null;
		
		if(nodes != null) {
			int len = nodes.getLength();
			for(int i=0 ; i<len ; i++) {
				Node node = nodes.item(i);
				value = getValueFromNode(node);
				if (value!=null) break;
			}
		}
		
		return value;
	}

	public static List<String> getListFromNode(NodeList nodes) {
		List<String> result = new ArrayList<String>();
		
		if(nodes != null) {
			int len = nodes.getLength();
			for(int i=0 ; i<len ; i++) {
				Node node = nodes.item(i);
				String value = getValueFromNode(node);
				result.add(value);
			}
		}
		
		return result;
	}

	
	/**
	 * Método que devuelve el valor de un nodo contenido en una lista. Tanto la lista como 
	 * el nombre del Nodo se pasan como parámetro.
	 * 
	 * @param nodes NodeList
	 * @param nodeName String
	 * @return String
	 */
	public static String getValueFromNodeName(Node node, String nodeName) {
		String value = null;
		
		if(node == null) {
			return null;
		}

		if(node != null && 
				node.getNodeName() != null && 
				node.getNodeName().equalsIgnoreCase(nodeName)) {
			
			if(node.getFirstChild() != null) {
				value = node.getFirstChild().getNodeValue();
			}
			
		} 
		else if(node.hasChildNodes()) {
			value = getValueFromNodeName(node.getChildNodes(), nodeName);
		}
		
		return value;
	}	

	public static String getValueFromNode(Node node) {
		String value = null;
		
		if(node == null) {
			return null;
		}

		if(node != null) {
			
			if(node.getFirstChild() != null) {
				value = node.getFirstChild().getNodeValue();
			}
			else if(node.hasChildNodes()) {
				value = getValueFromNode(node.getChildNodes());
			}
		} 
		
		return value;
	}	

	
	/**
	 * Método que se encarga de devolver el resultado de la ejecución de una expresión 
	 * XPath sobre un Nodo XML. Tanto la expresión XPath como el Nodo se pasan como parámetro.
	 * 
	 * @param document Node
	 * @param xp String
	 * @return Object
	 * @throws Exception
	 */
	public static NodeList executeXpathQuery(Node node, String strXpath) throws XPathExpressionException {
		
		NodeList result = null;
		if(strXpath != null) {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile(strXpath);
			result = (NodeList)expr.evaluate(node, XPathConstants.NODESET);
		}
		
		return result;
	}
	
	/*
	 * Retorna el xpath relatiu al node indicat
	 */
	public static String generaXPathFromNode(Node node, String nodeNameRoot) {
		String xPathNode = "";
		
		List<String> xpathStructure = genereXPathStructureFromNode(node, nodeNameRoot);
		if(xpathStructure==null || xpathStructure.isEmpty()) {
			return xPathNode;
		}
		
		//Genera el String xpath
		for(String xpath : xpathStructure) {
			xPathNode = xPathNode + xpath;
		}
		
		//Elimina el primer '/' per tal que el xpath sigui relatiu
		xPathNode = xPathNode.substring(1);
		
		return xPathNode;
	}
	
	private static List<String> genereXPathStructureFromNode(Node node, String nodeNameRoot) {
		List<String> elem = new ArrayList<String>();
		
		Node parent = node.getParentNode();
		if(parent != null && 
				!parent.getNodeName().equalsIgnoreCase(nodeNameRoot)) {
			
			List<String> xpathRecursiu = genereXPathStructureFromNode(parent, nodeNameRoot);
			elem.addAll(xpathRecursiu);
		} 
		elem.add("/" + node.getNodeName());
		
		return elem;
	}		
}

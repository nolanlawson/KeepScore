package com.nolanlawson.keepscore.helper;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * XML utilites
 * 
 * @author nolan
 * 
 */
public class XmlHelper {

	public static String prettyPrint(String unformattedXml) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			// initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			StreamSource source = new StreamSource(new StringReader(
					unformattedXml));
			transformer.transform(source, result);
			return result.getWriter().toString();
		} catch (Throwable t) {
			// should never happen
			throw new RuntimeException(t);
		}
	}

	public static XmlPullParser loadData(String xmlData)
			throws XmlPullParserException {
		XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
		XmlPullParser parser = parserFactory.newPullParser();
		parser.setInput(new StringReader(xmlData));
		
		return parser;
	}
	
}

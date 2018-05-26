package com.etybeno.freelancer;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SampleParser {

    // XML Source
    private static final String INVOICE_XML = "/home/thangpham/Downloads/invoice.xml";

    private static final String POSTAL_ADDRESS = "PostalAddress";

    public SampleParser() {
        try {
            File file = new File(INVOICE_XML);

            //Proceed only if xml file exists
            if (file.exists()) {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                //Ignore the DTD since its DTD is not available
                docBuilder.setEntityResolver(ignoreDtdResolver());
                Document document = docBuilder.parse(file);
                document.getDocumentElement().normalize();
                //
                //Fix for 2 processors, we can increase by configuration later
                ExecutorService executors = Executors.newFixedThreadPool(2);
                //get all POSTAL_ADDRESS elements from xml
                //This will use AddressProcessor to handle Address node.
                //We can easily add more processor to handle other nodes.
                NodeList postalAddressItemsList = document.getElementsByTagName(POSTAL_ADDRESS);
                if (postalAddressItemsList.getLength() != 0) {
                    executors.execute(new AddressProcessor(postalAddressItemsList));
                    //
                    executors.shutdown();
                    while (!executors.awaitTermination(1, TimeUnit.SECONDS)) {
                    }
                    //
                    System.out.println("All task handlers finished.");
                } else System.err.println("XML file does not contain any address part");
            } else {
                System.out.println(INVOICE_XML + " Does not exists.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public EntityResolver ignoreDtdResolver() {
        return new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                if (systemId.contains(".dtd")) {
                    return new InputSource(new StringReader(""));
                } else return null;
            }
        };
    }


    public static void main(String[] args) {
        SampleParser parser = new SampleParser();
    }
}

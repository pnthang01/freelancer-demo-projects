package com.etybeno.freelancer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AddressProcessor implements Runnable {

    private CommonAddressCache commonAddressCache = CommonAddressCache.loadCache();

    public static final int CHUNK_SIZE = 3;

    // Required Fields(Tag Names) from XML
    private static final String STREET = "Street";
    private static final String CITY = "City";
    private static final String STATE = "State";
    private static final String POSTAL_CODE = "PostalCode";
    private static final String COUNTRY = "Country";
    private static final String ISO_COUNTRY_CODE = "isoCountryCode";

    private NodeList postalAddressItemsList;

    public AddressProcessor(NodeList postalAddressItemsList) {
        this.postalAddressItemsList = postalAddressItemsList;
    }

    public void run() {
        int size = postalAddressItemsList.getLength();
        int counter = 0;
        List<Node> xmlAddressNode = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        try {
            while (counter < size) {
                Node postalAddressItem = postalAddressItemsList.item(counter++);
                // retrieve required element values from the postal address element from xml
                xmlAddressNode.add(postalAddressItem);
                //We split address to chunk size to process
                if (xmlAddressNode.size() >= CHUNK_SIZE || counter >= size) {
                    executorService.execute(new AddressChunkTask(xmlAddressNode));
                    xmlAddressNode.clear();
                }
            }
            executorService.shutdown();
            while (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
            }
            System.out.println("All addresses are finished with size " + size);
        } catch (Exception ex) {
            System.err.println("Error during process address: " + ex.getMessage());
        }
    }

    private class AddressChunkTask implements Runnable {

        private List<Node> addressList = new ArrayList<Node>();

        AddressChunkTask(List<Node> postalAddressItemList) {
            this.addressList.addAll(postalAddressItemList);
        }

        public void run() {
            for (Node node : addressList) {
                try {
                    List<String> streets = getItemValue(node, STREET);
                    List<String> cityValy = getItemValue(node, CITY);
                    String city = cityValy.isEmpty()? "" : cityValy.get(0);
                    List<String> stateValue = getItemValue(node, STATE);
                    String state = stateValue.isEmpty() ? "" : stateValue.get(0);
                    List<String> postalValue = getItemValue(node, POSTAL_CODE);
                    String postalCode = postalValue.isEmpty() ? "" : postalValue.get(0);
                    String countryCode = getAttributeValue(node, COUNTRY, ISO_COUNTRY_CODE);
                    String street = streets.stream().collect(Collectors.joining(","));
                    Address address = commonAddressCache.getAddress(street, city, state, postalCode, countryCode);
//                displayAddress(address);
                } catch (Exception ex) {
                    System.err.println("Address data is wrong format: " + node.getNodeValue());
                }
            }
        }
    }

    /**
     * This method can be write to abstract processor class if we implement new features
     *
     * @param item
     * @param tagName
     * @return
     */
    private List<String> getItemValue(Node item, String tagName) {
        List<String> rs = new ArrayList<String>();
        NodeList tagNameNodeList = ((Element) item).getElementsByTagName(tagName);
        for (int i = 0; i < tagNameNodeList.getLength(); i++) {
            String childItemValue = tagNameNodeList.item(i).getTextContent().trim();
            if (childItemValue.length() > 0) {
                rs.add(childItemValue);
            }
        }
        return rs;
    }

    /**
     * This method can be write to abstract processor class if we implement new features
     *
     * @param item
     * @param tagName
     * @param attrName
     * @return
     */
    private String getAttributeValue(Node item, String tagName, String attrName) {
        String itemValue = "";
        Element elm = (Element) item;
        NodeList tagNameNodeList = elm.getElementsByTagName(tagName);
        for (int i = 0; i < tagNameNodeList.getLength(); i++) {
            Node childItem = tagNameNodeList.item(i);
            itemValue += ((Element) childItem).getAttribute(attrName) + ",";
            break;
        }
        if (itemValue.endsWith(",")) {
            itemValue = itemValue.substring(0, itemValue.length() - 1);
        }

        return itemValue;
    }

    private void displayAddress(Address obj) {
        System.out.println("Street       -->" + obj.getLines());
        System.out.println("City         -->" + obj.getCity());
        System.out.println("State        -->" + obj.getState());
        System.out.println("Postal Code  -->" + obj.getPostalCode());
        System.out.println("Country Code -->" + obj.getCountryCode());
        System.out.println("------------------------**********************------------------");
    }

}

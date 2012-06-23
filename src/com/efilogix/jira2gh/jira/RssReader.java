package com.efilogix.jira2gh.jira;

import java.io.Reader;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/** reads a JIRA XML generated by the RSS creator (a simple report) */
public class RssReader implements Runnable {

    private ProjectCollector collector;

    private Reader xml;

    @Override
    public void run() {
        SAXReader reader = new SAXReader();
        try {
            Document doc = reader.read(xml);
            Element root = doc.getRootElement();
            List<Element> nodes = doc.selectNodes("//channel/item");
            for (Element node : nodes) {
                collector.feed(node);
            }
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    public void setXml(Reader xml) {
        this.xml = xml;
    }

    public void setCollector(ProjectCollector collector) {
        this.collector = collector;
    }
}
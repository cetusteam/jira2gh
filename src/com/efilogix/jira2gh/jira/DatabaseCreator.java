package com.efilogix.jira2gh.jira;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class DatabaseCreator {

    private Reader xml;

    private String dbUrl;

    Connection connection;

    public void run() throws Exception {
        Map<String, Set<String>> tables = new HashMap<String, Set<String>>();
        Class.forName("org.hsqldb.jdbcDriver");
        connection = DriverManager.getConnection(dbUrl);
        //
        SAXReader reader = new SAXReader();
        Document doc = reader.read(xml);
        Element root = doc.getRootElement();
        List<Element> nodes = root.elements();
        for (Element node : nodes) {
            String tableName = "t_" + node.getName();
            Set<String> columns = tables.get(tableName);
            if (columns == null) {
                createTable(tableName);
                columns = new HashSet<String>();
                tables.put(tableName, columns);
            }
            List<Attribute> attributes = node.attributes();
            Map<String, String> keyValues = new LinkedHashMap<String, String>();
            for (Attribute attribute : attributes) {
                keyValues
                        .put("jg_" + attribute.getName(), attribute.getValue());
            }
            // sub elements
            List<Element> subnodes = node.elements();
            for (Element subnode : subnodes) {
                keyValues.put("jg_" + subnode.getName(), subnode.getText());
            }
            insertValues(tableName, columns, keyValues);
        }
        connection.commit();
        if (!dbUrl.contains(":mem:")) {
            PreparedStatement ps = connection.prepareStatement("shutdown");
            ps.executeUpdate();
            ps.close();
        }
        connection.close();
    }

    private void insertValues(String tableName, Set<String> columns,
            Map<String, String> keyValues) throws SQLException {
        for (String columnName : keyValues.keySet()) {
            if (!columns.contains(columnName)) {
                PreparedStatement ps = connection
                        .prepareStatement("alter  table " + tableName
                                + " add column " + columnName
                                + " varchar(16000)");
                ps.executeUpdate();
                ps.close();
                columns.add(columnName);
            }
        }
        String sql = "insert into " + tableName + " ("
                + StringUtils.join(keyValues.keySet(), ",") + ") values ("
                + StringUtils.repeat("?", ", ", keyValues.size()) + ")";
        PreparedStatement ps = connection.prepareStatement(sql);
        int i = 0;
        for (String value : keyValues.values()) {
            i++;
            ps.setString(i, value);
        }
        ps.executeUpdate();
        ps.close();
    }

    private void createTable(String tableName) throws Exception {
        String sql = "create table " + tableName + " (dummy_field varchar(1))";
        PreparedStatement ps = connection.prepareStatement(sql);
        System.out.println("creating table: " + tableName + " -- " + sql);
        ps.executeUpdate();
        ps.close();
    }

    public void setXml(Reader xml) {
        this.xml = xml;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }
}

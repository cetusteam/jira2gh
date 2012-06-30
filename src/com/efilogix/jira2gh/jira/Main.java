package com.efilogix.jira2gh.jira;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.StringUtils;

public class Main {

    public static void main(String[] args) throws Exception {
        String xsltFile = IOUtils.toString(new AutoCloseInputStream(ClassLoader
                .getSystemResourceAsStream("markdown.xslt")), "iso-8859-1");
        System.out.println("First chars of template: "
                + StringUtils.left(xsltFile, 50));
        //
        File xmlFile = new File(args[0]);
        String xml = FileUtils.readFileToString(xmlFile);
        Reader xmlReader = new StringReader(xml);
        String dbUrl = "jdbc:hsqldb:mem:."; //"jdbc:hsqldb:file:test.database.hsql"
        //
        int baseGhIssue = Integer.parseInt(args[1]);
        //
        DatabaseCreator dbCreator = new DatabaseCreator();
        dbCreator.setXml(xmlReader);
        dbCreator.setDbUrl(dbUrl);
        dbCreator.run();
        //
        ProjectCreator pc = new ProjectCreator();
        pc.setDbUrl(dbUrl);
        pc.run();
        //
        System.out.println("total projects: " + pc.getProjects().size());
        for (Project project : pc.getProjects()) {
            if (!project.key.equals("PER")) {
                continue;
            }
            Converter conv = new Converter();
            conv.setProject(project);
            conv.setBaseGhIssue(baseGhIssue);
            conv.run();
        }
    }
}

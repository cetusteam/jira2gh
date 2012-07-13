package com.efilogix.jira2gh.jira;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.FileUtils;

import com.efilogix.jira2gh.UploadToGithub;

public class Main {

    public static void main(String[] args) throws Exception {
        File xmlFile = new File(args[0]);
        String xml = FileUtils.readFileToString(xmlFile);
        Reader xmlReader = new StringReader(xml);
        String dbUrl = "jdbc:hsqldb:mem:."; // "jdbc:hsqldb:file:test.database.hsql"
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
        pc.setBaseGhIssue(baseGhIssue);
        pc.run();
        //
        System.out.println("total projects: " + pc.getProjects().size());
        for (Project project : pc.getProjects()) {
            if (!project.key.equals("PER")) {
                continue;
            }
            Converter conv = new Converter();
            conv.setProject(project);
            conv.setProjects(pc.getProjects());
            conv.run();
        }
        // Upload the project data to github
        UploadToGithub github = new UploadToGithub();
        github.setProjectCreator(pc);
        // github.run();
    }
}

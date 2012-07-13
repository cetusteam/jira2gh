package com.efilogix.jira2gh;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import com.efilogix.jira2gh.jira.Issue;
import com.efilogix.jira2gh.jira.Project;
import com.efilogix.jira2gh.jira.ProjectCreator;

public class UploadToGithub implements Runnable {

    private static Map<String, String> projectMatching = new HashMap<String, String>();
    // Matching between the Jira and Github projects.
    static {
        projectMatching.put("Tools", "tools");
        projectMatching.put("Perseus", "perseus");
    }
    private static Map<String, String> userMatching = new HashMap<String, String>();
    static {
        userMatching.put("javier", "jmena");
        userMatching.put("ivan", "iromero");
        userMatching.put("aldemar", "avillegas");
    }
    private Map<String, User> githubUsers = new HashMap<String, User>();
    private ProjectCreator projectCreator;
    private List<Repository> repositories;

    public void setProjectCreator(ProjectCreator projectCreator) {
        this.projectCreator = projectCreator;
    }

    private void doRequest() {
        getGithubRepositories();
        getGithubUsers();
        for (Project project : projectCreator.getProjects()) {
            System.out.println("collector project name " + project.name);
            if (projectMatching.containsKey(project.name)) {
                uploadIssuesToProject(projectMatching.get(project.name),
                        project);
            }
        }
        System.out.println("In the request");
        GitHubClient client = new GitHubClient();
        client.setCredentials("iromero", "cupel7lids");
        System.out.println("client.getUser() " + client.getUser());
    }

    private void getGithubUsers() {
        GitHubClient client = new GitHubClient();
        client.setCredentials("iromero", "cupel7lids");
        UserService userService = new UserService(client);
        for (Map.Entry<String, String> user : userMatching.entrySet()) {
            try {
                User gitHubClient = userService.getUser(user.getValue());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void uploadIssuesToProject(String githubProjectName,
            Project jiraProject) {
        Repository repository = getRepository(githubProjectName);
        if (repository == null) {
            return;
        }
        for (Issue issue : jiraProject.issues) {
            createIssue(issue, repository);
        }
    }

    private void createIssue(Issue jiraIssue, Repository repository) {
        org.eclipse.egit.github.core.Issue githubIssue = new org.eclipse.egit.github.core.Issue();
        // TODO: Get the matching github user.
        // githubIssue.setAssignee(issue.assignee);
    }

    private Repository getRepository(String projectName) {
        for (Repository repository : repositories) {
            if (repository.getName().equals(projectName)) {
                return repository;
            }
        }
        return null;
    }

    private void getGithubRepositories() {
        RepositoryService service = new RepositoryService();
        service.getClient().setCredentials("iromero", "cupel7lids");
        try {
            repositories = service.getRepositories();
            for (Repository repo : service.getRepositories())
                System.out.println(repo.getName() + " Watchers: "
                        + repo.getWatchers());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        doRequest();
    }
}

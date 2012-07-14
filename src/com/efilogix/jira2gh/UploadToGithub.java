package com.efilogix.jira2gh;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.MilestoneService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import com.efilogix.jira2gh.jira.Issue;
import com.efilogix.jira2gh.jira.Project;
import com.efilogix.jira2gh.jira.ProjectCreator;

public class UploadToGithub implements Runnable {

    private static Map<String, String> projectMatching = new HashMap<String, String>();
    // TODO: What are the name of another jira projects that we want to upload
    // to Github too?
    // Matching between the Jira and Github projects.
    static {
        projectMatching.put("Tools", "tools");
        projectMatching.put("Perseus", "perseus");
    }
    private static Map<String, String> userMatching = new HashMap<String, String>();
    // TODO: Has Jairo any assigned in Jira so we can make a mapping for him
    // too?
    static {
        userMatching.put("javier", "jmena");
        userMatching.put("ivan", "iromero");
        userMatching.put("aldemar", "avillegas");
    }
    private Map<String, Integer> milestoneMatching = new HashMap<String, Integer>();
    private Map<String, String> issueMatching = new HashMap<String, String>();
    private Map<String, User> githubUsers = new HashMap<String, User>();
    private ProjectCreator projectCreator;
    private List<Repository> repositories;
    private Map<String, List<Milestone>> githubMilestonesByRepositories = new HashMap<String, List<Milestone>>();

    public void setProjectCreator(ProjectCreator projectCreator) {
        this.projectCreator = projectCreator;
    }

    private void loadData() {
        // Load from a csv file.
        loadMatchingMilestones();
        // Load from a csv file.
        loadMathchingIssues();
        // Get from github
        getGithubRepositories();
        // Get form github.
        getGithubUsers();
        // Get from github
        getGithubMilestones();
    }

    private void doRequest() {
        // Per every jira project it upload all its issues to Github
        for (Project project : projectCreator.getProjects()) {
            System.out.println("collector project name " + project.name);
            if (projectMatching.containsKey(project.name)) {
                uploadIssuesToProject(projectMatching.get(project.name),
                        project);
            }
        }
    }

    /**
     * Get the users from Github and put them in a Map.
     */
    private void getGithubUsers() {
        GitHubClient client = new GitHubClient();
        client.setCredentials("iromero", "cupel7lids");
        UserService userService = new UserService(client);
        for (Map.Entry<String, String> user : userMatching.entrySet()) {
            try {
                User gitHubClient = userService.getUser(user.getValue());
                githubUsers.put(user.getKey(), gitHubClient);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Upload to Github all the issues of a specific jira project.
     */
    private void uploadIssuesToProject(String githubProjectName,
            Project jiraProject) {
        // Get the matching repository in github
        Repository repository = getRepository(githubProjectName);
        if (repository == null) {
            return;
        }
        for (Issue issue : jiraProject.issues) {
            createIssue(issue, repository);
        }
    }

    /**
     * Create a github issue.
     */
    private void createIssue(Issue jiraIssue, Repository repository) {
        org.eclipse.egit.github.core.Issue githubIssue = new org.eclipse.egit.github.core.Issue();
        // Get the matching github user.
        User githubUser = getGithubUser(jiraIssue.assignee);
        if (githubIssue != null) {
            githubIssue.setAssignee(githubUser);
        }
        // TODO: I'm assuming that the body of the jiraissue is html.
        githubIssue.setBodyHtml(jiraIssue.body);
        githubIssue.setTitle(jiraIssue.title);
        // TODO: Check if the jira resolution could be used in github without
        // any problem.
        githubIssue.setState(jiraIssue.resolution);
        Milestone githubMilestone = getGithubMilestone(jiraIssue.milestone);
        githubIssue.setMilestone(githubMilestone);
    }

    /**
     * Get the github milestone
     */
    private Milestone getGithubMilestone(String jiraMilestone) {
        String jiraMilestones[] = jiraMilestone.split(",");
        String firstJiraMilestone = "";
        Milestone milestone = null;
        if (jiraMilestones.length > 0) {
            firstJiraMilestone = jiraMilestones[0];
        } else {
            // There is not jira milestone, I don't know if: A) create a new
            // github milestone or B) use a github milestone in order to assign
            // it all the jira issues without milestone.
        }
        if (milestoneMatching.containsKey(firstJiraMilestone)) {
            // milestone = milestoneMatching.get(firstJiraMilestone);
        }
        return milestone;
    }

    /**
     * Get a github user by name
     */
    private User getGithubUser(String userName) {
        if (userName == null) {
            return null;
        }
        if (githubUsers.containsKey(userName)) {
            return githubUsers.get(userName);
        }
        return null;
    }

    /**
     * Get a github repository by name
     */
    private Repository getRepository(String projectName) {
        for (Repository repository : repositories) {
            if (repository.getName().equals(projectName)) {
                return repository;
            }
        }
        return null;
    }

    /**
     * Get all the available repositories in github.
     */
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

    /**
     * Get all milestones from github
     */
    private void getGithubMilestones() {
        MilestoneService service = new MilestoneService();
        service.getClient().setCredentials("iromero", "cupel7lids");
        // TODO: Not sure yet if user the userid, repository id and if exist an
        // state equal to all
        try {
            for (Repository repository : repositories) {
                List<Milestone> milestones = service.getMilestones("iromero",
                        repository.getName(), "all");
                githubMilestonesByRepositories.put(repository.getName(),
                        milestones);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Load the milestones from a csv file to a Map
     */
    private void loadMatchingMilestones() {
        // Load in milestoneMatching
    }

    /**
     * Load the issues from a csv file to a Map.
     */
    private void loadMathchingIssues() {
        // Load in issueMatching
    }

    @Override
    public void run() {
        loadData();
        doRequest();
    }
}

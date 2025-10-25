### Java Spring template project

This project is based on a GitLab [Project Template](https://docs.gitlab.com/ee/gitlab-basics/create-project.html).

Improvements can be proposed in the [original project](https://gitlab.com/gitlab-org/project-templates/spring).

### CI/CD with Alina DevOps

This template is compatible with [Alina DevOps](https://github.com/Aliteya).

If Alina DevOps is not already enabled for this project, you can [turn it on](https://github.com/Aliteya) in the project settings.

### IMPORTANT

In [pom.xml](pom.xml) file change repository name:

```xml
    <properties>
		<java.version>22</java.version>
		<sonar.projectKey>Marketplace-internship-project_YOUR-REPOSITORY-NAME</sonar.projectKey>
	</properties>
```
to correct one

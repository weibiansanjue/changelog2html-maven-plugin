package world.weibiansanjue.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 */
@Mojo(name = "echo")
public class DemoMojo extends AbstractMojo {

    @Parameter(property = "project.groupId")
    private String groupId;

    @Parameter(property = "project.artifactId")
    private String artifactId;

    @Parameter(property = "project.version")
    private String version;

    /**
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("groupId: " + groupId);
        getLog().info("artifactId: " + artifactId);
        getLog().info("version: " + version);
    }
}

package world.weibiansanjue.maven.plugins;

import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mojo(name = "convert")
public class ConvertMojo extends AbstractMojo {

    @Parameter(property = "inputFilePath", defaultValue = "${basedir}/CHANGELOG.md")
    private File inputFile;

    @Parameter(property = "outputFilePath", defaultValue = "${basedir}/changelog.html")
    private File outputFile;

    @Parameter(property = "title", defaultValue = "${project.artifactId} 变更记录")
    private String title;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Map<String, String> params = new HashMap<>();
        params.put("title", title);
        getLog().info("start convert: " + inputFile);
        try {
            convert(inputFile, outputFile, params);
            getLog().info("convert success: " + outputFile);
        } catch (IOException e) {
            getLog().error("convert fail!", e);
        }
    }

    private void convert(File input, File output, Map<String, String> params) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(input.toURI()), StandardCharsets.UTF_8);
        String markdown = String.join("\n", lines);
        markdown = markdown.replace("<!-- <div class=\"cl-context-version\"> -->", "<div class=\"cl-context-version\">")
                .replace("<!-- </div> -->", "</div>");

        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(TocExtension.create()))
                .set(TocExtension.TITLE, "版本目录")
                .set(TocExtension.TITLE_LEVEL, 3)
                .set(TocExtension.LIST_CLASS, "cl-toc-ul")
                .set(TocExtension.DIV_CLASS, "cl-toc");

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        String html = renderer.render(parser.parse(markdown));


        String htmlTemplate = ResourceUtil.getString("template/changelog.html");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            htmlTemplate = htmlTemplate.replace("${" + entry.getKey() + "}", entry.getValue());
            getLog().info("params " + entry.getKey() + "=" + entry.getValue());
        }

        html = htmlTemplate.replace("${cl-context}", html);

        FileUtils.writeStringToFile(
                output,
                html,
                StandardCharsets.UTF_8,
                false);
    }
}

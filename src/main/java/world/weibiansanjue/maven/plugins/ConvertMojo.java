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
import java.util.List;

@Mojo(name = "convert")
public class ConvertMojo extends AbstractMojo {

    private static final String INPUT = "%s%sCHANGELOG.md";
    private static final String OUTPUT = "%s%schangelog.html";

    @Parameter(property = "basedir")
    private String basedir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String input = String.format(INPUT, basedir, File.separator);
        String output = String.format(OUTPUT, basedir, File.separator);
        getLog().info("开始转换：" + input);
        try {
            convert(input, output);
            getLog().info("转换成功: " + output);
        } catch (IOException e) {
            getLog().error("转换失败!", e);
        }
    }

    private static void convert(String input, String output) throws IOException {

        List<String> lines = Files.readAllLines(Paths.get(input), StandardCharsets.UTF_8);
        String markdown = String.join("\n", lines);

        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(TocExtension.create()));

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        String html = renderer.render(parser.parse(markdown));

        FileUtils.writeStringToFile(
                new File(output),
                html,
                StandardCharsets.UTF_8,
                false);
    }

}

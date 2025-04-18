package world.weibiansanjue.maven.plugins;

import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Mojo(
        name = "convert"
        , defaultPhase = LifecyclePhase.PROCESS_RESOURCES
)
public class ConvertMojo extends AbstractMojo {

    @Parameter(property = "files")
    private List<ConverFile> files;

    @Parameter(property = "inputFilePath", defaultValue = "${basedir}/CHANGELOG.md")
    private File inputFile;

    @Parameter(property = "outputFilePath", defaultValue = "${basedir}/changelog.html")
    private File outputFile;

    @Parameter(property = "title", defaultValue = "${project.artifactId} 变更记录")
    private String title;

    private static final String TOC_DEFINE = "[TOC levels=%d]";
    private static final int TOC_LEVELS_DEFAULT = 2;
    private static final String VERSION_CONTEXT_DIV_START = "<div class=\"cl-context-version\">";
    private static final String VERSION_CONTEXT_DIV_END   = "</div>";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (files != null && !files.isEmpty()) {
            // 处理多文件转换
            for (ConverFile file : files) {

                File input = file.getInput();
                File output = file.getOutput();

                getLog().info("start convert: " + input);
                Map<String, String> params = file.getParams();
                if (Objects.isNull(params)) {
                    params = new HashMap<>();
                }
                try {
                    convert(input, output, params);
                    getLog().info("convert success: " + output);
                } catch (IOException e) {
                    getLog().error("convert fail: " + input, e);
                }
            }
        } else {
            // 保持原有的单文件转换逻辑
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
    }

    private void convert(File input, File output, Map<String, String> params) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(StringUtils.LF);
        lines.add(getTocLevels(params.get("tocLevel")));
        lines.add(StringUtils.LF);
        lines.add(VERSION_CONTEXT_DIV_START);
        lines.add(StringUtils.LF);
        lines.addAll(Files.readAllLines(Paths.get(input.toURI()), StandardCharsets.UTF_8));
        lines.add(StringUtils.LF);
        lines.add(VERSION_CONTEXT_DIV_END);
        lines.add(StringUtils.LF);
        String markdown = String.join(StringUtils.LF, lines);

        getLog().debug("markdown content: " + markdown);

        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(
                    TocExtension.create(),
                    TablesExtension.create(),
                    TaskListExtension.create()
                ))
               .set(TocExtension.TITLE, params.getOrDefault("tocTitle", "版本目录"))
               .set(TocExtension.TITLE_LEVEL, 3)
               .set(TocExtension.LIST_CLASS, "cl-toc-ul")
               .set(TocExtension.DIV_CLASS, "cl-toc")
               .set(TablesExtension.CLASS_NAME, "cl-table")
               .set(HtmlRenderer.CODE_STYLE_HTML_OPEN, "<code class=\"cl-code\">")
               .set(HtmlRenderer.CODE_STYLE_HTML_CLOSE, "</code>")
               .set(TaskListExtension.ITEM_DONE_MARKER,
                    "<input type=\"checkbox\" class=\"task-list-item-checkbox\" checked>")
               .set(TaskListExtension.ITEM_NOT_DONE_MARKER,
                    "<input type=\"checkbox\" class=\"task-list-item-checkbox\">");

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        String html = renderer.render(parser.parse(markdown));

        String htmlTemplate = ResourceUtil.getString("template/changelog.html");
        if (!params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                htmlTemplate = htmlTemplate.replace("${" + entry.getKey() + "}", entry.getValue());
                getLog().info("params " + entry.getKey() + " = " + entry.getValue());
            }
        }
        html = htmlTemplate.replace("${cl-context}", html);

        FileUtils.writeStringToFile(output, html, StandardCharsets.UTF_8, false);
    }

    private String getTocLevels(String levels) {
        String define = String.format(TOC_DEFINE, TOC_LEVELS_DEFAULT);
        if (StringUtils.isEmpty(levels)) {
            return define;
        }
        try {
            int l = Integer.parseInt(levels);
            int ll = l >= 2 && l <= 4 ? l : TOC_LEVELS_DEFAULT;
            return String.format(TOC_DEFINE, ll);
        } catch (Exception e) {
            return define;
        }
    }

}

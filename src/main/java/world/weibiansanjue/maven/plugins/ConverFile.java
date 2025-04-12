package world.weibiansanjue.maven.plugins;

import java.io.File;
import java.util.Map;

public class ConverFile {

    /** 输入文件路径 */
    private File input;

    /** 输出文件路径 */
    private File output;

    /** 自定义参数 */
    private Map<String, String> params;

    public File getInput() {
        return input;
    }

    public File getOutput() {
        return output;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
    
}

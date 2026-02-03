package ch.so.agi.sodata.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gg.jte")
public class JteProperties {

    private boolean developmentMode = false;
    private boolean usePrecompiledTemplates = false;
    private String templateLocation = "src/main/jte";
    private String templateSuffix = ".jte";
    private boolean exposeRequestAttributes = false;
    private boolean trimControlStructures = false;

    public boolean isDevelopmentMode() {
        return developmentMode;
    }

    public void setDevelopmentMode(boolean developmentMode) {
        this.developmentMode = developmentMode;
    }

    public boolean isUsePrecompiledTemplates() {
        return usePrecompiledTemplates;
    }

    public void setUsePrecompiledTemplates(boolean usePrecompiledTemplates) {
        this.usePrecompiledTemplates = usePrecompiledTemplates;
    }

    public String getTemplateLocation() {
        return templateLocation;
    }

    public void setTemplateLocation(String templateLocation) {
        this.templateLocation = templateLocation;
    }

    public String getTemplateSuffix() {
        return templateSuffix;
    }

    public void setTemplateSuffix(String templateSuffix) {
        this.templateSuffix = templateSuffix;
    }

    public boolean isExposeRequestAttributes() {
        return exposeRequestAttributes;
    }

    public void setExposeRequestAttributes(boolean exposeRequestAttributes) {
        this.exposeRequestAttributes = exposeRequestAttributes;
    }

    public boolean isTrimControlStructures() {
        return trimControlStructures;
    }

    public void setTrimControlStructures(boolean trimControlStructures) {
        this.trimControlStructures = trimControlStructures;
    }
}

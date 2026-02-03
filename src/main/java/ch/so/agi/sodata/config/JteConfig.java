package ch.so.agi.sodata.config;

import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;

import java.nio.file.FileSystems;
import java.nio.file.Paths;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JteProperties.class)
public class JteConfig {

    @Bean
    public TemplateEngine jteTemplateEngine(JteProperties jteProperties) {
        if (jteProperties.isDevelopmentMode() && jteProperties.isUsePrecompiledTemplates()) {
            throw new IllegalStateException("You can't use development mode and precompiled templates together");
        }
        if (jteProperties.isUsePrecompiledTemplates()) {
            return TemplateEngine.createPrecompiled(ContentType.Html);
        }
        if (jteProperties.isDevelopmentMode()) {
            String[] split = jteProperties.getTemplateLocation().split("/");
            CodeResolver codeResolver = new DirectoryCodeResolver(FileSystems.getDefault().getPath("", split));
            TemplateEngine templateEngine = TemplateEngine.create(codeResolver, Paths.get("jte-classes"),
                    ContentType.Html, getClass().getClassLoader());
            templateEngine.setTrimControlStructures(jteProperties.isTrimControlStructures());
            return templateEngine;
        }
        throw new IllegalStateException(
                "You need to either set gg.jte.usePrecompiledTemplates or gg.jte.developmentMode to true");
    }

    @Bean
    public ViewResolver jteViewResolver(TemplateEngine templateEngine, JteProperties jteProperties) {
        return new JteViewResolver(templateEngine, jteProperties);
    }
}

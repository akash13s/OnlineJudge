package com.projects.onlinejudge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "language-config")
@Configuration
public class LanguageConfig {

    private List<Language> languages;

    private Map<String, Language> languageMap;

    @PostConstruct
    public void init() {
        languageMap = new HashMap<>();
        languages.forEach(language -> languageMap.put(language.getLanguage(), language));
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public Map<String, Language> getLanguageMap() {
        return languageMap;
    }

    public void setLanguageMap(Map<String, Language> languageMap) {
        this.languageMap = languageMap;
    }

    public static class Language {
        private String language;
        private String fileExtension;
        private String compileCommand;
        private String runCommand;

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getFileExtension() {
            return fileExtension;
        }

        public void setFileExtension(String fileExtension) {
            this.fileExtension = fileExtension;
        }

        public String getCompileCommand() {
            return compileCommand;
        }

        public void setCompileCommand(String compileCommand) {
            this.compileCommand = compileCommand;
        }

        public String getRunCommand() {
            return runCommand;
        }

        public void setRunCommand(String runCommand) {
            this.runCommand = runCommand;
        }
    }
}
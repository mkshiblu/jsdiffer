package io.jsrminer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JSRMinerConfig {
    @JsonProperty("excluded_extensions")
    private List<String> excludedExtensions = new ArrayList<>();

    @JsonProperty("included_extensions")
    private List<String> includedExtensions = new ArrayList<>();

    @JsonProperty("projects")
    private Map<String, List<String>> repositoryCommits = new LinkedHashMap<>();

    public static final String FILE_PATH_SEPARATOR = File.separator;

    public static void fromConfigFile() throws IOException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        var x = mapper.readValue(new File("config.yml")
                , JSRMinerConfig.class);
        x.repositoryCommits = null;
    }
}

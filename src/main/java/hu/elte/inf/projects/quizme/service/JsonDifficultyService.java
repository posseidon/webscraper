package hu.elte.inf.projects.quizme.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JsonDifficultyService {

    private static final Logger LOG = LoggerFactory.getLogger(JsonDifficultyService.class);

    private final ObjectMapper objectMapper;
    private JsonNode difficultyConfig;
    private Map<String, String> dataValueToUiLevelCache;

    public JsonDifficultyService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        loadConfiguration();
    }

    private void loadConfiguration() {
        try {
            ClassPathResource resource = new ClassPathResource("config/difficulty-mappings.json");
            difficultyConfig = objectMapper.readTree(resource.getInputStream());
            buildCache();
            LOG.info("Loaded difficulty mappings from JSON file");
        } catch (IOException e) {
            LOG.error("Failed to load difficulty mappings JSON file", e);
            throw new RuntimeException("Could not load difficulty configuration", e);
        }
    }

    private void buildCache() {
        dataValueToUiLevelCache = new HashMap<>();
        JsonNode levels = difficultyConfig.get("difficultyLevels");

        levels.fields().forEachRemaining(entry -> {
            String uiLevel = entry.getKey();
            JsonNode levelConfig = entry.getValue();
            JsonNode mappings = levelConfig.get("mappings");

            mappings.fields().forEachRemaining(langEntry -> {
                JsonNode values = langEntry.getValue();
                if (values.isArray()) {
                    for (JsonNode value : values) {
                        String normalizedValue = value.asText().toLowerCase().trim();
                        dataValueToUiLevelCache.put(normalizedValue, uiLevel);
                    }
                }
            });
        });
    }

    public boolean matchesDifficulty(String questionDifficulty, String uiDifficultyLevel) {
        if (questionDifficulty == null || uiDifficultyLevel == null) {
            return true;
        }

        if ("mixed".equalsIgnoreCase(uiDifficultyLevel)) {
            return true;
        }

        String normalizedQuestion = questionDifficulty.toLowerCase().trim();
        String mappedLevel = dataValueToUiLevelCache.get(normalizedQuestion);

        return uiDifficultyLevel.equalsIgnoreCase(mappedLevel);
    }

    public Optional<String> getUiLevelForQuestionDifficulty(String questionDifficulty) {
        if (questionDifficulty == null) {
            return Optional.empty();
        }

        String normalizedQuestion = questionDifficulty.toLowerCase().trim();
        return Optional.ofNullable(dataValueToUiLevelCache.get(normalizedQuestion));
    }

    @Cacheable("jsonDifficultyLevels")
    public Map<String, DifficultyLevelDto> getAllDifficultyLevels() {
        Map<String, DifficultyLevelDto> levels = new HashMap<>();
        JsonNode levelsNode = difficultyConfig.get("difficultyLevels");

        levelsNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode levelNode = entry.getValue();

            DifficultyLevelDto dto = new DifficultyLevelDto(
                    key,
                    levelNode.get("displayName").asText(),
                    levelNode.get("description").asText(),
                    levelNode.get("icon").asText(),
                    levelNode.get("sortOrder").asInt());

            levels.put(key, dto);
        });

        return levels;
    }

    /**
     * Reload configuration from file (useful for runtime updates)
     */
    public void reloadConfiguration() {
        loadConfiguration();
        LOG.info("Reloaded difficulty mappings configuration");
    }

    /**
     * Add new mapping to JSON and update cache
     * Note: This updates runtime cache only, not the JSON file
     */
    public void addRuntimeMapping(String dataValue, String uiLevel) {
        String normalizedValue = dataValue.toLowerCase().trim();
        dataValueToUiLevelCache.put(normalizedValue, uiLevel);
        LOG.info("Added runtime mapping: '{}' -> {}", dataValue, uiLevel);
    }

    public static class DifficultyLevelDto {
        private final String uiKey;
        private final String displayName;
        private final String description;
        private final String icon;
        private final int sortOrder;

        public DifficultyLevelDto(String uiKey, String displayName, String description, String icon, int sortOrder) {
            this.uiKey = uiKey;
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
            this.sortOrder = sortOrder;
        }

        // Getters
        public String getUiKey() {
            return uiKey;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }

        public int getSortOrder() {
            return sortOrder;
        }
    }
}
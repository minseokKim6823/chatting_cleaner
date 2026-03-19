package taehwa.kakaotalk_chatting_rule.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class ForbiddenWordProvider {

    private static final Logger log = LoggerFactory.getLogger(ForbiddenWordProvider.class);
    private static final String RESOURCE_PATH = "moderation/forbidden-words.txt";

    private final List<String> forbiddenWords;

    public ForbiddenWordProvider() {
        this.forbiddenWords = loadForbiddenWords();
    }

    public boolean contains(String message) {
        String lowerMessage = message.toLowerCase();
        return forbiddenWords.stream().anyMatch(lowerMessage::contains);
    }

    public String firstWord() {
        return forbiddenWords.isEmpty() ? "" : forbiddenWords.get(0);
    }

    private List<String> loadForbiddenWords() {
        ClassPathResource resource = new ClassPathResource(RESOURCE_PATH);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> words = reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !line.startsWith("#"))
                    .map(String::toLowerCase)
                    .toList();

            log.info("loaded {} forbidden words from {}", words.size(), RESOURCE_PATH);
            return words;
        } catch (IOException e) {
            throw new IllegalStateException("failed to load forbidden words from " + RESOURCE_PATH, e);
        }
    }
}

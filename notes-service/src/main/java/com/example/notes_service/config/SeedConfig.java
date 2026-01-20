package com.example.notes_service.config;

import com.example.notes_service.model.Note;
import com.example.notes_service.repository.NoteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SeedConfig {

    @Bean
    CommandLineRunner seedNotes(NoteRepository repo, ObjectMapper mapper) {
        return args -> {
            if (repo.count() > 0) return;

            ClassPathResource resource = new ClassPathResource("seed-notes.json");
            if (!resource.exists()) return;

            try (InputStream is = resource.getInputStream()) {
                List<Note> notes = Arrays.asList(mapper.readValue(is, Note[].class));
                repo.saveAll(notes);
            }
        };
    }
}

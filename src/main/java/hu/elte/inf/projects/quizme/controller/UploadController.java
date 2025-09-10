package hu.elte.inf.projects.quizme.controller;

import hu.elte.inf.projects.quizme.repository.dto.QuizData;
import hu.elte.inf.projects.quizme.service.json.QuizImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("upload")
public class UploadController {
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    private final QuizImportService quizImportService;

    public UploadController(QuizImportService quizImportService) {
        this.quizImportService = quizImportService;
    }

    @PostMapping("/chemistry")
    public ResponseEntity<String> uploadChemistry(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required and cannot be empty");
        }

        if (!MediaType.APPLICATION_JSON_VALUE.equals(file.getContentType())) {
            return ResponseEntity.badRequest().body("Only JSON files are allowed");
        }

        try {
            Optional<QuizData> quizData = quizImportService.importQuizFile(file.getBytes());
            quizImportService.persist(quizData);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to upload file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

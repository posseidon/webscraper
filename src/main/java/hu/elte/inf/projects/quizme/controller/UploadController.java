package hu.elte.inf.projects.quizme.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import hu.elte.inf.projects.quizme.repository.dto.QuizData;
import hu.elte.inf.projects.quizme.service.json.QuizImportService;

@RestController
@RequestMapping("upload")
public class UploadController {

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

    @PostMapping("/chemistry/multi")
    public ResponseEntity<String> uploadMultipleChemistry(@RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body("At least one file is required and cannot be empty");
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("One of the files is empty");
            }
            if (!MediaType.APPLICATION_JSON_VALUE.equals(file.getContentType())) {
                return ResponseEntity.badRequest().body("Only JSON files are allowed");
            }
            try {
                Optional<QuizData> quizData = quizImportService.importQuizFile(file.getBytes());
                quizImportService.persist(quizData);
            } catch (IOException e) {
                return new ResponseEntity<>("Failed to upload file: " + e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }



}

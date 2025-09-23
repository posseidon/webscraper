package hu.elte.inf.projects.quizme.controller.dto;

import java.net.MalformedURLException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import hu.elte.inf.projects.quizme.service.QuizService;

@RestController
@RequestMapping("/dto")
public class DtoController {

    private final QuizService quizService;

    public DtoController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PutMapping("/titles/audio-video-overview")
    @ResponseBody
    public ResponseEntity<String> updateTitleAudioVideoOverview(
            @RequestBody List<TitleAudioOverviewUpdateRequest> updates) {
        for (TitleAudioOverviewUpdateRequest update : updates) {
            try {
                quizService.updateTitleAudioVideoOverview(update.getTitleName(), update.getAudioOverview(),
                        update.getVideoOverview());
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            } catch (MalformedURLException e) {
                return new ResponseEntity<>(
                        "Invalid URL for title " + update.getTitleName() + ": " + update.getAudioOverview() + ", "
                                + update.getVideoOverview(),
                        HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("Title audio and video overviews updated successfully!", HttpStatus.OK);
    }

    @DeleteMapping("/questions")
    public ResponseEntity<Void> deleteAllQuestions() {
        quizService.deleteAllQuestions();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/topics")
    public ResponseEntity<Void> deleteAllTopics() {
        quizService.deleteAllTopics();
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @DeleteMapping("/titles")
    public ResponseEntity<Void> deleteAllTitles() {
        quizService.deleteAllTitles();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/title/{titleName}")
    public ResponseEntity<Void> deleteTitleByName(@PathVariable String titleName) {
        boolean deleted = quizService.deleteTitleAndRelatedData(titleName);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/subcategories")
    public ResponseEntity<Void> deleteAllSubCategories() {
        quizService.deleteAllSubCategories();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/categories")
    public ResponseEntity<Void> deleteAllCategories() {
        quizService.deleteAllCategories();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/alias")
    public ResponseEntity<String> setAlias(@RequestBody List<AliasItem> translationItems) {
        quizService.processAndStoreAlias(translationItems);
        String responseMessage = "Successfully processed " + translationItems.size() + " translation items.";
        return ResponseEntity.ok(responseMessage);
    }
}

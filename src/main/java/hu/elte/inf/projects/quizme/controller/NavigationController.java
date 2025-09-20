package hu.elte.inf.projects.quizme.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import hu.elte.inf.projects.quizme.controller.dto.nav.NavCategory;
import hu.elte.inf.projects.quizme.controller.dto.nav.NavSubCategory;
import hu.elte.inf.projects.quizme.controller.dto.nav.NavTitle;
import hu.elte.inf.projects.quizme.service.QuizService;

@ControllerAdvice
public class NavigationController {

    @Autowired
    private QuizService quizService;

    @ModelAttribute("navigation")
    public List<NavCategory> getNavigation() {
        return quizService.findAllDistinctCategories().stream()
                .map(category -> {
                    List<NavSubCategory> subCategories = quizService.findDistinctSubCategories(category.getName()).stream()
                            .map(subCategory -> {
                                List<NavTitle> titles = quizService.findTitlesByCategoryAndSubCategory(category.getName(), subCategory.getName()).stream()
                                        .map(title -> new NavTitle(title.getName(), "/quiz/" + category.getName() + "/" + subCategory.getName() + "/" + title.getName()))
                                        .collect(Collectors.toList());
                                return new NavSubCategory(subCategory.getName(), "/quiz/" + category.getName() + "/" + subCategory.getName(), titles);
                            })
                            .collect(Collectors.toList());
                    return new NavCategory(category.getName(), "/quiz/" + category.getName(), subCategories);
                })
                .collect(Collectors.toList());
    }
}

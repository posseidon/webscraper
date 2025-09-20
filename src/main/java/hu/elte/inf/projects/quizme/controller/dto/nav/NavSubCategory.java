package hu.elte.inf.projects.quizme.controller.dto.nav;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NavSubCategory {
    private String name;
    private String url;
    private List<NavTitle> titles;
}

package hu.elte.inf.projects.quizme.controller.dto.nav;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NavTitle {
    private String name;
    private String url;
}

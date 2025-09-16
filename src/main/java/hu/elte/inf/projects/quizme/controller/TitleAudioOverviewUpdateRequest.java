package hu.elte.inf.projects.quizme.controller;

public class TitleAudioOverviewUpdateRequest {
    private String titleName;
    private String audioOverview;

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public String getAudioOverview() {
        return audioOverview;
    }

    public void setAudioOverview(String audioOverview) {
        this.audioOverview = audioOverview;
    }
}

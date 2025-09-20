package hu.elte.inf.projects.quizme.controller.dto;

public class TitleAudioOverviewUpdateRequest {
    private String titleName;
    private String audioOverview;
    private String videoOverview;

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

    public String getVideoOverview() {
        return videoOverview;
    }

    public void setVideoOverview(String videoOverview) {
        this.videoOverview = videoOverview;
    }
}

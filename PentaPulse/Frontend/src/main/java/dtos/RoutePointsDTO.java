package dtos;

import java.util.List;

public class RoutePointsDTO {

    private String email;
    private String nume_traseu;
    private String start_loc;
    private String finish_loc;
    private List<CoordinatesDTO> coordinatesDTOList;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNume_traseu() {
        return nume_traseu;
    }

    public void setNume_traseu(String nume_traseu) {
        this.nume_traseu = nume_traseu;
    }

    public String getStart_loc() {
        return start_loc;
    }

    public void setStart_loc(String start_loc) {
        this.start_loc = start_loc;
    }

    public String getFinish_loc() {
        return finish_loc;
    }

    public void setFinish_loc(String finish_loc) {
        this.finish_loc = finish_loc;
    }

    public List<CoordinatesDTO> getCoordinatesDTOList() {
        return coordinatesDTOList;
    }

    public void setCoordinatesDTOList(List<CoordinatesDTO> coordinatesDTOList) {
        this.coordinatesDTOList = coordinatesDTOList;
    }
}

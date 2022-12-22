package com.devstree.project.model;

/**
 * Created by Jitendra on 23,November,2022
 */
public class Place{
    private Integer id;
    private String placeName;
    private String cityName;
    private Double latitude;
    private Double longitude;
    private Double distnace;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getDistnace() {
        return distnace;
    }

    public void setDistnace(Double distnace) {
        this.distnace = distnace;
    }
}

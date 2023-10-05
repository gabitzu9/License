package dtos;

import android.location.Address;

import com.google.android.gms.maps.model.LatLng;

public class LocationDTO {

    private LatLng destinationLatLng;
    private Address address;


    public LocationDTO() {
    }

    public LocationDTO(LatLng destinationLatLng, Address address) {
        this.destinationLatLng = destinationLatLng;
        this.address = address;
    }

    public LatLng getDestinationLatLng() {
        return destinationLatLng;
    }

    public void setDestinationLatLng(LatLng destinationLatLng) {
        this.destinationLatLng = destinationLatLng;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}

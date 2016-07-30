package mynim.is.where.oh.com.whereismynim;

import com.google.android.gms.maps.model.LatLng;

public class LocationInfo {

    public LocationInfo(LatLng loc, String date){
        this.loc = loc;
        this.date = date;
    }

    public LatLng loc;
    public String date;
}

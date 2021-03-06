package de.neuefische.aeropath.service;

import de.neuefische.aeropath.API.GooglePlaceAPI;
import de.neuefische.aeropath.db.WaypointMongoDb;
import de.neuefische.aeropath.model.Waypoint;
import de.neuefische.aeropath.utils.IdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class WaypointService {
    private final WaypointMongoDb waypointDb;
    private final IdUtils idUtils;
    private final GooglePlaceAPI googlePlaceAPI;

    @Autowired
    public WaypointService(WaypointMongoDb waypointDb, IdUtils idUtils, GooglePlaceAPI googlePlaceAPI) {
        this.waypointDb = waypointDb;
        this.idUtils = idUtils;
        this.googlePlaceAPI = googlePlaceAPI;
    }

    public List<Waypoint> getAll(String user) {
        return waypointDb.findByUser(user);
    }

    public void deleteAllWaypoints(String user) {
        List<Waypoint> waypointsToDelete = waypointDb.findByUser(user);
        waypointDb.deleteAll(waypointsToDelete);
    }

    public Waypoint add(double latitude, double longitude, String description, String placeId, String user) {
        Waypoint waypoint = new Waypoint();
        waypoint.setId(idUtils.generateRandomId());
        waypoint.setLatitude(latitude);
        waypoint.setLongitude(longitude);
        waypoint.setDescription(description);
        waypoint.setPlaceId(placeId);
        if (placeId != null) {
            String imageUrl = googlePlaceAPI.getImageUrl(placeId);
            waypoint.setImageUrl(imageUrl);
            String waypointName = googlePlaceAPI.getNameUrl(placeId);
            waypoint.setWaypointName(waypointName);
            String waypointAddress = googlePlaceAPI.getAddressUrl(placeId);
            waypoint.setWaypointAddress(waypointAddress);
        }
        waypoint.setUser(user);
        return waypointDb.save(waypoint);
    }

    public Waypoint getWaypointById(String id) {
        Optional<Waypoint> optionalWaypoint = waypointDb.findById(id);
        if (optionalWaypoint.isPresent()) {
            return optionalWaypoint.get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Waypoint not found");
        }
    }

    public Waypoint updateWaypointDescription(String id, String description) {
        Waypoint waypoint = getWaypointById(id);
        waypoint.setDescription(description);
        return waypointDb.save(waypoint);
    }

    public void deleteWaypoint(String id) {
        waypointDb.deleteById(id);
    }

    public Optional<Waypoint> getWaypoint(String id) {
        return waypointDb.findById(id);
    }

}

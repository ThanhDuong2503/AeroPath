package de.neuefische.aeropath.service;

import de.neuefische.aeropath.db.WaypointMongoDb;
import de.neuefische.aeropath.model.Waypoint;
import de.neuefische.aeropath.utils.IdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WaypointService {
    private final WaypointMongoDb waypointDb;
    private final IdUtils idUtils;

    @Autowired
    public WaypointService(WaypointMongoDb waypointDb, IdUtils idUtils) {
        this.waypointDb = waypointDb;
        this.idUtils = idUtils;
    }

    public List<Waypoint> getAll(String user) {
        return waypointDb.findByUser(user);
    }

    public Waypoint add(double latitude, double longitude, String description, String placeId, String user) {
        Waypoint waypoint = new Waypoint();
        waypoint.setId(idUtils.generateRandomId());
        waypoint.setLatitude(latitude);
        waypoint.setLongitude(longitude);
        waypoint.setDescription(description);
        waypoint.setPlaceId(placeId);
        waypoint.setUser(user);
        return waypointDb.save(waypoint);
    }

    public void deleteWaypoint(String id) {
        waypointDb.deleteById(id);
    }

    public Optional<Waypoint> getWaypoint(String id) {
        return waypointDb.findById(id);
    }

}

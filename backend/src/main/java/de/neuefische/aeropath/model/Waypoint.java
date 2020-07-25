package de.neuefische.aeropath.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Waypoint {
    @Id
    private String id;
//    private String description;
    private String user;
    private String latitude;
    private String longitude;
    private String placeId;
}

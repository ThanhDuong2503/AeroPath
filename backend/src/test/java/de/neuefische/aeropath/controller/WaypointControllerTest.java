package de.neuefische.aeropath.controller;
import de.neuefische.aeropath.db.UserDb;
import de.neuefische.aeropath.db.WaypointMongoDb;
import de.neuefische.aeropath.model.*;
import de.neuefische.aeropath.utils.IdUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WaypointControllerTest {

    @LocalServerPort
    public int port;

    @Autowired
    public TestRestTemplate restTemplate;

    @Autowired
    private WaypointMongoDb db;

    @Autowired
    public PasswordEncoder encoder;

    @Autowired
    public UserDb userDb;

    @MockBean
    private IdUtils idUtils;

    @BeforeEach
    public void resetDatabase() {
        db.deleteAll();
        userDb.deleteAll();
    }

    private String loginUser() {
        String savePassword = "savePassword";
        FlightUser user = new FlightUser("newTestUser", encoder.encode(savePassword), "user name", "", "admin", "", null, UserSource.CUSTOM, "");
        userDb.save(user);

        String loginUrl = "http://localhost:" + port + "/auth/login";
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(loginUrl, new LoginData("newTestUser", "savePassword"), String.class);
        return tokenResponse.getBody();
    }

    @Test
    public void getWaypointsShouldReturnAllWaypointsOfActiveUser() {
        //GIVEN
        String token = loginUser();

        String url = "http://localhost:" + port + "/api/map";
        db.save(new Waypoint("1", 44.4, 44.4, "", "", "newTestUser", "", "","" ));
        db.save(new Waypoint("2", 45.4, 44.4, "", "", "newTestUser","","",""));
        db.save(new Waypoint("3", 46.4, 44.4, "", "", "testuser","","",""));


        //WHEN
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<Waypoint[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Waypoint[].class);

        //THEN
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        Waypoint[] waypoints = response.getBody();
        assertEquals(waypoints.length, 2);
        assertEquals(waypoints[0], new Waypoint("1", 44.4, 44.4, "", "", "newTestUser","","",""));
        assertEquals(waypoints[1], new Waypoint("2", 45.4, 44.4, "", "", "newTestUser","","",""));
    }

    @Test
    @DisplayName("delete all waypoints should delete all waypoints of the active user")
    public void deleteAllWaypoints(){
        //GIVEN
        String token = loginUser();
        db.save(new Waypoint("1", 44.4, 44.4, "", "", "newTestUser","","",""));
        db.save(new Waypoint("2", 44.4, 44.4, "", "", "newTestUser","","",""));
        db.save(new Waypoint("3", 44.4, 44.4, "", "", "testUser2","","",""));
        db.save(new Waypoint("4", 44.4, 44.4, "", "", "testUser3","","",""));

        //WHEN
        String url = "http://localhost:" + port + "/api/map";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity entity = new HttpEntity(headers);
        restTemplate.exchange(url,HttpMethod.DELETE,entity,Void.class);
        ResponseEntity<Waypoint[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Waypoint[].class);

        //THEN
        assertTrue(db.findById("1").isEmpty());
        assertTrue(db.findById("2").isEmpty());
        assertTrue(db.findById("3").isPresent());
        assertTrue(db.findById("4").isPresent());

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        Waypoint[] waypoints = response.getBody();
        assertEquals(waypoints.length, 0);
    }

    @Test
    public void addWaypointShouldAddWaypoint() {
        // GIVEN
        String token = loginUser();

        when(idUtils.generateRandomId()).thenReturn("some-random-id");

        WaypointDto waypointDto = new WaypointDto(34.34425, 42.43225,"12345678", null);
        String url = "http://localhost:" + port + "/api/map";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<WaypointDto> requestEntity = new HttpEntity<>(waypointDto, headers);

        // WHEN
        ResponseEntity<Waypoint> putResponse = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Waypoint.class);

        // THEN
        Waypoint expectedWaypoint = new Waypoint("some-random-id", 34.34425, 42.43225,"12345678", null, "newTestUser",null,null,null);
        assertEquals(HttpStatus.OK, putResponse.getStatusCode());
        assertNotNull(putResponse.getBody());
        assertEquals(expectedWaypoint, putResponse.getBody());

        Optional<Waypoint> byId = db.findById("some-random-id");
        assertTrue(byId.isPresent());
        assertEquals(byId.get(), expectedWaypoint);
    }

    @Test
    @DisplayName("add description should add description to waypoint")
    public void addDescription(){
        //GIVEN
        String token = loginUser();
        db.save(new Waypoint("1", 44.4, 44.4, "", "", "newTestUser","","","" ));
        UpdateWaypointDescriptionDto updateWaypointDescriptionDto = new UpdateWaypointDescriptionDto( "someDescription");
        String url = "http://localhost:" + port + "/api/map/1/description";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<UpdateWaypointDescriptionDto> requestEntity = new HttpEntity<>(updateWaypointDescriptionDto,headers);

        //WHEN
        ResponseEntity<Waypoint> putResponse = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Waypoint.class);

        //THEN
        assertEquals(putResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(putResponse.getBody(), new Waypoint("1", 44.4, 44.4, "someDescription", "", "newTestUser","","",""));
    }

    @Test
    @DisplayName("update description should replace existing description")
    public void updateDescription(){
        //GIVEN
        String token = loginUser();
        db.save(new Waypoint("1", 44.4, 44.4, "old Description", "", "newTestUser","","","" ));
        UpdateWaypointDescriptionDto updateWaypointDescriptionDto = new UpdateWaypointDescriptionDto("new Description");
        String url = "http://localhost:" + port + "/api/map/1/description";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<UpdateWaypointDescriptionDto> requestEntity = new HttpEntity<>(updateWaypointDescriptionDto,headers);

        //WHEN
        ResponseEntity<Waypoint> putResponse = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Waypoint.class);

        //THEN
        assertEquals(putResponse.getStatusCode(), HttpStatus.OK);
        assertEquals(putResponse.getBody(), new Waypoint("1", 44.4, 44.4, "new Description", "", "newTestUser","","",""));
    }

    @Test
    @DisplayName("delete by id should delete waypoint with this id")
    public void deleteWaypoint(){
        //GIVEN
        String token = loginUser();
        db.save(new Waypoint("1", 44.4, 44.4, "", "", "newTestUser","","",""));
        db.save(new Waypoint("2", 44.4, 44.4, "", "", "newTestUser","","",""));

        //WHEN
        String url = "http://localhost:" + port + "/api/map/2";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity entity = new HttpEntity(headers);
        restTemplate.exchange(url,HttpMethod.DELETE,entity,Void.class);

        //THEN
        assertTrue(db.findById("2").isEmpty());
    }


    @Test
    @DisplayName("get by id should return waypoint with this id")
    public void getWaypointById(){
        //GIVEN
        String token = loginUser();
        db.save(new Waypoint("1", 44.4, 44.4, "", "", "newTestUser","","",""));
        db.save(new Waypoint("2", 44.4, 44.4, "", "", "newTestUser","","",""));

        //WHEN
        String url = "http://localhost:" + port + "/api/map/2";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<Waypoint> response = restTemplate.exchange(url, HttpMethod.GET, entity, Waypoint.class);

        //THEN
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody(), new Waypoint("2", 44.4, 44.4, "", "", "newTestUser","","",""));
    }

    @Test
    @DisplayName("when id does not exist get waypoint by id should return status not found")
    public void getWaypointByIdNotfound(){
        //GIVEN
        String token = loginUser();
        db.save(new Waypoint("1", 44.4, 44.4, "", "", "newTestUser","","",""));
        //WHEN
        String url = "http://localhost:" + port + "/api/map/2";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<Waypoint> response = restTemplate.exchange(url, HttpMethod.GET, entity, Waypoint.class);

        //THEN
        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
    }

}

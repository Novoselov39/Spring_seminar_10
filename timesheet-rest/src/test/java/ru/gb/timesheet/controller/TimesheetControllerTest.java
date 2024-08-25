package ru.gb.timesheet.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import ru.gb.timesheet.model.Project;
import ru.gb.timesheet.model.Timesheet;
import ru.gb.timesheet.repository.ProjectRepository;
import ru.gb.timesheet.repository.TimesheetRepository;
import ru.gb.timesheet.service.TimesheetService;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;


import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TimesheetControllerTest {


    @Autowired
    TimesheetRepository timesheetRepository;


    @LocalServerPort
    private int port;
    private RestClient restClient;

    @BeforeEach
    void beforeEach() {
        restClient = RestClient.create("http://localhost:" + port);
    }

    @Test
    void getById() {

        Timesheet timesheet = new Timesheet();
        timesheet.setCreatedAt(LocalDate.now());
        timesheet.setMinutes(2);
        timesheet.setProjectId(3L);
        timesheetRepository.save(timesheet);

        ResponseEntity<Timesheet> actual = restClient.get()
                .uri("/timesheets/"+timesheet.getId())
                .retrieve()
                .toEntity(Timesheet.class);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        Timesheet responseBody = actual.getBody();
        assertNotNull(responseBody);
        assertEquals(timesheet.getId(), responseBody.getId());
        assertEquals(timesheet.getProjectId(), responseBody.getProjectId());
        assertEquals(timesheet.getMinutes(), responseBody.getMinutes());
        assertEquals(timesheet.getCreatedAt(), responseBody.getCreatedAt());
    }

    @Test
    void getByAll() {
        Timesheet timesheet = new Timesheet();
        timesheet.setCreatedAt(LocalDate.now());
        timesheet.setMinutes(2);
        timesheet.setProjectId(3L);
        timesheetRepository.save(timesheet);


        Timesheet timesheet1 = new Timesheet();
        timesheet1.setCreatedAt(LocalDate.now());
        timesheet1.setMinutes(3);
        timesheet1.setProjectId(4L);
        timesheetRepository.save(timesheet1);



        ResponseEntity<List<Timesheet>> actual = restClient.get()
                .uri("/timesheets")
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<Timesheet>>() {
                });

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        Timesheet responseBody = Objects.requireNonNull(actual.getBody()).getFirst();

        assertNotNull(responseBody);
        assertEquals(timesheet.getId(), responseBody.getId());

    }
    @Test
    void testCreate() {

        Timesheet toCreate = new Timesheet();
        toCreate.setId(1L);
        toCreate.setMinutes(3);
        toCreate.setProjectId(4L);



        ResponseEntity<Timesheet> response = restClient.post()
                .uri("/timesheets")
                .body(toCreate)
                .retrieve()
                .toEntity(Timesheet.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Timesheet responseBody = response.getBody();
        assertNotNull(responseBody);
        assertNotNull(responseBody.getId());
        assertEquals(responseBody.getMinutes(), toCreate.getMinutes());
        assertTrue(timesheetRepository.existsById(responseBody.getId()));
    }

    @Test
    void testDeleteById() {
        Timesheet toDelete = new Timesheet();
        toDelete.setCreatedAt(LocalDate.now());
        toDelete.setMinutes(2);
        toDelete.setProjectId(3L);
        timesheetRepository.save(toDelete);

        ResponseEntity<Void> response = restClient.delete()
                .uri("/timesheets/" + toDelete.getId())
                .retrieve()
                .toBodilessEntity(); // less
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Проверяем, что запись в БД НЕТ
        assertFalse(timesheetRepository.existsById(toDelete.getId()));
    }

    @BeforeEach
    void deleteAll(){
        timesheetRepository.deleteAll();
    }
}
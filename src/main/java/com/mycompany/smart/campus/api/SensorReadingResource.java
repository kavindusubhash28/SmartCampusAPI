package com.mycompany.smart.campus.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    @GET
    public Response getReadings(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Sensor not found")
                    .build();
        }

        List<SensorReading> readings = DataStore.sensorReadings.get(sensorId);
        if (readings == null) {
            readings = new ArrayList<>();
        }

        return Response.ok(readings).build();
    }

    @POST
    public Response addReading(@PathParam("sensorId") String sensorId, SensorReading reading) {
        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Sensor not found")
                    .build();
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Sensor is in maintenance and cannot accept readings")
                    .build();
        }

        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Reading body is required")
                    .build();
        }

        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }

        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        DataStore.sensorReadings.putIfAbsent(sensorId, new ArrayList<>());
        DataStore.sensorReadings.get(sensorId).add(reading);

        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED)
                .entity(reading)
                .build();
    }
}
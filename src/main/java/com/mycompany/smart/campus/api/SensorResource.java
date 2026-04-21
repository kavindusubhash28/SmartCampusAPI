package com.mycompany.smart.campus.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        Collection<Sensor> allSensors = DataStore.sensors.values();

        if (type == null || type.isBlank()) {
            return Response.ok(allSensors).build();
        }

        List<Sensor> filteredSensors = new ArrayList<>();
        for (Sensor sensor : allSensors) {
            if (sensor.getType() != null && sensor.getType().equalsIgnoreCase(type)) {
                filteredSensors.add(sensor);
            }
        }

        return Response.ok(filteredSensors).build();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Sensor id is required")
                    .build();
        }

        if (DataStore.sensors.containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Sensor with this id already exists")
                    .build();
        }

        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("roomId is required")
                    .build();
        }

        Room room = DataStore.rooms.get(sensor.getRoomId());
        if (room == null) {
            return Response.status(422)
                    .entity("Referenced room does not exist")
                    .build();
        }

        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        DataStore.sensors.put(sensor.getId(), sensor);

        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }
        room.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED)
                .entity(sensor)
                .build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource() {
        return new SensorReadingResource();
    }
}
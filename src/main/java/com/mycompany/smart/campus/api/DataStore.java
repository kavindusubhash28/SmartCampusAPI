package com.mycompany.smart.campus.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {
    public static Map<String, Room> rooms = new HashMap<>();
    public static Map<String, Sensor> sensors = new HashMap<>();
    public static Map<String, List<SensorReading>> sensorReadings = new HashMap<>();
}
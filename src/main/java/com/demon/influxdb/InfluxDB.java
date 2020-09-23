package com.demon.influxdb;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="spring.datasource.influxdbtest")
public class InfluxDB extends InfluxDBConnect {

}

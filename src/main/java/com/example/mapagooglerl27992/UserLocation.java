package com.example.mapagooglerl27992;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class UserLocation {
    public String userID;
    public double Longitude;
    public double Latitude;

    public UserLocation(String userID, double Longitude, double Latitude) {
        this.userID = userID;
        this.Longitude = Longitude;
        this.Latitude = Latitude;
    }

    private static final String TASK_QUEUE_NAME = "StudentsQueue";
    private final static String EXCHANGE_NAME = "Students";
    public void send() throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri("amqps://neqywkiw:6dSviZEenuOPq3gfEckC5iypHgT4Y5tN@hawk.rmq.cloudamqp.com/neqywkiw");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);

            String message = "test";

            channel.basicPublish(EXCHANGE_NAME, "location", null, message.getBytes("UTF-8"));
//            channel.basicPublish("", TASK_QUEUE_NAME,
//                    MessageProperties.PERSISTENT_TEXT_PLAIN,
//                    message.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}


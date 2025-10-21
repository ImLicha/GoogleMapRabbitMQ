package com.example.mapagooglerl27992;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.tools.json.JSONWriter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class TestRabbit {
    private final static String myID = "27992";
    private static final String TASK_QUEUE_NAME = "StudentsQueue";
    private final static String EXCHANGE_NAME = "Students";
    public void send(LatLng currentLocation) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri("amqps://neqywkiw:6dSviZEenuOPq3gfEckC5iypHgT4Y5tN@hawk.rmq.cloudamqp.com/neqywkiw");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);

            double latitude = currentLocation.latitude;
            double longitude = currentLocation.longitude;

            Gson gson = new Gson();
            Map<String, String> stringMap = new LinkedHashMap<>();
            stringMap.put("UserID" , myID);
            stringMap.put("Latitude" , String.valueOf(latitude));
            stringMap.put("Longitude" , String.valueOf(longitude));

            String json = gson.toJson(stringMap);

            channel.basicPublish(EXCHANGE_NAME, "location", null, json.getBytes());
//            channel.basicPublish("", TASK_QUEUE_NAME,
//                    MessageProperties.PERSISTENT_TEXT_PLAIN,
//                    message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + json + "'");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public void receive() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri("amqps://neqywkiw:6dSviZEenuOPq3gfEckC5iypHgT4Y5tN@hawk.rmq.cloudamqp.com/neqywkiw");
        final Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        channel.basicQos(1);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            TypeToken<Map<String, String>> mapType = new TypeToken<Map<String, String>>(){};
            String message = new String(delivery.getBody(), "UTF-8");
            Gson gson = new Gson();
            Map<String, String> stringMap = gson.fromJson(message, mapType);
            System.out.println(stringMap.keySet());
            System.out.println(stringMap.values());
            Log.d(" [x] Received ", stringMap.toString());
            try {
                //doWork(stringMap.toString());
            } finally {
                System.out.println(" [x] Done");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> { });
    }

//    private static void doWork(String task) {
//        for (char ch : task.toCharArray()) {
//            if (ch == '.') {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException _ignored) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//        }
//    }

}
package com.example.mapagooglerl27992;

import android.util.Log;

import com.rabbitmq.client.*;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class RabbitMQ {
    private final static String EXCHANGE_NAME = "TestExchange";
    private final static String QUEUE_NAME = "TestQueue";
    private final static String URL = "amqps://qhhfzlxv:Exr8vOYICmPwmOErWFMjaLxDJCpXlMBT@hawk.rmq.cloudamqp.com/qhhfzlxv";
    private final static String HOSTNAME = "hawk.rmq.cloudamqp.com";
    private final static String USERNAME = "qhhfzlxv";
    private final static String PASSWORD = "Exr8vOYICmPwmOErWFMjaLxDJCpXlMBT";

    Connection connection;
    ConnectionFactory factory = new ConnectionFactory();
    public BlockingDeque queue = new LinkedBlockingDeque();
    public void publishMessage(String message) {
        try {
            queue.putLast(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void setupConnectionFactory() {
        String uri = URL;
        try {
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(uri);
        } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e1) {
            e1.printStackTrace();
        }
    }

    public void publishToAMQP() {
        Thread publishThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel ch = connection.createChannel();
                        ch.confirmSelect();

                        while (true) {
                            String message = (String) queue.takeFirst();
                            try {
                                ch.exchangeDeclare(EXCHANGE_NAME, "direct");
                                ch.basicPublish(EXCHANGE_NAME, "test", null, message.getBytes());
                                ch.waitForConfirmsOrDie();
                            } catch (Exception e) {
                                queue.putFirst(message);
                                throw e;
                            }
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        Log.d("", "Connection broken: " + e.getClass().getName());
                        try {
                            Thread.sleep(5000); //sleep and then try again
                        } catch (InterruptedException e1) {
                            break;
                        }
                    }
                }
            }
        });
        publishThread.start();
    }
    public void sendMessage(final String message) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    JSONObject jsonMessage = new JSONObject();
//                    jsonMessage.put("message", message);
//
//                    // Konwertujemy obiekt JSON na String
//                    String jsonString = jsonMessage.toString();

                    Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel();
                    channel.basicQos(1);
                    channel.confirmSelect();


                    AMQP.Queue.DeclareOk q = channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                    AMQP.Exchange.DeclareOk e = channel.exchangeDeclare(EXCHANGE_NAME, "direct");
                    channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "test");
                    channel.basicPublish(EXCHANGE_NAME, "test", null ,message.getBytes("UTF-8"));
                    channel.waitForConfirmsOrDie();
                    System.out.println(" [x] Sent '" + message + "'");

                    channel.close();
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void receiveMessage() {
        try {

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
                // Tutaj możesz dodać logikę do przetwarzania otrzymanej wiadomości
            };

            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

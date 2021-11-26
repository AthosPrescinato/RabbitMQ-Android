package com.athosprescinato.senderrabbitmqclass;

import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;


public class RabbitMQConnection {

    private static String QUEUE_NAME = "";
    private final static String EXCHANGE_NAME = ""; // Put Exchange name here | Insira o Exchange Name aqui
    private final static String USER_NAME = ""; // Put Username here | Insira o nome de usuario
    private final static String USER_PASSWORD = ""; // Put Password here | Insira a senha aqui
    private final static String HOST_URL = ""; // Put Host URL here | Insira a URL aqui
    private final static int PORT = 5671; // This port is default || Essa é a porta padrão.


    private Connection connection = null;
    private static RabbitMQConnection instance = null;
    private Channel channel;
    ActionListenerCallback  callBack = null;

    public interface ActionListenerCallback  {

        public void onMessage(String successMessage, Object consumerTag);

    }


    public RabbitMQConnection(String queueName) {
        try {
            connection = getConnection(queueName);
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            Log.i("[x] ", "Create Connection and Channel"); // Search in "logcat"
            receive();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static RabbitMQConnection getInstance(String LockerID){
        if( instance == null ){
            instance = new RabbitMQConnection(LockerID);
        }
        return instance;
    }

    private static Connection getConnection(String queueName) throws Exception {
//        if (connection == null) {

            QUEUE_NAME = queueName;

            ConnectionFactory factory = new ConnectionFactory();

            factory.setUsername(USER_NAME);
            factory.setPassword(USER_PASSWORD);

            //Informações da Conexão || Conection Information
            factory.setHost(HOST_URL);
            factory.setPort(PORT);

            //Config Reconectar Automaticamente || Set Recovery Automaticc
            factory.setAutomaticRecoveryEnabled(true);

            // Permite ao cliente conexão utilizando o TLS
            factory.useSslProtocol();

            try {

            // Conectando || Connecting
            Connection connection = factory.newConnection();

            return connection; }

            catch (java.net.ConnectException e) {
                Thread.sleep(5000);
                System.out.println(" [x] Reconnecting... ");
                //getConnection();
            }
//        }
//        return connection;
        return null;
    }

    public void setActionListener(ActionListenerCallback  callBack) {
        this.callBack = callBack;
        System.out.println("Performing some task, prior to invoking the callback");
    }

    public void send(String message) {
        try {

            channel.basicPublish(EXCHANGE_NAME, QUEUE_NAME, null, message.getBytes("UTF-8"));

            Log.i(" [x] Sent", message + "'");

            //channel.close();
            //connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void receive() {
        try {

            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, QUEUE_NAME);

            System.out.println(" [x] Waiting for messages. ");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
                if (this.callBack != null) {
                    callBack.onMessage(message, consumerTag);
                }
            };

            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {

            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
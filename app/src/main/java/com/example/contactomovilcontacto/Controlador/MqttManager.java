package com.example.contactomovilcontacto.Controlador;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttManager {
    private static final String TAG = "MqttManager";

    private MqttAndroidClient mqttAndroidClient;
    private String mqttBroker = "tcp://localhost:1883";
    private String clientId = MqttClient.generateClientId();

    private static MqttManager instance;

    private MqttManager(Context context) {
        mqttAndroidClient = new MqttAndroidClient(context, mqttBroker, clientId);
    }

    public static MqttManager getInstance(Context context) {
        if (instance == null) {
            instance = new MqttManager(context);
        }
        return instance;
    }

    public void connectMqtt() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);

        try {
            IMqttToken token = mqttAndroidClient.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Manejar la conexión exitosa
                    subscribeToTopic("topic/usuario1");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Manejar la conexión fallida
                    Log.e(TAG, "Error en la conexión MQTT: " + exception.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Excepción en connectMqtt: " + e.getMessage());
        }
    }

    private void subscribeToTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Manejar la suscripción exitosa
                    Log.d(TAG, "Suscrito a: " + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Manejar la suscripción fallida
                    Log.e(TAG, "Error en la suscripción MQTT: " + exception.getMessage());
                }
            });

            mqttAndroidClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    // Manejar la pérdida de conexión
                    Log.w(TAG, "Conexión MQTT perdida");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // Manejar la llegada de mensajes
                    String payload = new String(message.getPayload());
                    // Actualizar Firebase con el mensaje recibido
                    updateFirebase(payload);
                    // Imprimir mensaje en el CMD
                    updateCmd(payload);
                    Log.d(TAG, "Mensaje recibido en " + topic + ": " + payload);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Manejar la entrega completa del mensaje (opcional)
                    Log.d(TAG, "Entrega completa del mensaje");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Excepción en subscribeToTopic: " + e.getMessage());
        }
    }

    public void publishMessage(String topic, String message) {
        try {
            mqttAndroidClient.publish(topic, new MqttMessage(message.getBytes()));
            Log.d(TAG, "Mensaje publicado en " + topic + ": " + message);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error en la publicación del mensaje MQTT: " + e.getMessage());
        }
    }

    private void updateFirebase(String message) {
        // Lógica para actualizar Firebase con el mensaje recibido
        Log.d(TAG, "Actualizar Firebase con el mensaje: " + message);
    }

    private void updateCmd(String message) {
        // Lógica para imprimir mensajes en el CMD
        System.out.println("Mensaje recibido: " + message);
    }
}

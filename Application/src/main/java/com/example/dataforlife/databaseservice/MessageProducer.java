package com.example.dataforlife.databaseservice;

import android.util.Log;

import java.io.IOException;

/**
 * Created by kokoghlanian on 04/05/2018.
 */

public class MessageProducer extends IConnectToRabbitMQ {
    /**
     * @param server       The server address
     * @param exchange     The named exchange
     * @param exchangeType The exchange type name
     */
    public MessageProducer(String server, String exchange, String exchangeType) {
        super(server, exchange, exchangeType);
    }

    public void publishToRabbitMQ(String message){

        Thread thread = new Thread()
        {
            @Override
            public void run() {
                while(Running){

                    try {
                        connectToRabbitMQ();
                        mModel.basicPublish("", "influxData", null, message.getBytes());
                        Log.e("PUBLISH IN RABBIT","PUBLISH OK");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

        };
        thread.start();
    }

    public void dispose(){
        Running = false;
    }
}

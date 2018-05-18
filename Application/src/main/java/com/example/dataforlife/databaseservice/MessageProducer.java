package com.example.dataforlife.databaseservice;

import android.util.Log;

import com.example.dataforlife.model.CustomMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;

import java.io.ByteArrayOutputStream;
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

    public void publishToRabbitMQ(CustomMessage message){

        Thread thread = new Thread()
        {
            @Override
            public void run() {

                connectToRabbitMQ();
                while(Running){
                    try {

                         ObjectMapper mapper = new ObjectMapper();
                         ByteArrayOutputStream out = new ByteArrayOutputStream();
                         mapper.writeValue(out, message);
                         //Log.e("PUBLISH IN RABBIT","CreateMessage : " + out.toString());
                         mModel.basicPublish(mExchange, "influxData",
                                 new AMQP.BasicProperties.Builder()
                                         .contentType("application/json")
                                         .build(),out.toByteArray());
                         //Log.e("PUBLISH IN RABBIT","PUBLISH OK");
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

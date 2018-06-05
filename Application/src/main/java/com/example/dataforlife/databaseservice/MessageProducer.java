package com.example.dataforlife.databaseservice;

import android.util.Log;

import com.example.dataforlife.model.CustomMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AlreadyClosedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by kokoghlanian on 04/05/2018.
 */

public class MessageProducer extends IConnectToRabbitMQ {
    /**
     * @param server       The server address
     * @param exchange     The named exchange
     * @param exchangeType The exchange type name
     *
     *
     */

    public MessageProducer(String server, String exchange, String exchangeType) {
        super(server, exchange, exchangeType);
    }

    synchronized public void publishToRabbitMQ(CustomMessage messageProducerBuffer){

            Thread thread = new Thread()
            {
                @Override
                public void run() {

                        try {

                            if(connectToRabbitMQ()){

                                ObjectMapper mapper = new ObjectMapper();
                                ByteArrayOutputStream out = new ByteArrayOutputStream();
                                mapper.writeValue(out, messageProducerBuffer);
                                //Log.e("PUBLISH IN RABBIT","CreateMessage : " + out.toString());
                                mModel.basicPublish(mExchange, "influxData",
                                        new AMQP.BasicProperties.Builder()
                                                .contentType("application/json")
                                                .build(),out.toByteArray());
                            }

                        } catch (IOException | AlreadyClosedException e) {
                            e.printStackTrace();
                        }
                    }

            };
            thread.start();
        }

    }


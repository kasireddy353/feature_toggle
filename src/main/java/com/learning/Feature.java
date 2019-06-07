package com.learning;

import com.sun.org.apache.xpath.internal.operations.Bool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class Feature {

    private static final AtomicReference<Feature> reference = new AtomicReference<>();
    private static Feature instance;
    private static ConcurrentHashMap<String, String> features = new ConcurrentHashMap<>();

    private static JedisPool jedisPool;



    public static Feature create(final String redisHostName, final int redisPort){
        return create(new JedisPoolConfig(), redisHostName, redisPort);
    }

    public static synchronized Feature create(final JedisPoolConfig poolConfig, final String redisHostName, final int redisPort){

        instance = reference.get();

        if (instance == null) {
            instance = new Feature();
            jedisPool = new JedisPool(poolConfig, redisHostName, redisPort);
            subscribe();
            loadData();

            if (!reference.compareAndSet(null, instance)) {
                System.out.println("initial");
                //another thread has initialized the reference
                instance = reference.get();


            }
        }

        return instance;
    }

    private static void loadData() {
        try(Jedis connection = jedisPool.getResource()){

            Set<String> keys = connection.keys("myfeature*");
            keys.stream().forEach((key) -> {

                String value = connection.get(key);
                features.put(key, value);
            });

        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Exception while loading data is ---> " + e);
            //log error
        }
    }


    static JedisPubSub jedisPubSub = new JedisPubSub() {
        @Override
        public void onMessage(String channel, String message) {
            System.out.println("Message received is ----->" + message);

            String[] split = message.split(":");
            features.put(split[0], split[1]);
        }
    };


    private static void subscribe(){

        Runnable r = () -> {

            try(Jedis connection = jedisPool.getResource()){

                connection.subscribe(jedisPubSub, "test");
                System.out.println("Subscribed to test channel");

            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Exception is ---> " + e);
                //log error
            }
        };

        new Thread(r).start();

    }


    public boolean featureEnabled(final String featureName){

        String featureValue = features.get(featureName);

        if(featureName == null){
            return false;
        }

        return Boolean.valueOf(featureValue).booleanValue();
    }












}

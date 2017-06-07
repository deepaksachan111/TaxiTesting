package com.tns.taxitesting;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by TNS on 04-Apr-17.
 */

public class NMEAParse {

    // fucking java interfaces
    interface SentenceParser {
        public boolean parse(String[] tokens, GPSPosition position);
    }

    // utils
    static float Latitude2Decimal(String lat, String NS) {





            float med = 0;

            if (!lat.equals("")) {
                med = Float.parseFloat(lat.substring(2)) / 60.0f;
                med += Float.parseFloat(lat.substring(0, 2));
                if (NS.startsWith("S")) {
                    med = -med;
                }


            }
            return med;



    }

    static float Longitude2Decimal(String lon, String WE) {
        float med = 0;
        if(!lon.equals("")) {
             med = Float.parseFloat(lon.substring(3)) / 60.0f;
            med += Float.parseFloat(lon.substring(0, 3));
            if (WE.startsWith("W")) {
                med = -med;
            }

        }
        return med;
    }

    // parsers
    class GPGGA implements SentenceParser {
        public boolean parse(String [] tokens, GPSPosition position) {

            if(!tokens[1].equals("")) {
                position.time = Float.parseFloat(tokens[1]);
                position.lat = Latitude2Decimal(tokens[2], tokens[3]);
                position.lon = Longitude2Decimal(tokens[4], tokens[5]);
                position.quality = Integer.parseInt(tokens[6]);
                position.no_of_satelitte = Integer.parseInt(tokens[7]);
                return true;

            }
            return false;
        }
    }

    class GPRMC implements SentenceParser {
        public boolean parse(String [] tokens, GPSPosition position) {

            Log.d("token dir",tokens[8]);
            if(!tokens[1].equals("")) {
                position.time = Float.parseFloat(tokens[1]);
                position.lat = Latitude2Decimal(tokens[3], tokens[4]);
                position.lat_area = tokens[4];
                position.lon = Longitude2Decimal(tokens[5], tokens[6]);
                position.long_area = tokens[6];

                position.speed = Float.parseFloat(tokens[7]);
                position.dir = tokens[8];
                return true;

            }
            return false;
        }
    }




    public class GPSPosition {
        public float time = 0.0f;
        public float lat = 0.0f;
        public float lon = 0.0f;
        public boolean fixed = false;
        public int quality = 0;
        public String dir = "'";
        public int no_of_satelitte = 0;
        public float speed = 0.0f;
        public String lat_area;
        public String long_area;


        public void updatefix() {
            fixed = quality > 0;
        }

        public String toString() {
            return String.format("%f, %f, %f,  %d, %s:deepak,  %3s,  %f ,%3s ,%3s", lat, lon, time, quality, "deepak", no_of_satelitte, speed,lat_area,long_area);
        }
    }

    GPSPosition position = new GPSPosition();

    private static final Map<String, SentenceParser> sentenceParsers = new HashMap<String, SentenceParser>();

    public NMEAParse() {
        sentenceParsers.put("GPGGA", new GPGGA());
        sentenceParsers.put("GPRMC", new GPRMC());

        //only really good GPS devices have this sentence but ...

    }

    public GPSPosition parse(String line) {



        if(line.startsWith("$")) {
            String nmea = line.substring(1);
            String[] tokens = nmea.split(",");
            String type = tokens[0];
            //TODO check crc
            if(sentenceParsers.containsKey(type)) {
                sentenceParsers.get(type).parse(tokens, position);
            }
            position.updatefix();
        }



        return position;
    }
}
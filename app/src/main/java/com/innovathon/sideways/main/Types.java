package com.innovathon.sideways.main;

import com.innovathon.sideways.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ahmad on 7/17/2016.
 */
public class Types
{
    public enum locationTypes
    {

        item1("Roadside",   R.drawable.ic_roadside_main_48dp,     0, 3,  "roadside"),
        item2("Hazard",     R.drawable.ic_hazard_main_48dp,      4, 9,  "hazard"),
        item3("Place",      R.drawable.ic_place_main_48dp,    10, 14, "place"),
        item4("Food",       R.drawable.ic_food_main_48dp,   15, 16, "food"),
//        item5("Trail",     R.drawable.ic_2_5_trails,    15, 19, "trail"),
//        item6("Place",     R.drawable.ic_2_6_place,     20, 23, "places"),
//        item7("Transport", R.drawable.ic_2_7_transport, 24, 26, "transport"),
//        item8("Vendor",    R.drawable.ic_2_8_vendor,    27, 30, "vendor"),

//        special_item1("Meeting", android.R.drawable.sym_contact_card, 31,31, "meeting"),

        BEST_LAST_KNOWN(  "", R.drawable.ic_2_10_none,  0 , 0, "best last known"),
        CURR_GPS_LOCATION(  "", R.drawable.ic_2_10_none,  0 , 0, "current gps location"),
        FROM_OUR_DATABASE(  "", R.drawable.ic_2_10_none,  0 , 0, "from out database"),
        USER_ADDED_TMP(     "", R.drawable.ic_2_10_none,  0 , 0, "user added temp"),
        FROM_GOOGLE_SEARCH( "", R.drawable.ic_2_10_none,  0, 0 , "from google search");


        private String fullName;
        private int image;
        public int iSubtypeBeg = -1;
        public int iSubtypeEnd = -1;
        private String identifier;

        locationTypes(String fullName, int image, int ibeg, int iend, String identifier)
        {
            this.fullName = fullName;
            this.image = image;
            iSubtypeBeg = ibeg;
            iSubtypeEnd = iend;
            this.identifier = identifier;
        }

        locationTypes(String fullName, int image)
        {
            this.fullName = fullName;
            this.image = image;
        }

        //Reverse lookup hash map
        private static final Map<String, locationTypes> lookup = new HashMap<String, locationTypes>();

        static {
            for(locationTypes d : locationTypes.values())
                lookup.put(d.identifier(), d);
        }

        public static locationTypes get(String identifier) {
            return lookup.get(identifier);
        }


        public String fullName()
        {
            return fullName;
        }
        public int image() {return image;}
        public String identifier() {return identifier;}
    }

    /* Hazard */
    /*----------------------------------------------*/
    public enum subTypes
    {
        //0 - Roadside
        PLAZA(           "Travel Plaza", R.drawable.ic_roadside_travelplaza_48dp,       "travelplaza",      R.drawable.ic_roadside_marker_travelplaza_48dp, 3),
        PARKING(         "Parking",      R.drawable.ic_roadside_parking_48dp,            "parking",          R.drawable.ic_roadside_marker_parking_48dp, 3),
        CARWASH(         "Car Wash",      R.drawable.ic_roadside_carwash_48dp,            "carwash",          R.drawable.ic_roadside_marker_carwash_48dp, 3),
        GASSTATION(      "Gas Station",  R.drawable.ic_roadside_gasstation_48dp,         "gas station",      R.drawable.ic_roadside_marker_gasstation_48dp,3),
		
        //4 - Hazard
        ACCIDENT(       "Accident",		R.drawable.ic_hazard_accident_48dp,			"accident",		R.drawable.ic_hazard_marker_accident2_48dp,3),
        CONSTRUCTION(   "Construction", R.drawable.ic_hazard_construction_48dp,		"construction", R.drawable.ic_hazard_marker_construction_48dp, 3),
        CLOSURE(        "Path Closed",  R.drawable.ic_hazard_pathclosed_48dp,		"closed" ,		R.drawable.ic_hazard_marker_pathclosed_48dp,3),
        POTHOLE(        "Pothole",		R.drawable.ic_hazard_pothhole_48dp,			"pothole" ,		R.drawable.ic_hazard_marker_pothhole_48dp,3),
        FLOODING(       "Flooding",  	R.drawable.ic_hazard_flooding_48dp,			"flooding" ,	R.drawable.ic_hazard_marker_flooding_48dp,3),
        WASTE(       	"Waste",  		R.drawable.ic_hazard_waste_48dp,			"waste" ,		R.drawable.ic_hazard_marker_waste_48dp,3),
//      HOLE(           "Pothole",      R.drawable.ic_hazard_pothole,       "pothole",      R.drawable.ic_marker_hazard_pothole, 3),


        //7 - Places
        SITE(           "Historic Site",    R.drawable.ic_place_historic_48dp,	"historic", R.drawable.ic_place_marker_historic_48dp, 3),
        CAMPING(		"Camping",         	R.drawable.ic_place_camping_48dp,   "camping",  R.drawable.ic_place_marker_camping_48dp, 3),
        PARK(			"Park",         	R.drawable.ic_place_parks_48dp,   	"park",  	R.drawable.ic_place_marker_parks_48dp, 3),
        TRAIL(			"Trail",         	R.drawable.ic_place_trail_48dp,   	"trail",  	R.drawable.ic_place_marker_trail_48dp, 3),
        BATHROOM(       "Bathroom",         R.drawable.ic_place_bathroom_48dp,	"bathroom", R.drawable.ic_place_marker_bathroom_48dp, 3),

		
        //9 - FOOD
        FOODTRUCK(      "Food Truck",	R.drawable.ic_food_truck_48dp,		"truck",	R.drawable.ic_food_marker_truck_48dp, 3),
        ICECREAM(       "Ice Cream",    R.drawable.ic_food_icecream_48dp,	"icecream",	R.drawable.ic_food_marker_icecream_48dp, 3),



//        CHARGING(       "Charging Station", R.drawable.ic_place_charging,       "charging", R.drawable.ic_marker_place_charging, 3),
//        WIFI(           "Wifi",             R.drawable.ic_place_wifi,           "wifi",     R.drawable.ic_marker_place_wifi, 3),

//        FLOODING(       "Flooding",     R.drawable.ic_hazard_flooding,      "flooding",     R.drawable.ic_marker_hazard_flooding, 3),
//        WASTE(          "Waste",        R.drawable.ic_hazard_birdwaste,     "waste",        R.drawable.ic_marker_hazard_birdwaste, 3),
//        ANIMAL(         "Dead Animal",  R.drawable.ic_hazard_deadbird,      "animal",       R.drawable.ic_marker_hazard_deadbird, 3),
//        SPILL(          "Spill",        R.drawable.ic_hazard_spill,         "spill",        R.drawable.ic_marker_hazard_spill, 3),

        //7 - Crowd
//        MODERATE(       "Moderately Busy",  R.drawable.ic_crowd_busy1,      "mbusy",        R.drawable.ic_marker_crowd_busy1, 3),
//        VERY(           "Very Busy",        R.drawable.ic_crowd_busy2,      "vbusy",        R.drawable.ic_marker_crowd_busy2, 3),
//        PROTEST(        "Protest",          R.drawable.ic_crowd_protest,    "protest",      R.drawable.ic_marker_crowd_protest, 3),
//
//        //10 - Police
//        CAR(            "Police Car",   R.drawable.ic_police_copcar,        "car",          R.drawable.ic_marker_police_copcar, 3),
//        POLICE(         "Police",       R.drawable.ic_2_3_police,           "cop",          R.drawable.ic_marker_police_copstanding, 3),
//
//        //12 - Parking
//        HOURLY(         "Hourly",       R.drawable.ic_parking_hourly,       "hourly",       R.drawable.ic_marker_parking_hourly, 3),
//        PRIVATE(        "Paid Private", R.drawable.ic_parking_private,      "private",      R.drawable.ic_marker_parking_private, 3),
//        STREET(         "Street",       R.drawable.ic_parking_street,       "street",       R.drawable.ic_marker_parking_street, 3),
//
//        //15 - Trails
//        VIEW(           "Great View",   R.drawable.ic_trails_greatview,     "view",         R.drawable.ic_marker_trails_greatview, 3),
//        LAKE(           "Lake",         R.drawable.ic_trails_lake,          "lake",         R.drawable.ic_marker_trails_lake, 3),
//        HILLS(          "Hills",        R.drawable.ic_trails_hills,         "hills",        R.drawable.ic_marker_trails_hills, 3),
//        WATER(          "Flooding",     R.drawable.ic_trails_flooding,      "flooding",     R.drawable.ic_marker_trails_flooding, 3),
//        UNEVEN(         "Uneven Path",  R.drawable.ic_trails_uneven,        "uneven",       R.drawable.ic_marker_trails_uneven, 3),
//
//
//        //24 - Transport
//        BIKE(           "Bike Stand",   R.drawable.ic_transport_bike,       "bike",         R.drawable.ic_marker_transport_bike, 3),
//        BUS(            "Bus Stop",     R.drawable.ic_transport_bus,        "bus",          R.drawable.ic_marker_transport_bus, 3),
//        SUBWAY(         "Subway",       R.drawable.ic_transport_subway,     "subway",       R.drawable.ic_marker_transport_subway, 3),


        //31
        BEST_LAST_KNOWN(    "", R.drawable.ic_2_10_none, "best last known",      -1, -1),
        CURR_GPS_LOCATION(  "", R.drawable.ic_2_10_none, "current gps location", -1, -1),
        FROM_OUR_DATABASE(  "", R.drawable.ic_2_10_none, "from out database",    -1, -1),
        USER_ADDED_TMP(     "", R.drawable.ic_2_10_none, "user added temp",      -1, -1),
        FROM_GOOGLE_SEARCH( "", R.drawable.ic_2_10_none, "from google search",   -1, -1);
        private String fullName;
        private int image;
        private int marker_icon ;
        private String identifier;
        private int image_resolution_indicator;

        subTypes(String fullName, int image, String identifier, int markericon, int image_resolution_indicator)
        {
            this.fullName = fullName;
            this.image = image;
            this.identifier = identifier;
            this.marker_icon = markericon;
            this.image_resolution_indicator = image_resolution_indicator;
        }

        //Reverse lookup hash map
        private static final Map<String, subTypes> lookup = new HashMap<String, subTypes>();

        static
        {
            for(subTypes d : subTypes.values())
                lookup.put(d.identifier(), d);
        }

        public static subTypes get(String identifier) {
            return lookup.get(identifier);
        }

        public String fullName() {
            return fullName;
        }
        public int image() {return image;}
        public String identifier() {return identifier;}
        public int micon(){ return marker_icon;}
        public int image_res_ind(){ return this.image_resolution_indicator; };
    }

//    /* Crowd */
//    /*----------------------------------------------*/
//    public enum subTypesItem2
//    {
//
//        MODERATE("Moderately Busy", R.drawable.ic_crowd_busy1),
//        VERY("Very Busy", R.drawable.ic_crowd_busy2),
//        PROTEST("Protest", R.drawable.ic_crowd_protest);
//
//        private String fullName;
//        private int image;
//
//        subTypesItem2(String fullName, int image) {
//            this.fullName = fullName;
//            this.image = image;
//        }
//
//        public String fullName() {
//            return fullName;
//        }
//        public int image() {return image;}
//    }
//
//    /* Police */
//    /*----------------------------------------------*/
//    public enum subTypesItem3
//    {
//
//        CAR("Police Car", R.drawable.ic_police_copcar),
//        POLICE("Police", R.drawable.ic_2_3_police);
//
//
//        private String fullName;
//        private int image;
//
//        subTypesItem3(String fullName, int image) {
//            this.fullName = fullName;
//            this.image = image;
//        }
//
//        public String fullName() {
//            return fullName;
//        }
//        public int image() {return image;}
//    }
//
//    /* PARKING */
//    /*----------------------------------------------*/
//    public enum subTypesItem4
//    {
//
//        HOURLY("Hourly", R.drawable.ic_parking_hourly),
//        PRIVATE("Paid Private", R.drawable.ic_parking_private),
//        STREET("Street", R.drawable.ic_parking_street);
//
//        private String fullName;
//        private int image;
//
//        subTypesItem4(String fullName, int image) {
//            this.fullName = fullName;
//            this.image = image;
//        }
//
//        public String fullName() {
//            return fullName;
//        }
//        public int image() {return image;}
//    }
//
//    /* Crime
//    /*----------------------------------------------
//    public enum subTypesCrime{
//
//        ASSAULT("Assault", R.drawable.ic_crime_assault),
//        ROBBERY("Robbery", R.drawable.ic_crime_robbery);
//
//
//
//        private String fullName;
//        private int image;
//
//        subTypesCrime(String fullName, int image) {
//            this.fullName = fullName;
//            this.image = image;
//        }
//
//        public String fullName() {
//            return fullName;
//        }
//        public int image() {return image;}
//    }*/
//
//    /* Trail */
//    /*----------------------------------------------*/
//    public enum subTypesItem5
//    {
//
//        VIEW("Great View", R.drawable.ic_trails_greatview),
//        LAKE("Lake", R.drawable.ic_trails_lake),
//        HILLS("Hills", R.drawable.ic_trails_hills),
//        WATER("Flooding", R.drawable.ic_trails_flooding),
//        UNEVEN("Uneven Path", R.drawable.ic_trails_uneven);
//
//        private String fullName;
//        private int image;
//
//        subTypesItem5(String fullName, int image) {
//            this.fullName = fullName;
//            this.image = image;
//        }
//
//        public String fullName() {
//            return fullName;
//        }
//        public int image() {return image;}
//    }
//
//
//    /* Place */
//    /*----------------------------------------------*/
//    public enum subTypesItem6
//    {
//
//        SITE("Historic Site", R.drawable.ic_place_historicsite),
//        BATHROOM("Bathroom", R.drawable.ic_place_bathroom),
//        CHARGING("Charging Station", R.drawable.ic_place_charging),
//        WIFI("Wifi", R.drawable.ic_place_wifi);
//
//        private String fullName;
//        private int image;
//
//        subTypesItem6(String fullName, int image) {
//            this.fullName = fullName;
//            this.image = image;
//        }
//
//        public String fullName() {
//            return fullName;
//        }
//        public int image() {return image;}
//    }
//
//    /* TRANSPORT */
//    /*----------------------------------------------*/
//    public enum subTypesItem7
//    {
//
//        BIKE("Bike Stand",R.drawable.ic_transport_bike),
//        BUS("Bus Stop",R.drawable.ic_transport_bus),
//        SUBWAY("Subway",R.drawable.ic_transport_subway);
//
//        private String fullName;
//        private int image;
//
//        subTypesItem7(String fullName, int image) {
//            this.fullName = fullName;
//            this.image = image;
//        }
//
//        public String fullName() {
//            return fullName;
//        }
//        public int image() {return image;}
//    }
//
//    /* VENDOR */
//    /*----------------------------------------------*/
//    public enum subTypesItem8
//    {
//
//        FOODTRUCK("Food Truck", R.drawable.ic_vendor_foodtruck),
//        FOOD("Food", R.drawable.ic_vendor_food),
//        ICECREAM("Ice Cream", R.drawable.ic_vendor_icecream),
//        GIFT("Gift Shop", R.drawable.ic_vendor_giftshop);
//
//        private String fullName;
//        private int image;
//
//        subTypesItem8(String fullName, int image) {
//            this.fullName = fullName;
//            this.image = image;
//        }
//
//        public String fullName() {
//            return fullName;
//        }
//        public int image() {return image;}
//    }

/*
    public enum gpsStuff {
        BEST_LAST_KNOWN,
        CURR_GPS_LOCATION,
        FROM_OUR_DATABASE,
        USER_ADDED_TMP,
        FROM_GOOGLE_SEARCH;
    }
    */
}

package com.innovathon.sideways.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.innovathon.sideways.R;
import com.innovathon.sideways.main.MainActivity;
import com.innovathon.sideways.main.Types;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;


public class GoGetter extends DefaultAsyncProcess
{

	private String urlreaddb;
	MainActivity mainAct;
	String[] keys = {"tl_lat","tl_lon","br_lat","br_lon"};
	MarkerOptions mo = new MarkerOptions();
	float m_color = BitmapDescriptorFactory.HUE_GREEN;

	private Double[] viewingarea;
	private double center_lat = 0;
	private double center_lon = 0;
	private String types = null;
	private String keyword = null;
	private double radiusmeters = 0;

	private Double[] dLatLng = new Double[2];
	private String[] sLatLng = new String[2];

	private ArrayList<Double[]> locationsFromOurDB = new ArrayList<Double[]>();

	private final static long  minTimeToMakeRequest = 2000;

	public GoGetter(Context thisact, String urlreaddb, double latitude, double longitude, MainActivity act_, double mRadiusMeters)
	{
		super(act_);
		mainAct = act_;
	}

	public String getUrlreaddb()
	{
		return urlreaddb;
	}

	public void setUrlreaddb(String urlreaddb)
	{
		this.urlreaddb = urlreaddb;
	}

	public GoGetter(Activity act_, String urlreaddb_)
	{
		super(act_);
		mainAct = (MainActivity) act_;
		urlreaddb = urlreaddb_;
	}

	public GoGetter(Activity act_, String urlreaddb_, double center_lat_, double center_lon_, String types_, String keyword_, double radiusmeters_)
	{
		super(act_);
		mainAct = (MainActivity) act;
		urlreaddb = urlreaddb_;
		center_lat = center_lat_;
		center_lon = center_lon_;
		types = types_;
		keyword = keyword_;
		radiusmeters  = radiusmeters_;
	}
	@Override
	protected void doTheThing()
	{
		try
		{

			locationsFromOurDB.clear();
//			mainAct.clearTempMarkers();
			getData();
		}
		catch(Exception e)
		{
			Log.e("Wihapp", "Shame on you ! ", e);
		}
	}

	private static long timeLastRequest = -1;

	private void getData() throws Exception
	{
		getDataFromOurDB();

//		getDataFromGoogle();
	}


	private void getDataFromGoogle()
	{
		if (radiusmeters <= 0)
			return;

		makeSureWeAreNotExceedingGoogleRateQuota();

		final ArrayList<String> results = search(this.center_lat, this.center_lon, this.radiusmeters , this.types, this.keyword);
		mainAct.runOnUiThread
				(
						new Runnable()
						{

							//					IconGenerator iconGen = new IconGenerator(mainAct);
							@Override
							public void run()
							{
								for(String r: results)
								{
									String[] fields = r.split("\\|");
									String name = fields[0];
									String placeId = "";
									if (fields.length == 4)
										placeId = fields[3];
									name = name +"|" + placeId;
//						  if (name.length() > 12)
//						  {
//							  name = name.substring(0,8) + "...";
//						  }
									sLatLng[0] = clean(fields[1]);
									sLatLng[1] = clean(fields[2]);
									if (!toDouble(sLatLng, dLatLng))
										continue;
									double lat = dLatLng[0];
									double lon = dLatLng[1];

									if (!doWeAlreadyHaveThisInOurDB(lat,lon))
										MarkerManager.getMarkerManager().addMarker(new LatLng(lat,lon), name, "", Types.locationTypes.FROM_GOOGLE_SEARCH, true);

								}
							}
						}
				);

	}

	private void makeSureWeAreNotExceedingGoogleRateQuota()
	{
		if (timeLastRequest < 0)
			timeLastRequest = System.currentTimeMillis();
		else
		{
			long timeThisRequest = System.currentTimeMillis();
			if (timeThisRequest - timeLastRequest < minTimeToMakeRequest)
			{
				try
				{
					Thread.sleep(minTimeToMakeRequest);
				}
				catch(Exception e)
				{

				}
			}
			else
			{
				timeLastRequest = timeThisRequest;
			}
		}

	}

	protected boolean doWeAlreadyHaveThisInOurDB(double lat, double lon)
	{
		for(Double[] loc: locationsFromOurDB)
		{
			if (Math.abs(loc[0] - lat) < 0.00001 && Math.abs(loc[1] - lon) < 0.00001)
				return true;
		}

		return false;
	}

	private void getDataFromOurDB() throws Exception
	{
		Log.i("INFO", "about to get data from db");
		String s = urlreaddb;
		String kv = "?";

		viewingarea = mainAct.getViewingArea();

		int i = 0;

		for(String key: keys)
			if (i==0)
				kv += key+"="+viewingarea[i++];
			else
				kv += "&" + key+"="+viewingarea[i++];

		s += kv;

		InputStream is = HTTPRequestPoster.sendGetRequest(s, null)	;
		if (is == null)
		{
			mainAct.runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					mainAct.prompt("Can not get info from server")	;
				}

			});

			return;
		}
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "US-ASCII"));
			String content = "";
			String line = null;
			while((line = br.readLine()) != null)
				content += line;
			if (content == null)
				return;
			JSONParser parser = new JSONParser();
			Object object = parser.parse(content);
			if (object == null)
				return ;
			JSONArray jArray = (JSONArray) object;
			int num = jArray.size();
//			 final MarkerOptions[] markers = new MarkerOptions[num];
			final LatLng[] dbpoints = new LatLng[num];
			final String[] names = new String[num];
			final String[] infoes = new String[num];
			final String[] type = new String[num];
			final String[] stype = new String[num];
			final String[] picaths = new String[num];
			for(i = 0; i < num ; i++)
			{
				JSONObject geoObject = (JSONObject) jArray.get(i);
//			 	 String geoId = (String) geoObject.get("id");
				String namestr = (String)geoObject.get(act.getString(R.string.UC));
				String placeidstr = (String) geoObject.get(act.getString(R.string.ID));
				String infostr = getAdditionalInfo(geoObject);
				sLatLng[0] = (String) geoObject.get(act.getString(R.string.LA));
				sLatLng[1] = (String) geoObject.get(act.getString(R.string.LO));
				sLatLng[0] = clean(sLatLng[0]);
				sLatLng[1] = clean(sLatLng[1]);
				String typeindex = (String) geoObject.get(act.getString(R.string.TY));
				String stypeindx = (String) geoObject.get(act.getString(R.string.ST));
				if (! toDouble(sLatLng, dLatLng))
					continue;

				LatLng loc = new LatLng(dLatLng[0], dLatLng[1]);
				Double[] entry = new Double[2];
				entry[0] = dLatLng[0];
				entry[1] = dLatLng[1];
				locationsFromOurDB.add(entry);
				dbpoints[i] = loc;
				names[i] = namestr+"|" + placeidstr;
				infoes[i] = infostr;
				type[i] = typeindex;
				stype[i] = stypeindx;

			}

			mainAct.runOnUiThread(
					new Runnable()
					{

						@Override
						public void run()
						{
							MarkerManager.getMarkerManager().addMarkers(dbpoints, names, infoes, Types.locationTypes.FROM_OUR_DATABASE);
						}

					}

			);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private String getAdditionalInfo(JSONObject o)
	{
		HashMap<String,String> extrainfo = new HashMap<String,String>();
		int i = 1;
//		String alias = (String) o.get(act.getString(R.string.AL));
		String voteup = (String) o.get(act.getString(R.string.VU));
//		String id = (String) o.get("id");
		String timestamp = (String) o.get(act.getString(R.string.TS));
		String staticloc = (String) o.get(act.getString(R.string.SL));
		String type = (String) o.get(act.getString(R.string.TY));
		String stype = (String) o.get(act.getString(R.string.ST));
//		Integer itype = null;
//		if (type != null)
//			itype = Integer.parseInt(type);
//		String strtype = innovate.ae.arq.pathz.Types.locationTypes.values()[itype].name();

		extrainfo.put(act.getString(R.string.ID),(String)o.get(act.getString(R.string.ID)));
//		extrainfo.put(act.getString(R.string.AL),alias);
		extrainfo.put(act.getString(R.string.VU),voteup);
		extrainfo.put(act.getString(R.string.VD),(String)o.get(act.getString(R.string.VD)));
//		extrainfo.put("id",id);
		extrainfo.put(act.getString(R.string.TS),timestamp);
		extrainfo.put(act.getString(R.string.SL),staticloc);
		extrainfo.put(act.getString(R.string.TY),type);
		extrainfo.put(act.getString(R.string.ST),stype);
        extrainfo.put(act.getString(R.string.UC), (String)o.get(act.getString(R.string.UC)));
        extrainfo.put(act.getString(R.string.PP), (String)o.get(act.getString(R.string.PP)));
		String ret = JSONObject.toJSONString(extrainfo) ;
		return ret;
	}
	private String getRatingAndOtherInfoAsString(JSONObject jsonobj)
	{
		HashMap<String,String> extrainfo = new HashMap<String,String>();
		int i = 1;
		Object rateinfo;
		while((rateinfo = jsonobj.get("item" + i)) != null)
		{
			String rateinfostr = rateinfo.toString();
			extrainfo.put("item" + i,rateinfostr);
			i++;
		}

		Object misc = jsonobj.get("misc");
		if (misc == null)
			misc = jsonobj.get("json_info");

		if (misc != null)
		{
			String miscstr = misc.toString();
			extrainfo.put("misc",miscstr);
		}

		String locationtype = (String) jsonobj.get("location_type");
		extrainfo.put("'placetype'",locationtype);

		String foodtype = (String) jsonobj.get("food_type");
		extrainfo.put("'foodtype'",foodtype);
		String ret = JSONObject.toJSONString(extrainfo) ;
		return ret;
	}

	private String addLocationTypeToJson(String infostr, String label, String value)
	{
		JSONParser parser = new JSONParser();
		try
		{
			Object object = parser.parse(infostr);
			if (object == null)
				return null;
			JSONObject attr = (JSONObject) object;

			HashMap<String,String> temp = new HashMap<String,String>();
			for(Object key : attr.keySet())
			{
				Object str = attr.get(key);
				temp.put(key.toString(), str.toString());
			}

			temp.put(label, value);
			String output = JSONObject.toJSONString(temp);
			return output;
		}
		catch(Exception e)
		{
			return null;
		}

	}
	private boolean toDouble(String[] sLatLng, Double[] dLatLng)
	{
		try
		{
			dLatLng[0] = Double.parseDouble(sLatLng[0]);
			dLatLng[1] = Double.parseDouble(sLatLng[1]);
			return true;
		}
		catch(NumberFormatException e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public GoogleMap getMap()
	{
		return mainAct.getMap();
	}

	String googleapisearchurl = act.getString(R.string.google_api_search_url) ;
	private String url;
	//	private static String googlemapsAppKey = "AIzaSyBkV3SAfB6DmvpgCz72Tf0XhVQ0cUvAMrs";
	private String googlemapsAppKey = act.getString(R.string.google_maps_app_key);
	private ArrayList<String> search(double lat, double lon, double radius, String keyword, String type)
	{
		ArrayList<String> results = new ArrayList<String>();
		String myloc = lat+","+lon;
		String r = radius+"";
		if (keyword != null)
		{
			url = googleapisearchurl.replace("MYLOCATION", myloc)
					.replace("RADIUS", r)
					.replace("KEYWORD",keyword)
					.replace("APPKEY",googlemapsAppKey);
			try
			{
				updateResults(results,url);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		}

		if(type != null)
		{
			String url = googleapisearchurl.replace("keyword=KEYWORD", "type="+type).replace("MYLOCATION", myloc).replace("RADIUS", r).replace("APPKEY",googlemapsAppKey);
			try
			{
				updateResults(results,url);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return results;
	}

	String result = null;
	int timeout_for_googlemaps_search = 5;

	private void updateResults(final ArrayList<String> results, final String url) throws IOException
	{
		try
		{
			result = FileDnUp.getRemoteText(url) ;
			extractResults(result,results);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void extractResults(String result, ArrayList<String> results)
	{
		if (result == null)
			return;
		Document doc = Jsoup.parse(result);
		Elements found = doc.getElementsByTag("result");
		for(Element e: found)
		{
			Elements econt = e.getElementsByTag("name");
			if (econt == null || econt.isEmpty())
				continue;
			String rname = econt.first().text();
			econt = e.getElementsByTag("lat");
			if (econt == null || econt.isEmpty())
				continue;
			String rlat = econt.first().text();
			econt = e.getElementsByTag("lng");
			if (econt == null || econt.isEmpty())
				continue;
			String rlon = econt.first().text();

			econt = e.getElementsByTag("place_id");
			String placeId = "" ;
			if (econt != null && econt.first() != null && econt.first().text() != null)
				placeId = econt.first().text();
			results.add(rname+"|"+rlat+"|"+rlon+"|"+placeId);
		}

	}

	private String clean(String string)
	{
		string = string.replaceAll("[^\\-+0-9\\.]", "");
		return string;
	}
}

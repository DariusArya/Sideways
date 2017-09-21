package com.innovathon.sideways.util;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.innovathon.sideways.R;
import com.innovathon.sideways.main.MainActivity;
import com.innovathon.sideways.main.Types;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkerManager
{

	private static MarkerManager theOneAndOnly = null;

    public boolean  allow_user_input = true;
	public float blk_color = BitmapDescriptorFactory.HUE_BLUE;
	public float tmp_color = BitmapDescriptorFactory.HUE_CYAN;
	public float cur_color = BitmapDescriptorFactory.HUE_BLUE;


	public BitmapDescriptor blk = BitmapDescriptorFactory.defaultMarker(blk_color);
	public BitmapDescriptor cur = BitmapDescriptorFactory.defaultMarker(cur_color);
	public BitmapDescriptor tmp = BitmapDescriptorFactory.defaultMarker(tmp_color);
	public BitmapDescriptor newfgg = BitmapDescriptorFactory.defaultMarker();
	public BitmapDescriptor fgg, fdb, uat;

	public HashMap<String, String> extrainfo = new HashMap<String,String>();

	public HashMap<String, String> getExtraInfo()
	{
		return extrainfo;
	}

	public enum InfoType
	{
		NONE,
		INPUT,
		OUTPUT,
		INPUT2
	}

	public enum CRITERIATYPE
	{
		PLACETYPE;
	}

	private HashMap<CRITERIATYPE, Object> mCriteria;

	public void filterMarkers(HashMap<CRITERIATYPE, Object> criteria)
	{

		for(CRITERIATYPE criteriatype: criteria.keySet())
		{
			switch (criteriatype)
			{
				case PLACETYPE:
					ArrayList<Types.locationTypes> chosenTypes = (ArrayList<Types.locationTypes>) criteria.get(criteriatype);
					for( Types.locationTypes ltype:  Types.locationTypes.values())
						if (!chosenTypes.contains(ltype))
							removeAll(ltype);
					break;
			}
		}

		mCriteria = criteria;
	}

	public void clearFilter()
	{
		mCriteria = null;
	}

    public static MarkerManager getMarkerManager()
    {
        if (theOneAndOnly == null)
            theOneAndOnly = new MarkerManager();

        return theOneAndOnly;
    }

//	public boolean meetCriteria(CRITERIATYPE criteriaType, String criteriaInQuestion)
//	{
//		String loctypestr = mCriteria.get(criteriaType);
//		String[] items = loctypestr.split("\\|");
//		loctypestr = items[items.length-1];
//		return loctypestr.equalsIgnoreCase(criteriaInQuestion);
//	}

	public GoogleMap map;
	public MainActivity mainAct;

    HashMap<String, Marker> markers = new HashMap<String,Marker>();
    HashMap< Types.locationTypes, ArrayList<String>> typeToKeys = new HashMap< Types.locationTypes, ArrayList<String>>();

	public boolean removeMarker(Marker marker)
	{
		if (getKey(marker.getPosition(), false) == null)
			return false;
		return true;
	}

	public boolean addMarker(LatLng loc, String namestr, String info, Types.locationTypes loctype, boolean ifExistsLeaveInPlace)
    {
		String key = null;
		Types.subTypes locsubt = null;


		if ((key = getKey(loc, ifExistsLeaveInPlace)) == null)
			return false;

		if (info != null)
		{
			JSONParser parser = new JSONParser();
			try
			{
				Object object = parser.parse(info);
				if (object != null)
				{
					JSONObject jsonObjectInfo = (JSONObject) object;
                    String loctypestr =  (String) jsonObjectInfo.get(mainAct.getString(R.string.TY));
                    String locsubtypestr =  (String) jsonObjectInfo.get(mainAct.getString(R.string.ST));
                    if (loctypestr != null)
                    {
                        loctype = findLocTypeFromString(loctypestr);
						locsubt = findLocSubTypeFromStr(loctype,locsubtypestr);
                    }
				}
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}

            Matcher m = Pattern.compile(mainAct.getString(R.string.TY)+".{0,1}\":\"(.*?)\"").matcher(info);
			if (m.find())
			{
				String loctypestr = m.group(1);
				loctype = findLocTypeFromString(loctypestr);
				if(mCriteria != null)
					for(CRITERIATYPE criteriatype: mCriteria.keySet())
					{
						switch (criteriatype)
						{
							case PLACETYPE:
								ArrayList<Types.locationTypes> chosenTypes = (ArrayList<Types.locationTypes>) mCriteria.get(criteriatype);
								if (!chosenTypes.contains(loctype))
									return false;
								break;
						}
					}

			}
		}

        if (locsubt == null && loctype == Types.locationTypes.BEST_LAST_KNOWN)
            locsubt = Types.subTypes.BEST_LAST_KNOWN;
        if (locsubt == null && loctype == Types.locationTypes.CURR_GPS_LOCATION)
            locsubt = Types.subTypes.CURR_GPS_LOCATION;

		MarkerOptions mo = generateMarker(loc,locsubt, namestr);
        Log.i("INFO", "about to add marker " + locsubt.fullName());
        if(mainAct.isInMovingMode())
		{
			generateAndAddNotificationsToNotificationsQue(loc, locsubt, namestr);
		}
		if (mo != null)
		{
			markers.put(key, map.addMarker(mo));
			ArrayList<String> keysForThisType = typeToKeys.get(loctype);
			if (keysForThisType == null)
				keysForThisType = new ArrayList<String>();
			keysForThisType.add(key);
			typeToKeys.put(loctype, keysForThisType);
			String eiKey = createInfoMapKey(namestr, locsubt.fullName(), loc);
			if (namestr != null && (!extrainfo.containsKey(eiKey) || (extrainfo.get(eiKey) != info && info != null)))
				extrainfo.put(eiKey, info);
			return true;
		}
		else
			return false;

	}

	private void generateAndAddNotificationsToNotificationsQue(LatLng loc, Types.subTypes type, String namestr)
	{
        Log.i("INFO", "will add note if appropriate.");
        if (type != Types.subTypes.BEST_LAST_KNOWN &&
            type != Types.subTypes.CURR_GPS_LOCATION &&
            type != Types.subTypes.FROM_OUR_DATABASE &&
            type != Types.subTypes.USER_ADDED_TMP &&
            type != Types.subTypes.FROM_GOOGLE_SEARCH)
        {
			try
			{
				mainAct.addNotification(type.fullName());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			Log.i("INFO", "added note");
        }
	}


	private Types.subTypes findLocSubTypeFromStr(Types.locationTypes loctype, String locsubtypestr)
    {
        int typebindex = loctype.iSubtypeBeg;
        int typeeindex = loctype.iSubtypeEnd;
        int index = 0;

        for(Types.subTypes t : Types.subTypes.values())
        {
            if (index++ < typebindex)
                continue;
            if (t.identifier().equalsIgnoreCase(locsubtypestr))
                return t;
            if (index > typeeindex)
                return null;

        }

        return null;
    }

    public static String createInfoMapKey(String namestr, String typefullname, LatLng loc)
	{
		//not using namestr which is the comment section for a location, for now. we might use it later.
		return typefullname + "@" +  makeKey(loc);
	}

	private static Types.locationTypes findLocTypeFromString(String loctypestr)
	{
		Types.locationTypes loctype;
		for(Types.locationTypes t : Types.locationTypes.values())
			if (t.identifier().equalsIgnoreCase(loctypestr))
				return t;

		return null;
	}


	/**
	 * if a marker already exists decides to either leave it in place, or update it, 
	 * based on the value of ifExistsLeaveInPlace, if not or if it has to be updated, 
	 * creates a marker according to loctype, adds it to the map, and adds it to HashMaps, 
	 * one to keep track of locations marker has been added to, and the other one to keep track 
	 * of LocTypes of those markers, 
	 * @param loc
	 * @param loctype
	 * @param ifExistsLeaveInPlace
	 * @return true if it added or updated the marker, if the marker existed and was to be left in place it simply returns false.
	 */
	public boolean addMarker(LatLng loc, Types.locationTypes loctype, boolean ifExistsLeaveInPlace)
	{
		return addMarker(loc, null, null, loctype, ifExistsLeaveInPlace);
	}

	public String getKey(LatLng loc, boolean ifExistsLeaveInPlace)
	{
		if (loc == null)
			return null;
		String key = makeKey(loc);

		//if it has to be updated (perhaps with a new loctype) 
		//then it has to be found and removed;
		if (markers.containsKey(key))
			if (ifExistsLeaveInPlace)
				return null;
			else
			{
				Marker m = markers.remove(key); // remove from the table, and remove from the map.
				String name = m.getTitle();
				extrainfo.remove(name);
				m.remove();
				if (typeToKeys != null && !typeToKeys.isEmpty())	// remove from the tables we have for loctypes 
					for( Types.locationTypes type: typeToKeys.keySet())
						typeToKeys.get(type).remove(key);
			}


		return key;
	}

	public static String makeKey(LatLng loc)
	{
		return loc.latitude + "|" + loc.longitude;
	}

	static String pat = ".*ic_1.{1,4}" ;
	static boolean mBestLastLocationReceived = false;
	private MarkerOptions generateMarker(LatLng loc, Types.subTypes type, String name)
	{
        if (type == Types.subTypes.BEST_LAST_KNOWN)
        {
            if (mBestLastLocationReceived)
                return null;
            mBestLastLocationReceived = true;
        }

		MarkerOptions rec = new MarkerOptions();
		rec = rec.position(loc);
		String namestr = type.fullName();
		rec = rec.title(namestr);
		rec.snippet(type.name());

		switch(type)
		{
			case BEST_LAST_KNOWN:
				rec.snippet(type.name());
				rec = rec.icon(blk);
				return rec;

			case CURR_GPS_LOCATION:
				rec.snippet(type.name());
				rec = rec.icon(cur);
				return rec;

			case FROM_OUR_DATABASE:
				rec.snippet(type.name());
				rec = rec.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_hh));
				return rec;

			case USER_ADDED_TMP:
				rec.snippet(type.name());
				rec = rec.icon(tmp);
				return rec;

			case FROM_GOOGLE_SEARCH :
				rec.snippet(type.name());
				rec = rec.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_gray));
				return rec;
		}
//		innovate.ae.arq.pathz.Types.locationTypes type = innovate.ae.arq.pathz.Types.locationTypes.values()[iType];
        rec = rec.icon(BitmapDescriptorFactory.fromResource(type.micon()));
//        rec = rec.snippet(type.fullName());
//		String pattern = pat + type.fullName().toLowerCase() + "$";
//		int id = 0;
//		Drawable icondrawable = null;
//		for(Field f: R.drawable.class.getDeclaredFields())
//		{
//			String fname = f.getName();
//			if (fname.matches(pattern))
//			{
//				R.drawable dr = new R.drawable();
//				try
//				{
//					rec = rec.icon(BitmapDescriptorFactory.fromResource((Integer) f.get(dr)));
//				}
//				catch(Exception e)
//				{
//
//				}
//
//				break;
//
//			}
//
//
//		}




		return rec;

	}

	public void removeAll( Types.locationTypes loctype)
	{
		final ArrayList<String> keys = typeToKeys.get(loctype);

		if (keys != null)
		{
			mainAct.runOnUiThread(new Runnable(){

				@Override
				public void run()
				{
					while(!keys.isEmpty())
					{
						String key = keys.remove(0);
						markers.remove(key).remove();
					}
				}});
		}
	}

    public void removeAll()
    {
        for(Types.locationTypes loctype: typeToKeys.keySet())
        {
            removeAll(loctype);
        }
    }

	/**
	 *
	 * @param points
	 * @param names
	 * @param infoes
	 * @param loctype
	 * @return number of markers successfully added. 
	 */
	public int addMarkers(LatLng[] points, String[] names, String[] infoes, Types.locationTypes loctype)
	{
		int num = 0;
		int i = 0;
		for(LatLng point: points)
		{
			String namestr = names[i];
			String infostr = infoes[i];
			name = namestr;
			boolean result;
			if (loctype ==  Types.locationTypes.BEST_LAST_KNOWN)
				result = addMarker(point, name, infostr, loctype, true);  // don't add best last known if the marker for an identified place is already there.
			else
				result = addMarker(point, name, infostr, loctype, false); // for any other type remove the marker already there including best last known.
			if (result)
				num++;
			i++;
		}

		return num;

	}

	// Since we are consuming the event this is necessary to
	// manage closing openned markers before openning new ones
	static Marker lastOpenned = null;
	private AlertDialog alertDialog;


	public void setOnMarkerListener()
	{
		map.setOnMarkerClickListener(new OnMarkerClickListener()
		{
			@Override
			public boolean onMarkerClick(Marker marker)
			{
				// Check if there is an open info window
				if (lastOpenned != null)
				{
					// Close the info window
					lastOpenned.hideInfoWindow();

					// Is the marker the same marker that was already open
					if (lastOpenned.equals(marker))
					{
						// Nullify the lastOpenned object
						lastOpenned = null;
						// Return so that the info window isn't openned again
						return true;
					}
				}

				// Open the info window for the marker
//		    marker.showInfoWindow();
				// Re-assign the last openned such that we can close it later
				lastOpenned = marker;

				//now we handle the click.
				handle(marker);

				// Event was handled by our code do not launch default behaviour.
				return true;
			}
		});

	}


	protected void handle(Marker marker)
	{
		String title = marker.getTitle();
		String type = marker.getSnippet();

		Types.subTypes loctype =  Types.subTypes.valueOf(type);

		switch(loctype)
		{
			case BEST_LAST_KNOWN :
				mainAct.prompt("You can't enter information on best last known. \n GPS should be on.");
				break;

			case CURR_GPS_LOCATION:
				marker.setSnippet("You are here.");
				marker.showInfoWindow();
				showAnyPlaceDealPanel(marker);
				break;

			case FROM_GOOGLE_SEARCH :
				showNewLocationPanel(marker);
				break;

			case USER_ADDED_TMP:
				showUserAddedTmpPanel(marker);
				break;

			default:
				showEditLocationPanel(marker);
				break;

		}

	}

	static Marker chosenmarker = null;



	public void showAnyPlaceDealPanel(Marker marker)
	{
		chosenmarker = marker;
		createAndShowPlaceTypeDlgBox();
//		FragmentManager fm = mainAct.getFragmentManager();
//		final WihappPlaceTypeInputDialog placetypedlg = new WihappPlaceTypeInputDialog();	
//		placetypedlg.setCallerObject(this);

//		placetypedlg.show(fm, "");
//		Log.d("com.wihapp","dialog box"); 

	}

	protected void createAndShowPlaceTypeDlgBox()
	{
//		android.app.FragmentTransaction ft = mainAct.getFragmentManager().beginTransaction();
//		final WihappPlaceTypeInputDialog placetypedlg = new WihappPlaceTypeInputDialog();	
//		placetypedlg.show(ft, "");
//		Log.d("com.wihapp","dialog box"); 
		View checkBoxView = createPlaceView();
		AlertDialog.Builder builder = new AlertDialog.Builder(mainAct);
		builder.setView(checkBoxView);
		alertDialog = builder.create();
		alertDialog.show();
//	    builder.setTitle(" Select The Type ");
//	    builder.setView(checkBoxView)
//	           .setCancelable(false)
//	           .setPositiveButton("OK", new DialogInterface.OnClickListener() 
//	           {
//	               @Override
//				   public void onClick(DialogInterface dialog, int id) 
//	               {
//	            	   setSelectedDays(selected);
//	               }
//	           })
//	           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
//	           {
//	               @Override
//	               public void onClick(DialogInterface dialog, int id) 
//	               {
//	                    dialog.cancel();
//	               }
//	           }).show();	

	}

	private View createPlaceView()
	{
		LayoutInflater inflater = (LayoutInflater) mainAct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//		requestFeature(Window.FEATURE_NO_TITLE);


		return null;
	}


	private static boolean panelIsOpen = false;
	private static int topOfThePanel;
	private static String name;

	// shows input panel
	private void showNewLocationPanel(final Marker marker)
	{

		final LatLng location = marker.getPosition();
		LatLng newposition = findTheNewPositionToMoveTo(marker);

		if (allow_user_input)
		{
			final InfoType infoType = InfoType.INPUT;
			openAppropriatePanel(location, newposition, infoType, marker);
		}
		else
		{
			final InfoType infoType = InfoType.NONE;
			openAppropriatePanel(location, newposition, infoType, marker);
		}
	}

	// shows input panel
	private void showAnyPlaceDealPanel( Types.locationTypes chosenloctype, Marker chosenmarker)
	{
		final LatLng location = chosenmarker.getPosition();
		LatLng newposition = findTheNewPositionToMoveTo(chosenmarker);

		name = chosenloctype.name();
		name = translateName(name);
		if(allow_user_input)
		{
			final InfoType infoType = InfoType.INPUT2;
			openAppropriatePanel(location, newposition, infoType, chosenmarker);
		}
		else
		{
			final InfoType infoType = InfoType.NONE;
			openAppropriatePanel(location, newposition, infoType, chosenmarker);
		}
	}



	public static String translateName(String n)
	{
		for(String[] s: trantable)
		{
			if (s[0].equals(n))
				return s[1];

			if (s[1].equals(n))
				return s[0];
		}
		return n;
	}

	static String[][] trantable =
			{{"FOOD_TRUCK" 	, "Food Truck"},
                    {"FOOD" 		, "Food"},
					{"HAPPY_HOUR"	, "Happy Hour"},
					{"COFFEE"		, "Cafe"},
					{"FAST_FOOD"	, "Fast Food"}
			};

	private void openAppropriatePanel(final LatLng location, final LatLng newposition, final InfoType infoType, final Marker marker)
	{
		panelIsOpen = true;
		mainAct.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mainAct.openLocIOPanel(topOfThePanel, name, location, newposition,marker, infoType);
			}
		});
//		mainAct.moveSpotLight(location, newposition);
//		mainAct.runOnUiThread(new Runnable()
//		{
//			@Override
//			public void run()
//			{
////				mainAct.hideMainActElements();
//			}
//		});
//		map.animateCamera(CameraUpdateFactory.newLatLng(newposition), 500,new CancelableCallback()
//		{
//
//			@Override
//			public void onCancel()
//			{
//				if (panelIsOpen)
//					mainAct.runOnUiThread(new Runnable()
//					{
//						@Override
//						public void run()
//						{
//							mainAct.closeLocIOPanel();
//						}
//					});
//			}
//
//			@Override
//			public void onFinish()
//			{
//				panelIsOpen = true;
//				mainAct.runOnUiThread(new Runnable()
//				{
//					@Override
//					public void run()
//					{
//						mainAct.openLocIOPanel(topOfThePanel, name, location, newposition,marker, infoType);
//					}
//				});
//
//			}
//
//		});

	}

	public LatLng findTheNewPositionToMoveTo(Marker marker)
	{
		final LatLng location = marker.getPosition();

		Point position = map.getProjection().toScreenLocation(location);
		Point bottrght = mainAct.getScreenDimensions();

		int xNewPos = bottrght.x / 2;
		topOfThePanel = 0;
//	    topOfThePanel = (int) (bottrght.y * WhMain.getTopPortionHeightRatio());

		name = marker.getTitle();
		int yNewPos = topOfThePanel/2;

		int xDisplacement = xNewPos - position.x;
		int yDisplacement = yNewPos - position.y;

		Point curCamPos = map.getProjection().toScreenLocation(map.getCameraPosition().target);
		int xOldCenter = curCamPos.x;
		int yOldCenter = curCamPos.y;

		int xNewCenter = xOldCenter - xDisplacement;
		int yNewCenter = yOldCenter - yDisplacement;

		LatLng newposition = map.getProjection().fromScreenLocation(new Point(xNewCenter, yNewCenter));

		return newposition;

	}

	//shows output panel
	private void showEditLocationPanel(Marker marker)
	{
		final LatLng location = marker.getPosition();
		LatLng newposition = findTheNewPositionToMoveTo(marker);
		final InfoType infoType = InfoType.OUTPUT;
		openAppropriatePanel(location, newposition, infoType, marker);
	}


	private static void showUserAddedTmpPanel(Marker marker)
	{
		// TODO Auto-generated method stub

	}

	public void openInputPanel( Types.locationTypes userchoice)
	{
		showAnyPlaceDealPanel(userchoice, chosenmarker);
	}

	public Vector<Marker> get(Types.locationTypes loctype)
	{
		ArrayList<String> keys = typeToKeys.get(loctype);
		if (keys == null)
			return null;
		Vector<Marker> ret = new Vector<Marker>();
		for(String key: keys)
		{
			Marker m = markers.get(key);
			if (m != null)
				ret.add(m);
		}

		return ret;
	}





}
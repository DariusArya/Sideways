package com.innovathon.sideways.util;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;

@SuppressWarnings("rawtypes")
public class DefaultAsyncProcess extends AsyncTask<Void,Void,Void> implements Process
{
	protected Activity act;
	Object result;
	private boolean isRunning = false; 
	public DefaultAsyncProcess(Activity act_)
	{
		act = act_;
	}



	@SuppressWarnings("unchecked")
	@Override
	public void launch() 
	{
//		execute();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
        else
            execute((Void[])null);
	}

	@Override
	public void beginning() 
	{
		onPreExecute();		
	}

	@Override
	public void start() 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ending() 
	{
		onPostExecute((Void) result);		
	}
	
	public boolean isRunning()
	{
		return isRunning;
	}
	
	@Override
	protected void onPreExecute() 
	{
		// TODO Auto-generated method stub
		isRunning = true;
		super.onPreExecute();
	}
	@Override
	protected void onPostExecute(Void result) 
	{
		isRunning = false; 
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}
	@Override
	protected Void doInBackground(Void... params) 
	{
		doTheThing();
		return null;
	}
	protected void doTheThing() 
	{
		// TODO Auto-generated method stub
		
	}

}

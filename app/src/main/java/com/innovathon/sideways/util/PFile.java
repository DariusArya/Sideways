package com.innovathon.sideways.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;


public class PFile extends File
{


	@Override
	public PFile getParentFile()
	{
		PFile par = this.getParentFolder();
		if (par != null)
			return new PFile(par.getAbsolutePath());
		return par;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5619591299725226889L;

	public enum IO
	{
		R, W, RW;
	} 
	
	public PFile(File parent, String child)
	{
		super(parent, child);		
	}
	
	public PFile(String pathname)
	{
		super(pathname);
	}
		
	public PFile(String parent, String child)
	{
		super(parent, child);	
	}
	
	public PFile(URI uri)
	{
		super(uri);
	}
	

	/**
	 * checking file for reading and writing.
	 * @param mode
	 * @return
	 */
	public boolean checkFor(IO mode)
	{
		if (mode == IO.RW)
			return checkFor(IO.R) && checkFor(IO.W);
		
		BufferedReader buf = null;
		try 
		{
			if (mode == IO.W) //check for being writable
			{
				BufferedWriter bufr = null;
				try 
				{
					bufr = new BufferedWriter(new FileWriter(getAbsolutePath()));
				} 
				catch (Exception ex)
				{
					if (bufr != null) 
						bufr.close();
					return false;
				}
				bufr.close();
				return true;
			}
	        //check for reading   
			buf = new BufferedReader(new FileReader(getAbsolutePath()));
			char[]  arg0 = new char[1];
			@SuppressWarnings("unused")
			int num = buf.read(arg0);
			buf.close();
			return true;
		} 
		catch (Exception ex)
		{
			return false;
		} 
		finally 
		{
			try 
			{
				if (buf != null)
					buf.close();
			} 
			catch (IOException ex)
			{
				return false;
			}
		}
	}

	/**
	 * 
	 */
	
	public static String getParentFolder(String fullpathToBluePrintFile)
	{
		File f = new File(fullpathToBluePrintFile);
		
		return f.getParent();
	}
	
	public PFile getParentFolder()
	{
		String par = getParent();
		if (par != null)
			return new PFile(this.getParent());
		else
			return null;
	}

	 public PFile copyTo(String toFileName) throws IOException
	 {
		 String fromFileName = getAbsolutePath();
		 File fromFile = new File(fromFileName);
		 File toFile = new File(toFileName);

		 if (!fromFile.exists())
			 throw new IOException("FileCopy: " + "no such source file: " + fromFileName);
  
		 if (!fromFile.isFile())
			 throw new IOException("FileCopy: " + "can't copy directory: "+ fromFileName);
   
		 if (!fromFile.canRead())
			 throw new IOException("FileCopy: " + "source file is unreadable: " + fromFileName);

		 if (toFile.isDirectory())
			 toFile = new File(toFile, fromFile.getName());

		 if (toFile.exists()) 
		 {
			 if (!toFile.canWrite())
				 throw new IOException("FileCopy: " + "destination file is unwriteable: " + toFileName);

			 String response = "Y";
			 if (!response.equals("Y") && !response.equals("y"))
				 throw new IOException("FileCopy: " + "existing file was not overwritten.");
   		 } 
		 else 
		 {
			 String parent = toFile.getParent();
			 
			 if (parent == null)
				 parent = System.getProperty("user.dir");
			 
			 File dir = new File(parent);
			 
			 if (!dir.exists())
				 throw new IOException("FileCopy: " + "destination directory doesn't exist: " + parent);
			
			 if (dir.isFile())
				 throw new IOException("FileCopy: " + "destination is not a directory: " + parent);
    
			 if (!dir.canWrite())
				 throw new IOException("FileCopy: " + "destination directory is unwriteable: " + parent);
		 }

   
		 FileInputStream from = null;
   
		 FileOutputStream to = null;
   
		 try 
		 {
			 from = new FileInputStream(fromFile);
			 to = new FileOutputStream(toFile);
			 byte[] buffer = new byte[4096];
			 int bytesRead;
			 while ((bytesRead = from.read(buffer)) != -1)
				 to.write(buffer, 0, bytesRead); // write
   		 } 
		 finally 
		 {
			 if (from != null)
				 try 
			 	 {
					 from.close();
			 	 } 
			 	 catch (IOException e)
			 	 {
			 		 ;
			 	 }
			 
			 if (to != null)
				 try 
			 	 {
					 to.close();
			 	 } 
			 	 catch (IOException e)
			 	 {
			 		return null ;
			 	 }
		 }
		 
		 return new PFile(toFileName);
	 }
	 
	 /**
	  * moves a file from fromFileName to toFileName
	  * @param toFileName the final path and name
	  * @return
	  * @throws IOException
	  */
	 
	 public PFile moveTo(String toFileName) throws IOException
	 {
		 String fromFileName = getAbsolutePath();
		 PFile dest = copyTo(toFileName);
		 File from = new File(fromFileName);
		 from.delete();
		
		 //if it reaches here no problem has happened
		 return dest;
	 }
	
	 public void moveAppend(String dst)
	 {
		 PFile dest = new PFile(dst);
		 String srcText = getText();
		 dest.putText(srcText);
	 }
	 /**
	  * deletes a file
	  * @param file
	  * @return if it succeeds returns true, otherwise false
	  * @throws IOException
	  */
	 public static boolean delete(String file)
	 {
		 File d = new File(file);
		 if (d.exists())
			 return d.delete();
		 else
			 return false;
	 }
	 
	  // Read in the bytes
     long offset = 0;
     long numRead = 0;
	 /**
	  * For a small file, it reads it all in one swoop, for a large file, you should be able to 
	  * apply it repeatedly until you get all the bytes.
	  * @return bytes of the file.
	  * @throws IOException
	  */
	 public byte[] getBytes() throws IOException
	 {
	     InputStream is = new FileInputStream(getAbsolutePath());

	     
	     int length = 0;

	     // You cannot create an array using a long type.
	     // It needs to be an int type.
	     // Before converting to an int type, check
	     // to ensure that file is not larger than Integer.MAX_VALUE.
	     if (offset == 0 && length() > Integer.MAX_VALUE )
	         length = Integer.MAX_VALUE;
	     else
	    	 length = (int) length();

	     // Create the byte array to hold the data
	     byte[] bytes = new byte[length];

	     while (offset < bytes.length && (numRead=is.read(bytes)) >= 0)
	     {
	         offset += numRead;
	     }

	     // Ensure all the bytes have been read in
	     if (numRead <= 0 && offset < length()) 
	     {
	         throw new IOException("Could not completely read file "+ getName());
	     }

	     // Close the input stream and return bytes
	     is.close();
	     return bytes;
	 }

	 public PFile create()
	 {
		 if (isDirectory() && !exists())
		 {
			 FileTasks.mkDir(getAbsolutePath());
			 return this;
		 }
		 
		 File par = getParentFile();
		 
		 if (par != null && !par.exists())
		 {
			 System.out.println("creating " + par.getAbsolutePath());
			 FileTasks.mkDir(par.getAbsolutePath());
		 }
		 
		 if (isFile() && !exists())
		 {
			try 
			{
				if (createNewFile())
					return this;
			} 
			catch (IOException e)
			{
				e.printStackTrace();
				return null;
			}
		 }
		 
		return this;		 
	 }
	 
	 public String getText()
	 {
		 String filepath = getAbsolutePath();
		 try 
		 {
			 BufferedReader buf = new BufferedReader(new FileReader(filepath));
			 String text = "";
			 String line = "";
			 while ((line = buf.readLine()) != null)
				 text += line + '\n';
				   
			 return text;
		 } 
		 catch (FileNotFoundException e)
		 {
			 e.printStackTrace();
			 return null; 
		 } 
		 catch (IOException e)
		 {
			 e.printStackTrace();
			 return null;
		 }		   
	 }
	
	 public String getText(int maxlines)
	 {
		 String filepath = getAbsolutePath();
		 try 
		 {
			 BufferedReader buf = new BufferedReader(new FileReader(filepath));
			 String text = "";
			 String line = "";
			 int counter = 0; 
			 while ((line = buf.readLine()) != null && counter++ < maxlines)
				 text += line + '\n';
				   
			 return text;
		 } 
		 catch (FileNotFoundException e)
		 {
			 e.printStackTrace();
			 return null; 
		 } 
		 catch (IOException e)
		 {
			 e.printStackTrace();
			 return null;
		 }		   
	 }
	 
	 public String getText(int recb, int numLines, boolean headerIncluded)
	 {
		 String filepath = getAbsolutePath();
		 try 
		 {
			 BufferedReader buf = new BufferedReader(new FileReader(filepath));
			 String text = "";
			 String line = "";
			 int index = 0;
			 int counter = 0; 
			 while ((line = buf.readLine()) != null && counter < numLines)
			 {
				 if (index == 0 && headerIncluded)
				 {
					 text += line + '\n';
					 index++;
					 continue;
				 }
				 if (index++ >= recb && counter++ < numLines)
					 text += line + '\n';
			 }   
			 if (buf.readLine() == null && text.length() == 0 && counter == 0)
				 return null;
			 return text;
		 } 
		 catch (FileNotFoundException e)
		 {
			 e.printStackTrace();
			 return null; 
		 } 
		 catch (IOException e)
		 {
			 e.printStackTrace();
			 return null;
		 }		   
	 }
	 
	private  int linecounter = 0; 
	 private BufferedReader buf = null;
	 private boolean endOfFileHasReached = false;
	 /**
	  * gets the next <num> lines from the file
	  * @param num the number of lines that has to be read
	  * @return lines in an array
	  */
	 public String[] getNextLines(int num)
	 {
	
		 if (endOfFileHasReached)
			 return null;
			try 
			{
				if (buf == null)
					open();
				
				
			    int curLine = linecounter ; 
			    ArrayList<String> lines =  new ArrayList<String>();
			    String line = null ;
			    while(!endOfFileHasReached && curLine - linecounter < num &&  (line = buf.readLine()) != null )
			    {
			    	lines.add(line);
			    	curLine++;
			    }
		
			    linecounter = curLine; 
			    
			    if (line == null)
			    	endOfFileHasReached = true;
			    String[] linesArray  = new String[lines.size()];
			    for(int k = 0 ; k < linesArray.length; k++)
			    	linesArray[k] = lines.get(k);
			    return linesArray ;
			    
			} catch (FileNotFoundException e)
			{
				return null;  
			} 
			catch (IOException e)
			{
				return null;
			}
	 }
	 public boolean endOfFile() 
	 {
		 return endOfFileHasReached;
	 }
	 public boolean putText(String text, boolean doAppend)
	 {
		 String filepath = getAbsolutePath();
		 if (!exists())
			try 
		 	{
				create();
		 	} 
		 	catch (Exception e1)
		 	{
		 		e1.printStackTrace();
		 		return false;
		 	}
//		
//		 if (!canWrite() || text == null)
//			 return false;		
//				
		 try
		 {
			 //append text
			 BufferedWriter buf = new BufferedWriter(new FileWriter(filepath, doAppend));
			 buf.write(text);
			 buf.close();
			 return true;
		 }
		 catch (IOException e)
		 {
			 e.printStackTrace();
			 return false;
		 }
	 }
	 
	 public boolean putText(String text)
	 {
		return putText(text, true);
	 }

	 public static java.io.Reader getReader(String file)
	{
		PFile temp = new PFile(file);
		return temp.getReader();
	}

	 public java.io.Reader getReader() 
	{
		try 
		{
			if (!exists())
				return null;
			
			java.io.Reader r = new BufferedReader(new FileReader(getAbsolutePath()));
			return r;			
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}		
	}
	
	 public static Writer getWriter(String file)
	{
		PFile temp = new PFile(file);
		return temp.getWriter();
	}

	 public Writer getWriter()
	{
		try
		{
			if (!exists())
				createNewFile();

			Writer w = new BufferedWriter(new FileWriter(getAbsolutePath()));
			return w;
				
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	
	 public java.util.Map<String, String> createMap(int numLines, int recb, int fieldUsedForKey, String reFieldSeparator)
	{
		 java.util.Map<String,String> ret = new java.util.HashMap<String,String>();
		 String filepath = getAbsolutePath();
		 try 
		 {
			 BufferedReader buf = new BufferedReader(new FileReader(filepath));
			 String line = "";
			 int index = 0;
			 int counter = 0; 
			 while ((line = buf.readLine()) != null )
			 {
				 if (numLines > 0 && counter >=  numLines)
					 break;
		   	
				 if (index++ >= recb)
				 {
					 String[] fields = line.split(reFieldSeparator);
					 if (fieldUsedForKey >= fields.length)
						 fieldUsedForKey = 0;
					 
					 String key = fields[fieldUsedForKey];
					  ret.put(key, line);
				 }
			 }   
			 return ret;
		 } 
		 catch (FileNotFoundException e)
		 {
			 e.printStackTrace();
			 return null; 
		 } 
		 catch (IOException e)
		 {
			 e.printStackTrace();
			 return null;
		 }		   
	}

	 public void reset() throws IOException
	{
		if (buf != null) 
			buf.reset();
		linecounter =0;			
	}

	 public void mark(int i) throws IOException
	{
		if (buf != null && buf.markSupported())
			buf.mark(0);		
	}
	 
	 public void open() throws FileNotFoundException, IOException
	{
		buf = new BufferedReader(new FileReader(this.getCanonicalPath()));
		buf.mark(0);
		linecounter = 0; 		
	}

	public static String getText(String fullfilepathname)
	{
		PFile temp = new PFile(fullfilepathname);
		String ret = temp.getText();
		return ret; 
	}

	public PFile[] listFiles(final String regex)
	{
		FilenameFilter filter = new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name)
			{
			
				if (name.matches(regex))
				{
					return true;
				} 
				else 
				{
					return false;
				}
			}
		};
		
		File[] listfiles = super.listFiles(filter);
		PFile[] retfiles = new PFile[listfiles.length];
		int k = 0;
		for(File f: listfiles)
			retfiles[k++] = new PFile(f.getAbsolutePath());

		return retfiles;
	}

    
	
	
	

	
	
	


	
}

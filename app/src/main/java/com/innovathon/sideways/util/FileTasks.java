package com.innovathon.sideways.util;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author 3mymb
 */
public class FileTasks {
    static final int BUFFER = 2048;
    private static String dlgtitle = "";
    private static FileOpTypes ft;
    private static String extension = ".csv";
    private static String filepath;

    public FileTasks(String dlgtitle_, FileOpTypes type) {

        dlgtitle = dlgtitle_;
        ft = type;
    }

    public FileTasks() {
    }

    public static String getCurrentFolder() {
        String path = null;
        try {
            path = (new File(".")).getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    /**
     * @param f    The file that is going to be checked
     * @param mode FileOpTypes
     * @return returns either true or false depending on the mode and status of the file.
     * if the mode is 0, and the file can be openned and read, returns true, similarly if the
     * the mode is 1 and the file can be opened and written to returns true, otherwise returns false.
     */
    public static boolean checkFile(File f, FileOpTypes mode) {
        BufferedReader buf = null;
        try {
            if (mode == FileOpTypes.WRITE) //check for being writable
            {
                BufferedWriter bufr = null;
                try {
                    bufr = new BufferedWriter(new FileWriter(f));
                } catch (Exception ex) {
//                    if (this.mainPanel != null)
//                        JOptionPane.showMessageDialog(mainPanel,
//                                                      ex,
//                                                      "The file can not be written to.",
//                                                      JOptionPane.ERROR_MESSAGE);
//                    else
//                       Logger.getLogger(FileTasks.class.getName()).log(Level.SEVERE, null, ex);
                    if (bufr != null)
                        bufr.close();
                    return false;
                }


                return true;
            }


            String fpath = f.getAbsolutePath();

            buf = new BufferedReader(new FileReader(fpath));

            @SuppressWarnings("unused")
            String line1 = buf.readLine();
            buf.close();
            return true;
        } catch (Exception ex) {
//             if (this.mainPanel != null)
//            	 JOptionPane.showMessageDialog(mainPanel,ex,"The file can not be read from.", JOptionPane.ERROR_MESSAGE);
//             else
//            	 Logger.getLogger(FileTasks.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            try {
                if (buf != null)
                    buf.close();
            } catch (IOException ex) {
//                Logger.getLogger(FileTasks.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
    }

    public static boolean checkFile(String file, FileOpTypes ft) {
        return (checkFile(new File(file), ft));
    }

    public static String[] find(final String fileNameRE, String in) {
        File f = new File(in);
        if (!f.isDirectory())
            return null;

        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.matches(fileNameRE);
            }

        };

        return f.list(filter);
    }

    public static boolean zip(String... files) throws FileNotFoundException {
        String destfile = files[files.length - 1];
        BufferedInputStream origin = null;

        try {
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destfile)));
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < files.length - 1; i++) {
//					   System.out.println("Adding: "+files[i]);
                FileInputStream fi = new FileInputStream(files[i]);

                File f = new File(files[i]);
                String filename = f.getName();
                origin = new BufferedInputStream(fi, BUFFER);
                // create zip entry
                ZipEntry entry = new ZipEntry(filename);

                // add entries to ZIP file
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


    }

    public static boolean zip(File... files) throws FileNotFoundException {
        String[] filenames = new String[files.length];
        int k = 0;
        for (File f : files)
            filenames[k++] = f.getAbsolutePath();

        return zip(filenames);
    }

    public static void unzip(String zipfile, String dest) throws IOException {
        int BUFFER = 2048;
        byte data[] = new byte[BUFFER];
        FileInputStream fis = new FileInputStream(zipfile);
        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;

        if (!dest.endsWith("/") || !dest.endsWith("\\"))
            dest += "\\";


        //build the directories first
        while ((entry = zin.getNextEntry()) != null) {
            String fullname = entry.getName();
            String[] path = fullname.split("/|\\\\");
            String fileonly = path[path.length - 1];
            String destfile = dest + fileonly;
            File destFile = new File(destfile);

            if (destFile.isDirectory())
                mkDir(destFile);
            else {
                File parfolder = new File(destfile).getParentFile();
                if (!parfolder.exists())
                    mkDir(parfolder);
                FileOutputStream fos = new FileOutputStream(destfile);
                BufferedOutputStream buf = new BufferedOutputStream(fos, BUFFER);
                int count = -1;
                while ((count = zin.read(data, 0, BUFFER)) != -1)
                    buf.write(data, 0, count);

                buf.close();
            }
        }


    }

    private static boolean mkDir(File dir) {
        File par = dir.getParentFile();
        if (par.exists())
            return dir.mkdir();
        else {
            if (mkDir(par))
                return dir.mkdir();
            else
                return false;

        }
    }

    public static boolean mkDir(String fullpath) {
        File d = new File(fullpath);
        return mkDir(d);

    }

    /**
     * moves a file from fromFileName to toFileName
     *
     * @param fromFileName the initial path and name
     * @param toFileName   the final path and name
     * @return
     * @throws IOException
     */
    public static void move(String fromFileName, String toFileName) throws IOException {
        PFile src = new PFile(fromFileName);
        src.moveTo(toFileName);
    }

    public static void copy(String fromFileName, String toFileName) throws IOException {
        PFile src = new PFile(fromFileName);
        src.copyTo(toFileName);
    }

    /* * deletes a file
     * @param file
     * @return if it succeeds returns true, otherwise false
     * @throws IOException
     */
    public static boolean delete(String file) {
        PFile toBeDeleted = new PFile(file);
        try {
            toBeDeleted.delete();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static byte[] getBytesFromFile(File thefile) throws IOException {
        return getBytesFromFile(thefile.getAbsolutePath());
    }

    private static byte[] getBytesFromFile(String thefile) {
        PFile file = new PFile(thefile);
        try {
            return file.getBytes();
        } catch (IOException e) {
            return null;
        }
    }

    public static void moveAppend(String src, String dst) {
        PFile srcfile = new PFile(src);
        srcfile.moveAppend(dst);
    }

    public static String[] sortFileCreationDateOldestFirst(String[] files) {
        ArrayList<Integer> orderedlist = new ArrayList<Integer>();
        String[] sortedfilelist = new String[files.length];

        for (int k = 0; k < files.length; k++)
            findNextOldest(files, orderedlist);

        for (int k = 0; k < files.length; k++)
            sortedfilelist[k] = files[orderedlist.get(k)];

        return sortedfilelist;

    }

    private static void findNextOldest(String[] filenames, ArrayList<Integer> orderedlist) {
        int k = -1;
        int ret = k;
        long oldestone = -1;
        for (String filename : filenames) {
            if (orderedlist.contains(++k))
                continue;
            File f = new File(filename);
            if (oldestone == -1) {
                oldestone = f.lastModified();
                ret = k;
                continue;
            }
            if (oldestone > f.lastModified()) {
                oldestone = f.lastModified();
                ret = k;
            }
        }

        orderedlist.add(ret);
    }

    public void setFileFilter(String string) {
        extension = string.trim();
        if (extension.charAt(0) != '.')
            extension = "." + extension;
    }

    public FileTasks setIOType(FileOpTypes type) {
        ft = type;
        return this;
    }

    public void setDlgTitle(String dlgtitle_) {
        dlgtitle = dlgtitle_;
    }

    public String getFilePath() {
        return filepath;
    }

    public enum FileOpTypes {
        READ,
        WRITE
    }
}


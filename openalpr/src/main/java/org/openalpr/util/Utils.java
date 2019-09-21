package org.openalpr.util;

import android.content.res.AssetManager;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * OpenALPR utils.
 */
public class Utils {

    private Utils() {}

    /**
     * Copies the assets folder.
     *
     * @param assetManager The assets manager.
     * @param fromAssetPath The from assets path.
     * @param toPath The to assets path.
     * @param fileToBeEdited The name of file that should be copied with different method so that it could be possible to replace text inside that file.
     *                 This is "openalpr.conf" file whose "runtime_dir" value should be updated during copying file from assets
     * @param textToBeReplaced  This is data directory in "openalpr.conf" file
     * @param replacingText     This is user's real data directory and will replace example data directory in "openalpr.conf" file
     * @return A boolean indicating if the process went as expected.
     */
    public static boolean copyAssetFolder(AssetManager assetManager, String fromAssetPath, String toPath,
                                          String fileToBeEdited, String textToBeReplaced, String replacingText) {
        try {
            String[] files = assetManager.list(fromAssetPath);

            new File(toPath).mkdirs();

            boolean res = true;

            for (String file : files)

                if (file.contains(".")) {
                    res &= copyAsset(assetManager, fromAssetPath + "/" + file, toPath + "/" + file,
                            fileToBeEdited, textToBeReplaced, replacingText);
                } else {
                    res &= copyAssetFolder(assetManager, fromAssetPath + "/" + file, toPath + "/" + file,
                            fileToBeEdited, textToBeReplaced, replacingText);
                }

            return res;
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * Copies an asset to the application folder.
     *
     * @param assetManager The asset manager.
     * @param fromAssetPath The from assets path.
     * @param toPath The to assests path.
     * @param fileToBeEdited The name of file that should be copied with different method so that it could be possible to replace text inside that file.
     *                       This is "openalpr.conf" file whose "runtime_dir" value should be updated during copying file from assets
     * @param textToBeReplaced  This is data directory in "openalpr.conf" file
     * @param replacingText     This is user's real data directory and will replace example data directory in "openalpr.conf" file
     * @return A boolean indicating if the process went as expected.
     */
    private static boolean copyAsset(AssetManager assetManager, String fromAssetPath, String toPath,
                                     String fileToBeEdited, String textToBeReplaced, String replacingText) {
        InputStream in = null;
        OutputStream out = null;

        BufferedReader bfr = null;
        PrintWriter pw = null;
        try {
            // If the file being copied is "openalpr.conf" then copy it with BufferedReader so that we can replace the
            // "runtime_dir = /data/data/com.mecofarid.openalprsample/runtime_data" with user's real data directory. Since Android 5.0 (phones)
            // and Android 4.4 (Tablets) multiple user accounts are supported so symlink for data directory will be "/data/user/12/....."
            if (fromAssetPath.contains(fileToBeEdited)){
                bfr = new BufferedReader(new InputStreamReader(assetManager.open(fromAssetPath)));

                new File(toPath).createNewFile();

                pw = new PrintWriter(new BufferedOutputStream(new FileOutputStream(toPath)));

                editAndCopyFile(bfr, pw, textToBeReplaced, replacingText);

                bfr.close();

                bfr = null;

                pw.flush();
                pw.close();

                pw = null;
            }
            else {
                in = assetManager.open(fromAssetPath);

                new File(toPath).createNewFile();

                out = new FileOutputStream(toPath);

                copyFile(in, out);
                in.close();

                in = null;

                out.flush();
                out.close();

                out = null;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Copies a file.
     *
     * @param in The input stream.
     * @param out The output stream.
     *
     * @throws IOException
     */
    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];

        int read;

        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * Copies a file line-by-line.
     * @param bfr            BuffereReader
     * @param pw             PrintWriter
     * @param oldText        Example app data directory
     * @param newText        Real user data directory
     * @throws IOException
     */

    private static void editAndCopyFile(BufferedReader bfr, PrintWriter pw, String oldText, String newText) throws IOException{
        StringBuilder fileContent = new StringBuilder();
        String line;
        while ((line = bfr.readLine()) != null){
            if (line.contains(oldText)){
                line = line.replace(oldText, newText);
            }
            fileContent.append(line);
            fileContent.append(System.getProperty("line.separator")); // Append new line
        }
        pw.write(fileContent.toString());
    }

}

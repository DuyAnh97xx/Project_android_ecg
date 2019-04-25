package com.nordicsemi.nrfUARTv2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SaveData {

    public void save(final ArrayList<Double> dataSave, String nameTag) {
        String currentTime = DateFormat.getTimeInstance().format(new Date());
        //String currentDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date());

        String filename = "" + currentTime + nameTag;
        //String filename2 = "" + currentTime + " -RAW 2.txt";
        OutputStreamWriter writer = null;

        File sdCard = Environment.getExternalStorageDirectory();
        if (sdCard.exists()) {
            File directory = new File(sdCard.getAbsolutePath() + "/HR_Data");

            if (!directory.exists()) {
                directory.mkdirs();
                Log.i("making", "Creating Directory: " + directory);
            }

            Log.i("made", "Directory found: " + directory);

            File newFile = new File(directory, filename);
            try {
                writer = new OutputStreamWriter(new FileOutputStream(newFile));
                for (int i = 0; i < dataSave.size(); i++) {
                    Log.i("writer", "Writing to file");
                    writer.write(String.valueOf(dataSave.get(i) + "\n"));
                }
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }

        }
    }
}

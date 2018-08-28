package com.stg.makeathon.agrohelper.service;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileOperation {
    public interface FileOperationCallback {
        void onSuccess(Object response);
        void onError(String errMsg);
    }
    public static void getDataFromFile(final Context context, final String filename, final FileOperationCallback callback) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                File file = context.getFileStreamPath(filename);
                if (file == null || !file.exists()) {
                    Log.e("FileOperation","File "+filename+" not found.");
                    if (callback != null) {
                        callback.onError("Unable to load data.");
                        return;
                    }
                }
                FileInputStream fis;
                try {
                    fis = context.openFileInput(filename);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    Object data = ois.readObject();
                    Log.d("FileOperation", data.toString());
                    if (callback != null) {
                        callback.onSuccess(data);
                        return;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError("Unable to load data.");
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError("Unable to load data.");
                        return;
                    }
                }
            }
        };
        new Thread(task).start();
    }
    public static void saveDataToFile(final Context context, final String filename, final Object data, final FileOperationCallback callback) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                FileOutputStream fos;
                try {
                    fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(data);
                    if (callback != null) {
                        callback.onSuccess(data);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError("Unable to save data.");
                        return;
                    }
                }
            }
        };
        new Thread(task).start();
    }
}

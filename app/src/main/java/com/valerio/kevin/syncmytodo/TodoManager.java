package com.valerio.kevin.syncmytodo;

import android.os.Environment;

import java.io.File;

public class TodoManager {

    private static final String FTP_PASSWORD = "valerio83";
    private static final String FTP_USERNAME = "darkprod";
    private static final String FTP_URI = "ftp.darkprod.altervista.org";
    private static final String FULLURI_LOCAL_TODO = Environment.getExternalStorageDirectory().getPath() + File.separator + "SyncMyTodo" + File.separator + "ToDo.txt";
    private static final String FOLDER_LOCAL_TODO = Environment.getExternalStorageDirectory().getPath() + File.separator + "SyncMyTodo";
    private FTPServeur ftpServeur;

    public TodoManager() {
        ftpServeur = new FTPServeur.FTPServeurBuilder()
                .setFTPPassword(FTP_PASSWORD)
                .setFTPUsername(FTP_USERNAME)
                .setFTPServeurURI(FTP_URI)
                .build();
    }

    public void getAndSaveTodo() {
        FTPServeur.DownloadFilesTask downloadFilesTask = new FTPServeur.DownloadFilesTask(ftpServeur, FULLURI_LOCAL_TODO);
        downloadFilesTask.execute();

    }
}

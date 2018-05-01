package com.valerio.kevin.syncmytodo;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class FTPServeur {

    private String ftpUsername;
    private String ftpPassword;
    private String ftpServerURI;
    private String TAG = "FTPServeur.java";
    private String ftpTodoPATH;

    FTPServeur(FTPServeurBuilder ftpServeurBuilder) {
        this.ftpUsername = ftpServeurBuilder.ftpUsername;
        this.ftpPassword = ftpServeurBuilder.ftpPassword;
        this.ftpServerURI = ftpServeurBuilder.ftpServerURI;
        this.ftpTodoPATH = ftpServeurBuilder.ftpTodoPATH;
    }

    public static class DownloadFilesTask extends AsyncTask<Void, Integer, Boolean> {

        /*
        @description Télécharge remoteFile et le met dans folderDestination + "TodoList.txt"
         */
        private FTPServeur FTPServeur;
        private String folderDestination;

        public DownloadFilesTask(FTPServeur ftpServeur, String folderDestination) {
            this.FTPServeur = ftpServeur;
            this.folderDestination = folderDestination;

        }

        void downloadTodo(String folderDestination) {

            FTPClient ftpClient = new FTPClient();
            try {

                ftpClient.connect(FTPServeur.ftpServerURI, 21);
                ftpClient.login(FTPServeur.ftpUsername, FTPServeur.ftpPassword);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                File downloadFile1 = new File(folderDestination);
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile1));
                boolean success = ftpClient.retrieveFile(FTPServeur.ftpTodoPATH, outputStream);
                outputStream.close();

                if (success) System.out.println(FTPServeur.ftpTodoPATH + " téléchargé !");
                else System.out.println(FTPServeur.ftpTodoPATH + " NON téléchargé !");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void tryCreateDirectory(String directory) {

            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), directory);
            if (!file.mkdirs()) {
                Log.e("Directory TAG", "Directory not created");
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            System.out.println("DownloadFilesTask" + " Destination : " + folderDestination);
            tryCreateDirectory(folderDestination);
            downloadTodo(folderDestination.replace("ToDo.txt", ""));
            return true;
        }
    }


    public static class FTPServeurBuilder {
        private String ftpUsername;
        private String ftpPassword;
        private String ftpServerURI;
        private String ftpTodoPATH;

        public FTPServeurBuilder setFTPUsername(String ftpUsername) {
            this.ftpUsername = ftpUsername;
            return this;
        }


        public FTPServeurBuilder setFTPPassword(String ftpPassword) {
            this.ftpPassword = ftpPassword;
            return this;
        }

        public FTPServeurBuilder setFTPServeurURI(String ftpServerURI) {
            this.ftpServerURI = ftpServerURI;
            return this;
        }

        public FTPServeur build() {
            return new FTPServeur(this);
        }
    }
}

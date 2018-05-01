package com.valerio.kevin.syncmytodo;

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
    private String ftpTodoPATH;

    FTPServeur(FTPServeurBuilder ftpServeurBuilder) {
        ftpServeurBuilder.ftpUsername = ftpUsername;
        ftpServeurBuilder.ftpPassword = ftpPassword;
        ftpServeurBuilder.ftpServerURI = ftpServerURI;
        ftpServeurBuilder.ftpTodoPATH = ftpTodoPATH;
    }

    /*
    @description Télécharge remoteFile et le met dans folderDestination + "TodoList.txt"
     */
    public void downloadTodo(String folderDestination) {

        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftpServerURI, 22);
            ftpClient.login(ftpUsername, ftpPassword);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            File downloadFile1 = new File(folderDestination);
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile1));
            boolean success = ftpClient.retrieveFile(ftpTodoPATH, outputStream);
            outputStream.close();

            if (success) System.out.println(ftpTodoPATH + " téléchargé !");
            else System.out.println(ftpTodoPATH + " NON téléchargé !");

        } catch (IOException e) {
            e.printStackTrace();
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

        public FTPServeurBuilder setFTPTodoPath(String ftpTodoPATH) {
            this.ftpTodoPATH = ftpTodoPATH;
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

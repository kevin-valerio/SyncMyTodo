package com.valerio.kevin.syncmytodo;

public class TodoManager {

    private static final String FTP_PASSWORD = "valerio83";
    private static final String FTP_USERNAME = "darkprod";
    private static final String FTP_URI = "ftp://ftp.darkprod.altervista.org/TodoList.txt";
    private static final String FOLDER_LOCAL_TODO = "downloaded/TodoList.txt";
    private FTPServeur ftpServeur;

    public TodoManager() {
        ftpServeur = new FTPServeur.FTPServeurBuilder()
                .setFTPPassword(FTP_PASSWORD)
                .setFTPUsername(FTP_USERNAME)
                .setFTPServeurURI(FTP_URI)
                .build();
    }

    public void getAndSaveTodo() {
        ftpServeur.downloadTodo(FOLDER_LOCAL_TODO);
    }
}

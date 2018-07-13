package com.valerio.kevin.syncmytodo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import it.sauronsoftware.ftp4j.FTPClient;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //"ftp.darkprod.altervista.org"; "darkprod"; "http://darkprod.altervista.org/TodoList.txt";
    public static String FTP_HOST = "";
    public static String FTP_USER = "";
    public static String FTP_PASS = "";
    public static String TODO_URL = "";
    public static Boolean SHOWNOTIF = true;
    public static Boolean DOWNLOAD_ON_STARTUP = true;
    private TextView todoTextView = null;
    private Button uploadBtn = null;
    private FloatingActionButton fab = null;

    public static String TODONAME_ONSERVER = "TodoList.txt";
    public static String TAG = "MainActivity";
    public static String PATH_WITHOUTNAME = Environment.DIRECTORY_DOWNLOADS + File.separator + "SyncMyTodo";
    public static String TODO_NAME = "SyncMyTodo-TODO.txt";

    /*
     * TODO : Sur PC : récupérer le Todo sur ftp  tout le temps
     * */

    public void uploadFile(File fileName) throws UnknownHostException {

        FTPClient client = new FTPClient();

        try {
            client.connect(FTP_HOST, 21);
            client.login(FTP_USER, FTP_PASS);
            client.setType(FTPClient.TYPE_TEXTUAL);
            client.changeDirectory("/");
            client.upload(fileName, new MyTransferListener());

        } catch (UnknownHostException e) {

            throw new UnknownHostException();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.disconnect(true);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private void createSyncMyTodoDirectory() {
        File yourAppDir = new File(PATH_WITHOUTNAME);
        if (!yourAppDir.exists() && !yourAppDir.isDirectory()) {
            if (yourAppDir.mkdirs()) {
                Log.i("CreateDir", "App dir created");
            } else {
                Log.w("CreateDir", "Unable to create app dir!");
            }
        } else {
            Log.i("CreateDir", "App dir already exists");
        }
    }

    @SuppressLint("CheckResult")
    private void askForPermissions() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.setLogging(true);
        fab = findViewById(R.id.fab);
        uploadBtn = findViewById(R.id.upload);

        rxPermissions.requestEach(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET)
                .subscribe(permission -> {
                    if (permission.granted) {
                        doOnPermissionsGranted();
                        Toast.makeText(MainActivity.this, "Permissions autorisées, yay !", Toast.LENGTH_SHORT).show();
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        Toast.makeText(MainActivity.this, "Permissions refusée. Moche...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Permissions refusées, SyncMyTodo ne fonctionnera pas bien... :c", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void doOnPermissionsGranted() {
        fab.setOnClickListener(view -> downloadFiles());
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadSettings();
        checkIfAreSettingsNotEmpty();
        askForPermissions();

        todoTextView = findViewById(R.id.todo_text);
        createSyncMyTodoDirectory();
        //Paramètres
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Ouverture du menu
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Clic sur les items du menu
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //On init le to-do et tout
        if (DOWNLOAD_ON_STARTUP) {
            downloadFiles();
        }
        setTodoAsViewableOnly();

        uploadBtn.setOnClickListener(view -> {
            writeToFile(String.valueOf(todoTextView.getText()), getApplicationContext(), Environment.getExternalStoragePublicDirectory(PATH_WITHOUTNAME) + "/" + TODONAME_ONSERVER);
            try {
                int error = new TodoUploader().execute(1, 1, 1).get();
                if (error == 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder
                            .setCancelable(true)
                            .setMessage("Vos logs FTP semblent erronnés, l'host n'est pas reconnu...")
                            .setPositiveButton("Vérifier mes parametres", (dialogInterface, i) -> {
                                Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                                startActivity(settings);
                            })
                            .setTitle("Impossible de synchroniser le todo");

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        });

    }

    private void checkIfAreSettingsNotEmpty() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Aucun paramètre FTP trouvé !")
                .setMessage("Vous devez remplir les logs du serveur où se trouve votre To Do")
                .setPositiveButton("Aller aux parammetres", (dialogInterface, i) -> {
                    Intent settings = new Intent(this, SettingsActivity.class);
                    startActivity(settings);
                })
                .setNegativeButton("Rester ici", (dialogInterface, i) -> Toast.makeText(getApplicationContext(), "N'oubliez pas de remplir vos logs...", Toast.LENGTH_LONG).show());

        AlertDialog.Builder alertURL = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Aucun lien de Todotrouvé !")
                .setMessage("Vous devez remplir le lien de votre Todo")
                .setPositiveButton("Aller aux parammetres", (dialogInterface, i) -> {
                    Intent settings = new Intent(this, SettingsActivity.class);
                    startActivity(settings);
                })
                .setNegativeButton("Rester ici", (dialogInterface, i) -> Toast.makeText(getApplicationContext(), "N'oubliez pas de remplir vos logs...", Toast.LENGTH_LONG).show());


        if (FTP_PASS.equals("") || FTP_HOST.equals("") || FTP_USER.equals(""))
            alertDialog.show();

        if (TODO_URL.equals("")) {
            alertURL.show();
        }
    }

    private void loadSettings() {

        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        SHOWNOTIF = preferences.getBoolean(SettingsActivity.NOTIF, true);
        DOWNLOAD_ON_STARTUP = preferences.getBoolean(SettingsActivity.AUTODOWNLOAD, true);
        FTP_PASS = preferences.getString(SettingsActivity.USER, "");
        FTP_HOST = preferences.getString(SettingsActivity.PASS, "");
        FTP_USER = preferences.getString(SettingsActivity.SERVER, "");
        TODO_URL = preferences.getString(SettingsActivity.TUDOURL, "");
    }

    private void writeToFile(String data, Context context, String path) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(new File(path)));
            outputStreamWriter.write(data);
            outputStreamWriter.close();

        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public boolean downloadFiles() {

        try {
            String url = TODO_URL;
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            removePreviousTodoFile(Environment.getExternalStoragePublicDirectory(PATH_WITHOUTNAME));

            request.setDescription("SyncMyTodo is downloading your todo now !");
            request.setTitle("SyncMyTodo - Synchronisisation (" + TODO_URL.replace("/" + TODONAME_ONSERVER, "") + ").");
            request.allowScanningByMediaScanner();

            if (SHOWNOTIF) {
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            } else {
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            }

            request.setDestinationInExternalPublicDir(PATH_WITHOUTNAME, TODO_NAME);
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
            }
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                @SuppressLint("SimpleDateFormat")
                @RequiresApi(api = Build.VERSION_CODES.O)
                public void onReceive(Context ctxt, Intent intent) {
                    Toast.makeText(MainActivity.this, "Synchronisation réussie avec succès !", Toast.LENGTH_SHORT).show();
                    TextView dateLastSync = findViewById(R.id.date_last_sync);
                    dateLastSync.setText(new SimpleDateFormat("dd/mm/yyyy HH:mm:ss").format(new Date()).replace(" ", " à "));
                    showTodo();
                }
            };
            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            getApplicationContext().registerReceiver(onComplete,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    void removePreviousTodoFile(File fileOrDirectory) {
        try {

            if (fileOrDirectory.isDirectory())
                for (File child : fileOrDirectory.listFiles())
                    removePreviousTodoFile(child);

            fileOrDirectory.delete();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Impossible de supprimer le dossier", Toast.LENGTH_SHORT).show();
            Log.e(TAG, e.getMessage());
        }
    }

    private void showTodo() {
        SpannableString spannableStr = new SpannableString(getTodo());
        spannableStr.setSpan(new StyleSpan(Typeface.BOLD), 0, 10, 0);
        todoTextView.setText(getTodo());
    }

    private String getTodo() {

        File file = new File(Environment.getExternalStoragePublicDirectory(PATH_WITHOUTNAME) + "/" + TODO_NAME);
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append("\n\r");
            }
            br.close();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Hein ? Impossible de récupérer le todo ... Mauvais serveur ?", Toast.LENGTH_SHORT).show();
            Log.d(TAG, e.getMessage());
        }

        return text.toString();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressLint("ResourceAsColor")
    private void setTodoAsViewableOnly() {
        todoTextView.setTextColor(R.color.colorAccent);
        todoTextView.setShowSoftInputOnFocus(false);
        Toast.makeText(MainActivity.this, "Vous pouvez voir en read-only votre todo!", Toast.LENGTH_SHORT).show();

    }

    private void setTodoAsEditable() {

        todoTextView.setTextColor(Color.BLACK);
        todoTextView.setShowSoftInputOnFocus(true);
        Toast.makeText(MainActivity.this, "Vous pouvez éditer votre todo!", Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
        } else if (id == R.id.action_delete_todo) {
            removePreviousTodoFile(Environment.getExternalStoragePublicDirectory(PATH_WITHOUTNAME));
            Toast.makeText(MainActivity.this, "Todos supprimés !", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.action_about) {
            showAboutPopup();

        }

        return super.onOptionsItemSelected(item);
    }

    private void showAboutPopup() {
        final TextView message = new TextView(getApplicationContext());
        final SpannableString s = new SpannableString("Cette application a été crée le 12 et 13 Juillet 2018 par une journée d'ennui par Kevin VALERIO. Vous trouverez surement le code source de l'application" +
                "sur mon Github : github.com/kevin-valerio");
        Linkify.addLinks(s, Linkify.WEB_URLS);
        message.setText(s);
        message.setMovementMethod(LinkMovementMethod.getInstance());
        message.setFontFeatureSettings("'wdth' 150");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setCancelable(true)
                .setView(message)
                .setPositiveButton("Thanks Kevin!", null)
                .setTitle("A propos de");

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.edit_todo) {
            setTodoAsEditable();
            uploadBtn.setVisibility(View.VISIBLE);

        } else if (id == R.id.see_todo) {
            uploadBtn.setVisibility(View.INVISIBLE);
            setTodoAsViewableOnly();
        } else if (id == R.id.share_todo) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, String.valueOf(todoTextView.getText()));
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    public class TodoUploader extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... arg) {
            File file = new File(Environment.getExternalStoragePublicDirectory(PATH_WITHOUTNAME) + "/" + TODONAME_ONSERVER);
            try {
                uploadFile(file);
            } catch (UnknownHostException e) {
                return 1;
            }
            Toast.makeText(MainActivity.this, "Todo mit en ligne avec succès !", Toast.LENGTH_SHORT).show();
            return 0;
        }
    }
}


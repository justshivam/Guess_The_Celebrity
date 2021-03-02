package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private String[] names;
    private String[] links;
    private String name;
    private byte id;

    public class GetLinks extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            URL url;
            StringBuilder builder = new StringBuilder();
            try {
                url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while(data != -1){
                    char current = (char) data;
                    builder.append(current);
                    data = reader.read();
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error");
            }
            return builder.toString();
        }
    }

    public class GetImage extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }

    public void buttonAction(View v){
        byte check = Byte.parseByte(v.getTag().toString());
        buttonActionLogic(check == id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View layout = findViewById(R.id.layout);
        layout.setVisibility(View.INVISIBLE);
        TextView view = findViewById(R.id.loading);
        view.setVisibility(View.VISIBLE);
        getList();
        setContent();
        view.setVisibility(View.INVISIBLE);
        layout.setVisibility(View.VISIBLE);
    }

    private void buttonActionLogic(boolean isCorrect){
        TextView view = findViewById(R.id.loading);
        if(isCorrect)
            view.setText("Correct!!");
        else
            view.setText("Wrong!!");
        View layout = findViewById(R.id.layout);
        layout.setVisibility(View.INVISIBLE);
        view.setVisibility(View.VISIBLE);
        new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                view.setVisibility(View.INVISIBLE);
                setContent();
                layout.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void getList(){
        String link = "https://www.imdb.com/list/ls052283250/";
        GetLinks getLinks = new GetLinks();
        String html = null;
        try {
            html = getLinks.execute(link).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<String> elements = breakIntoImageElements(html);
        names = elementsToArrays(elements, true);
        links = elementsToArrays(elements, false);
    }

    private String[] elementsToArrays(ArrayList<String> l, boolean getName){
        String[] result = new String[l.size()/2];
        int toggle = (getName ? 1 : 0);
        for (int i = 0; i < (l.size()/2); i++) {
            result[i] = l.get((i*2) + toggle);
        }
        return result;
    }

    private ArrayList<String> breakIntoImageElements(String html){
        ArrayList<String> list = new ArrayList<>();
        String beforeElement = "<div class=\"lister-item mode-detail\">";
        String[] split = html.split(beforeElement);
        for (int i = 1; i < split.length; i++) {
            String string = split[i];
            Pattern p = Pattern.compile("src=\"" + "(.*?)\"");
            Matcher matcher = p.matcher(string);
            while(matcher.find())
                list.add(matcher.group(1));
            p = Pattern.compile("img alt=\"" + "(.*?)\"");
            matcher = p.matcher(string);
            while(matcher.find())
                list.add(matcher.group(1));
        }

        return list;
    }

    private void setContent(){
        int num = genNum100();
        name = names[num];
        String link = links[num];
        Bitmap bitmap = null;
        GetImage getImage = new GetImage();
        try {
            bitmap = getImage.execute(link).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ImageView view = findViewById(R.id.image);
        view.setImageBitmap(bitmap);
        randomiseOptions();
    }

    private void randomiseOptions(){
        Button[] buttons = new Button[]
                {findViewById(R.id.button1),
                        findViewById(R.id.button2),
                        findViewById(R.id.button3),
                        findViewById(R.id.button4)};
        for(int i=0; i<4; i++)
            buttons[i].setText(names[genNum100()]);
        id = genNum4();
        buttons[id].setText(name);
    }

    private byte genNum4(){
        return (byte) (Math.random() * 4);
    }

    private short genNum100(){
        return (short) (Math.random() * 100);
    }
}
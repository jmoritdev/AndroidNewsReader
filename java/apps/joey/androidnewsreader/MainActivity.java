package apps.joey.androidnewsreader;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static final String URL_IDS = "https://hacker-news.firebaseio.com/v0/topstories.json";
    static final String URL_ART_START = "https://hacker-news.firebaseio.com/v0/item/";
    static final String URL_ART_END = ".json";

    ListView listview;
    TextView textview;
    ImageView imageView;

    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> titleList;
    ArrayList<String> urlList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listview = (ListView) findViewById(R.id.listView);
        textview = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setVisibility(View.INVISIBLE);

        titleList = new ArrayList<String>();
        urlList = new ArrayList<String>();

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titleList);
        listview.setAdapter(arrayAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlList.get(position)));
                startActivity(browserIntent);
            }
        });

        new DownloadTask().execute();
    }

    class DownloadTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String resArray = "";
            HttpURLConnection connection = null;
            InputStream in = null;
            InputStreamReader reader = null;

            try {
                URL urlIds = new URL(URL_IDS);
                connection = (HttpURLConnection) urlIds.openConnection();
                in = connection.getInputStream();
                reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1){
                    char c = (char) data;
                    resArray += c;
                    data = reader.read();
                }

                JSONArray jsonArray = new JSONArray(resArray);
                int index = 0;
                for(int x = 0; x < jsonArray.length(); x++){
                    String id = jsonArray.getString(x);
                    URL articleUrl = new URL(URL_ART_START + id + URL_ART_END);
                    connection = (HttpURLConnection) articleUrl.openConnection();
                    in = connection.getInputStream();
                    reader = new InputStreamReader(in);
                    String jsonObjectString = "";
                    data = reader.read();
                    while (data != -1){
                        char c = (char) data;
                        jsonObjectString += c;
                        data = reader.read();
                    }
                    JSONObject jsonObject = new JSONObject(jsonObjectString);

                    if(jsonObject.has("title") && jsonObject.has("url")){
                        titleList.add(jsonObject.getString("title"));
                        urlList.add(jsonObject.getString("url"));
                        publishProgress("Al " + index + " artikels gedownload.");
                        index++;
                    }
                }

            } catch (Exception e){
                e.printStackTrace();
            } finally {
                if(connection != null){
                    connection.disconnect();
                }
                if(in != null){
                    try{
                        in.close();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
                if(reader != null){
                    try{
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            String update = values[0];
            textview.setText(update);
            textview.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            arrayAdapter.notifyDataSetChanged();
            textview.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    public void refresh(View view){
        imageView.setVisibility(View.INVISIBLE);
        titleList.clear();
        urlList.clear();
        arrayAdapter.notifyDataSetChanged();
        new DownloadTask().execute();
    }
}

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

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final String URL_ART_START = "https://hacker-news.firebaseio.com/v0/item/";

    ArrayList<String> urlList;

    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get ListView object from xml
        final ListView listView = (ListView) findViewById(R.id.listView);

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // Use Firebase to populate the list.
        Firebase.setAndroidContext(this);
        //https://burning-inferno-4491.firebaseio.com/todoItems

        final List idList = new ArrayList<String>();

        new Firebase("https://hacker-news.firebaseio.com/v0/newstories")
                .addChildEventListener(new ChildEventListener() {
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        idList.add((long) dataSnapshot.getValue());
                        //update list()
                    }

                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        adapter.remove((String) dataSnapshot.getValue());
                    }

                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        initData((long) dataSnapshot.getValue());
                    }

                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }

                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });

        urlList = new ArrayList<String>();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlList.get(position)));
                startActivity(browserIntent);
            }
        });

    }

    //update list??
    public void initData(long articleId){
        new Firebase(URL_ART_START + articleId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.add((String) dataSnapshot.child("title").getValue());
                urlList.add((String) dataSnapshot.child("url").getValue());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}

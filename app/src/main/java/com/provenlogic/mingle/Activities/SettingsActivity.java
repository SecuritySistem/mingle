package com.provenlogic.mingle.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.provenlogic.mingle.Adapters.MySettingListAdapter;
import com.provenlogic.mingle.R;

public class SettingsActivity extends AppCompatActivity {

    private RecyclerView setting_list;
    private MySettingListAdapter adapter;
   // private String[] titles = {"Basic info", "Account", "Account Preferences", "Help Centre", "About"};
   private String[] titles = {"Basic info", "Account"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setting_list = (RecyclerView) findViewById(R.id.setting_list);
        adapter = new MySettingListAdapter(titles, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        setting_list.setLayoutManager(linearLayoutManager);
        setting_list.setItemAnimator(new DefaultItemAnimator());
        setting_list.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}

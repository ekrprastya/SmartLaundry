package com.example.smartlaundry.activity.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.example.smartlaundry.R;
import com.example.smartlaundry.activity.user.DetailUser;
import com.example.smartlaundry.models.DataLaundry;
import com.example.smartlaundry.adapter.LaundryAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import maes.tech.intentanim.CustomIntent;

import static com.example.smartlaundry.utils.Path.ByOrder;
import static com.example.smartlaundry.utils.Path.DATA;
import static com.example.smartlaundry.utils.Path.LAUNDRY;

public class ListOrderAdmin extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SearchView searchView;
    private DatabaseReference rootRef;
    private ArrayList<DataLaundry> dataLaundries;
    private LaundryAdapter laundryAdapter;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_order_admin);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        searchView = findViewById(R.id.searchAbsen);
        avLoadingIndicatorView = findViewById(R.id.avOrderadmin);
        rootRef = FirebaseDatabase.getInstance().getReference(LAUNDRY).child(ByOrder);
        rootRef.keepSynced(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.VERTICAL);
        manager.setStackFromEnd(true);
        manager.setReverseLayout(true);
        recyclerView = findViewById(R.id.rcvorderadmin);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(manager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        avLoadingIndicatorView.smoothToShow();
        if (rootRef!=null){
            rootRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        dataLaundries = new ArrayList<>();
                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                            dataLaundries.add(ds.getValue(DataLaundry.class));
                        }
                        laundryAdapter = new LaundryAdapter(ListOrderAdmin.this,dataLaundries);
                        recyclerView.setAdapter(laundryAdapter);
                        laundryAdapter.notifyDataSetChanged();
                        avLoadingIndicatorView.smoothToHide();
                        laundryAdapter.setOnItemClickListener(position -> {
                            String metode ="Order";
                            Intent intent = new Intent(getApplicationContext(), DetailAdmin.class);
                            intent.putExtra(DATA,dataLaundries.get(position));
                            intent.putExtra("metode",metode);
                            startActivity(intent);
                        });
                    } else {
                        avLoadingIndicatorView.smoothToHide();
                        Toasty.info(getApplicationContext(),"Data Tidak Ditemukan",Toasty.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toasty.error(getApplicationContext(),""+databaseError.getMessage(),Toasty.LENGTH_LONG).show();
                }
            });
        }

        if (searchView!=null){
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    cari(newText);
                    return true;
                }
            });
        }
    }

    private void cari(String newText) {
        ArrayList<DataLaundry> searchList = new ArrayList<>();
        if (dataLaundries!=null){
            for (DataLaundry data : dataLaundries){
                if (data.getTanggal().toLowerCase().contains(newText.toLowerCase())){
                    searchList.add(data);
                }
            }
            laundryAdapter = new LaundryAdapter(ListOrderAdmin.this,searchList);
            recyclerView.setAdapter(laundryAdapter);
            laundryAdapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);
            laundryAdapter.setOnItemClickListener(position -> {
                Intent intent = new Intent(ListOrderAdmin.this,DetailAdmin.class);
                intent.putExtra(DATA,searchList.get(position));
                startActivity(intent);
            });
        }
        else {
            Toasty.info(getApplicationContext(),"Data Tidak Ditemukan",Toasty.LENGTH_LONG).show();
        }

    }

    @Override
    public void onBackPressed() {
        Intent intentHome = new Intent(ListOrderAdmin.this, HomeAdmin.class);
        startActivity(intentHome);
        finishAffinity();
        finish();
        CustomIntent.customType(this,"left-to-right");
    }
}

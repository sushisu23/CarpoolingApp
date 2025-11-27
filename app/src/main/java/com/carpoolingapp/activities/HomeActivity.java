package com.carpoolingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.carpoolingapp.R;
import com.carpoolingapp.adapters.BookingAdapter;
import com.carpoolingapp.models.Booking;
import com.carpoolingapp.utils.FirebaseHelper;
import com.carpoolingapp.utils.SharedPrefsHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private TextView userNameText, sectionTitle;
    private MaterialButton riderModeButton, driverModeButton;
    private RecyclerView recyclerView;
    private View emptyState;
    private BottomNavigationView bottomNav;

    private FirebaseHelper firebaseHelper;
    private SharedPrefsHelper prefsHelper;
    private BookingAdapter adapter;
    private List<Booking> bookingList;

    private boolean isRiderMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        initFirebase();
        setupListeners();
        setupBottomNav();
        loadUserData();
        setupRecyclerView();
        loadBookings();
    }

    private void initViews() {
        userNameText = findViewById(R.id.userNameText);
        sectionTitle = findViewById(R.id.sectionTitle);
        riderModeButton = findViewById(R.id.riderModeButton);
        driverModeButton = findViewById(R.id.driverModeButton);
        recyclerView = findViewById(R.id.recyclerView);
        emptyState = findViewById(R.id.emptyState);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void initFirebase() {
        firebaseHelper = FirebaseHelper.getInstance();
        prefsHelper = new SharedPrefsHelper(this);
    }

    private void setupListeners() {
        riderModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToRiderMode();
            }
        });

        driverModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToDriverMode();
            }
        });

        findViewById(R.id.searchCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Search feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.profileImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            }
        });
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_create) {
                startActivity(new Intent(HomeActivity.this, CreateRideActivity.class));
                return true;
            } else if (itemId == R.id.nav_messages) {
                startActivity(new Intent(HomeActivity.this, MessagesActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadUserData() {
        String userName = prefsHelper.getUserName();
        userNameText.setText(userName);
    }

    private void setupRecyclerView() {
        bookingList = new ArrayList<>();
        adapter = new BookingAdapter(this, bookingList, new BookingAdapter.OnBookingClickListener() {
            @Override
            public void onBookingClick(Booking booking) {
                Toast.makeText(HomeActivity.this, "Booking details coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void switchToRiderMode() {
        isRiderMode = true;
        updateModeUI();
        loadBookings();
    }

    private void switchToDriverMode() {
        isRiderMode = false;
        updateModeUI();
        loadBookings();
    }

    private void updateModeUI() {
        if (isRiderMode) {
            riderModeButton.setBackgroundTintList(getColorStateList(R.color.status_active));
            riderModeButton.setTextColor(getColor(R.color.white));
            driverModeButton.setBackgroundTintList(null);
            driverModeButton.setTextColor(getColor(R.color.primary_blue));
            sectionTitle.setText(R.string.your_bookings);
        } else {
            driverModeButton.setBackgroundTintList(getColorStateList(R.color.status_active));
            driverModeButton.setTextColor(getColor(R.color.white));
            riderModeButton.setBackgroundTintList(null);
            riderModeButton.setTextColor(getColor(R.color.primary_blue));
            sectionTitle.setText(R.string.your_listings);
        }
    }

    private void loadBookings() {
        String userId = prefsHelper.getUserId();
        if (userId == null) return;

        firebaseHelper.getBookingsRef()
                .orderByChild(isRiderMode ? "riderId" : "driverId")
                .equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        bookingList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Booking booking = snapshot.getValue(Booking.class);
                            if (booking != null) {
                                booking.setBookingId(snapshot.getKey());
                                bookingList.add(booking);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if (bookingList.isEmpty()) {
                            emptyState.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyState.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(HomeActivity.this,
                                "Failed to load bookings",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_home);
    }
}
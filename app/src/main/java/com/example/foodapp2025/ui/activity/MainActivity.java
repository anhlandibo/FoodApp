package com.example.foodapp2025.ui.activity;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.foodapp2025.R;
import com.example.foodapp2025.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the message bubble
        final ImageView messageBubble = findViewById(R.id.messageBubble);

        // Set the initial position of the bubble
        messageBubble.setVisibility(View.VISIBLE);
        messageBubble.setX(0);  // Start from the left edge
        messageBubble.setY(0);  // Start from the top edge

        // Set touch listener for dragging
        messageBubble.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            boolean isDragging = false;  // Track whether we are currently dragging

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Record the difference between the view's position and the touch position
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        isDragging = false;  // Reset dragging state
                        return false; // Let the click event go through

                    case MotionEvent.ACTION_MOVE:
                        // Only handle dragging logic if the user is actually moving the bubble
                        if (!isDragging) {
                            isDragging = true;  // Mark as dragging
                        }

                        // Get new X and Y positions
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Prevent the bubble from going off-screen
                        if (newX < 0) newX = 0;
                        if (newX > screenWidth - view.getWidth()) newX = screenWidth - view.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY > screenHeight - view.getHeight()) newY = screenHeight - view.getHeight();

                        // Move the bubble smoothly
                        view.animate().x(newX).y(newY).setDuration(0).start();
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isDragging) {
                            // Snap to the closest edge after dragging
                            float finalX = event.getRawX() + dX;
                            float finalY = event.getRawY() + dY;

                            // Snap horizontally (left or right)
                            if (finalX < screenWidth / 2) {
                                snapToEdge(view, 0, view.getY());
                            } else {
                                snapToEdge(view, screenWidth - view.getWidth(), view.getY());
                            }

                            // Snap vertically (top or bottom)
                            if (finalY < screenHeight / 2) {
                                snapToEdge(view, view.getX(), 0);
                            } else {
                                snapToEdge(view, view.getX(), screenHeight - view.getHeight());
                            }
                            return true;
                        }
                        // Allow the click event to be triggered if there was no dragging
                        return false;

                    default:
                        return false;
                }
            }
        });


        // Set onClick listener to navigate to the ChatFragment
        messageBubble.setOnClickListener(v -> {
            Log.d("MainActivity", "Message Bubble clicked!");
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

            // Prevent duplicate navigation
            if (!(navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.ChatFragment)) {
                navController.navigate(R.id.ChatFragment);
                setBottomNavigationVisibility(false); // Hide bottom navigation
            }
        });

        // Setup Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = null;
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }

        // Manage Bottom Navigation Visibility on Fragment Change
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.ChatFragment) {
                setBottomNavigationVisibility(false);
            } else {
                setBottomNavigationVisibility(true);
            }
        });
    }

    private void snapToEdge(View view, float x, float y) {
        ValueAnimator animatorX = ValueAnimator.ofFloat(view.getX(), x);
        animatorX.setDuration(300);
        animatorX.addUpdateListener(animation -> view.setX((float) animation.getAnimatedValue()));
        animatorX.start();

        ValueAnimator animatorY = ValueAnimator.ofFloat(view.getY(), y);
        animatorY.setDuration(300);
        animatorY.addUpdateListener(animation -> view.setY((float) animation.getAnimatedValue()));
        animatorY.start();
    }

    public void setBottomNavigationVisibility(boolean isVisible) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}

package com.sadid.myhometutor;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Custom UCropActivity that handles window insets properly
 * to prevent toolbar buttons from overlapping with status bar
 */
public class CustomUCropActivity extends com.yalantis.ucrop.UCropActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge layout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        } else {
            // For older versions, use system UI flags
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
        
        // Make status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }
        
        // Apply window insets to the root layout
        setupWindowInsets();
    }
    
    /**
     * Sets up window insets handling to prevent UI overlap with system bars
     */
    private void setupWindowInsets() {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                
                // Find the toolbar (it's typically the first child in UCrop's layout)
                ViewGroup contentView = (ViewGroup) view;
                if (contentView.getChildCount() > 0) {
                    View childView = contentView.getChildAt(0);
                    if (childView != null) {
                        // Apply top padding to the main content view to push it below status bar
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) childView.getLayoutParams();
                        if (params != null) {
                            params.topMargin = insets.top;
                            childView.setLayoutParams(params);
                        } else {
                            // Fallback: apply padding if margin params not available
                            childView.setPadding(
                                childView.getPaddingLeft(),
                                insets.top,
                                childView.getPaddingRight(),
                                childView.getPaddingBottom()
                            );
                        }
                    }
                }
                
                // Return the insets to allow other views to handle them
                return windowInsets;
            });
            
            // Request to apply insets
            ViewCompat.requestApplyInsets(rootView);
        }
    }
}

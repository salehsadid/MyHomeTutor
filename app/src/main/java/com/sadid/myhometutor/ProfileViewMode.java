package com.sadid.myhometutor;

/**
 * ProfileViewMode - Constants for role-based profile visibility
 * 
 * VIEW_MODE_PUBLIC: 
 *   - Student viewing tutor before connection
 *   - Shows: Name, education, experience, preferences, location
 *   - Hides: Phone, email, verification document
 * 
 * VIEW_MODE_CONNECTED:
 *   - Student viewing tutor after connection established
 *   - Shows: Name, education, phone, email, preferences
 *   - Hides: Verification document
 * 
 * VIEW_MODE_ADMIN:
 *   - Admin viewing any user
 *   - Shows: All fields including verification document
 *   - Has: Approve/Reject buttons
 */
public class ProfileViewMode {
    public static final String VIEW_MODE_PUBLIC = "public";
    public static final String VIEW_MODE_CONNECTED = "connected";
    public static final String VIEW_MODE_ADMIN = "admin";
    
    // Intent extras
    public static final String EXTRA_VIEW_MODE = "view_mode";
    public static final String EXTRA_USER_ID = "userId";
    public static final String EXTRA_USER_TYPE = "userType";
    public static final String EXTRA_CURRENT_USER_ID = "currentUserId";
}

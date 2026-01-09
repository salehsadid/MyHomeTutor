// ========================================
// QUICK REFERENCE: Base64 Image Operations
// ========================================

// 1. PICK IMAGE FROM GALLERY
// ---------------------------
private final ActivityResultLauncher<String> mGetContent = 
    registerForActivityResult(new ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) {
                profileImageUri = uri;
            }
        });

// Launch picker
mGetContent.launch("image/*");


// 2. CONVERT IMAGE URI TO BASE64
// --------------------------------
// Default settings (800px, 75% quality)
String base64 = Base64ImageHelper.convertUriToBase64(context, imageUri);

// Custom settings
String base64 = Base64ImageHelper.convertUriToBase64(context, imageUri, 1200, 80);


// 3. VALIDATE SIZE
// ----------------
if (Base64ImageHelper.isBase64SizeValid(base64String, 1500)) {
    // OK to upload (under 1.5MB)
} else {
    // Too large
}


// 4. UPLOAD TO FIRESTORE
// ----------------------
Map<String, Object> data = new HashMap<>();
data.put("profileImageBase64", base64String);

db.collection("users").document(userId)
    .update(data)
    .addOnSuccessListener(aVoid -> {
        Toast.makeText(this, "Uploaded!", Toast.LENGTH_SHORT).show();
    });


// 5. UPLOAD TO REALTIME DATABASE
// -------------------------------
DatabaseReference ref = FirebaseDatabase.getInstance()
    .getReference("users").child(userId);
    
ref.child("profileImageBase64").setValue(base64String);


// 6. FETCH FROM FIRESTORE
// ------------------------
db.collection("users").document(userId).get()
    .addOnSuccessListener(doc -> {
        String base64 = doc.getString("profileImageBase64");
        if (base64 != null) {
            Base64ImageHelper.loadBase64IntoImageView(imageView, base64, R.drawable.placeholder);
        }
    });


// 7. FETCH FROM REALTIME DATABASE
// --------------------------------
DatabaseReference ref = FirebaseDatabase.getInstance()
    .getReference("users").child(userId);
    
ref.child("profileImageBase64").addListenerForSingleValueEvent(new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot snapshot) {
        String base64 = snapshot.getValue(String.class);
        if (base64 != null) {
            Base64ImageHelper.loadBase64IntoImageView(imageView, base64, R.drawable.placeholder);
        }
    }
    
    @Override
    public void onCancelled(DatabaseError error) { }
});


// 8. DECODE TO BITMAP
// --------------------
Bitmap bitmap = Base64ImageHelper.convertBase64ToBitmap(base64String);
if (bitmap != null) {
    imageView.setImageBitmap(bitmap);
}


// 9. DISPLAY IN IMAGEVIEW (SIMPLE)
// ---------------------------------
Base64ImageHelper.loadBase64IntoImageView(
    imageView, 
    base64String, 
    R.drawable.placeholder
);


// 10. DISPLAY IN IMAGEVIEW (WITH GLIDE)
// --------------------------------------
Base64ImageHelper.loadBase64IntoImageViewWithGlide(
    context,
    imageView,
    base64String,
    R.drawable.placeholder
);


// COMPLETE UPLOAD EXAMPLE
// ------------------------
private void uploadImage() {
    String base64 = Base64ImageHelper.convertUriToBase64(this, imageUri, 800, 75);
    
    if (base64 == null) {
        Toast.makeText(this, "Conversion failed", Toast.LENGTH_SHORT).show();
        return;
    }
    
    if (!Base64ImageHelper.isBase64SizeValid(base64, 1500)) {
        Toast.makeText(this, "Image too large", Toast.LENGTH_SHORT).show();
        return;
    }
    
    Map<String, Object> data = new HashMap<>();
    data.put("profileImageBase64", base64);
    
    db.collection("users").document(userId)
        .update(data)
        .addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Uploaded successfully!", Toast.LENGTH_SHORT).show();
            Base64ImageHelper.loadBase64IntoImageViewWithGlide(this, imageView, base64, R.drawable.placeholder);
        })
        .addOnFailureListener(e -> {
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
}


// COMPLETE FETCH & DISPLAY EXAMPLE
// ---------------------------------
private void loadProfileImage() {
    db.collection("users").document(userId).get()
        .addOnSuccessListener(document -> {
            if (document.exists()) {
                String base64 = document.getString("profileImageBase64");
                String url = document.getString("profileImageUrl");
                
                if (base64 != null && !base64.isEmpty()) {
                    // Load from Base64 (new method)
                    Base64ImageHelper.loadBase64IntoImageViewWithGlide(
                        this, ivProfile, base64, R.drawable.placeholder);
                } else if (url != null && !url.isEmpty()) {
                    // Fallback to URL (backward compatibility)
                    Glide.with(this).load(url).placeholder(R.drawable.placeholder).into(ivProfile);
                }
            }
        });
}

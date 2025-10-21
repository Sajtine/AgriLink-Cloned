    package com.example.loginappclone;

    import android.app.Activity;
    import android.content.ContentValues;
    import android.content.Context;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.database.Cursor;
    import android.database.sqlite.SQLiteDatabase;
    import android.database.sqlite.SQLiteOpenHelper;
    import android.util.Log;
    import android.widget.Toast;

    import androidx.annotation.NonNull;

    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.Map;


    public class MyDatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "AgriLink.db";
        private static final int DATABASE_VERSION = 23;

        // Users Table
        private static final String TABLE_NAME = "users";
        private static final String COLUMN_ID = "id";
        private static final String COLUMN_USERNAME = "username";
        private static final String COLUMN_EMAIL = "email";
        private static final String COLUMN_USER_TYPE = "user_type";
        private static final String COLUMN_PHONE_NUMBER = "phone_number";
        private static final String COLUMN_ADDRESS = "address";
        private static final String COLUMN_PASSWORD = "password";

        // Markets Table
        private static final String TABLE_MARKETS = "markets";
        private static final String COLUMN_MARKET_ID = "id";
        private static final String COLUMN_MARKET_NAME = "name";
        private static final String COLUMN_MARKET_STREET = "street";
        private static final String COLUMN_MARKET_BARANGGAY = "barangay";
        private static final String COLUMN_MARKET_PHONE = "phone";
        private static final String COLUMN_MARKET_VENDOR = "vendor";
        private static final String COLUMN_MARKET_VENDOR_ID = "vendor_id";
        private static final String COLUMN_MARKET_MUNICIPALITY = "municipality";
        private static final String COLUMN_MARKET_LATITUDE = "latitude";
        private static final String COLUMN_MARKET_LONGITUDE = "longitude";

        // Products Offer Table
        private static final String TABLE_PRODUCT_OFFERS = "product_offers";
        private static final String COLUMN_PRODUCT_OFFER_ID = "id";
        private static final String COLUMN_VENDOR_ID = "vendor_id";
        private static final String COLUMN_FARMER_ID = "farmer_id";
        private static final String COLUMN_FARMER_NAME = "farmer_name";
        private static final String COLUMN_FARMER_NUMBER = "farmer_number";
        private static final String COLUMN_PRODUCT_NAME = "product_name";
        private static final String COLUMN_PRODUCT_QUANTITY = "quantity";
        private static final String COLUMN_PRICE = "price";
        private static final String COLUMN_DELIVERY_DATE = "delivery_date";
        private static final String COLUMN_STATUS = "status";
        private static final String COLUMN_REQUEST_DATE = "request_date";
        private static final String COLUMN_RECEIVED_DATE = "received_date";
        private static final String  COLUMN_PICKUP_OPTION = "pickup_option";
        private static final String COLUMN_PAYMENT_METHOD = "payment_method";

        // Crops Table
        private static final String TABLE_CROPS = "crops";
        private static final String COLUMN_CROP_ID = "id";
        private static final String COLUMN_CROP_NAME = "crop_name";
        private static final String COLUMN_CROP_PLANTING_MONTHS = "planting_months";

        // Vendor products table
        private static final String TABLE_VENDOR_PRODUCTS = "vendor_products";
        private static final String COLUMN_PRODUCT_ID = "product_id";
        private static final String COLUMN_VENDOR_PRODUCT_NAME = "vendor_product_name";
        private static final String COLUMN_VENDOR_PRODUCT_PRICE = "vendor_product_price";
        private static final String COLUMN_PRODUCT_UNIT = "product_unit";
        private static final String COLUMN_VENDOR_PRODUCT_ID = "vendor_product_id";

        private Context context;

        // Create Database
        public MyDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
            db.setForeignKeyConstraintsEnabled(true);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create Users Table Query
            String createUsersTable = "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT, " +
                    COLUMN_EMAIL + " TEXT UNIQUE, " +
                    COLUMN_USER_TYPE + " TEXT, " +
                    COLUMN_PHONE_NUMBER + " TEXT, " +
                    COLUMN_ADDRESS + " TEXT, " +
                    COLUMN_PASSWORD + " TEXT);";

            // Create Markets Table Query
            String createMarketsTable = "CREATE TABLE " + TABLE_MARKETS + " (" +
                    COLUMN_MARKET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MARKET_NAME + " TEXT, " +
                    COLUMN_MARKET_STREET + " TEXT, " +
                    COLUMN_MARKET_BARANGGAY + " TEXT, " +
                    COLUMN_MARKET_PHONE + " TEXT, " +
                    COLUMN_MARKET_VENDOR + " TEXT, " +
                    COLUMN_MARKET_VENDOR_ID + " INTEGER, " +
                    COLUMN_MARKET_MUNICIPALITY + " TEXT, " +
                    COLUMN_MARKET_LATITUDE + " REAL, " +
                    COLUMN_MARKET_LONGITUDE + " REAL, " +
                    "FOREIGN KEY(" + COLUMN_MARKET_VENDOR_ID + ") REFERENCES " + TABLE_NAME + "(" + COLUMN_ID + ") ON DELETE CASCADE);";


            // Create Product Offers Table Query
            String createProductOffersTable = "CREATE TABLE " + TABLE_PRODUCT_OFFERS + " (" +
                    COLUMN_PRODUCT_OFFER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_VENDOR_ID + " INTEGER, " +
                    COLUMN_FARMER_ID + " INTEGER, " +
                    COLUMN_FARMER_NAME + " TEXT, " +
                    COLUMN_FARMER_NUMBER + " TEXT, " +
                    COLUMN_PRODUCT_NAME + " TEXT, " +
                    COLUMN_PRODUCT_QUANTITY + " INTEGER, " +
                    COLUMN_PRICE + " REAL, " +
                    COLUMN_DELIVERY_DATE + " TEXT, " +
                    COLUMN_STATUS + " TEXT DEFAULT 'Pending', " +
                    COLUMN_REQUEST_DATE + " TEXT, " +
                    COLUMN_RECEIVED_DATE + " TEXT, " +
                    COLUMN_PICKUP_OPTION + " TEXT, " +
                    COLUMN_PAYMENT_METHOD + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_VENDOR_ID + ") REFERENCES " + TABLE_NAME + "(" + COLUMN_ID + ") ON DELETE CASCADE);";

            // Create Crops Table Query
            String createCropsTable = "CREATE TABLE " + TABLE_CROPS + " (" +
                    COLUMN_CROP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CROP_NAME + " TEXT, " +
                    COLUMN_CROP_PLANTING_MONTHS + " TEXT);";


            // Create Vendors Product table
            String createVendorProductsTable = "CREATE TABLE " + TABLE_VENDOR_PRODUCTS + " (" +
                    COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_VENDOR_PRODUCT_NAME + " TEXT, " +
                    COLUMN_VENDOR_PRODUCT_PRICE + " REAL, " +
                    COLUMN_PRODUCT_UNIT + " TEXT, " +
                    COLUMN_VENDOR_PRODUCT_ID + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_VENDOR_PRODUCT_ID + ") REFERENCES " + TABLE_NAME + "(" + COLUMN_ID + ") ON DELETE CASCADE);";


            db.execSQL(createUsersTable);
            db.execSQL(createMarketsTable);
            db.execSQL(createProductOffersTable);
            db.execSQL(createCropsTable);
            db.execSQL(createVendorProductsTable);

//            importMarketsFromCSV(db);

            android.util.Log.d("DB_DEBUG", "Database created successfully!");

            // Add temporary market data
//            addTemporaryMarket(db);



            // Add crops data to the table
            db.execSQL("INSERT INTO " + TABLE_CROPS + " (" + COLUMN_CROP_NAME + ", " + COLUMN_CROP_PLANTING_MONTHS + ") VALUES " +
                    "('Rice', '6,7,8,9')," +
                    "('Corn', '5,6,7,8')," +
                    "('Sugarcane', '10,11,12')," +
                    "('Tomato', '11,12,1')," +
                    "('Eggplant', '9,10,11')," +
                    "('Ampalaya', '9,10,11')," +
                    "('Okra', '2,3,4,5')," +
                    "('Onion', '11,12')," +
                    "('Garlic', '11,12')," +
                    "('Peanut', '5,6,7')," +
                    "('Kamote', '5,6,7')," +
                    "('Cassava', '6,7,8')," +
                    "('Mango', '12,1,2')," +
                    "('Banana', '1,2,3,4,5,6,7,8,9,10,11,12')," +
                    "('Coconut', '1,2,3,4,5,6,7,8,9,10,11,12')," +
                    "('Coffee', '10,11,12')," +
                    "('Cabbage', '10,11,12')," +
                    "('Carrot', '11,12,1')," +
                    "('Lettuce', '11,12,1,2')," +
                    "('Chayote', '6,7,8')," +
                    "('Watermelon', '2,3,4')," +
                    "('Papaya', '1,2,3,4,5,6,7,8,9,10,11,12')," +
                    "('Pineapple', '4,5,6')," +
                    "('Calamansi', '1,2,3,4,5,6,7,8,9,10,11,12')," +
                    "('Melon', '2,3,4')," +
                    "('Soybean', '5,6,7')," +
                    "('Chili Pepper (Siling Labuyo)', '11,12,1')," +
                    "('Ginger', '4,5,6')," +
                    "('Turmeric (Luyang Dilaw)', '5,6,7');");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKETS);  // Drop markets table if it exists
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT_OFFERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CROPS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_VENDOR_PRODUCTS);
            onCreate(db);
        }

//        -------------------------------------- Firebase Functions --------------------------------------------

        // Register User with Firebase and save data inside Firebase
        public void registerUser(String username, String phoneNumber, String userRole, String password, Context context) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference rootRef = database.getReference("users");
            String rolePath = userRole.equalsIgnoreCase("farmer") ? "farmers" : "vendors";

            // Format Philippine number
            String formattedNumber;
            if (phoneNumber.startsWith("0")) {
                formattedNumber = "+63" + phoneNumber.substring(1);
            } else if (!phoneNumber.startsWith("+63")) {
                formattedNumber = "+63" + phoneNumber;
            } else {
                formattedNumber = phoneNumber;
            }

            // Generate a unique UID for this user
            String userUID = rootRef.push().getKey();
            if (userUID == null) {
                Toast.makeText(context, "Failed to generate user ID", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if phone already exists in BOTH farmers and vendors
            rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean phoneExists = false;

                    // Check farmers
                    for (DataSnapshot farmerSnap : snapshot.child("farmers").getChildren()) {
                        String existingPhone = farmerSnap.child("phone_number").getValue(String.class);
                        if (formattedNumber.equals(existingPhone)) {
                            phoneExists = true;
                            Toast.makeText(context, "Phone number already registered as Farmer!", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }

                    // Check vendors only if not found in farmers
                    if (!phoneExists) {
                        for (DataSnapshot vendorSnap : snapshot.child("vendors").getChildren()) {
                            String existingPhone = vendorSnap.child("phone_number").getValue(String.class);
                            if (formattedNumber.equals(existingPhone)) {
                                phoneExists = true;
                                Toast.makeText(context, "Phone number already registered as Vendor!", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }

                    // If phone doesn’t exist. Register new user
                    if (!phoneExists) {
                        DatabaseReference userRef = rootRef.child(rolePath).child(userUID);

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("username", username);
                        userData.put("phone_number", formattedNumber);
                        userData.put("userRole", userRole.toLowerCase());
                        userData.put("password", password);

                        userRef.setValue(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(context, MainActivity.class);
                                    context.startActivity(intent);
                                    if (context instanceof Activity) {
                                        ((Activity) context).finish();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }


        // Log in
        public void loginUser(String phoneNumber, String password) {

            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("users");

            String formattedNumber;

            // Format Philippine number
            if (phoneNumber.startsWith("0")) {
                formattedNumber = "+63" + phoneNumber.substring(1);
            } else if (!phoneNumber.startsWith("+63")) {
                formattedNumber = "+63" + phoneNumber;
            } else {
                formattedNumber = phoneNumber;
            }

            // Check in "farmers" first
            rootRef.child("farmers")
                    .orderByChild("phone_number")
                    .equalTo(formattedNumber)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot userSnap : snapshot.getChildren()) {
                                    String uid = userSnap.getKey();
                                    checkPassword(userSnap, password, "farmers", uid);
                                    return;
                                }
                            } else {
                                // Check in "vendors" if not found in farmers
                                rootRef.child("vendors")
                                        .orderByChild("phone_number")
                                        .equalTo(formattedNumber)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                                                        String uid = userSnap.getKey();
                                                        checkPassword(userSnap, password, "vendors", uid);
                                                        return;
                                                    }
                                                } else {
                                                    Toast.makeText(context, "User not Found!", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(context, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Check password
        private void checkPassword(DataSnapshot snapshot, String userPassword, String role, String uid) {
            String password = snapshot.child("password").getValue(String.class);

            if (userPassword.equals(password)) {
                Toast.makeText(context, "Login Successfully!", Toast.LENGTH_SHORT).show();

                // Store user login to shared preferences
                SharedPreferences sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("uid", uid);
                editor.putString("role", role);
                editor.apply();

                if (role.equals("farmers")) {
                    context.startActivity(new android.content.Intent(context, Home.class));
                } else {
                    android.content.Intent intent = new android.content.Intent(context, Vendor.class);
                    context.startActivity(intent);
                    Log.d("Vendor UID", uid);
                }

            } else {
                Toast.makeText(context, "Incorrect Password!", Toast.LENGTH_SHORT).show();
            }
        }




        // Optional SQLite save for offline login
        private void saveToLocalSQLite(String username, String email, String password, String userRole) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, username);
            values.put(COLUMN_EMAIL, email);
            values.put(COLUMN_USER_TYPE, userRole);
            values.put(COLUMN_PASSWORD, password);
            db.insert(TABLE_NAME, null, values);
            db.close();
        }


        // Save locally
        private void saveUserSession(String email, String username, String userRole, Context context) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("email", email);
            editor.putString("username", username);
            editor.putString("role", userRole);
            editor.apply();
        }



        //        -------------------------------------- Firebase Functions --------------------------------------------


        // Check Login
        public String checkUserRole(String email, String password) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + ", " + COLUMN_USERNAME + ", " + COLUMN_USER_TYPE + " FROM " + TABLE_NAME + " WHERE " +
                    COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?", new String[]{email, password});

            String result = null;
            if (cursor.moveToFirst()) {
                int userId = cursor.getInt(0);
                String username = cursor.getString(1);
                String role = cursor.getString(2);
                result = userId + "," + username + "," + role;
            }

            cursor.close();
            db.close();
            return result;
        }

        // Delete User Data
        public void deleteData() {
            SQLiteDatabase db = this.getWritableDatabase();
            int deletedRows = db.delete(TABLE_NAME, null, null);
            db.close();
            android.util.Log.d("DB_DEBUG", "Deleted " + deletedRows + " rows from database.");
        }

        // Get User Details by Email
        public Cursor getUserDetails(String email) {
            SQLiteDatabase db = this.getReadableDatabase();
            return db.rawQuery("SELECT username, email, phone_number, address FROM users WHERE email=?", new String[]{email});
        }

        // Update User Details or Farmer Details
        public boolean updateFarmerDetails(String email, String newName, String newAddress, String newPhone_number){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("username", newName);
            values.put("address", newAddress);
            values.put("phone_number", newPhone_number);

            int rowsAffected = db.update("users", values, "email = ?", new String[]{email});
            return rowsAffected > 0;

        }

        // Get Market Details by Municipality (or any other field)
        public Cursor getMarketDetails(String municipality, String productFilter) {
            SQLiteDatabase db = this.getReadableDatabase();

            if (productFilter.equals("All")){
                return db.rawQuery(
                        "SELECT * FROM markets WHERE municipality = ?",
                        new String[]{municipality}
                );
            }else{
                return db.rawQuery(
                        "SELECT m.* FROM markets m " +
                                "JOIN vendor_products vp ON m.vendor_id = vp.vendor_product_id " +
                                "WHERE m.municipality = ? AND vp.vendor_product_name = ?",
                        new String[]{municipality, productFilter}
                );
            }
        }


        // Method to store request in the db
        public boolean insertRequest(String product_name, int quantity, double price, String delivery_date, int vendor_id, int farmer_id, String farmer_name, String phone_number, String request_date, String pickUpOption, String paymentMethod){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("vendor_id", vendor_id);
            values.put("product_name", product_name);
            values.put("quantity", quantity);
            values.put("price", price);
            values.put("delivery_date", delivery_date);
            values.put("farmer_id", farmer_id);
            values.put("farmer_name", farmer_name);
            values.put("farmer_number", phone_number);
            values.put("request_date", request_date);
            values.put("pickup_option", pickUpOption);
            values.put("payment_method", paymentMethod);

            long result = db.insert(TABLE_PRODUCT_OFFERS, null, values);
            return result != -1;
        }

        // Check if vendor is already in the markets table
        public boolean isVendorInMarkets(String vendorId) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MARKETS + " WHERE " + COLUMN_MARKET_VENDOR_ID + "=?", new String[]{vendorId});
            boolean exists = cursor.moveToFirst();
            cursor.close();
            return exists;
        }

        // Add vendor to markets table
        public void addVendorToMarkets(int vendorId, String vendorName) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(COLUMN_MARKET_VENDOR_ID, vendorId);
            values.put(COLUMN_MARKET_VENDOR, vendorName);

            // Optional: you can set default values for other fields like name/street/phone
            db.insert(TABLE_MARKETS, null, values);
            db.close();
        }

        // Get farmers offer
        public Cursor getAllFarmerOffers(int vendorId) {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT id, farmer_name, farmer_number, product_name, price, quantity, delivery_date FROM " + TABLE_PRODUCT_OFFERS  + " WHERE status = 'Pending' AND  vendor_id = ?";
            return db.rawQuery(query, new String[]{String.valueOf(vendorId)});
        }

        // Get all prodcuts received by the vendor
        public Cursor getAllReceivedProducts(int vendorId){
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT id, farmer_name, farmer_number, product_name, price, quantity, status, received_date FROM " + TABLE_PRODUCT_OFFERS +  " WHERE status = 'Received' AND vendor_id = ?";
            return db.rawQuery(query, new String[]{String.valueOf(vendorId)});
        }

        // Get products sold by vendor
        public Cursor getVendorProductsSold(int vendorId) {
            SQLiteDatabase db = this.getReadableDatabase();
            return db.rawQuery("SELECT * FROM vendor_products WHERE vendor_product_id = ?", new String[]{String.valueOf(vendorId)});
        }

        // Get market info
        public Cursor getMarketInfoByVendorId(int vendorId) {
            SQLiteDatabase db = this.getReadableDatabase();
            return db.rawQuery(
                    "SELECT name, street, barangay, phone, municipality, longitude, latitude FROM markets WHERE vendor_id = ?",
                    new String[]{String.valueOf(vendorId)}
            );
        }


        // Update market info
        public boolean updateMarketInfo(int vendorId, String name, String street, String barangay, String phone, String municipality, String longitude, String latitude) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("name", name);
            values.put("street", street);
            values.put("barangay", barangay);
            values.put("phone", phone);
            values.put("municipality", municipality);
            values.put("longitude", longitude);
            values.put("latitude", latitude);

            int rowsAffected = db.update("markets", values, "vendor_id = ?", new String[]{String.valueOf(vendorId)});
            db.close();

            return rowsAffected > 0;
        }

        // Get status of the requests or the farmers offer status
        public ArrayList<HashMap<String, String>> getProductOffersByStatus(String status, int farmerId) {
            ArrayList<HashMap<String, String>> offerList = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();

            // JOIN product_offers with markets using vendor_id
            String query = "SELECT po.*, m." + COLUMN_MARKET_NAME + ", m." + COLUMN_MARKET_BARANGGAY +
                    " FROM " + TABLE_PRODUCT_OFFERS + " po " +
                    "INNER JOIN " + TABLE_MARKETS + " m ON po." + COLUMN_VENDOR_ID + " = m." + COLUMN_MARKET_VENDOR_ID +
                    " WHERE po." + COLUMN_STATUS + " = ? AND po." + COLUMN_FARMER_ID + " = ?";

            Cursor cursor = db.rawQuery(query, new String[]{status, String.valueOf(farmerId)});

            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> offer = new HashMap<>();
                    offer.put("id", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_OFFER_ID)));
                    offer.put("vendor_id", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VENDOR_ID)));
                    offer.put("farmer_id", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FARMER_ID)));
                    offer.put("farmer_name", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FARMER_NAME)));
                    offer.put("product_name", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_NAME)));
                    offer.put("quantity", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_QUANTITY)));
                    offer.put("price", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRICE)));
                    offer.put("delivery_date", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DELIVERY_DATE)));
                    offer.put("status", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
                    offer.put("request_date", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQUEST_DATE)));

                    // Market Info (from join)
                    offer.put("market_name", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MARKET_NAME)));
                    offer.put("vendor_barangay", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MARKET_BARANGGAY)));

                    offerList.add(offer);
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();
            return offerList;
        }


        // Approved Products or Accepted Products
        public Cursor getApprovedOffersByVendor(int vendorId) {
            SQLiteDatabase db = this.getReadableDatabase();

            // Query to select only approved offers for the given vendorId
            String query = "SELECT * FROM " + TABLE_PRODUCT_OFFERS + " WHERE " +
                    COLUMN_VENDOR_ID + " = ? AND " +
                    COLUMN_STATUS + " = 'Accepted'";  //

            return db.rawQuery(query, new String[]{String.valueOf(vendorId)});
        }


        // Update Products by vendor or the status (Accept and Decline)
        public void updateOfferStatus(int offerId, String newStatus) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("status", newStatus);  // Assuming your table has a "status" column

            // Update the status of the offer by offerId
            db.update("product_offers", values, COLUMN_PRODUCT_OFFER_ID + " = ?", new String[]{String.valueOf(offerId)});
        }

        // For received status and the date
        public void markOfferAsReceived(int offerId, String currentDate){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("status", "Received");
            values.put("received_date", currentDate);

            db.update("product_offers", values, COLUMN_PRODUCT_OFFER_ID + " =?", new String[]{String.valueOf(offerId)});
        }

        // Get crops by month
        public Cursor getCropsForMonth(int month) {
            SQLiteDatabase db = this.getReadableDatabase();

            String[] selectionArgs = { "%" + month + "%"};

            Cursor cursor = db.rawQuery(
                    "SELECT " + COLUMN_CROP_NAME +
                    " FROM " + TABLE_CROPS +
                    " WHERE " + COLUMN_CROP_PLANTING_MONTHS + " LIKE ?",
                    selectionArgs
            );

            return cursor;
        }

        // Add vendors products
        public boolean addVendorProducts(String product_name, double product_price, String product_unit, int vendor_id){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            // To check if the product already exists
            String query = "SELECT * FROM " + TABLE_VENDOR_PRODUCTS +
                    " WHERE LOWER(vendor_product_name) = LOWER(?) AND vendor_product_id = ?";
            Cursor cursor = db.rawQuery(query, new String[]{product_name, String.valueOf(vendor_id)});

            if(cursor.getCount() > 0) {
                cursor.close();
                return false;
            }

            values.put("vendor_product_name", product_name);
            values.put("vendor_product_price", product_price);
            values.put("product_unit", product_unit);
            values.put("vendor_product_id", vendor_id);

            long result = db.insert(TABLE_VENDOR_PRODUCTS, null, values);

            return result != -1;

        }

        // Retrieve the products by vendor
        public ArrayList<String> getVendorProducts(int vendorId) {
            ArrayList<String> products = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT vendor_product_name, vendor_product_price, product_unit FROM vendor_products WHERE vendor_product_id = ?", new String[]{String.valueOf(vendorId)});

            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(0);
                    double price = cursor.getDouble(1);
                    String unit = cursor.getString(2);
                    products.add(name + " - ₱" + price + "/" + unit);
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();
            return products;
        }

        // update vendor products
        public boolean updateVendorProduct(int vendorId, String oldName, String newName, String newPrice, String newUnit) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("vendor_product_name", newName);
            values.put("vendor_product_price", Double.parseDouble(newPrice));
            values.put("product_unit", newUnit);

            int rowsAffected = db.update("vendor_products", values, "vendor_product_id = ? AND vendor_product_name = ?", new String[]{String.valueOf(vendorId), oldName});

            return rowsAffected > 0;

        }

        // delete product of the vendor
        public boolean deleteVendorProduct(int vendorId, String productName) {
            SQLiteDatabase db = this.getWritableDatabase();

            int result = db.delete(
                    "vendor_products",
                    "vendor_product_id = ? AND vendor_product_name = ? COLLATE NOCASE",
                    new String[]{String.valueOf(vendorId), productName}
            );

            return result > 0;
        }


        // vendor checking for info filled up
        public boolean isVendorInfoComplete(int vendorId){
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_MARKETS +
                            " WHERE " + COLUMN_MARKET_VENDOR_ID + " = ?" +
                            " AND " + COLUMN_MARKET_NAME + " IS NOT NULL AND " + COLUMN_MARKET_NAME + " != ''" +
                            " AND " + COLUMN_MARKET_STREET + " IS NOT NULL AND " + COLUMN_MARKET_STREET + " != ''" +
                            " AND " + COLUMN_MARKET_BARANGGAY + " IS NOT NULL AND " + COLUMN_MARKET_BARANGGAY + " != ''" +
                            " AND " + COLUMN_MARKET_MUNICIPALITY + " IS NOT NULL AND " + COLUMN_MARKET_MUNICIPALITY + " != ''",
                    new String[]{String.valueOf(vendorId)}
            );

            boolean isComplete = cursor.moveToFirst();
            return isComplete;
        }


    }

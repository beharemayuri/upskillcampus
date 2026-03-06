/* * PROJECT: Food Delivery Application
 * NAME: Mayuri Behare
 * NOTE: This file contains the Java Logic, XML Layout, and Drawable references.
 */

//Activity


package com.example.fooddeliveryapp.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fooddeliveryapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FoodDeliveryApplication extends AppCompatActivity {
//public class AdminAddFoodActivity extends AppCompatActivity{

    TextInputEditText etFoodName, etFoodPrice, etFoodDesc, etImageUrl;
    MaterialButton btnUploadFood;
    ProgressBar progressBar;
    ImageView imgPreview;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_food);

        db = FirebaseFirestore.getInstance();

        etImageUrl = findViewById(R.id.etImageUrl);
        etFoodName = findViewById(R.id.etFoodName);
        etFoodPrice = findViewById(R.id.etFoodPrice);
        etFoodDesc = findViewById(R.id.etFoodDesc);
        btnUploadFood = findViewById(R.id.btnUploadFood);
        progressBar = findViewById(R.id.progressBar);
        imgPreview = findViewById(R.id.imgPreview);

        etImageUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = s.toString().trim();

                if (!url.isEmpty()) {
                    imgPreview.setPadding(0,0,0,0);
                    imgPreview.clearColorFilter();

                    Glide.with(AdminAddFoodActivity.this)
                            .load(url)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_delete)
                            .into(imgPreview);
                } else {
                    resetPreviewImage();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnUploadFood.setOnClickListener(v -> uploadFoodToFirestore());
    }

    private void resetPreviewImage() {
        imgPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        imgPreview.setColorFilter(Color.parseColor("#CCCCCC"));
        int paddingPixel = (int)(60 * getResources().getDisplayMetrics().density);
        imgPreview.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
    }

    private void uploadFoodToFirestore() {
        String name = etFoodName.getText().toString().trim();
        String price = etFoodPrice.getText().toString().trim();
        String desc = etFoodDesc.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();

        if (name.isEmpty() || price.isEmpty() || desc.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Please fill all details! 📝", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUploadFood.setEnabled(false);
        btnUploadFood.setText("Uploading...");

        String id = db.collection("foods").document().getId();

        Map<String, Object> foodMap = new HashMap<>();
        foodMap.put("id", id);
        foodMap.put("name", name);
        foodMap.put("price", price);
        foodMap.put("description", desc);
        foodMap.put("imageUrl", imageUrl);

        db.collection("foods").document(id).set(foodMap)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AdminAddFoodActivity.this, "Dish Added Successfully! 😋", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnUploadFood.setEnabled(true);
                    btnUploadFood.setText("Add Dish to Menu");
                    Toast.makeText(AdminAddFoodActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}


//AdminOrderActivity

package com.example.fooddeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fooddeliveryapp.R;
import com.example.fooddeliveryapp.adapters.AdminOrderAdapter;
import com.example.fooddeliveryapp.models.OrderModel;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;

public class AdminOrderActivity extends AppCompatActivity {

    RecyclerView adminRecyclerView;
    ArrayList<OrderModel> allOrdersList;
    AdminOrderAdapter adapter;
    FirebaseFirestore db;

    TextView tvPageTitle;
    ExtendedFloatingActionButton fabAddFood;
    SwipeRefreshLayout swipeRefresh;
    LinearLayout layoutEmptyState;
    TabLayout adminTabLayout; //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order);

        db = FirebaseFirestore.getInstance();

        adminRecyclerView = findViewById(R.id.recyclerViewOrders);
        tvPageTitle = findViewById(R.id.tvPageTitle);
        fabAddFood = findViewById(R.id.fabAddFood);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        adminTabLayout = findViewById(R.id.adminTabLayout);

        adminTabLayout.addTab(adminTabLayout.newTab().setText("Active"));
        adminTabLayout.addTab(adminTabLayout.newTab().setText("History"));

        adminRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allOrdersList = new ArrayList<>();
        adapter = new AdminOrderAdapter(this, new ArrayList<>());
        adminRecyclerView.setAdapter(adapter);

        adminTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterOrders(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        fabAddFood.setOnClickListener(v -> {
            startActivity(new Intent(AdminOrderActivity.this, AdminAddFoodActivity.class));
        });

        swipeRefresh.setOnRefreshListener(this::loadAllOrders);

        loadAllOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllOrders();
    }

    private void loadAllOrders() {
        swipeRefresh.setRefreshing(true);

        db.collection("AllOrders").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double revenue = 0; // Analytics aggregator
                    allOrdersList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        OrderModel model = snapshot.toObject(OrderModel.class);
                        if (model != null) {
                            allOrdersList.add(model);

                            // Aggregate revenue for business insights
                            try {
                                String price = model.getFoodPrice().replace("₹", "").trim();
                                revenue += Double.parseDouble(price);
                            } catch (Exception e) { /* Handle formatting */ }
                        }
                    }

                    // Displaying analytics insight in the title area
                    tvPageTitle.setText("Admin Revenue: ₹" + revenue);

                    Collections.reverse(allOrdersList);
                    filterOrders(adminTabLayout.getSelectedTabPosition());
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(AdminOrderActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void filterOrders(int position) {
        ArrayList<OrderModel> filteredList = new ArrayList<>();

        for (OrderModel order : allOrdersList) {
            String status = order.getStatus() != null ? order.getStatus() : "Pending";

            if (position == 0) {
                if (!status.equalsIgnoreCase("Delivered") && !status.equalsIgnoreCase("Rejected")) {
                    filteredList.add(order);
                }
            } else {
                if (status.equalsIgnoreCase("Delivered") || status.equalsIgnoreCase("Rejected")) {
                    filteredList.add(order);
                }
            }
        }

        adapter.updateList(filteredList);

        if (filteredList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            adminRecyclerView.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            adminRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}


//CartActivity

package com.example.fooddeliveryapp.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddeliveryapp.R;
import com.example.fooddeliveryapp.adapters.CartAdapter;
import com.example.fooddeliveryapp.models.CartModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    RecyclerView recyclerCart;
    CartAdapter adapter;
    ArrayList<CartModel> list;

    TextView txtItemTotal, txtDeliveryFee, txtTaxes, txtGrandTotal, txtBottomTotal;
    MaterialButton btnProceedPay;
    ImageView btnBack;

    int itemTotal = 0;
    int deliveryFee = 40;
    int taxes = 25;
    int grandTotal = 0;

    String payUserId, payUserName, payPhone, payAddress;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerCart = findViewById(R.id.recyclerCart);
        txtItemTotal = findViewById(R.id.txtItemTotal);
        txtDeliveryFee = findViewById(R.id.txtDeliveryFee);
        txtTaxes = findViewById(R.id.txtTaxes);
        txtGrandTotal = findViewById(R.id.txtGrandTotal);
        txtBottomTotal = findViewById(R.id.txtBottomTotal);
        btnProceedPay = findViewById(R.id.btnProceedPay);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        adapter = new CartAdapter(this, list);
        recyclerCart.setAdapter(adapter);

        loadCart();

        btnProceedPay.setOnClickListener(v -> {
            if (list.size() > 0) {
                checkAddressAndPlaceOrder();
            } else {
                Toast.makeText(CartActivity.this, "Your Cart is Empty!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCart() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("AddToCart").document(uid)
                .collection("UserCart").get().addOnSuccessListener(query -> {
                    list.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        CartModel c = doc.toObject(CartModel.class);
                        if (c != null) {
                            c.setDocumentId(doc.getId());
                            list.add(c);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    calculateTotal();
                });
    }

    private void checkAddressAndPlaceOrder() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String address = documentSnapshot.getString("address");
                        String phone = documentSnapshot.getString("phone");
                        String userName = documentSnapshot.getString("name");

                        if (address == null || address.isEmpty()) {
                            Toast.makeText(CartActivity.this, "Please set Delivery Address in Profile first!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(CartActivity.this, ProfileActivity.class));
                        } else {
                            showPaymentDialog(userId, userName, phone, address);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(CartActivity.this, "Error fetching user details", Toast.LENGTH_SHORT).show());
    }

    private void showPaymentDialog(String userId, String userName, String phone, String address) {
        String[] paymentMethods = {"💵 Cash on Delivery (COD)", "💳 Pay Online (UPI / Card)"};

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Payment Method\nTotal to Pay: ₹" + grandTotal)
                .setSingleChoiceItems(paymentMethods, 0, null)
                .setPositiveButton("Confirm Order", (dialog, which) -> {

                    int selectedPosition = ((androidx.appcompat.app.AlertDialog) dialog).getListView().getCheckedItemPosition();

                    if (selectedPosition == 0) {
                        Toast.makeText(this, "Order Confirmed via COD", Toast.LENGTH_SHORT).show();
                        placeOrderLoop(userId, userName, phone, address, "COD");
                    } else {
                        payUserId = userId;
                        payUserName = userName;
                        payPhone = phone;
                        payAddress = address;

                        startMockPaymentGateway(grandTotal);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void startMockPaymentGateway(int amountInRupees) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_mock_payment);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvMockAmount = dialog.findViewById(R.id.tvMockAmount);
        TextView tvMockStatus = dialog.findViewById(R.id.tvMockStatus);

        tvMockAmount.setText("₹ " + amountInRupees);
        dialog.show();

        new Handler().postDelayed(() -> {
            tvMockStatus.setText("Payment Successful! ✅");
            tvMockStatus.setTextColor(Color.parseColor("#4CAF50"));

            new Handler().postDelayed(() -> {
                dialog.dismiss();

                String fakePaymentId = "pay_" + System.currentTimeMillis();

                placeOrderLoop(payUserId, payUserName, payPhone, payAddress, "Paid Online (ID: " + fakePaymentId + ")");
            }, 1000);

        }, 3000); //
    }

    private void placeOrderLoop(String userId, String userName, String phone, String address, String paymentMethod) {

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat dateF = new java.text.SimpleDateFormat("MMM dd, yyyy");
        java.text.SimpleDateFormat timeF = new java.text.SimpleDateFormat("HH:mm:ss a");
        String currentDate = dateF.format(cal.getTime());
        String currentTime = timeF.format(cal.getTime());

        for (int i = 0; i < list.size(); i++) {
            CartModel item = list.get(i);

            int randomNum = new java.util.Random().nextInt(9000) + 1000;
            String orderId = "FD" + randomNum + i;

            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("orderId", orderId);
            orderMap.put("foodName", item.getFoodName());
            orderMap.put("foodPrice", item.getFoodPrice());
            orderMap.put("quantity", item.getQuantity());
            orderMap.put("userName", userName);
            orderMap.put("userPhone", phone);
            orderMap.put("userAddress", address);
            orderMap.put("userId", userId);
            orderMap.put("status", "Pending");
            orderMap.put("orderDate", currentDate);
            orderMap.put("orderTime", currentTime);
            orderMap.put("paymentMethod", paymentMethod);
            orderMap.put("totalPaid", String.valueOf(grandTotal));

            db.collection("AllOrders").document(orderId).set(orderMap);
            db.collection("CurrentUser").document(userId).collection("MyOrder").document(orderId).set(orderMap);

            if (i == list.size() - 1) {
                clearCart(userId, orderId);
            }
        }
    }

    private void clearCart(String userId, String finalOrderId) {
        db.collection("AddToCart").document(userId)
                .collection("UserCart").get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }

                    Intent intent = new Intent(CartActivity.this, OrderSuccessActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("ORDER_ID", finalOrderId);
                    startActivity(intent);
                    finish();
                });
    }

    public void calculateTotal() {
        itemTotal = 0;
        if (list == null || list.isEmpty()) {
            updateUI(0);
            return;
        }

        for (CartModel c : list) {
            try {
                // Null-safe parsing
                if (c != null && c.getFoodPrice() != null) {
                    String priceStr = c.getFoodPrice().replaceAll("[^0-9]", "");
                    String qtyStr = (c.getQuantity() != null) ? c.getQuantity() : "1";

                    if (!priceStr.isEmpty()) {
                        itemTotal += (Integer.parseInt(priceStr) * Integer.parseInt(qtyStr));
                    }
                }
            } catch (Exception e) {
                Log.e("CartCrashFix", "Error calculating: " + e.getMessage());
            }
        }
        updateUI(itemTotal);
    }


    private void updateUI(int total) {
        grandTotal = (total > 0) ? (total + deliveryFee + taxes) : 0;
        txtItemTotal.setText("₹ " + total);
        txtGrandTotal.setText("₹ " + grandTotal);
        txtBottomTotal.setText("₹ " + grandTotal);
    }
}


//DeliveryDashboardActivity

package com.example.fooddeliveryapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fooddeliveryapp.R;
import com.example.fooddeliveryapp.adapters.DeliveryAdapter;
import com.example.fooddeliveryapp.models.OrderModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;

public class DeliveryDashboardActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DeliveryAdapter adapter;
    ArrayList<OrderModel> list;
    FirebaseFirestore db;

    SwipeRefreshLayout swipeRefreshDelivery;
    LinearLayout layoutNoDelivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_dashboard);

        recyclerView = findViewById(R.id.deliveryRecyclerView);
        swipeRefreshDelivery = findViewById(R.id.swipeRefreshDelivery);
        layoutNoDelivery = findViewById(R.id.layoutNoDelivery);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        adapter = new DeliveryAdapter(this, list);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        swipeRefreshDelivery.setOnRefreshListener(this::loadDeliveryTasks);

        loadDeliveryTasks();
    }

    private void loadDeliveryTasks() {
        swipeRefreshDelivery.setRefreshing(true);

        db.collection("AllOrders")
                .whereIn("status", Arrays.asList("Accepted", "Out for Delivery"))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    list.clear();
                    for (DocumentSnapshot d : queryDocumentSnapshots) {
                        try {
                            OrderModel order = d.toObject(OrderModel.class);
                            list.add(order);
                        } catch (Exception e) {
                            Log.e("DeliveryError", "Data Error: " + e.getMessage());
                        }
                    }
                    adapter.notifyDataSetChanged();
                    swipeRefreshDelivery.setRefreshing(false);

                    if (list.isEmpty()) {
                        layoutNoDelivery.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        layoutNoDelivery.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    swipeRefreshDelivery.setRefreshing(false);
                    Toast.makeText(DeliveryDashboardActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}


//DetailActivity

package com.example.fooddeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fooddeliveryapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {

    TextView tvName, tvDescription, tvPrice;
    ImageView imgDetail, btnGoToCart;
    Button btnAddToCart, btnBuyNow; //

    FirebaseFirestore db;
    FirebaseAuth auth;
    String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        tvName = findViewById(R.id.tvDetailName);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvPrice = findViewById(R.id.tvDetailPrice);
        imgDetail = findViewById(R.id.imgDetail);
        btnAddToCart = findViewById(R.id.btnOrder);
        btnGoToCart = findViewById(R.id.btnGoToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow); //

        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        String price = getIntent().getStringExtra("price");
        imageUrl = getIntent().getStringExtra("imageUrl");

        tvName.setText(name);
        tvDescription.setText(description);
        tvPrice.setText("₹ " + price);
        Glide.with(this).load(imageUrl).into(imgDetail);

        btnAddToCart.setOnClickListener(v -> addToCart(name, price, imageUrl));

        btnGoToCart.setOnClickListener(v -> startActivity(new Intent(DetailActivity.this, CartActivity.class)));

        btnBuyNow.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(DetailActivity.this, "Please Login First!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DetailActivity.this, LoginActivity.class));
                return;
            }
            placeOrder(name, price, imageUrl);
        });
    }

    private void placeOrder(String foodName, String foodPrice, String foodImage) {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String address = documentSnapshot.getString("address");
                        String phone = documentSnapshot.getString("phone");
                        String userName = documentSnapshot.getString("name");

                        if (address == null || address.isEmpty()) {
                            Toast.makeText(this, "Please set your Delivery Address first!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(DetailActivity.this, ProfileActivity.class));
                        } else {
                            saveOrderToDatabase(userId, userName, phone, address, foodName, foodPrice, foodImage);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error checking address", Toast.LENGTH_SHORT).show());
    }

    private void saveOrderToDatabase(String userId, String userName, String phone, String address, String foodName, String foodPrice, String foodImage) {

        String orderId = String.valueOf(System.currentTimeMillis());

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        String saveCurrentDate = currentDate.format(calForDate.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        String saveCurrentTime = currentTime.format(calForDate.getTime());

        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("orderId", orderId);
        orderMap.put("foodName", foodName);
        orderMap.put("foodPrice", foodPrice);
        orderMap.put("imageUrl", foodImage);
        orderMap.put("userName", userName);
        orderMap.put("userPhone", phone);
        orderMap.put("userAddress", address);
        orderMap.put("userId", userId);
        orderMap.put("orderDate", saveCurrentDate);
        orderMap.put("orderTime", saveCurrentTime);
        orderMap.put("status", "Pending");
        orderMap.put("totalPrice", foodPrice);

        db.collection("AllOrders").document(orderId).set(orderMap);

        db.collection("CurrentUser").document(userId).collection("MyOrder").document(orderId).set(orderMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Intent intent = new Intent(DetailActivity.this, OrderSuccessActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(DetailActivity.this, "Order Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addToCart(String foodName, String foodPrice, String foodImage) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please Login First!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DetailActivity.this, LoginActivity.class));
            return;
        }
        String userId = auth.getCurrentUser().getUid();

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        String saveCurrentDate = currentDate.format(calForDate.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        String saveCurrentTime = currentTime.format(calForDate.getTime());

        Map<String, Object> cartMap = new HashMap<>();
        cartMap.put("foodName", foodName);
        cartMap.put("foodPrice", foodPrice);
        cartMap.put("imageUrl", foodImage);
        cartMap.put("quantity", "1");
        cartMap.put("totalPrice", foodPrice);
        cartMap.put("date", saveCurrentDate);
        cartMap.put("time", saveCurrentTime);

        db.collection("AddToCart").document(userId).collection("UserCart")
                .add(cartMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(DetailActivity.this, "Added to Cart!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}


//EditProfileActivity

package com.example.fooddeliveryapp.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fooddeliveryapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    ImageView btnBack;
    TextInputEditText etEditName, etEditPhone, etEditAddress;
    MaterialButton btnSaveProfile;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        etEditName = findViewById(R.id.etEditName);
        etEditPhone = findViewById(R.id.etEditPhone);
        etEditAddress = findViewById(R.id.etEditAddress);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        btnBack.setOnClickListener(v -> finish());

        loadUserData();

        btnSaveProfile.setOnClickListener(v -> saveUserData());
    }

    private void loadUserData() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etEditName.setText(documentSnapshot.getString("name"));
                        etEditPhone.setText(documentSnapshot.getString("phone"));
                        etEditAddress.setText(documentSnapshot.getString("address"));
                    }
                });
    }

    private void saveUserData() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        String name = etEditName.getText().toString().trim();
        String phone = etEditPhone.getText().toString().trim();
        String address = etEditAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all details! 📝", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("address", address);

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated Successfully! ✅", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update!", Toast.LENGTH_SHORT).show());
    }
}


//FavoriteActivity

package com.example.fooddeliveryapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fooddeliveryapp.R;
import com.example.fooddeliveryapp.adapters.FoodAdapter;
import com.example.fooddeliveryapp.models.FoodModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {
    RecyclerView recyclerFavorites;
    ArrayList<FoodModel> list;
    FoodAdapter adapter;
    FirebaseFirestore db;
    FirebaseAuth auth;
    LinearLayout layoutNoFav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerFavorites = findViewById(R.id.recyclerFavorites);
        layoutNoFav = findViewById(R.id.layoutNoFav);

        recyclerFavorites.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        adapter = new FoodAdapter(this, list);
        recyclerFavorites.setAdapter(adapter);

        loadFavoritesFromFirestore();
    }

    private void loadFavoritesFromFirestore() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        db.collection("Favorites").document(uid).collection("UserFav")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    list.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        FoodModel model = doc.toObject(FoodModel.class);
                        if (model != null) list.add(model);
                    }
                    adapter.notifyDataSetChanged();

                    // Show empty state if no favorites
                    layoutNoFav.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }
}


//LoginActivity

package com.example.fooddeliveryapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fooddeliveryapp.R;
import com.example.fooddeliveryapp.adapters.FoodAdapter;
import com.example.fooddeliveryapp.models.FoodModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {
    RecyclerView recyclerFavorites;
    ArrayList<FoodModel> list;
    FoodAdapter adapter;
    FirebaseFirestore db;
    FirebaseAuth auth;
    LinearLayout layoutNoFav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerFavorites = findViewById(R.id.recyclerFavorites);
        layoutNoFav = findViewById(R.id.layoutNoFav);

        recyclerFavorites.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        adapter = new FoodAdapter(this, list);
        recyclerFavorites.setAdapter(adapter);

        loadFavoritesFromFirestore();
    }

    private void loadFavoritesFromFirestore() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        db.collection("Favorites").document(uid).collection("UserFav")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    list.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        FoodModel model = doc.toObject(FoodModel.class);
                        if (model != null) list.add(model);
                    }
                    adapter.notifyDataSetChanged();

                    // Show empty state if no favorites
                    layoutNoFav.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }
}


//MainActivity

package com.example.fooddeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.fooddeliveryapp.R;
import com.example.fooddeliveryapp.adapters.BannerAdapter;
import com.example.fooddeliveryapp.adapters.FoodAdapter;
import com.example.fooddeliveryapp.models.BannerModel;
import com.example.fooddeliveryapp.models.FoodModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText etSearch;
    RecyclerView recyclerRecommended;
    BottomNavigationView bottomNav;
    ViewPager2 viewPagerBanner;

    LinearLayout catPizza, catBurger, catBiryani, catDessert;
    ImageView imgCatPizza, imgCatBurger, imgCatBiryani, imgCatDessert;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    FoodAdapter foodAdapter;
    ArrayList<FoodModel> foodList;
    ArrayList<FoodModel> filteredList;

    BannerAdapter bannerAdapter;
    List<BannerModel> bannerList;

    private Handler sliderHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etSearch = findViewById(R.id.etSearch);
        recyclerRecommended = findViewById(R.id.recyclerRecommended);
        bottomNav = findViewById(R.id.bottomNav);
        viewPagerBanner = findViewById(R.id.viewPagerBanner);

        catPizza = findViewById(R.id.catPizza);
        catBurger = findViewById(R.id.catBurger);
        catBiryani = findViewById(R.id.catBiryani);
        catDessert = findViewById(R.id.catDessert);

        imgCatPizza = findViewById(R.id.imgCatPizza);
        imgCatBurger = findViewById(R.id.imgCatBurger);
        imgCatBiryani = findViewById(R.id.imgCatBiryani);
        imgCatDessert = findViewById(R.id.imgCatDessert);

        recyclerRecommended.setLayoutManager(new LinearLayoutManager(this));
        foodList = new ArrayList<>();
        filteredList = new ArrayList<>();
        foodAdapter = new FoodAdapter(this, filteredList);
        recyclerRecommended.setAdapter(foodAdapter);

        loadCategoryImages();

        loadRealOfferBanners();

        loadFoodFromDatabase();

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filter(s.toString());
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        catPizza.setOnClickListener(v -> etSearch.setText("Pizza"));
        catBurger.setOnClickListener(v -> etSearch.setText("Burger"));
        catBiryani.setOnClickListener(v -> etSearch.setText("Biryani"));
        catDessert.setOnClickListener(v -> etSearch.setText("Dessert"));

        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) return true;
                else if (id == R.id.nav_cart) {
                    startActivity(new Intent(MainActivity.this, CartActivity.class));
                    return false;
                } else if (id == R.id.nav_orders) {
                    startActivity(new Intent(MainActivity.this, OrderActivity.class));
                    return false;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    return false;
                }
                return false;
            });
        }
    }

    private void loadCategoryImages() {
        String urlPizza = "https://images.pexels.com/photos/1566837/pexels-photo-1566837.jpeg?auto=compress&cs=tinysrgb&w=400";
        String urlBurger = "https://images.pexels.com/photos/1639557/pexels-photo-1639557.jpeg?auto=compress&cs=tinysrgb&w=400";
        String urlBiryani = "https://images.pexels.com/photos/1624487/pexels-photo-1624487.jpeg?auto=compress&cs=tinysrgb&w=400";
        String urlDessert = "https://images.pexels.com/photos/135068/pexels-photo-135068.jpeg?auto=compress&cs=tinysrgb&w=400";

        Glide.with(this).load(urlPizza).circleCrop().into(imgCatPizza);
        Glide.with(this).load(urlBurger).circleCrop().into(imgCatBurger);
        Glide.with(this).load(urlBiryani).circleCrop().into(imgCatBiryani);
        Glide.with(this).load(urlDessert).circleCrop().into(imgCatDessert);
    }

    private void loadRealOfferBanners() {
        bannerList = new ArrayList<>();

        db.collection("banners")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bannerList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        BannerModel banner = doc.toObject(BannerModel.class);
                        if (banner != null) {
                            bannerList.add(banner);
                        }
                    }

                    if (!bannerList.isEmpty()) {
                        bannerAdapter = new BannerAdapter(MainActivity.this, bannerList);
                        viewPagerBanner.setAdapter(bannerAdapter);
                        setupAutoSlider();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to load banners", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupAutoSlider() {
        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
    }

    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPagerBanner.getAdapter() != null) {
                int totalItems = viewPagerBanner.getAdapter().getItemCount();

                if (totalItems > 0) {
                    int currentItem = viewPagerBanner.getCurrentItem();
                    if (currentItem == totalItems - 1) {
                        viewPagerBanner.setCurrentItem(0);
                    } else {
                        viewPagerBanner.setCurrentItem(currentItem + 1);
                    }
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(bannerList != null && !bannerList.isEmpty()){
            sliderHandler.postDelayed(sliderRunnable, 3000);
        }
    }

    private void filter(String text) {
        ArrayList<FoodModel> temp = new ArrayList<>();
        for (FoodModel food : foodList) {
            if (food.getName().toLowerCase().contains(text.toLowerCase())) {
                temp.add(food);
            }
        }
        foodAdapter.filterList(temp);
    }

    private void loadFoodFromDatabase() {
        db.collection("foods").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                foodList.clear();
                filteredList.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    FoodModel food = document.toObject(FoodModel.class);
                    if (food != null) {
                        foodList.add(food);
                        filteredList.add(food);
                    }
                }
                foodAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(MainActivity.this, "Error loading food", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


//OrderActivity

package com.example.fooddeliveryapp.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddeliveryapp.R;
import com.example.fooddeliveryapp.adapters.OrderAdapter;
import com.example.fooddeliveryapp.models.OrderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class OrderActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FirebaseFirestore db;
    FirebaseAuth auth;
    OrderAdapter adapter;
    ArrayList<OrderModel> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.recyclerViewOrders);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        adapter = new OrderAdapter(this, list);
        recyclerView.setAdapter(adapter);

        loadMyOrders();
    }

    private void loadMyOrders() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();

        db.collection("CurrentUser").document(userId).collection("MyOrder")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    list.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        OrderModel model = snapshot.toObject(OrderModel.class);
                        list.add(model);
                    }
                    adapter.notifyDataSetChanged();

                    if (list.isEmpty()) {
                        Toast.makeText(OrderActivity.this, "No Orders Found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OrderActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}


//OrderSuccessActivity

package com.example.fooddeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fooddeliveryapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrderSuccessActivity extends AppCompatActivity {

    TextView txtOrderId;
    MaterialButton btnBackToHome, btnTrackOrder;
    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        txtOrderId = findViewById(R.id.txtOrderId);
        btnTrackOrder = findViewById(R.id.btnTrackOrder);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        String orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId != null) {
            txtOrderId.setText("#" + orderId);
        }

        // ⭐ आर्डर सफल होने पर रिवॉर्ड पॉइंट्स अपडेट करें
        updateUserRewards();

        // 📍 Logic to Open Tracking Screen
        btnTrackOrder.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, TrackingActivity.class);
            startActivity(intent);
        });

        btnBackToHome.setOnClickListener(v -> {
            goToHome();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goToHome();
            }
        });
    }

    private void updateUserRewards() {
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();

            db.collection("users").document(uid)
                    .update("rewardPoints", FieldValue.increment(50))
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Congratulations! You earned 50 Rewards ⭐", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error updating rewards: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void goToHome() {
        Intent intent = new Intent(OrderSuccessActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}


//ProfileActivity

package com.example.fooddeliveryapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.fooddeliveryapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {

    ImageView btnBackProfile, imgProfilePic;
    TextView tvUserName, tvUserEmail, txtRewardPoints;
    LinearLayout layoutEditProfile, layoutYourOrders, layoutSavedAddress, layoutFavorites; // Added layoutFavorites
    MaterialButton btnLogout;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // IDs Connect
        btnBackProfile = findViewById(R.id.btnBackProfile);
        imgProfilePic = findViewById(R.id.imgProfilePic);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        txtRewardPoints = findViewById(R.id.txtRewardPoints);
        layoutEditProfile = findViewById(R.id.layoutEditProfile);
        layoutYourOrders = findViewById(R.id.layoutYourOrders);
        layoutSavedAddress = findViewById(R.id.layoutSavedAddress);
        layoutFavorites = findViewById(R.id.layoutFavorites); // Connected layoutFavorites
        btnLogout = findViewById(R.id.btnLogout);

        loadUserData();
        loadLoyaltyPoints();

        btnBackProfile.setOnClickListener(v -> finish());

        layoutFavorites.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, FavoriteActivity.class));
        });

        layoutSavedAddress.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, SavedAddressesActivity.class)));
        layoutYourOrders.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, OrderActivity.class)));
        layoutEditProfile.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class)));

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadLoyaltyPoints() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Long points = doc.getLong("rewardPoints");
                txtRewardPoints.setText("Balance: " + (points != null ? points : 0) + " Points");
            }
        });
    }

    private void loadUserData() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        tvUserEmail.setText(auth.getCurrentUser().getEmail());
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                tvUserName.setText(doc.getString("name"));
                String imageUrl = doc.getString("profileImage");
                Glide.with(this).load(imageUrl != null ? imageUrl : R.mipmap.ic_launcher_round).circleCrop().into(imgProfilePic);
            }
        });
    }
}


//RegisterActivity

package com.example.fooddeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fooddeliveryapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText etName, etEmail, etPhone, etPassword, etAddress;
    Button btnRegister;
    TextView tvLogin;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etAddress = findViewById(R.id.etAddress);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {

        String name = String.valueOf(etName.getText()).trim();
        String email = String.valueOf(etEmail.getText()).trim();
        String phone = String.valueOf(etPhone.getText()).trim();
        String password = String.valueOf(etPassword.getText()).trim();
        String address = String.valueOf(etAddress.getText()).trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = mAuth.getCurrentUser().getUid();

                    Map<String, Object> user = new HashMap<>();
                    user.put("uid", uid);
                    user.put("name", name);
                    user.put("email", email);
                    user.put("phone", phone);
                    user.put("address", address);

                    db.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(this, "Account created successfully 🎉", Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(this, MainActivity.class));
                                finish();

                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}


//SavedAddressesActivity

package com.example.fooddeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fooddeliveryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SavedAddressesActivity extends AppCompatActivity {

    ImageView btnBack, btnEditAddress;
    TextView tvSavedAddress;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_addresses);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        btnEditAddress = findViewById(R.id.btnEditAddress);
        tvSavedAddress = findViewById(R.id.tvSavedAddress);

        btnBack.setOnClickListener(v -> finish());

        btnEditAddress.setOnClickListener(v -> {
            startActivity(new Intent(SavedAddressesActivity.this, EditProfileActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAddress();
    }

    private void loadAddress() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String address = documentSnapshot.getString("address");
                        if (address != null && !address.isEmpty()) {
                            tvSavedAddress.setText(address);
                        } else {
                            tvSavedAddress.setText("No address saved yet. Please add an address.");
                        }
                    }
                });
    }
}


//SplashActivity

package com.example.fooddeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import com.example.fooddeliveryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }

            finish();

        }, 3000); // 3000 ms = 3 seconds
    }
}


//TrackingActivity

package com.example.fooddeliveryapp.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fooddeliveryapp.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.api.IMapController;

public class TrackingActivity extends AppCompatActivity {

    private MapView mapView;
    private IMapController mapController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OSMDroid configuration
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_tracking);

        mapView = findViewById(R.id.map);
        mapView.setMultiTouchControls(true);

        // Correct controller
        mapController = mapView.getController();
        mapController.setZoom(14.0);

        // Ahmedabad example locations
        GeoPoint userLocation = new GeoPoint(23.0225, 72.5714);
        GeoPoint restaurantLocation = new GeoPoint(23.0300, 72.5800);

        mapController.setCenter(userLocation);

        // User Marker
        Marker userMarker = new Marker(mapView);
        userMarker.setPosition(userLocation);
        userMarker.setTitle("Your Location");
        mapView.getOverlays().add(userMarker);

        // Restaurant Marker
        Marker restaurantMarker = new Marker(mapView);
        restaurantMarker.setPosition(restaurantLocation);
        restaurantMarker.setTitle("TestyBites Kitchen");
        mapView.getOverlays().add(restaurantMarker);

        mapView.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}


//Adapters

//AdminOrderAdapter

com.example.fooddeliveryapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fooddeliveryapp.R;
import com.example.fooddeliveryapp.models.OrderModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    Context context;
    ArrayList<OrderModel> list;

    public AdminOrderAdapter(Context context, ArrayList<OrderModel> list) {
        this.context = context;
        this.list = list;
    }

    public void updateList(ArrayList<OrderModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel order = list.get(position);

        holder.tvAdminOrderId.setText("Order #" + order.getOrderId());
        holder.tvAdminDateTime.setText(order.getOrderDate() + " | " + order.getOrderTime());
        holder.tvAdminFoodName.setText(order.getFoodName());

        String price = order.getFoodPrice();
        if(price != null && !price.contains("₹")) {
            price = "₹ " + price;
        }
        holder.tvAdminFoodPrice.setText(price);

        holder.tvAdminCustomerName.setText("👤 Name: " + order.getUserName());
        holder.tvAdminCustomerPhone.setText("📞 Phone: " + order.getUserPhone());
        holder.tvAdminCustomerAddress.setText("📍 Address: " + order.getUserAddress());

        String payment = order.getPaymentMethod();
        if(payment == null || payment.isEmpty()) payment = "Cash on Delivery";
        holder.tvAdminPaymentMethod.setText("💳 Payment: " + payment);

        if (order.getImageUrl() != null && !order.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(order.getImageUrl())
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.imgAdminFood);
        }

        String status = order.getStatus();
        if(status == null) status = "Pending";
        status = status.trim();
        holder.tvAdminStatus.setText(status);

        if (status.equalsIgnoreCase("Pending")) {
            holder.tvAdminStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
        } else if (status.equalsIgnoreCase("Accepted") || status.equalsIgnoreCase("Cooking") || status.equalsIgnoreCase("Preparing")) {
            holder.tvAdminStatus.setTextColor(Color.parseColor("#2196F3")); // Blue
        } else if (status.equalsIgnoreCase("Out for Delivery")) {
            holder.tvAdminStatus.setTextColor(Color.parseColor("#9C27B0")); // Purple
        } else if (status.equalsIgnoreCase("Delivered")) {
            holder.tvAdminStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else if (status.equalsIgnoreCase("Rejected")) {
            holder.tvAdminStatus.setTextColor(Color.RED);
        }

        holder.btnUpdateStatus.setOnClickListener(v -> showUpdateDialog(order.getOrderId(), order.getUserId(), position));
    }

    private void showUpdateDialog(String orderId, String userId, int position) {
        String[] options = {"Accepted", "Cooking", "Out for Delivery", "Delivered", "Rejected"};
        new AlertDialog.Builder(context)
                .setTitle("Update Status for " + orderId)
                .setItems(options, (dialog, which) -> {
                    updateStatus(orderId, userId, options[which], position);
                }).show();
    }

    private void updateStatus(String orderId, String userId, String status, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("AllOrders").document(orderId).update("status", status);

        if (userId != null) {
            db.collection("CurrentUser").document(userId).collection("MyOrder").document(orderId).update("status", status)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Status Updated to " + status, Toast.LENGTH_SHORT).show();
                        list.get(position).setStatus(status);
                        notifyItemChanged(position);
                    });
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdminOrderId, tvAdminStatus, tvAdminDateTime;
        ImageView imgAdminFood;
        TextView tvAdminFoodName, tvAdminFoodPrice;
        TextView tvAdminCustomerName, tvAdminCustomerPhone, tvAdminCustomerAddress, tvAdminPaymentMethod;
        MaterialButton btnUpdateStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdminOrderId = itemView.findViewById(R.id.tvAdminOrderId);
            tvAdminStatus = itemView.findViewById(R.id.tvAdminStatus);
            tvAdminDateTime = itemView.findViewById(R.id.tvAdminDateTime);
            imgAdminFood = itemView.findViewById(R.id.imgAdminFood);
            tvAdminFoodName = itemView.findViewById(R.id.tvAdminFoodName);
            tvAdminFoodPrice = itemView.findViewById(R.id.tvAdminFoodPrice);
            tvAdminCustomerName = itemView.findViewById(R.id.tvAdminCustomerName);
            tvAdminCustomerPhone = itemView.findViewById(R.id.tvAdminCustomerPhone);
            tvAdminCustomerAddress = itemView.findViewById(R.id.tvAdminCustomerAddress);
            tvAdminPaymentMethod = itemView.findViewById(R.id.tvAdminPaymentMethod);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }
    }
}


//BannerAdapter

package com.example.fooddeliveryapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fooddeliveryapp.R;
import com.example.fooddeliveryapp.models.BannerModel;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {

    Context context;
    List<BannerModel> list;

    public BannerAdapter(Context context, List<BannerModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BannerModel banner = list.get(position);

        Glide.with(context)
                .load(banner.getImageUrl())
                .into(holder.imgBanner);

        if (banner.getTitle() != null) {
            holder.txtBannerTitle.setText(banner.getTitle());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBanner;
        TextView txtBannerTitle;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBanner = itemView.findViewById(R.id.imgBanner);
            txtBannerTitle = itemView.findViewById(R.id.txtBannerTitle);
        }
    }
}


//CartAdapter

package com.example.fooddeliveryapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddeliveryapp.R;
import com.example.fooddeliveryapp.models.CartModel;
import com.example.fooddeliveryapp.activities.CartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    Context context;
    ArrayList<CartModel> list;
    FirebaseFirestore db;
    FirebaseAuth auth;

    public CartAdapter(Context context, ArrayList<CartModel> list) {
        this.context = context;
        this.list = list;
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartModel cartItem = list.get(position);

        // Null-safe UI updates
        holder.tvName.setText(cartItem.getFoodName() != null ? cartItem.getFoodName() : "Item");
        holder.tvPrice.setText("₹ " + (cartItem.getFoodPrice() != null ? cartItem.getFoodPrice() : "0"));
        holder.tvQuantity.setText(cartItem.getQuantity() != null ? cartItem.getQuantity() : "1");

        holder.btnPlus.setOnClickListener(v -> {
            try {
                int currentQty = Integer.parseInt(cartItem.getQuantity());
                currentQty++;
                // 👈 updateFirebase को updateQuantityInFirebase से बदल दिया गया है
                updateQuantityInFirebase(cartItem.getDocumentId(), String.valueOf(currentQty), holder, cartItem);
            } catch (Exception e) {
                Log.e("CartAdapter", "Plus Error: " + e.getMessage());
            }
        });

        holder.btnMinus.setOnClickListener(v -> {
            try {
                int currentQty = Integer.parseInt(cartItem.getQuantity());
                if (currentQty > 1) {
                    currentQty--;
                    updateQuantityInFirebase(cartItem.getDocumentId(), String.valueOf(currentQty), holder, cartItem);
                } else {
                    deleteItem(holder.getAdapterPosition(), cartItem.getDocumentId());
                }
            } catch (Exception e) {
                Log.e("CartAdapter", "Minus Error: " + e.getMessage());
            }
        });
    }

    private void updateQuantityInFirebase(String docId, String newQty, ViewHolder holder, CartModel item) {
        if (auth.getCurrentUser() == null || docId == null) return;

        item.setQuantity(newQty); 
        holder.tvQuantity.setText(newQty);

        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> map = new HashMap<>();
        map.put("quantity", newQty);

        db.collection("AddToCart").document(userId)
                .collection("UserCart").document(docId)
                .update(map)
                .addOnSuccessListener(aVoid -> updateTotalBill());
    }

    private void updateTotalBill() {
        if (context instanceof CartActivity) {
            ((CartActivity) context).calculateTotal();
        }
    }

    private void deleteItem(int position, String docId) {
        if (auth.getCurrentUser() == null || docId == null) return;
        String userId = auth.getCurrentUser().getUid();
        db.collection("AddToCart").document(userId)
                .collection("UserCart").document(docId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && position < list.size()) {
                        list.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());
                        updateTotalBill();
                    }
                });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvQuantity, btnPlus, btnMinus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.txtCartFoodName);
            tvPrice = itemView.findViewById(R.id.txtCartFoodPrice);
            tvQuantity = itemView.findViewById(R.id.txtQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
        }
    }
}


//DeliveryAdapter

package com.example.fooddeliveryapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fooddeliveryapp.R;
import com.example.fooddeliveryapp.models.OrderModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.ViewHolder> {

    Context context;
    ArrayList<OrderModel> list;

    public DeliveryAdapter(Context context, ArrayList<OrderModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel order = list.get(position);

        holder.tvOrderId.setText("Order #" + order.getOrderId());
        holder.tvFoodName.setText(order.getFoodName());
        holder.tvCustomerName.setText("👤 " + order.getUserName());
        holder.tvCustomerPhone.setText("📞 " + order.getUserPhone());
        holder.tvCustomerAddress.setText("📍 " + order.getUserAddress());
        holder.tvStatus.setText(order.getStatus());
        holder.tvPrice.setText("₹ " + order.getFoodPrice());

        if ("Accepted".equalsIgnoreCase(order.getStatus())) {
            holder.btnAction.setText("Pick Up Order 🛵");
            holder.btnAction.setBackgroundColor(Color.parseColor("#0C4AA6"));
        } else if ("Out for Delivery".equalsIgnoreCase(order.getStatus())) {
            holder.btnAction.setText("Mark as Delivered ✅");
            holder.btnAction.setBackgroundColor(Color.parseColor("#4CAF50"));
        }

        Glide.with(context).load(order.getImageUrl()).placeholder(R.mipmap.ic_launcher).into(holder.imgFood);

        holder.btnAction.setOnClickListener(v -> {
            String currentStatus = order.getStatus();
            String nextStatus = "";

            if ("Accepted".equalsIgnoreCase(currentStatus)) {
                nextStatus = "Out for Delivery";
            } else if ("Out for Delivery".equalsIgnoreCase(currentStatus)) {
                nextStatus = "Delivered";
            }

            if (!nextStatus.isEmpty()) {
                updateStatus(order.getOrderId(), order.getUserId(), nextStatus, position);
            }
        });
    }

    private void updateStatus(String orderId, String userId, String newStatus, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("AllOrders").document(orderId).update("status", newStatus);
        db.collection("CurrentUser").document(userId).collection("MyOrder").document(orderId).update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Order " + newStatus, Toast.LENGTH_SHORT).show();
                    if ("Delivered".equals(newStatus)) {
                        list.remove(position); // List se hata do
                        notifyItemRemoved(position);
                    } else {
                        list.get(position).setStatus(newStatus); // Status badal do
                        notifyItemChanged(position);
                    }
                });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvFoodName, tvCustomerName, tvCustomerPhone, tvCustomerAddress, tvStatus, tvPrice;
        ImageView imgFood;
        MaterialButton btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvAdminOrderId);
            tvStatus = itemView.findViewById(R.id.tvAdminStatus);
            imgFood = itemView.findViewById(R.id.imgAdminFood);
            tvFoodName = itemView.findViewById(R.id.tvAdminFoodName);
            tvPrice = itemView.findViewById(R.id.tvAdminFoodPrice);
            tvCustomerName = itemView.findViewById(R.id.tvAdminCustomerName);
            tvCustomerPhone = itemView.findViewById(R.id.tvAdminCustomerPhone);
            tvCustomerAddress = itemView.findViewById(R.id.tvAdminCustomerAddress);
            btnAction = itemView.findViewById(R.id.btnUpdateStatus);
        }
    }
}


//FoodAdapter

package com.example.fooddeliveryapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fooddeliveryapp.R;
import com.example.fooddeliveryapp.models.FoodModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {

    Context context;
    ArrayList<FoodModel> list;

    FirebaseFirestore db;
    FirebaseAuth auth;

    public FoodAdapter(Context context, ArrayList<FoodModel> list) {
        this.context = context;
        this.list = list;
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_food, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodModel food = list.get(position);

        holder.txtFoodName.setText(food.getName());
        holder.txtFoodPrice.setText("₹ " + food.getPrice());
        holder.txtFoodDesc.setText(food.getDescription());

        Glide.with(context)
                .load(food.getImageUrl())
                .into(holder.imgFood);

        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            db.collection("Favorites")
                    .document(uid)
                    .collection("UserFav")
                    .document(food.getName())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            holder.imgFavorite.setImageResource(R.drawable.ic_favorite_filled);
                            holder.imgFavorite.setTag(true);
                        } else {
                            holder.imgFavorite.setImageResource(R.drawable.ic_favorite_border);
                            holder.imgFavorite.setTag(false);
                        }
                    });
        }

        holder.btnAdd.setOnClickListener(v -> uploadCart(food));

        holder.imgFavorite.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) return;

            String uid = auth.getCurrentUser().getUid();
            boolean isFavorite = holder.imgFavorite.getTag() != null && (boolean) holder.imgFavorite.getTag();

            if (!isFavorite) {
                // Add to Favorites Logic
                holder.imgFavorite.setImageResource(R.drawable.ic_favorite_filled);
                holder.imgFavorite.setTag(true);

                Map<String, Object> map = new HashMap<>();
                map.put("name", food.getName());
                map.put("price", food.getPrice());
                map.put("description", food.getDescription());
                map.put("imageUrl", food.getImageUrl());

                db.collection("Favorites").document(uid).collection("UserFav").document(food.getName()).set(map);
                Toast.makeText(context, "Added to favorites ❤️", Toast.LENGTH_SHORT).show();
            } else {
                // Remove from Favorites Logic
                holder.imgFavorite.setImageResource(R.drawable.ic_favorite_border);
                holder.imgFavorite.setTag(false);

                db.collection("Favorites").document(uid).collection("UserFav").document(food.getName()).delete();
                Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadCart(FoodModel food) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(context, "Please Login First!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> map = new HashMap<>();
        map.put("foodName", food.getName());     
        map.put("foodPrice", food.getPrice());   
        map.put("description", food.getDescription());
        map.put("imageUrl", food.getImageUrl());
        map.put("quantity", "1");                

        db.collection("AddToCart")
                .document(uid)
                .collection("UserCart")
                .document(food.getName())
                .set(map)
                .addOnSuccessListener(unused ->
                        Toast.makeText(context, food.getName() + " Added to Cart 🛒", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void filterList(ArrayList<FoodModel> filteredList) {

        list = filteredList;
        notifyDataSetChanged();
    }


    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtFoodName;
        TextView txtFoodPrice;
        TextView txtFoodDesc;

        ImageView imgFood;
        ImageView imgFavorite;

        MaterialButton btnAdd;


        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            txtFoodName = itemView.findViewById(R.id.txtFoodName);
            txtFoodPrice = itemView.findViewById(R.id.txtFoodPrice);
            txtFoodDesc = itemView.findViewById(R.id.txtFoodDesc);
            imgFood = itemView.findViewById(R.id.imgFood);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}


//OrderAdapter

package com.example.fooddeliveryapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fooddeliveryapp.R;
import com.example.fooddeliveryapp.models.OrderModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    Context context;
    ArrayList<OrderModel> list;
    FirebaseFirestore db;

    public OrderAdapter(Context context, ArrayList<OrderModel> list) {
        this.context = context;
        this.list = list;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel model = list.get(position);

        holder.txtItemName.setText(model.getFoodName());
        holder.txtPrice.setText("₹ " + model.getFoodPrice());
        holder.txtDate.setText(model.getOrderDate());
        holder.txtRestaurant.setText("TestyBites");

        if (model.getImageUrl() != null && !model.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(model.getImageUrl())
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.imgFood);
        }

        String status = model.getStatus();
        if (status == null) status = "Pending";
        status = status.trim();

        holder.txtStatus.setText(status);

        if (status.equalsIgnoreCase("Pending")) {
            holder.txtStatus.setTextColor(Color.parseColor("#FF9800"));
            holder.btnRate.setVisibility(View.GONE);

        } else if (status.equalsIgnoreCase("Accepted") || status.equalsIgnoreCase("Preparing")) {
            holder.txtStatus.setTextColor(Color.parseColor("#2196F3"));
            holder.btnRate.setVisibility(View.GONE);

        } else if (status.equalsIgnoreCase("Out for Delivery")) {
            holder.txtStatus.setTextColor(Color.parseColor("#9C27B0"));
            holder.txtStatus.setText("Out for Delivery 🛵");
            holder.btnRate.setVisibility(View.GONE);

        } else if (status.equalsIgnoreCase("Delivered")) {
            holder.txtStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.txtStatus.setText("Delivered ✅");

            holder.btnRate.setVisibility(View.VISIBLE);

        } else if (status.equalsIgnoreCase("Rejected")) {
            holder.txtStatus.setTextColor(Color.parseColor("#F44336"));
            holder.txtStatus.setText("Rejected ❌");
            holder.btnRate.setVisibility(View.GONE);
        }

        holder.btnRate.setOnClickListener(v -> {
            showRatingDialog(model);
        });

        holder.btnReorder.setOnClickListener(v -> {
            Toast.makeText(context, model.getFoodName() + " added to cart again! 🛒", Toast.LENGTH_SHORT).show();
        });
    }

    private void showRatingDialog(OrderModel model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rate_food, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText etReview = dialogView.findViewById(R.id.etReview);
        Button btnSubmitRating = dialogView.findViewById(R.id.btnSubmitRating);

        btnSubmitRating.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String reviewText = etReview.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(context, "Please give at least 1 star! ⭐", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> ratingMap = new HashMap<>();
            ratingMap.put("foodName", model.getFoodName());
            ratingMap.put("rating", rating);
            ratingMap.put("review", reviewText);
            ratingMap.put("userId", model.getUserId());

            db.collection("FoodRatings").document(model.getOrderId())
                    .set(ratingMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Thank you for your feedback! ❤️", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error saving rating", Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtRestaurant, txtDate, txtItemName, txtPrice, txtStatus;
        ImageView imgFood;
        Button btnReorder, btnRate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtRestaurant = itemView.findViewById(R.id.txtRestaurant);
            txtDate = itemView.findViewById(R.id.txtDate);
            imgFood = itemView.findViewById(R.id.imgFood);
            txtItemName = itemView.findViewById(R.id.txtItemName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnReorder = itemView.findViewById(R.id.btnReorder);
            btnRate = itemView.findViewById(R.id.btnRate);
        }
    }
}


//SliderAdapter

package com.example.fooddeliveryapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.fooddeliveryapp.R;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.ViewHolder> {

    Context context;
    List<String> imageList;

    public SliderAdapter(Context context, List<String> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.slider_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context)
                .load(imageList.get(position))
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(com.example.fooddeliveryapp.R.id.sliderImage);
        }
    }
}


//Models

//BannerModel

package com.example.fooddeliveryapp.models;

public class BannerModel {

    String title;
    String imageUrl;
    boolean active;

    public BannerModel() {
    }

    public BannerModel(String title, String imageUrl, boolean active) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.active = active;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}


//CartModel

package com.example.fooddeliveryapp.models;

import com.google.firebase.firestore.DocumentId;

public class CartModel {
    String foodName;
    String foodPrice;
    String quantity; 
    String imageUrl;

    @DocumentId
    String documentId;

    public CartModel() {}

    public String getFoodName() { return foodName; }
    public String getFoodPrice() { return foodPrice; }
    public String getQuantity() { return quantity; }

    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
}


//FoodModel

package com.example.fooddeliveryapp.models;

public class FoodModel {
    private String name;
    private String description;
    private String price;
    private String imageUrl;

    // Firebase requires an empty constructor
    public FoodModel() {
    }

    public FoodModel(String name, String description, String price, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
}


//OrderModel

package com.example.fooddeliveryapp.models;

public class OrderModel {

    String orderId;
    String foodName;
    String foodPrice;
    String imageUrl;
    String userName;
    String userPhone;
    String userAddress;
    String userId;
    String status;
    String orderDate;
    String orderTime;
    String paymentMethod;

    public OrderModel() {
    }

    public OrderModel(String orderId, String foodName, String foodPrice, String imageUrl, String userName, String userPhone, String userAddress, String userId, String status, String orderDate, String orderTime, String paymentMethod) {
        this.orderId = orderId;
        this.foodName = foodName;
        this.foodPrice = foodPrice;
        this.imageUrl = imageUrl;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userAddress = userAddress;
        this.userId = userId;
        this.status = status;
        this.orderDate = orderDate;
        this.orderTime = orderTime;
        this.paymentMethod = paymentMethod;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getFoodPrice() { return foodPrice; }
    public void setFoodPrice(String foodPrice) { this.foodPrice = foodPrice; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getUserAddress() { return userAddress; }
    public void setUserAddress(String userAddress) { this.userAddress = userAddress; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public String getOrderTime() { return orderTime; }
    public void setOrderTime(String orderTime) { this.orderTime = orderTime; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}


//Services

//MyFirebaseMessagingService

package com.example.fooddeliveryapp.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.example.fooddeliveryapp.R;
import com.example.fooddeliveryapp.activities.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            showNotification(remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody());
        }
    }

    private void showNotification(String title, String message) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "order_updates";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Order Updates", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_cart) // Use your app logo
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        manager.notify(1, builder.build());
    }
}








// ==========================================
// SECTION 2: XML LAYOUT (activity_main.xml)
// ==========================================


/* 
activity_admin_add_food

<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F8F9FA"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="20dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Add New Dish 🍽️"
            android:textSize="28sp"
            android:textStyle="bold"
            android:textColor="#111111"
            android:layout_marginTop="10dp"/>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Paste URL to see live photo preview."
            android:textSize="14sp"
            android:textColor="#666666"
            android:layout_marginBottom="20dp"
            android:layout_marginTop="4dp"/>

        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:cardCornerRadius="16dp"
            app:cardElevation="4dp"
            app:strokeWidth="0dp"
            android:backgroundTint="#FFFFFF">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="20dp">

                <ImageView
                    android:id="@+id/imgPreview"
                    android:layout_width="match_parent"
                    android:layout_height="200dp"
                    android:layout_marginBottom="20dp"
                    android:background="#F0F0F0"
                    android:scaleType="centerCrop"
                    android:src="@android:drawable/ic_menu_gallery"
                    app:tint="#CCCCCC"
                    android:padding="60dp"/>

                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
                    app:startIconDrawable="@android:drawable/ic_menu_gallery"
                    android:layout_marginBottom="16dp">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/etImageUrl"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:hint="Paste Exact Image Link (.jpg/.png)"
                        android:inputType="textUri"/>
                </com.google.android.material.textfield.TextInputLayout>

                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
                    android:layout_marginBottom="16dp">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/etFoodName"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:hint="Food Name (e.g., Margherita Pizza)"
                        android:inputType="textCapWords"/>
                </com.google.android.material.textfield.TextInputLayout>

                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
                    app:prefixText="₹ "
                    android:layout_marginBottom="16dp">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/etFoodPrice"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:hint="Price"
                        android:inputType="numberDecimal"/>
                </com.google.android.material.textfield.TextInputLayout>

                <com.google.android.material.textfield.TextInputLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
                    android:layout_marginBottom="25dp">

                    <com.google.android.material.textfield.TextInputEditText
                        android:id="@+id/etFoodDesc"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:hint="Description (Ingredients...)"
                        android:inputType="textMultiLine"
                        android:minLines="3"
                        android:gravity="top"/>
                </com.google.android.material.textfield.TextInputLayout>

                <com.google.android.material.button.MaterialButton
                    android:id="@+id/btnUploadFood"
                    android:layout_width="match_parent"
                    android:layout_height="55dp"
                    android:text="Add Dish to Menu"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:textColor="#FFFFFF"
                    app:backgroundTint="#FF5722"
                    app:cornerRadius="12dp"/>

                <ProgressBar
                    android:id="@+id/progressBar"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:layout_marginTop="10dp"
                    android:visibility="gone"
                    android:indeterminateTint="#FF5722"/>

            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>
    </LinearLayout>
</ScrollView>


activity_admin_order

<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F4F4F4">

    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#FFFFFF"
        app:elevation="0dp">

        <androidx.appcompat.widget.Toolbar
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize">

            <TextView
                android:id="@+id/tvPageTitle"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Restaurant Dashboard 👨‍🍳"
                android:textColor="#282C3F"
                android:textSize="20sp"
                android:textStyle="bold" />
        </androidx.appcompat.widget.Toolbar>

        <com.google.android.material.tabs.TabLayout
            android:id="@+id/adminTabLayout"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:tabIndicatorColor="#FC8019"
            app:tabSelectedTextColor="#FC8019"
            app:tabTextColor="#7E808C"
            app:tabTextAppearance="@style/TextAppearance.AppCompat.Medium"
            app:tabIndicatorHeight="3dp"/>
    </com.google.android.material.appbar.AppBarLayout>

    <androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        android:id="@+id/swipeRefresh"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">

        <FrameLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent">

            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/recyclerViewOrders"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:padding="10dp"
                android:clipToPadding="false"/>

            <LinearLayout
                android:id="@+id/layoutEmptyState"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_gravity="center"
                android:orientation="vertical"
                android:gravity="center"
                android:visibility="gone">

                <ImageView
                    android:layout_width="100dp"
                    android:layout_height="100dp"
                    android:src="@android:drawable/ic_menu_myplaces"
                    app:tint="#CCCCCC"/>

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="15dp"
                    android:text="No Orders Yet!"
                    android:textSize="18sp"
                    android:textStyle="bold"
                    android:textColor="#888888"/>
            </LinearLayout>

        </FrameLayout>
    </androidx.swiperefreshlayout.widget.SwipeRefreshLayout>

    <com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
        android:id="@+id/fabAddFood"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="20dp"
        android:text="Add Dish"
        android:textColor="#FFFFFF"
        app:icon="@android:drawable/ic_input_add"
        app:iconTint="#FFFFFF"
        app:backgroundTint="#FC8019" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>


activity_cart

<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F4F4F4">

    <LinearLayout
        android:id="@+id/cartHeader"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:background="#FFFFFF"
        android:elevation="4dp"
        android:paddingHorizontal="16dp"
        app:layout_constraintTop_toTopOf="parent">

        <ImageView
            android:id="@+id/btnBack"
            android:layout_width="28dp"
            android:layout_height="28dp"
            android:src="@android:drawable/ic_menu_revert"
            app:tint="#111111"/>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Your Cart"
            android:textSize="18sp"
            android:textStyle="bold"
            android:textColor="#111111"
            android:layout_marginStart="16dp"/>
    </LinearLayout>

    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:fillViewport="true"
        app:layout_constraintTop_toBottomOf="@id/cartHeader"
        app:layout_constraintBottom_toTopOf="@id/bottomCheckoutBar">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingBottom="20dp">

            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/recyclerCart"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:nestedScrollingEnabled="false"
                android:background="#FFFFFF"
                android:paddingTop="8dp"/>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Bill Details"
                android:textSize="16sp"
                android:textStyle="bold"
                android:textColor="#333333"
                android:layout_marginStart="16dp"
                android:layout_marginTop="20dp"/>

            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_margin="16dp"
                app:cardCornerRadius="12dp"
                app:cardElevation="0dp"
                app:strokeWidth="1dp"
                app:strokeColor="#DDDDDD"
                android:backgroundTint="#FFFFFF">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp">

                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal">
                        <TextView android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:text="Item Total" android:textColor="#555555" android:textSize="14sp"/>
                        <TextView android:id="@+id/txtItemTotal" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="₹ 0" android:textColor="#333333" android:textSize="14sp"/>
                    </LinearLayout>

                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="10dp">
                        <TextView android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:text="Delivery Fee" android:textColor="#555555" android:textSize="14sp"/>
                        <TextView android:id="@+id/txtDeliveryFee" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="₹ 40" android:textColor="#4CAF50" android:textSize="14sp"/>
                    </LinearLayout>

                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:layout_marginTop="10dp">
                        <TextView android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:text="Taxes &amp; Charges" android:textColor="#555555" android:textSize="14sp"/>
                        <TextView android:id="@+id/txtTaxes" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="₹ 25" android:textColor="#333333" android:textSize="14sp"/>
                    </LinearLayout>

                    <View android:layout_width="match_parent" android:layout_height="1dp" android:background="#EEEEEE" android:layout_marginVertical="12dp"/>

                    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal">
                        <TextView android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:text="To Pay" android:textColor="#111111" android:textStyle="bold" android:textSize="16sp"/>
                        <TextView android:id="@+id/txtGrandTotal" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="₹ 0" android:textColor="#111111" android:textStyle="bold" android:textSize="16sp"/>
                    </LinearLayout>

                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

        </LinearLayout>
    </androidx.core.widget.NestedScrollView>

    <LinearLayout
        android:id="@+id/bottomCheckoutBar"
        android:layout_width="match_parent"
        android:layout_height="80dp"
        android:orientation="horizontal"
        android:background="#FFFFFF"
        android:elevation="16dp"
        android:paddingHorizontal="16dp"
        android:gravity="center_vertical"
        app:layout_constraintBottom_toBottomOf="parent">

        <LinearLayout
            android:layout_width="0dp"
            android:layout_weight="1"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="PAY USING"
                android:textSize="10sp"
                android:textColor="#888888"
                android:textStyle="bold"/>

            <TextView
                android:id="@+id/txtBottomTotal"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="₹ 0"
                android:textSize="18sp"
                android:textStyle="bold"
                android:textColor="#111111"
                android:layout_marginTop="2dp"/>
        </LinearLayout>

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnProceedPay"
            android:layout_width="wrap_content"
            android:layout_height="55dp"
            android:text="Proceed to Pay"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="#FFFFFF"
            app:backgroundTint="#FF5722"
            app:cornerRadius="12dp"
            app:icon="@android:drawable/ic_secure"
            app:iconTint="#FFFFFF"/>
    </LinearLayout>

</androidx.constraintlayout.widget.ConstraintLayout>


activity_delivery_dashboard

<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F8F9FA">

    <LinearLayout
        android:id="@+id/headerRider"
        android:layout_width="match_parent"
        android:layout_height="70dp"
        android:background="#2E3192"
        android:gravity="center_vertical"
        android:orientation="horizontal"
        android:paddingHorizontal="20dp"
        app:layout_constraintTop_toTopOf="parent">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Rider Dashboard 🛵"
            android:textColor="#FFFFFF"
            android:textSize="22sp"
            android:textStyle="bold" />
    </LinearLayout>

    <androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        android:id="@+id/swipeRefreshDelivery"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:layout_constraintTop_toBottomOf="@id/headerRider"
        app:layout_constraintBottom_toBottomOf="parent">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/deliveryRecyclerView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:padding="10dp"
            android:clipToPadding="false"/>
    </androidx.swiperefreshlayout.widget.SwipeRefreshLayout>

    <LinearLayout
        android:id="@+id/layoutNoDelivery"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:gravity="center"
        android:visibility="gone"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <ImageView
            android:layout_width="100dp"
            android:layout_height="100dp"
            android:src="@android:drawable/ic_menu_compass"
            app:tint="#CCCCCC"/>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="15dp"
            android:text="No Active Deliveries"
            android:textSize="20sp"
            android:textStyle="bold"
            android:textColor="#888888"/>
    </LinearLayout>
</androidx.constraintlayout.widget.ConstraintLayout>


activity_edit_profile

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F6F6F6"
    android:orientation="vertical">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:background="#FF5722"
        android:gravity="center_vertical"
        android:orientation="horizontal"
        android:paddingHorizontal="16dp">

        <ImageView
            android:id="@+id/btnBack"
            android:layout_width="28dp"
            android:layout_height="28dp"
            android:src="@android:drawable/ic_menu_revert"
            app:tint="@android:color/white"/>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="16dp"
            android:text="Edit Profile"
            android:textColor="@android:color/white"
            android:textSize="20sp"
            android:textStyle="bold"/>
    </LinearLayout>

    <androidx.cardview.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        app:cardCornerRadius="15dp"
        app:cardElevation="4dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="20dp">

            <com.google.android.material.textfield.TextInputLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content">
                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/etEditName"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:hint="Full Name"/>
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.textfield.TextInputLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="15dp">
                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/etEditPhone"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:inputType="phone"
                    android:hint="Phone Number"/>
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.textfield.TextInputLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="15dp">
                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/etEditAddress"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:inputType="textMultiLine"
                    android:minLines="3"
                    android:hint="Delivery Address"/>
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btnSaveProfile"
                android:layout_width="match_parent"
                android:layout_height="55dp"
                android:layout_marginTop="25dp"
                android:backgroundTint="#FF5722"
                android:text="Save Changes"
                android:textColor="@android:color/white"
                app:cornerRadius="12dp"/>
        </LinearLayout>
    </androidx.cardview.widget.CardView>
</LinearLayout>


activity_favorite

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#F8F9FA">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="16dp"
        android:background="#FFFFFF"
        android:elevation="4dp"
        android:gravity="center_vertical">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="My Favorites ❤️"
            android:textSize="20sp"
            android:textStyle="bold"
            android:textColor="#333333"/>
    </LinearLayout>

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recyclerFavorites"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:padding="8dp"
            android:clipToPadding="false"/>

        <LinearLayout
            android:id="@+id/layoutNoFav"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:orientation="vertical"
            android:gravity="center"
            android:visibility="gone">

            <ImageView
                android:layout_width="100dp"
                android:layout_height="100dp"
                android:src="@android:drawable/ic_menu_save"
                android:alpha="0.3"
                android:contentDescription="No Favorites"/>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Your heart list is empty!"
                android:textColor="#888888"
                android:layout_marginTop="12dp"
                android:textSize="16sp"/>
        </LinearLayout>
    </FrameLayout>
</LinearLayout>


activity_login

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/bg_gradient">

    <!-- White curved container -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginTop="180dp"
        android:background="@drawable/bg_curve"
        android:orientation="vertical"
        android:padding="24dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Login"
            android:textSize="28sp"
            android:textStyle="bold"
            android:textColor="#222"/>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Welcome back, login to continue"
            android:textColor="#777"
            android:layout_marginBottom="20dp"/>

        <com.google.android.material.textfield.TextInputLayout
            style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Email">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etEmail"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="textEmailAddress"/>
        </com.google.android.material.textfield.TextInputLayout>

        <com.google.android.material.textfield.TextInputLayout
            style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Password"
            android:layout_marginTop="12dp"
            app:endIconMode="password_toggle">

            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/etPassword"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="textPassword"/>
        </com.google.android.material.textfield.TextInputLayout>

        <Button
            android:id="@+id/btnLogin"
            android:layout_width="match_parent"
            android:layout_height="55dp"
            android:text="LOGIN"
            android:textStyle="bold"
            android:textColor="#FFFFFF"
            android:backgroundTint="#E23744"
            android:layout_marginTop="20dp"/>

        <TextView
            android:id="@+id/tvRegister"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="New user? Create account"
            android:textStyle="bold"
            android:textColor="#E23744"
            android:layout_marginTop="20dp"/>

        <TextView
            android:id="@+id/tvAdminAccess"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="v1.0"
            android:textSize="12sp"
            android:textColor="#000000"
            android:layout_gravity="center"
            android:layout_marginTop="30dp"/>

    </LinearLayout>

    <!-- Logo -->
    <ImageView
        android:layout_width="120dp"
        android:layout_height="120dp"
        android:src="@mipmap/ic_launcher_round"
        android:layout_centerHorizontal="true"
        android:layout_marginTop="60dp"/>

</RelativeLayout>


activity_main

<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#FFFFFF">

    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fillViewport="true"
        android:layout_marginBottom="60dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingBottom="20dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:padding="16dp"
                android:gravity="center_vertical">

                <ImageView
                    android:layout_width="28dp"
                    android:layout_height="28dp"
                    android:src="@android:drawable/ic_menu_mylocation"
                    app:tint="#FF5722"/>

                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_weight="1"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:layout_marginStart="10dp">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Home"
                        android:textSize="18sp"
                        android:textStyle="bold"
                        android:textColor="#333333"/>

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Surat, Gujarat"
                        android:textSize="12sp"
                        android:textColor="#757575"/>
                </LinearLayout>

                <ImageView
                    android:layout_width="36dp"
                    android:layout_height="36dp"
                    android:src="@android:drawable/ic_menu_myplaces"
                    android:background="@drawable/bg_button_rounded"
                    android:backgroundTint="#F5F5F5"
                    android:padding="6dp"/>
            </LinearLayout>

            <androidx.cardview.widget.CardView
                android:layout_width="match_parent"
                android:layout_height="50dp"
                android:layout_marginHorizontal="16dp"
                android:layout_marginBottom="16dp"
                app:cardCornerRadius="12dp"
                app:cardElevation="2dp"
                android:backgroundTint="#F8F9FA">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:orientation="horizontal"
                    android:gravity="center_vertical"
                    android:paddingHorizontal="15dp">

                    <ImageView
                        android:src="@android:drawable/ic_menu_search"
                        android:layout_width="20dp"
                        android:layout_height="20dp"
                        app:tint="#FF5722"/>

                    <EditText
                        android:id="@+id/etSearch"
                        android:hint="Search for 'Biryani' or 'Pizza'"
                        android:textSize="14sp"
                        android:background="@null"
                        android:layout_marginStart="10dp"
                        android:layout_width="0dp"
                        android:layout_weight="1"
                        android:layout_height="match_parent"
                        android:minHeight="48dp"/>
                </LinearLayout>
            </androidx.cardview.widget.CardView>

            <androidx.viewpager2.widget.ViewPager2
                android:id="@+id/viewPagerBanner"
                android:layout_width="match_parent"
                android:layout_height="160dp"
                android:layout_marginHorizontal="16dp"/>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="What's on your mind?"
                android:textSize="18sp"
                android:textStyle="bold"
                android:textColor="#333333"
                android:layout_marginStart="16dp"
                android:layout_marginTop="20dp"/>

            <HorizontalScrollView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:scrollbars="none"
                android:paddingHorizontal="16dp"
                android:layout_marginTop="10dp">

                <LinearLayout
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal">

                    <LinearLayout android:id="@+id/catPizza" android:orientation="vertical" android:gravity="center" android:layout_width="80dp" android:layout_height="wrap_content">
                        <ImageView android:id="@+id/imgCatPizza" android:layout_width="65dp" android:layout_height="65dp" android:background="@drawable/bg_button_rounded" android:backgroundTint="#FFF0EC"/>
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Pizza" android:textSize="12sp" android:layout_marginTop="4dp" android:textColor="#555555" android:textStyle="bold"/>
                    </LinearLayout>

                    <LinearLayout android:id="@+id/catBurger" android:orientation="vertical" android:gravity="center" android:layout_width="80dp" android:layout_height="wrap_content">
                        <ImageView android:id="@+id/imgCatBurger" android:layout_width="65dp" android:layout_height="65dp" android:background="@drawable/bg_button_rounded" android:backgroundTint="#FFF0EC"/>
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Burger" android:textSize="12sp" android:layout_marginTop="4dp" android:textColor="#555555" android:textStyle="bold"/>
                    </LinearLayout>

                    <LinearLayout android:id="@+id/catBiryani" android:orientation="vertical" android:gravity="center" android:layout_width="80dp" android:layout_height="wrap_content">
                        <ImageView android:id="@+id/imgCatBiryani" android:layout_width="65dp" android:layout_height="65dp" android:background="@drawable/bg_button_rounded" android:backgroundTint="#FFF0EC"/>
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Biryani" android:textSize="12sp" android:layout_marginTop="4dp" android:textColor="#555555" android:textStyle="bold"/>
                    </LinearLayout>

                    <LinearLayout android:id="@+id/catDessert" android:orientation="vertical" android:gravity="center" android:layout_width="80dp" android:layout_height="wrap_content">
                        <ImageView android:id="@+id/imgCatDessert" android:layout_width="65dp" android:layout_height="65dp" android:background="@drawable/bg_button_rounded" android:backgroundTint="#FFF0EC"/>
                        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Desserts" android:textSize="12sp" android:layout_marginTop="4dp" android:textColor="#555555" android:textStyle="bold"/>
                    </LinearLayout>
                </LinearLayout>
            </HorizontalScrollView>

            <View
                android:layout_width="match_parent"
                android:layout_height="8dp"
                android:background="#F4F4F4"
                android:layout_marginTop="20dp"/>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Explore TestyBites Specials"
                android:textSize="18sp"
                android:textStyle="bold"
                android:textColor="#333333"
                android:layout_marginStart="16dp"
                android:layout_marginTop="20dp"/>

            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/recyclerRecommended"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:nestedScrollingEnabled="false"
                android:layout_marginTop="10dp"/>

        </LinearLayout>
    </androidx.core.widget.NestedScrollView>

    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottomNav"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom"
        android:background="#FFFFFF"
        app:itemIconTint="@color/primary"
        app:itemTextColor="@color/primary"
        app:menu="@menu/bottom_menu"
        android:elevation="10dp"/>

</androidx.coordinatorlayout.widget.CoordinatorLayout>


activity_order

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="#F8F9FA">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:background="#FFFFFF"
        android:elevation="4dp"
        android:paddingHorizontal="16dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="My Orders"
            android:textSize="20sp"
            android:textStyle="bold"
            android:textColor="#111111"/>
    </LinearLayout>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerViewOrders"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:paddingBottom="20dp"
        android:clipToPadding="false"/>

</LinearLayout>


activity_order_success

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:background="#FFFFFF"
    android:padding="20dp">

    <com.airbnb.lottie.LottieAnimationView
        android:id="@+id/lottieSuccess"
        android:layout_width="200dp"
        android:layout_height="200dp"
        app:lottie_rawRes="@raw/success_anim"
        app:lottie_autoPlay="true"
        app:lottie_loop="false"
        android:layout_marginBottom="16dp"/>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Order Placed Successfully!"
        android:textSize="24sp"
        android:textStyle="bold"
        android:textColor="#4CAF50"
        android:textAlignment="center"/>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Your delicious food is on the way 🚀"
        android:textSize="14sp"
        android:textColor="#757575"
        android:textAlignment="center"
        android:layout_marginTop="12dp"
        android:layout_marginHorizontal="20dp"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:background="@drawable/bg_button_rounded"
        android:backgroundTint="#F5F5F5"
        android:padding="16dp"
        android:gravity="center"
        android:layout_marginTop="30dp"
        android:layout_marginHorizontal="20dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Order ID: "
            android:textSize="16sp"
            android:textColor="#555555"/>

        <TextView
            android:id="@+id/txtOrderId"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="#FD12345"
            android:textSize="18sp"
            android:textStyle="bold"
            android:textColor="#111111"/>

    </LinearLayout>

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnTrackOrder"
        android:layout_width="match_parent"
        android:layout_height="55dp"
        android:text="Track My Order 📍"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="#FF5722"
        app:backgroundTint="#FFFFFF"
        app:strokeColor="#FF5722"
        app:strokeWidth="2dp"
        app:cornerRadius="12dp"
        android:layout_marginTop="20dp"
        android:layout_marginHorizontal="20dp"/>

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnBackToHome"
        android:layout_width="match_parent"
        android:layout_height="55dp"
        android:text="Back to Home"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="#FFFFFF"
        app:backgroundTint="#FF5722"
        app:cornerRadius="12dp"
        android:layout_marginTop="40dp"
        android:layout_marginHorizontal="20dp"/>

</LinearLayout>


activity_profile

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F4F4F4"
    android:orientation="vertical">

    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="220dp"
        android:background="#FF5722"
        android:padding="20dp">

        <ImageView
            android:id="@+id/btnBackProfile"
            android:layout_width="30dp"
            android:layout_height="30dp"
            android:src="@android:drawable/ic_menu_revert"
            app:tint="#FFFFFF" />

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:gravity="center"
            android:orientation="vertical">

            <com.google.android.material.imageview.ShapeableImageView
                android:id="@+id/imgProfilePic"
                android:layout_width="90dp"
                android:layout_height="90dp"
                android:padding="2dp"
                android:scaleType="centerCrop"
                app:shapeAppearanceOverlay="@style/ShapeAppearance.MaterialComponents.MediumComponent"
                app:strokeColor="#FFFFFF"
                app:strokeWidth="3dp" />

            <TextView
                android:id="@+id/tvUserName"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="10dp"
                android:text="Virat Kohli"
                android:textColor="#FFFFFF"
                android:textSize="22sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/tvUserEmail"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="virat1@gmail.com"
                android:textColor="#FFE0B2"
                android:textSize="14sp" />
        </LinearLayout>
    </RelativeLayout>

    <androidx.cardview.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginHorizontal="16dp"
        android:layout_marginTop="10dp"
        app:cardCornerRadius="12dp"
        app:cardElevation="4dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp"
            android:background="#FFF8E1">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="TestyBites Rewards ⭐"
                android:textColor="#FF8F00"
                android:textStyle="bold"
                android:textSize="18sp"/>

            <TextView
                android:id="@+id/txtRewardPoints"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Loading rewards..."
                android:textColor="#333333"
                android:layout_marginTop="4dp"
                android:textSize="15sp" />
        </LinearLayout>
    </androidx.cardview.widget.CardView>

    <androidx.cardview.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginHorizontal="16dp"
        android:layout_marginTop="10dp"
        app:cardCornerRadius="15dp"
        app:cardElevation="8dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="10dp">

            <LinearLayout
                android:id="@+id/layoutEditProfile"
                android:layout_width="match_parent"
                android:layout_height="60dp"
                android:background="?attr/selectableItemBackground"
                android:clickable="true"
                android:gravity="center_vertical"
                android:paddingHorizontal="15dp">

                <ImageView
                    android:layout_width="24dp"
                    android:layout_height="24dp"
                    android:src="@android:drawable/ic_menu_edit"
                    app:tint="#757575" />

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="20dp"
                    android:layout_weight="1"
                    android:text="Edit Profile"
                    android:textColor="#333333"
                    android:textSize="16sp" />

                <ImageView
                    android:layout_width="20dp"
                    android:layout_height="20dp"
                    android:src="@android:drawable/arrow_down_float"
                    android:rotation="-90" />
            </LinearLayout>

            <View android:layout_width="match_parent" android:layout_height="1dp" android:background="#EEEEEE" />

            <LinearLayout
                android:id="@+id/layoutYourOrders"
                android:layout_width="match_parent"
                android:layout_height="60dp"
                android:background="?attr/selectableItemBackground"
                android:clickable="true"
                android:gravity="center_vertical"
                android:paddingHorizontal="15dp">

                <ImageView
                    android:layout_width="24dp"
                    android:layout_height="24dp"
                    android:src="@android:drawable/ic_menu_recent_history"
                    app:tint="#757575" />

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="20dp"
                    android:layout_weight="1"
                    android:text="Your Orders"
                    android:textColor="#333333"
                    android:textSize="16sp" />

                <ImageView
                    android:layout_width="20dp"
                    android:layout_height="20dp"
                    android:src="@android:drawable/arrow_down_float"
                    android:rotation="-90" />
            </LinearLayout>

            <View android:layout_width="match_parent" android:layout_height="1dp" android:background="#EEEEEE" />

            <LinearLayout
                android:id="@+id/layoutSavedAddress"
                android:layout_width="match_parent"
                android:layout_height="60dp"
                android:background="?attr/selectableItemBackground"
                android:clickable="true"
                android:gravity="center_vertical"
                android:paddingHorizontal="15dp">

                <ImageView
                    android:layout_width="24dp"
                    android:layout_height="24dp"
                    android:src="@android:drawable/ic_menu_mylocation"
                    app:tint="#757575" />

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="20dp"
                    android:layout_weight="1"
                    android:text="Saved Addresses"
                    android:textColor="#333333"
                    android:textSize="16sp" />

                <ImageView
                    android:layout_width="20dp"
                    android:layout_height="20dp"
                    android:src="@android:drawable/arrow_down_float"
                    android:rotation="-90" />
            </LinearLayout>

            <View android:layout_width="match_parent" android:layout_height="1dp" android:background="#EEEEEE" />

            <LinearLayout
                android:id="@+id/layoutFavorites"
                android:layout_width="match_parent"
                android:layout_height="60dp"
                android:background="?attr/selectableItemBackground"
                android:clickable="true"
                android:gravity="center_vertical"
                android:paddingHorizontal="15dp">

                <ImageView
                    android:layout_width="24dp"
                    android:layout_height="24dp"
                    android:src="@android:drawable/btn_star_big_on"
                    app:tint="#757575" />

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="20dp"
                    android:layout_weight="1"
                    android:text="My Favorites ❤️"
                    android:textColor="#333333"
                    android:textSize="16sp" />

                <ImageView
                    android:layout_width="20dp"
                    android:layout_height="20dp"
                    android:src="@android:drawable/arrow_down_float"
                    android:rotation="-90" />
            </LinearLayout>
        </LinearLayout>
    </androidx.cardview.widget.CardView>

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnLogout"
        android:layout_width="match_parent"
        android:layout_height="55dp"
        android:layout_margin="30dp"
        android:text="Logout"
        android:textAllCaps="false"
        android:textColor="#FFFFFF"
        android:textSize="16sp"
        android:textStyle="bold"
        app:backgroundTint="#D32F2F"
        app:cornerRadius="10dp" />

</LinearLayout>


activity_register

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/bg_gradient">

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginTop="180dp"
        android:background="@drawable/bg_curve">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="24dp"
            android:orientation="vertical">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Create Account"
                android:textSize="28sp"
                android:textStyle="bold"/>

            <com.google.android.material.textfield.TextInputLayout
                style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                android:hint="Full Name"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/etName"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"/>

            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.textfield.TextInputLayout
                style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Email"
                android:layout_marginTop="10dp">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/etEmail"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"/>
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.textfield.TextInputLayout
                style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Phone"
                android:layout_marginTop="10dp">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/etPhone"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"/>
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.textfield.TextInputLayout
                style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Password"
                android:layout_marginTop="10dp"
                app:endIconMode="password_toggle">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/etPassword"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"/>
            </com.google.android.material.textfield.TextInputLayout>

            <com.google.android.material.textfield.TextInputLayout
                style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="12dp"
                android:hint="Delivery Address">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/etAddress"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:minLines="2"
                    android:gravity="top"/>
            </com.google.android.material.textfield.TextInputLayout>

            <Button
                android:id="@+id/btnRegister"
                android:layout_width="match_parent"
                android:layout_height="55dp"
                android:text="SIGN UP"
                android:textStyle="bold"
                android:textColor="#FFF"
                android:backgroundTint="#E23744"
                android:layout_marginTop="20dp"/>

            <TextView
                android:id="@+id/tvLogin"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Already have an account? Login"
                android:textStyle="bold"
                android:textColor="#E23744"
                android:layout_gravity="center"
                android:layout_marginTop="20dp"
                android:padding="10dp"/>

        </LinearLayout>

    </ScrollView>

    <ImageView
        android:layout_width="120dp"
        android:layout_height="120dp"
        android:src="@mipmap/ic_launcher_round"
        android:layout_centerHorizontal="true"
        android:layout_marginTop="60dp"/>

</RelativeLayout>


activity_saved_addresses

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F6F6F6"
    android:orientation="vertical">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:background="#FF5722"
        android:gravity="center_vertical"
        android:orientation="horizontal"
        android:paddingHorizontal="16dp">

        <ImageView
            android:id="@+id/btnBack"
            android:layout_width="28dp"
            android:layout_height="28dp"
            android:src="@android:drawable/ic_menu_revert"
            app:tint="@android:color/white"/>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="16dp"
            android:text="Saved Addresses"
            android:textColor="@android:color/white"
            android:textSize="20sp"
            android:textStyle="bold"/>
    </LinearLayout>

    <androidx.cardview.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        app:cardCornerRadius="15dp"
        app:cardElevation="4dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="20dp"
            android:gravity="center_vertical">

            <ImageView
                android:layout_width="40dp"
                android:layout_height="40dp"
                android:background="@drawable/bg_image_rounded"
                android:backgroundTint="#FFF0EC"
                android:padding="8dp"
                android:src="@android:drawable/ic_menu_mylocation"
                app:tint="#FF5722"/>

            <LinearLayout
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"
                android:layout_marginStart="15dp"
                android:orientation="vertical">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Home Address"
                    android:textColor="#333333"
                    android:textSize="16sp"
                    android:textStyle="bold"/>

                <TextView
                    android:id="@+id/tvSavedAddress"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="5dp"
                    android:text="Loading your address..."
                    android:textColor="#757575"
                    android:textSize="14sp"/>
            </LinearLayout>

            <ImageView
                android:id="@+id/btnEditAddress"
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:src="@android:drawable/ic_menu_edit"
                app:tint="#FF5722"/>
        </LinearLayout>
    </androidx.cardview.widget.CardView>
</LinearLayout>


activity_splash

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#FFFFFF"
    android:gravity="center">

    <ImageView
        android:id="@+id/logo"
        android:layout_width="160dp"
        android:layout_height="160dp"
        android:layout_centerInParent="true"
        android:src="@mipmap/ic_launcher_round"
        android:backgroundTint="#FFF0EC"
        android:padding="20dp"
        android:elevation="8dp"/>

    <TextView
        android:id="@+id/tvAppName"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@id/logo"
        android:layout_centerHorizontal="true"
        android:layout_marginTop="20dp"
        android:text="TestyBites"
        android:textColor="#FF5722"
        android:textSize="36sp"
        android:textStyle="bold" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@id/tvAppName"
        android:layout_centerHorizontal="true"
        android:layout_marginTop="8dp"
        android:text="Delicious food, delivered fast! 🚀"
        android:textColor="#666666"
        android:textSize="16sp"
        android:textStyle="italic"/>

    <ProgressBar
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_centerHorizontal="true"
        android:layout_marginBottom="60dp"
        android:indeterminateTint="#FF5722"/>

</RelativeLayout>


activity_tracking

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <org.osmdroid.views.MapView
        android:id="@+id/map"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

    <androidx.cardview.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_margin="20dp"
        app:cardCornerRadius="15dp"
        app:cardElevation="10dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="20dp"
            android:background="#FFFFFF">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Live Tracking Simulated 🚀"
                android:textColor="#FF5722"
                android:textStyle="bold"
                android:textSize="18sp"/>

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Rider is reaching you in 5 mins"
                android:textColor="#757575"
                android:layout_marginTop="8dp"
                android:textSize="14sp"/>

        </LinearLayout>
    </androidx.cardview.widget.CardView>
</RelativeLayout>


dialog_mock_payment

<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="320dp"
    android:layout_height="wrap_content"
    android:layout_gravity="center"
    app:cardCornerRadius="20dp"
    app:cardElevation="10dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:orientation="vertical"
        android:padding="30dp"
        android:background="#FFFFFF">

        <ProgressBar
            android:layout_width="60dp"
            android:layout_height="60dp"
            android:indeterminateTint="#0C4AA6" />

        <TextView
            android:id="@+id/tvMockStatus"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="20dp"
            android:text="Processing Payment..."
            android:textColor="#333333"
            android:textSize="20sp"
            android:textStyle="bold" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="Please do not close or press back"
            android:textColor="#757575"
            android:textSize="14sp" />

        <TextView
            android:id="@+id/tvMockAmount"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="20dp"
            android:text="₹ 0"
            android:textColor="#FF5722"
            android:textSize="28sp"
            android:textStyle="bold" />

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="25dp"
            android:gravity="center"
            android:orientation="horizontal">

            <ImageView
                android:layout_width="18dp"
                android:layout_height="18dp"
                android:src="@android:drawable/ic_secure"
                app:tint="#4CAF50" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="6dp"
                android:text="100% Secure Connection"
                android:textColor="#4CAF50"
                android:textSize="12sp"
                android:textStyle="bold" />
        </LinearLayout>
    </LinearLayout>
</androidx.cardview.widget.CardView>


dialog_rate_food

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="24dp"
    android:background="@drawable/bg_button_rounded"
    android:backgroundTint="#FFFFFF"
    android:gravity="center_horizontal">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="How was your food? 😋"
        android:textSize="20sp"
        android:textStyle="bold"
        android:textColor="#111111"
        android:layout_marginBottom="16dp"/>

    <RatingBar
        android:id="@+id/ratingBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:numStars="5"
        android:stepSize="1.0"
        android:progressTint="#FFC107"
        android:layout_marginBottom="16dp"/>

    <EditText
        android:id="@+id/etReview"
        android:layout_width="match_parent"
        android:layout_height="100dp"
        android:hint="Write your feedback (optional)"
        android:background="@drawable/bg_button_rounded"
        android:backgroundTint="#F5F5F5"
        android:padding="12dp"
        android:gravity="top|start"
        android:textSize="14sp"
        android:layout_marginBottom="24dp"/>

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnSubmitRating"
        android:layout_width="match_parent"
        android:layout_height="55dp"
        android:text="Submit Rating"
        android:textColor="#FFFFFF"
        android:textSize="16sp"
        android:textStyle="bold"
        app:backgroundTint="#4CAF50"
        app:cornerRadius="12dp"/>

</LinearLayout>


item_admin_order

<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="10dp"
    android:layout_marginVertical="8dp"
    app:cardCornerRadius="16dp"
    app:cardElevation="4dp"
    android:backgroundTint="#FFFFFF">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical">

            <TextView
                android:id="@+id/tvAdminOrderId"
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"
                android:text="Order #FD1234"
                android:textColor="#111111"
                android:textSize="16sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/tvAdminStatus"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Pending"
                android:paddingHorizontal="12dp"
                android:paddingVertical="4dp"
                android:background="@drawable/bg_button_rounded"
                android:backgroundTint="#FFF3E0"
                android:textColor="#FF9800"
                android:textSize="12sp"
                android:textStyle="bold"/>
        </LinearLayout>

        <TextView
            android:id="@+id/tvAdminDateTime"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Feb 22, 2026 | 11:30 AM"
            android:textSize="12sp"
            android:textColor="#888888"
            android:layout_marginTop="2dp"/>

        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:background="#EEEEEE"
            android:layout_marginVertical="12dp"/>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical">

            <ImageView
                android:id="@+id/imgAdminFood"
                android:layout_width="60dp"
                android:layout_height="60dp"
                android:scaleType="centerCrop"
                android:background="#F0F0F0"
                android:src="@mipmap/ic_launcher"
                app:shapeAppearanceOverlay="@style/ShapeAppearance.MaterialComponents.SmallComponent"/>

            <LinearLayout
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:layout_marginStart="12dp">

                <TextView
                    android:id="@+id/tvAdminFoodName"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Margherita Pizza"
                    android:textColor="#333333"
                    android:textSize="16sp"
                    android:textStyle="bold"/>

                <TextView
                    android:id="@+id/tvAdminFoodPrice"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="₹ 299"
                    android:textColor="#FF5722"
                    android:textSize="14sp"
                    android:textStyle="bold"
                    android:layout_marginTop="4dp"/>
            </LinearLayout>
        </LinearLayout>

        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:background="#EEEEEE"
            android:layout_marginVertical="12dp"/>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:background="#F9F9F9"
            android:padding="12dp">

            <TextView
                android:id="@+id/tvAdminCustomerName"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="👤 Name: John Doe"
                android:textColor="#555555"
                android:textSize="14sp"/>

            <TextView
                android:id="@+id/tvAdminCustomerPhone"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="📞 Phone: 9876543210"
                android:textColor="#555555"
                android:textSize="14sp"
                android:layout_marginTop="4dp"/>

            <TextView
                android:id="@+id/tvAdminCustomerAddress"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="📍 Address: 123 Street, City"
                android:textColor="#555555"
                android:textSize="14sp"
                android:layout_marginTop="4dp"/>

            <TextView
                android:id="@+id/tvAdminPaymentMethod"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="💳 Payment: Cash on Delivery"
                android:textColor="#4CAF50"
                android:textStyle="bold"
                android:textSize="14sp"
                android:layout_marginTop="6dp"/>
        </LinearLayout>

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnUpdateStatus"
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:layout_marginTop="16dp"
            android:text="Update Order Status"
            android:textColor="#FFFFFF"
            app:backgroundTint="#0C4AA6"
            app:cornerRadius="8dp"
            app:icon="@android:drawable/ic_menu_edit"
            app:iconGravity="textStart"
            app:iconTint="#FFFFFF"/>

    </LinearLayout>
</com.google.android.material.card.MaterialCardView>


item_banner

<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:cardCornerRadius="16dp"
    app:cardElevation="4dp">
    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <ImageView
            android:id="@+id/imgBanner"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:scaleType="centerCrop"/>

        <TextView
            android:id="@+id/txtBannerTitle"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textColor="#FFFFFF"
            android:textSize="16sp"
            android:textStyle="bold"
            android:layout_gravity="bottom"
            android:padding="12dp"
            android:background="#80000000"/>

    </FrameLayout>
</androidx.cardview.widget.CardView>


item_cart

<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="16dp"
    android:background="#FFFFFF">

    <ImageView
        android:id="@+id/imgVegNonVeg"
        android:layout_width="16dp"
        android:layout_height="16dp"
        android:src="@android:drawable/presence_online"
        app:tint="#4CAF50"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        android:layout_marginTop="4dp"/>

    <TextView
        android:id="@+id/txtCartFoodName"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:text="Farmhouse Pizza"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="#333333"
        android:layout_marginStart="8dp"
        android:layout_marginEnd="10dp"
        app:layout_constraintStart_toEndOf="@id/imgVegNonVeg"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintEnd_toStartOf="@id/layoutQuantity"/>

    <TextView
        android:id="@+id/txtCartFoodPrice"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="₹ 299"
        android:textColor="#555555"
        android:textSize="14sp"
        android:layout_marginTop="6dp"
        app:layout_constraintStart_toStartOf="@id/txtCartFoodName"
        app:layout_constraintTop_toBottomOf="@id/txtCartFoodName"/>

    <LinearLayout
        android:id="@+id/layoutQuantity"
        android:layout_width="wrap_content"
        android:layout_height="32dp"
        android:orientation="horizontal"
        android:background="@drawable/bg_button_rounded"
        android:backgroundTint="#FFF0EC"
        android:gravity="center"
        android:paddingHorizontal="8dp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="@id/txtCartFoodPrice">

        <TextView
            android:id="@+id/btnMinus"
            android:layout_width="28dp"
            android:layout_height="match_parent"
            android:text="−"
            android:textSize="18sp"
            android:textStyle="bold"
            android:textColor="#FF5722"
            android:gravity="center"/>

        <TextView
            android:id="@+id/txtQuantity"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="1"
            android:textColor="#FF5722"
            android:textStyle="bold"
            android:textSize="14sp"
            android:layout_marginHorizontal="6dp"/>

        <TextView
            android:id="@+id/btnPlus"
            android:layout_width="28dp"
            android:layout_height="match_parent"
            android:text="+"
            android:textSize="18sp"
            android:textStyle="bold"
            android:textColor="#FF5722"
            android:gravity="center"/>
    </LinearLayout>

    <View
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:background="#EEEEEE"
        app:layout_constraintTop_toBottomOf="@id/txtCartFoodPrice"
        android:layout_marginTop="16dp"/>

</androidx.constraintlayout.widget.ConstraintLayout>


item_food

<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="16dp"
    android:layout_marginVertical="10dp"
    app:cardCornerRadius="16dp"
    app:cardElevation="2dp"
    android:backgroundTint="#FFFFFF">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp">

        <TextView
            android:id="@+id/txtFoodName"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:text="Food Name"
            android:textSize="18sp"
            android:textStyle="bold"
            android:textColor="#333333"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintEnd_toStartOf="@id/imgFood"/>

        <TextView
            android:id="@+id/txtFoodPrice"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="₹ 000"
            android:textColor="#111111"
            android:textSize="16sp"
            android:textStyle="bold"
            android:layout_marginTop="4dp"
            app:layout_constraintStart_toStartOf="@id/txtFoodName"
            app:layout_constraintTop_toBottomOf="@id/txtFoodName"/>

        <TextView
            android:id="@+id/txtFoodDesc"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:text="Delicious food description..."
            android:textSize="12sp"
            android:textColor="#888888"
            android:maxLines="2"
            android:layout_marginTop="8dp"
            app:layout_constraintStart_toStartOf="@id/txtFoodName"
            app:layout_constraintTop_toBottomOf="@id/txtFoodPrice"
            app:layout_constraintEnd_toStartOf="@id/imgFood"/>

        <com.google.android.material.imageview.ShapeableImageView
            android:id="@+id/imgFood"
            android:layout_width="115dp"
            android:layout_height="115dp"
            android:scaleType="centerCrop"
            app:shapeAppearanceOverlay="@style/ShapeAppearance.MaterialComponents.MediumComponent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toTopOf="parent"/>

        <ImageView
            android:id="@+id/imgFavorite"
            android:layout_width="34dp"
            android:layout_height="34dp"
            android:padding="8dp"
            android:src="@drawable/ic_favorite_border"
            android:background="@drawable/bg_button_rounded"
            android:backgroundTint="#B3FFFFFF"
            android:layout_margin="8dp"
            android:elevation="4dp"
            app:layout_constraintTop_toTopOf="@id/imgFood"
            app:layout_constraintEnd_toEndOf="@id/imgFood"/>

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnAdd"
            android:layout_width="95dp"
            android:layout_height="42dp"
            android:text="ADD"
            android:textColor="#FF5722"
            app:backgroundTint="#FFFFFF"
            app:strokeColor="#DDDDDD"
            app:strokeWidth="1dp"
            app:cornerRadius="10dp"
            app:layout_constraintBottom_toBottomOf="@id/imgFood"
            app:layout_constraintEnd_toEndOf="@id/imgFood"
            app:layout_constraintStart_toStartOf="@id/imgFood"
            android:layout_marginBottom="-20dp"/>

    </androidx.constraintlayout.widget.ConstraintLayout>
</com.google.android.material.card.MaterialCardView>


item_order

<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="16dp"
    android:layout_marginTop="12dp"
    android:layout_marginBottom="4dp"
    app:cardCornerRadius="16dp"
    app:cardElevation="2dp"
    app:strokeWidth="1dp"
    app:strokeColor="#F0F0F0"
    android:backgroundTint="#FFFFFF">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="vertical">

                <TextView
                    android:id="@+id/txtRestaurant"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="TestyBites"
                    android:textColor="#111111"
                    android:textStyle="bold"
                    android:textSize="18sp"/>

                <TextView
                    android:id="@+id/txtDate"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Oct 24, 2026 • 08:30 PM"
                    android:textColor="#757575"
                    android:textSize="12sp"
                    android:layout_marginTop="2dp"/>
            </LinearLayout>

            <TextView
                android:id="@+id/txtStatus"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentEnd="true"
                android:layout_centerVertical="true"
                android:text="Delivered"
                android:textColor="#4CAF50"
                android:textStyle="bold"
                android:textSize="12sp"
                android:background="#E8F5E9"
                android:paddingHorizontal="10dp"
                android:paddingVertical="6dp"
                app:cornerRadius="4dp"/>
        </RelativeLayout>

        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:background="#EEEEEE"
            android:layout_marginVertical="14dp"/>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical">

            <com.google.android.material.imageview.ShapeableImageView
                android:id="@+id/imgFood"
                android:layout_width="65dp"
                android:layout_height="65dp"
                android:scaleType="centerCrop"
                app:shapeAppearanceOverlay="@style/ShapeAppearance.MaterialComponents.SmallComponent"/>

            <LinearLayout
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:layout_marginStart="14dp">

                <TextView
                    android:id="@+id/txtItemName"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Spicy Chicken Burger"
                    android:textColor="#333333"
                    android:textSize="16sp"
                    android:textStyle="bold"/>

                <TextView
                    android:id="@+id/txtPrice"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="₹ 220"
                    android:textColor="#555555"
                    android:textSize="14sp"
                    android:textStyle="bold"
                    android:layout_marginTop="4dp"/>
            </LinearLayout>
        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginTop="16dp"
            android:weightSum="2">

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btnRate"
                style="@style/Widget.MaterialComponents.Button.OutlinedButton"
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="50dp"
                android:text="Rate Food ⭐"
                android:textColor="#FC8019"
                app:strokeColor="#FC8019"
                android:layout_marginEnd="8dp"
                android:visibility="gone"/> <com.google.android.material.button.MaterialButton
            android:id="@+id/btnReorder"
            android:layout_width="0dp"
            android:layout_weight="1"
            android:layout_height="50dp"
            android:text="REORDER"
            android:textColor="#FFFFFF"
            app:backgroundTint="#FC8019"
            app:cornerRadius="8dp"
            android:layout_marginStart="8dp"/>
        </LinearLayout>

    </LinearLayout>
</com.google.android.material.card.MaterialCardView>


slider_item

<?xml version="1.0" encoding="utf-8"?>
<ImageView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/sliderImage"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:scaleType="centerCrop"/>
*/



// ==========================================
// SECTION 3: DRAWABLE RESOURCES (Icon/Image Data)
// ==========================================


/*
bg_button_rounded
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/colorPrimary"/>
    <corners android:radius="25dp"/>
</shape>


bg_curve
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@android:color/white"/>

    <corners
        android:topLeftRadius="0dp"
        android:topRightRadius="0dp"
        android:bottomLeftRadius="40dp"
        android:bottomRightRadius="40dp"/>
</shape>


bg_gradient
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
        <gradient xmlns:android="http://schemas.android.com/apk/res/android"
            android:startColor="#E23744"
            android:endColor="#FF8A65"
            android:angle="270"/>
</shape>


bg_header_gradient
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
        <gradient
            android:startColor="#FF4D4D"
            android:endColor="#FF7A7A"
            android:angle="270"/>
    </shape>


bg_image_rounded

<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
        <gradient
            android:startColor="#FF4D4D"
            android:endColor="#FF7A7A"
            android:angle="270"/>
    </shape>


bg_status_delivered
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/successBg"/>
    <corners android:radius="20dp"/>
</shape>


ic_cart
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:height="24dp" android:tint="#000000" android:viewportHeight="24" android:viewportWidth="24" android:width="24dp">
      
    <path android:fillColor="@android:color/white" android:pathData="M11,9h2L13,6h3L16,4h-3L13,1h-2v3L8,4v2h3v3zM7,18c-1.1,0 -1.99,0.9 -1.99,2S5.9,22 7,22s2,-0.9 2,-2 -0.9,-2 -2,-2zM17,18c-1.1,0 -1.99,0.9 -1.99,2s0.89,2 1.99,2 2,-0.9 2,-2 -0.9,-2 -2,-2zM7.17,14.75l0.03,-0.12 0.9,-1.63h7.45c0.75,0 1.41,-0.41 1.75,-1.03l3.86,-7.01L19.42,4h-0.01l-1.1,2 -2.76,5L8.53,11l-0.13,-0.27L6.16,6l-0.95,-2 -0.94,-2L1,2v2h2l3.6,7.59 -1.35,2.45c-0.16,0.28 -0.25,0.61 -0.25,0.96 0,1.1 0.9,2 2,2h12v-2L7.42,15c-0.13,0 -0.25,-0.11 -0.25,-0.25z"/>
    
</vector>


ic_favorite_border
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">

    <path
        android:fillColor="#FFFFFF"
        android:pathData="M16.5,3c-1.74,0 -3.41,0.81 -4.5,2.09C10.91,3.81 9.24,3 7.5,3
        4.42,3 2,5.42 2,8.5c0,3.78 3.4,6.86 8.55,11.54L12,21.35l1.45,-1.32
        C18.6,15.36 22,12.28 22,8.5
        22,5.42 19.58,3 16.5,3z"/>
</vector>


ic_favorite_filled
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="#FF0000">

    <path
        android:fillColor="#FF0000"
        android:pathData="M12,21.35l-1.45,-1.32C5.4,15.36 2,12.28 2,8.5
        2,5.42 4.42,3 7.5,3c1.74,0 3.41,0.81 4.5,2.09
        C13.09,3.81 14.76,3 16.5,3
        19.58,3 22,5.42 22,8.5
        c0,3.78 -3.4,6.86 -8.55,11.54L12,21.35z"/>
</vector>


ic_food_splash
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
        android:width="120dp"
        android:height="120dp"
        android:viewportWidth="24"
        android:viewportHeight="24">

        <path
            android:fillColor="#FFFFFF"
            android:pathData="M7,2v11c0,1.1 0.9,2 2,2v7h2v-7c1.1,0 2,-0.9 2,-2V2h-2v7H9V2H7zM16,2v20h2V2h-2z"/>
</vector>


ic_home
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:height="24dp" android:tint="#000000" android:viewportHeight="24" android:viewportWidth="24" android:width="24dp">
      
    <path android:fillColor="@android:color/white" android:pathData="M18,11c0.7,0 1.37,0.1 2,0.29V9l-8,-6L4,9v12h7.68C11.25,20.09 11,19.08 11,18C11,14.13 14.13,11 18,11z"/>
      
    <path android:fillColor="@android:color/white" android:pathData="M18,13c-2.76,0 -5,2.24 -5,5s2.24,5 5,5s5,-2.24 5,-5S20.76,13 18,13zM21,18.5h-2.5V21h-1v-2.5H15v-1h2.5V15h1v2.5H21V18.5z"/>
    
</vector>


ic_launcher_background
<?xml version="1.0" encoding="utf-8"?>
<vector
    android:height="108dp"
    android:width="108dp"
    android:viewportHeight="108"
    android:viewportWidth="108"
    xmlns:android="http://schemas.android.com/apk/res/android">
    <path android:fillColor="#3DDC84"
          android:pathData="M0,0h108v108h-108z"/>
    <path android:fillColor="#00000000" android:pathData="M9,0L9,108"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M19,0L19,108"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M29,0L29,108"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M39,0L39,108"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M49,0L49,108"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M59,0L59,108"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M69,0L69,108"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M79,0L79,108"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M89,0L89,108"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M99,0L99,108"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M0,9L108,9"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M0,19L108,19"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M0,29L108,29"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M0,39L108,39"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M0,49L108,49"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M0,59L108,59"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M0,69L108,69"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M0,79L108,79"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M0,89L108,89"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M0,99L108,99"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M19,29L89,29"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M19,39L89,39"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M19,49L89,49"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M19,59L89,59"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M19,69L89,69"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M19,79L89,79"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M29,19L29,89"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M39,19L39,89"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M49,19L49,89"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M59,19L59,89"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M69,19L69,89"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
    <path android:fillColor="#00000000" android:pathData="M79,19L79,89"
          android:strokeColor="#33FFFFFF" android:strokeWidth="0.8"/>
</vector>


ic_launcher_foreground
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="24"
    android:viewportHeight="24">

    <path
        android:fillColor="#FF6F00"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2z"/>

    <path
        android:fillColor="#FFFFFF"
        android:pathData="M11,9H9V2H7v7H5V2H3v7c0,2.12 1.66,3.84 3.75,3.97V22h2.5v-9.03C11.34,12.84 13,11.12 13,9V2h-2V9z M16,6v14h2.5V6c0,-0.55 0.45,-1 1,-1s1,0.45 1,1v2h2V6c0,-1.66 -1.34,-3 -3,-3S16.5,4.34 16,6z"/>
</vector>


ic_orders
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:height="24dp" android:tint="#000000" android:viewportHeight="24" android:viewportWidth="24" android:width="24dp">
      
    <path android:fillColor="@android:color/white" android:pathData="M7,18c-1.1,0 -1.99,0.9 -1.99,2S5.9,22 7,22s2,-0.9 2,-2 -0.9,-2 -2,-2zM1,2v2h2l3.6,7.59 -1.35,2.45c-0.16,0.28 -0.25,0.61 -0.25,0.96 0,1.1 0.9,2 2,2h12v-2L7.42,15c-0.14,0 -0.25,-0.11 -0.25,-0.25l0.03,-0.12 0.9,-1.63h7.45c0.75,0 1.41,-0.41 1.75,-1.03l3.58,-6.49c0.08,-0.14 0.12,-0.31 0.12,-0.48 0,-0.55 -0.45,-1 -1,-1L5.21,4l-0.94,-2L1,2zM17,18c-1.1,0 -1.99,0.9 -1.99,2s0.89,2 1.99,2 2,-0.9 2,-2 -0.9,-2 -2,-2z"/>
    
</vector>


ic_profile
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:height="24dp" android:tint="#000000" android:viewportHeight="24" android:viewportWidth="24" android:width="24dp">
      
    <path android:fillColor="@android:color/white" android:pathData="M12,12c2.21,0 4,-1.79 4,-4s-1.79,-4 -4,-4 -4,1.79 -4,4 1.79,4 4,4zM12,14c-2.67,0 -8,1.34 -8,4v2h16v-2c0,-2.66 -5.33,-4 -8,-4z"/>
    
</vector>


my_app_logo
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="512dp"
    android:height="512dp"
    android:viewportWidth="24"
    android:viewportHeight="24">

    <path
        android:fillColor="#FF6F00"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2z"/>

    <path
        android:fillColor="#FFFFFF"
        android:pathData="M11,9H9V2H7v7H5V2H3v7c0,2.12 1.66,3.84 3.75,3.97V22h2.5v-9.03C11.34,12.84 13,11.12 13,9V2h-2V9z M16,6v14h2.5V6c0,-0.55 0.45,-1 1,-1s1,0.45 1,1v2h2V6c0,-1.66 -1.34,-3 -3,-3S16.5,4.34 16,6z"/>
</vector>
*/

// ==========================================
// SECTION 4: AndroidMainfest.xml
// ==========================================


/*
AndroidManifest.xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="com.example.fooddeliveryapp">

        <!-- Internet for map tiles -->
        <uses-permission android:name="android.permission.INTERNET"/>

        <!-- REQUIRED: both permissions -->
        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
        <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.FoodDeliveryApp"
        android:usesCleartextTraffic="true"
        tools:targetApi="31">

        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="YOUR_MAPS_API_KEY_HERE" />

        <activity
            android:name=".activities.TrackingActivity"
            android:exported="false" />

        <service
            android:name=".services.MyFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>

        <activity android:name=".activities.FavoriteActivity" android:exported="false" />
        <activity android:name=".activities.SavedAddressesActivity" android:exported="false" />
        <activity android:name=".activities.EditProfileActivity" android:exported="false" />
        <activity android:name=".activities.ProfileActivity" android:exported="false" />
        <activity android:name=".activities.OrderSuccessActivity" android:exported="false" />

        <activity
            android:name=".activities.SplashActivity"
            android:exported="true"
            android:theme="@style/Theme.FoodDeliveryApp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity android:name=".activities.DeliveryDashboardActivity" android:exported="false" />
        <activity android:name=".activities.CartActivity" android:exported="false" />
        <activity android:name=".activities.AdminOrderActivity" android:exported="false" />
        <activity android:name=".activities.AdminAddFoodActivity" android:exported="false" />
        <activity android:name=".activities.OrderActivity" android:exported="false" />
        <activity android:name=".activities.DetailActivity" android:exported="false" />
        <activity android:name=".activities.LoginActivity" android:exported="false" />
        <activity android:name=".activities.MainActivity" android:exported="false" />
        <activity android:name=".activities.RegisterActivity" android:exported="false" />
    </application>

</manifest>
*/ 



// ==========================================
// SECTION 5: build.gradle.kts
// ==========================================



/*
build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.fooddeliveryapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fooddeliveryapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("org.osmdroid:osmdroid-android:6.1.16")
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.airbnb.android:lottie:6.1.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("me.relex:circleindicator:2.1.6")
    implementation ("androidx.core:core-splashscreen:1.0.1")
    implementation ("com.razorpay:checkout:1.6.33")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}
*/



// ==========================================
// SECTION 6: bottom_menu.xml (Menu Floder)
// ==========================================

/*
bottom_menu
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">

    <item
        android:id="@+id/nav_home"
        android:icon="@drawable/ic_home"
        android:title="Home" />

    <item
        android:id="@+id/nav_cart"
        android:icon="@drawable/ic_cart"
        android:title="Cart" />

    <item
        android:id="@+id/nav_orders"
        android:icon="@drawable/ic_orders"
        android:title="Orders" />

    <item
        android:id="@+id/nav_profile"
        android:icon="@drawable/ic_profile"
        android:title="Profile" />

</menu>

*/



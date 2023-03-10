package com.chatgpt.android.activity;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.chatgpt.android.ApiController;
import com.chatgpt.android.AppConstants;
import com.chatgpt.android.ChatAdapter;
import com.chatgpt.android.ChatModel;
import com.chatgpt.android.Data;
import com.chatgpt.android.databinding.ActivityChatBinding;
import com.chatgpt.android.room.RoomDatabase;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private List<ChatModel> modelList = new ArrayList<>();
    private ChatAdapter adapter;
    private RoomDatabase database ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = RoomDatabase.getDatabaseReference(ChatActivity.this);


        getDataFromRoom();
        if(modelList.size()>0){
            binding.chatRecyclerView.smoothScrollToPosition(modelList.size()-1);
            invisibleViews();
        }
        setAdapter();
        binding.settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(ChatActivity.this,SettingsActivity.class);
                startActivity(intent);
            }
        });
        binding.sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = binding.chatTv.getText().toString();
                if(!query.equals("")){

                    if(modelList.size()==0)
                        invisibleViews();
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.sendMessageBtn.setVisibility(View.GONE);

                    ChatModel model =new ChatModel(AppConstants.SENDER_TYPE,query);

                    modelList.add(model);

                    insertDataIntoRoom(model);


                    adapter.setListData(modelList);
                    adapter.notifyDataSetChanged();

                    binding.chatRecyclerView.smoothScrollToPosition(modelList.size()-1);
                    binding.chatTv.setText("");
                    getDataFromAPI(new Data(query));
                }
            }
        });
    }
    private void getDataFromAPI(Data query){
        Call<String> call = ApiController.getInstance().getApiSets().getDataFromApi(query);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String data = null;
                data = response.body();
                if(response.isSuccessful()){
                    ChatModel model = new ChatModel(AppConstants.RECEIVER_TYPE,data);
                    modelList.add(model);

                    insertDataIntoRoom(model);
                    adapter.setListData(modelList);
                    adapter.notifyDataSetChanged();

                    Log.d("==============data",data);

                    binding.chatRecyclerView.smoothScrollToPosition(modelList.size()-1);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.sendMessageBtn.setVisibility(View.VISIBLE);

                }else{
                    binding.progressBar.setVisibility(View.GONE);
                    binding.sendMessageBtn.setVisibility(View.VISIBLE);
                    Log.d("failure_response",response.message());}
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.sendMessageBtn.setVisibility(View.VISIBLE);
                if( t instanceof UnknownHostException)
                    Toast.makeText(ChatActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setAdapter(){
        adapter = new ChatAdapter(ChatActivity.this);
        adapter.setListData(modelList);
        binding.chatRecyclerView.setAdapter(adapter);
    }

    private void invisibleViews(){
            binding.imageMagier.setVisibility(View.GONE);
            binding.tvDesc.setVisibility(View.GONE);
            binding.tvLooksNothing.setVisibility(View.GONE);
            binding.imageGetStarted.setVisibility(View.GONE);
    }

    private void insertDataIntoRoom(ChatModel model){
        database.dao().insertRecord(model);
    }
    private void getDataFromRoom(){
        modelList.clear();
        try{
        modelList =database.dao().getRecord(); }
        catch (Exception e){Log.d("error",e.getMessage());}
    }
}
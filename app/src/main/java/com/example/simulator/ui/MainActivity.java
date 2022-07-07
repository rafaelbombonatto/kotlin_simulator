package com.example.simulator.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simulator.R;
import com.example.simulator.data.MatchesAPI;
import com.example.simulator.databinding.ActivityMainBinding;
import com.example.simulator.domain.Match;
import com.example.simulator.ui.adapter.MatchesAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MatchesAPI matchesAPI;
    private MatchesAdapter matchesAdapter = new MatchesAdapter(Collections.emptyList());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         binding = ActivityMainBinding.inflate(getLayoutInflater());
         setContentView(binding.getRoot());

         setupHTTPClient();
         setupMatchesList();
         setupMatchesRefresh();
         setupFloatingActionButton();
    }

    private void setupHTTPClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://rafaelbombonatto.github.io/matches-simulator-api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        matchesAPI = retrofit.create(MatchesAPI.class);
    }

    private void setupMatchesList() {
        binding.rvMatches.setHasFixedSize(true);
        binding.rvMatches.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMatches.setAdapter(matchesAdapter);
        findMatchesFromAPI();
    }

    private void findMatchesFromAPI() {
        binding.srlMatches.setRefreshing(true);
        matchesAPI.getMatches().enqueue(new Callback<List<Match>>() {
            @Override
            public void onResponse(Call<List<Match>> call, Response<List<Match>> response) {
                if (response.isSuccessful())
                {
                    List<Match> matches = response.body();
                    matchesAdapter = new MatchesAdapter(matches);
                    binding.rvMatches.setAdapter(matchesAdapter);
                }
                else
                {
                    showErrorMessage(getString(R.string.api_return_error));
                }
                binding.srlMatches.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Match>> call, Throwable t) {
                showErrorMessage(t.getMessage());
                binding.srlMatches.setRefreshing(false);
            }
        });
    }

    private void showErrorMessage(String message) {
        Snackbar.make(binding.fabSimulate,message, Snackbar.LENGTH_LONG).show();
    }

    private void setupFloatingActionButton() {
        binding.fabSimulate.setOnClickListener(view -> {
//
           view.animate().rotationBy(360).setDuration(500).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Random random = new Random();
                    for (int i = 0; i < matchesAdapter.getItemCount(); i++) {
                        Match match = matchesAdapter.getMatches().get(i);
                        match.getHomeTeam().setScore(random.nextInt(match.getHomeTeam().getStars()+1));
                        match.getAwayTeam().setScore(random.nextInt(match.getAwayTeam().getStars()+1));
                        matchesAdapter.notifyItemChanged(i);
                    }
                }
            });
            //throw  new RuntimeException("Teste de Crashlytics");
        });
    }

    private void setupMatchesRefresh() {
        binding.srlMatches.setOnRefreshListener(this::findMatchesFromAPI);
    }


}

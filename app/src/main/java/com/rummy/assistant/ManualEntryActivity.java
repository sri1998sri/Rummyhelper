package com.rummy.assistant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rummy.assistant.model.Card;
import com.rummy.assistant.model.Rank;
import com.rummy.assistant.model.Suit;

import java.util.ArrayList;
import java.util.List;

public class ManualEntryActivity extends AppCompatActivity {

    private RecyclerView rvCards;
    private TextView txtSelectedCount;
    private Button btnUndo, btnClear, btnConfirm;
    private CardAdapter adapter;
    private List<Card> selectedCards = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);

        rvCards = findViewById(R.id.rvCards);
        txtSelectedCount = findViewById(R.id.txtSelectedCount);
        btnUndo = findViewById(R.id.btnUndo);
        btnClear = findViewById(R.id.btnClear);
        btnConfirm = findViewById(R.id.btnConfirm);

        setupRecyclerView();

        btnUndo.setOnClickListener(v -> {
            if (!selectedCards.isEmpty()) {
                selectedCards.remove(selectedCards.size() - 1);
                updateUI();
            }
        });

        btnClear.setOnClickListener(v -> {
            selectedCards.clear();
            updateUI();
        });

        btnConfirm.setOnClickListener(v -> {
            if (selectedCards.size() != 13) {
                Toast.makeText(this, "Please select exactly 13 cards", Toast.LENGTH_SHORT).show();
                return;
            }
            // Save selected cards (will implement persistence/global state later)
            Toast.makeText(this, "Hand confirmed!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setupRecyclerView() {
        List<Card> allCards = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                allCards.add(new Card(rank, suit));
            }
        }
        allCards.add(new Card(true, false)); // Printed Joker
        allCards.add(new Card(true, true));   // Wild Joker

        adapter = new CardAdapter(allCards, card -> {
            if (selectedCards.contains(card)) {
                selectedCards.remove(card);
            } else {
                if (selectedCards.size() < 13) {
                    selectedCards.add(card);
                } else {
                    Toast.makeText(this, "Maximum 13 cards", Toast.LENGTH_SHORT).show();
                }
            }
            updateUI();
        });

        rvCards.setLayoutManager(new GridLayoutManager(this, 4));
        rvCards.setAdapter(adapter);
    }

    private void updateUI() {
        txtSelectedCount.setText("Selected: " + selectedCards.size() + "/13");
        adapter.setSelectedCards(selectedCards);
        adapter.notifyDataSetChanged();
    }

    private class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
        private final List<Card> cards;
        private List<Card> selectedCards;
        private final OnCardClickListener listener;

        interface OnCardClickListener {
            void onCardClick(Card card);
        }

        CardAdapter(List<Card> cards, OnCardClickListener listener) {
            this.cards = cards;
            this.listener = listener;
            this.selectedCards = new ArrayList<>();
        }

        void setSelectedCards(List<Card> selectedCards) {
            this.selectedCards = selectedCards;
        }

        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
            return new CardViewHolder(view);
        }

        @Override
        public CardViewHolder onBindViewHolder(CardViewHolder holder, int position) {
            Card card = cards.get(position);
            holder.txtValue.setText(card.toString());

            if (selectedCards.contains(card)) {
                holder.itemView.setBackgroundColor(0xFFBBDEFB); // Light blue for selected
            } else {
                holder.itemView.setBackgroundColor(0x00000000);
            }

            holder.itemView.setOnClickListener(v -> listener.onCardClick(card));
            return holder;
        }

        @Override
        public int getItemCount() {
            return cards.size();
        }

        class CardViewHolder extends RecyclerView.ViewHolder {
            TextView txtValue;
            CardViewHolder(View itemView) {
                super(itemView);
                txtValue = itemView.findViewById(R.id.txtCardValue);
            }
        }
    }
}

package ies.carrillo.impostor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

import ies.carrillo.impostor.R;

public class WordPairAdapter extends RecyclerView.Adapter<WordPairAdapter.WordPairViewHolder> {

    private final Context context;
    // Mapeo original de Palabra -> Pista
    private final HashMap<String, String> wordMap;
    // Lista de claves (Palabra Secreta) para el orden del RecyclerView
    private List<String> secretWordsList;
    private final OnPairActionListener listener;

    public interface OnPairActionListener {
        void onDeletePairClicked(String secretWord);
    }

    public WordPairAdapter(Context context, HashMap<String, String> wordMap, List<String> secretWordsList, OnPairActionListener listener) {
        this.context = context;
        this.wordMap = wordMap;
        this.secretWordsList = secretWordsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WordPairViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_word_pair, parent, false);
        return new WordPairViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordPairViewHolder holder, int position) {
        final String secretWord = secretWordsList.get(position);
        final String impostorClue = wordMap.get(secretWord);

        holder.tvSecretWord.setText(secretWord);
        holder.tvImpostorClue.setText(impostorClue);

        // Configurar listener para el botón de borrar
        holder.btnDelete.setOnClickListener(v -> listener.onDeletePairClicked(secretWord));
    }

    @Override
    public int getItemCount() {
        return secretWordsList.size();
    }

    /**
     * Actualiza la lista de palabras cuando se añade o borra un par.
     */
    public void updateWords(List<String> newSecretWordsList) {
        this.secretWordsList = newSecretWordsList;
        notifyDataSetChanged();
    }

    static class WordPairViewHolder extends RecyclerView.ViewHolder {
        final TextView tvSecretWord;
        final TextView tvImpostorClue;
        final ImageButton btnDelete;

        WordPairViewHolder(View itemView) {
            super(itemView);
            tvSecretWord = itemView.findViewById(R.id.tv_word_secret);
            tvImpostorClue = itemView.findViewById(R.id.tv_word_impostor);
            btnDelete = itemView.findViewById(R.id.btn_delete_pair);
        }
    }
}
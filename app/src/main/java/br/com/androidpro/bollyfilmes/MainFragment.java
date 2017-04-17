package br.com.androidpro.bollyfilmes;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.com.androidpro.bollyfilmes.data.FilmesContract;
import br.com.androidpro.bollyfilmes.data.FilmesDBHelper;

public class MainFragment extends Fragment {

    private int posicaoItem = ListView.INVALID_POSITION;
    private static final String KEY_POSICAO = "SELECIONADO";
    private ListView list;
    private boolean useFilmeDestaque = false;
    private FilmesAdapter adapter;
    private FilmesDBHelper dbHelper;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        list = (ListView) view.findViewById(R.id.list_filmes);

        final ArrayList<ItemFilme> arrayList = new ArrayList<>();
        //arrayList.add(new ItemFilme("Homem Aranha", "Filme de herói Aranha", "10/04/2000",4));
        //arrayList.add(new ItemFilme("Homem Cobra", "Filme de herói cobra", "11/04/2000",2));
        //arrayList.add(new ItemFilme("Homem Javali", "Filme de herói javali", "12/04/2000",3));
        //arrayList.add(new ItemFilme("Homem Pássaro", "Filme de herói pássaro", "13/04/2000",5));
        //arrayList.add(new ItemFilme("Homem Cachorro", "Filme de herói cachorro", "14/04/2000",3.5f));
        //arrayList.add(new ItemFilme("Homem Gato", "Filme de herói gato", "15/04/2000",2.5f));

        adapter = new FilmesAdapter(getContext(),arrayList);
        adapter.setUseFilmeDestaque(useFilmeDestaque);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ItemFilme itemFilme = arrayList.get(position);
                Callback callback = (Callback) getActivity();
                callback.onItemSelected(itemFilme);
                posicaoItem = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_POSICAO)) {

            posicaoItem = savedInstanceState.getInt(KEY_POSICAO);
        }

        dbHelper = new FilmesDBHelper(getContext());

        new FilmesAsyncTask().execute();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if(posicaoItem != ListView.INVALID_POSITION){
            outState.putInt(KEY_POSICAO, posicaoItem);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if(posicaoItem != ListView.INVALID_POSITION && list != null){
            list.smoothScrollToPosition(posicaoItem);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_atualizar:
                new FilmesAsyncTask().execute();
                Toast.makeText(getContext(), "Atualizando os filmes...", Toast.LENGTH_LONG).show();
                return true;
            case R.id.menu_config:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void setUseFilmeDestaque(boolean useFilmeDestaque) {
        this.useFilmeDestaque = useFilmeDestaque;
        if(adapter != null){
            adapter.setUseFilmeDestaque(useFilmeDestaque);
        }
    }

    public class FilmesAsyncTask extends AsyncTask<Void, Void, List<ItemFilme>>{
        //primeiro parâmetro: entrada do doInBackground,
        // segundo parâmetro: parâmetro do onProgressUpdate
        // terceiro parâmetro: retorno do método que vai ser executado em background
        @Override
        protected List<ItemFilme> doInBackground(Void... integers) {
            // https://api.themoviedb.org/3/movie/popular?api_key=qwer08776&language=pt-BR

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            SharedPreferences preferences =  PreferenceManager.getDefaultSharedPreferences(getContext());
            String ordem = preferences.getString(getString(R.string.prefs_ordem_key), "");
            String idioma = preferences.getString(getString(R.string.prefs_idioma_key), "");

            if (ordem == null || ordem.equals("")){
                ordem = "popular";
            }

            if (idioma == null || idioma.equals("")){
                idioma = "pt-BR";
            }

            try{
                String urlBase = "https://api.themoviedb.org/3/movie/" + ordem + "?";
                String apiKey = "api_key";
                String language = "language";

                Uri uriApi = Uri.parse(urlBase).buildUpon().appendQueryParameter(apiKey,BuildConfig.TMDB_API_KEY).appendQueryParameter(language,idioma).build();
                URL url = new URL(uriApi.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if(inputStream == null){
                    return  null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String linha;
                StringBuffer buffer = new StringBuffer();
                while((linha = reader.readLine()) != null){
                    buffer.append(linha);
                    buffer.append("/n");
                }

                return JsonUtil.fromJsonToLIst(buffer.toString());

            }catch (IOException e){
                e.printStackTrace();
            } finally {
                if (urlConnection != null){
                    urlConnection.disconnect();
                }
                if(reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<ItemFilme> itemFilmes) { //pega o retorno do doInBackground e executa aqui

            //inserindo no banco de dados
            SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();

            for (ItemFilme itemFilme : itemFilmes) {
                ContentValues values = new ContentValues(); //acredito que seja um hashmap
                values.put(FilmesContract.FilmeEntry._ID, itemFilme.getId());
                values.put(FilmesContract.FilmeEntry.COLUMN_TITULO, itemFilme.getTitulo());
                values.put(FilmesContract.FilmeEntry.COLUMN_DESCRICAO, itemFilme.getDescricao());
                values.put(FilmesContract.FilmeEntry.COLUMN_POSTER_PATH, itemFilme.getPosterPath());
                values.put(FilmesContract.FilmeEntry.COLUMN_CAPA_PATH, itemFilme.getCapaPath());
                values.put(FilmesContract.FilmeEntry.COLUMN_AVALIACAO, itemFilme.getAvaliacao());

                String where = FilmesContract.FilmeEntry._ID + "=?";
                String[] whereValues = new String[] {String.valueOf(itemFilme.getId())};

                int update = writableDatabase.update(FilmesContract.FilmeEntry.TABLE_NAME, values, where, whereValues); //retorna o número de linhas que foram atualizadas

                if(update == 0){
                    //registro não existe, vai inserir
                    writableDatabase.insert(FilmesContract.FilmeEntry.TABLE_NAME, null, values);
                }
            }

            adapter.clear();
            adapter.addAll(itemFilmes);
            adapter.notifyDataSetChanged();
        }
    }

    public interface Callback{
        void onItemSelected (ItemFilme itemFilme);
    }
}
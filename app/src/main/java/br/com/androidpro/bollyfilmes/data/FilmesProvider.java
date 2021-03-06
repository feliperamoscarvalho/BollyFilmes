package br.com.androidpro.bollyfilmes.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

public class FilmesProvider extends ContentProvider {

    private static final UriMatcher URI_MATCHER = buildUriMatcher();

    private FilmesDBHelper dbHelper;

    private static final int FILME = 100;
    private static final int FILME_ID = 101;

    private static UriMatcher buildUriMatcher(){
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(FilmesContract.CONTENT_AUTHORITY, FilmesContract.PATH_FILMES, FILME);
        uriMatcher.addURI(FilmesContract.CONTENT_AUTHORITY, FilmesContract.PATH_FILMES + "/#", FILME_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new FilmesDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase readableDatabase = dbHelper.getReadableDatabase();

        Cursor cursor;

        switch(URI_MATCHER.match(uri)){
            case FILME:
                cursor = readableDatabase.query(FilmesContract.FilmeEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FILME_ID:
                selection = FilmesContract.FilmeEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(FilmesContract.FilmeEntry.getIdFromUri(uri))};

                cursor = readableDatabase.query(FilmesContract.FilmeEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

                break;
            default:
                throw new IllegalArgumentException("URI não identificada: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri); //notifica quando a uri tiver mudança

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        switch (URI_MATCHER.match(uri)){
            case FILME:
                return FilmesContract.FilmeEntry.CONTENT_TYPE;
            case FILME_ID:
                return FilmesContract.FilmeEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("URI não identificada: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();

        long id;
        switch (URI_MATCHER.match(uri)){
            case FILME:
                id = writableDatabase.insert(FilmesContract.FilmeEntry.TABLE_NAME, null, values);

                if(id == -1){
                    return null;
                }

                break;
            default:
                throw new IllegalArgumentException("URI não identificada: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null); //notifica que tem um insert novo (o segundo parâmetro é um observer, não estamos usando)

        return FilmesContract.FilmeEntry.buildUriForFilmes(id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();

        int delete = 0; //variável que vai guardar o resultado do delete

        switch (URI_MATCHER.match(uri)){
            case FILME:
                delete = writableDatabase.delete(FilmesContract.FilmeEntry.TABLE_NAME, selection, selectionArgs); //retorna o número de linhas que foram atualizadas

            case FILME_ID:
                selection = FilmesContract.FilmeEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(FilmesContract.FilmeEntry.getIdFromUri(uri))};

                delete = writableDatabase.delete(FilmesContract.FilmeEntry.TABLE_NAME, selection, selectionArgs); //retorna o número de linhas que foram atualizadas
        }
        if(delete != 0){
            //significa que houve atualização
            getContext().getContentResolver().notifyChange(uri, null); //notifica que tem um delete (o segundo parâmetro é um observer, não estamos usando)
        }

        return delete;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();

        int update = 0; //variável que vai guardar o resultado dos updates

        switch (URI_MATCHER.match(uri)){
            case FILME:
                update = writableDatabase.update(FilmesContract.FilmeEntry.TABLE_NAME, values, selection, selectionArgs); //retorna o número de linhas que foram atualizadas

            case FILME_ID:
                selection = FilmesContract.FilmeEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(FilmesContract.FilmeEntry.getIdFromUri(uri))};

                update = writableDatabase.update(FilmesContract.FilmeEntry.TABLE_NAME, values, selection, selectionArgs); //retorna o número de linhas que foram atualizadas

        }

        if(update != 0){
            //significa que houve atualização
            getContext().getContentResolver().notifyChange(uri, null); //notifica que tem um update (o segundo parâmetro é um observer, não estamos usando)
        }

        return update;
    }
}

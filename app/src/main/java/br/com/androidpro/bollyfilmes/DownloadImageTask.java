package br.com.androidpro.bollyfilmes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private ImageView imageView;

    public DownloadImageTask(ImageView imageView){
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {

        String url = strings[0];//o parâmetro de entrada vem como array, basta pegar a primeira posição
        Bitmap bitmap = null;

        try{
            InputStream in = new URL(url).openStream(); //abrir conexão
            bitmap = BitmapFactory.decodeStream(in); //cria a imagem a partir do InpuStream

        }catch (Exception e){
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }
}

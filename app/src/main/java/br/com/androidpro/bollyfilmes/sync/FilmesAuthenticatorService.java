package br.com.androidpro.bollyfilmes.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class FilmesAuthenticatorService extends Service{

    private FilmesAuthenticator filmesAuthenticator; //autenticador dentro do serviço

    @Override
    public void onCreate() {
        filmesAuthenticator = new FilmesAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //Faz a criação do serviço de autebticação
        return filmesAuthenticator.getIBinder();
    }
}

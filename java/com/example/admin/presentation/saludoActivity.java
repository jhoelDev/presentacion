package com.example.admin.presentation;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class saludoActivity extends Activity {
    private List<Datos> listaDatos;
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.137.1:3001");
        } catch (URISyntaxException e) {}
    }

    TextView txtNombre, txtEmpresa;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_saludo);


        mSocket.on("mensaje", onNewMessage);
        mSocket.connect();

        txtEmpresa =(TextView)findViewById(R.id.txtEmpresa);
        txtNombre =(TextView)findViewById(R.id.txtName);
        Intent intent = getIntent();

        Datos datos= new Datos();
        listaDatos = new ArrayList<Datos>();
        datos.nombre = intent.getStringExtra("nombre");
        datos.empresa = intent.getStringExtra("empresa");
        listaDatos.add(datos);
        txtEmpresa.setText(listaDatos.get(0).empresa);
        txtNombre.setText(listaDatos.get(0).nombre);
        listaDatos.remove(0);
        bucle();


    }

    private void bucle (){
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        if(listaDatos.size() > 0){
                            txtEmpresa.setText(listaDatos.get(0).empresa);
                            txtNombre.setText(listaDatos.get(0).nombre);
                            listaDatos.remove(0);
                            bucle();
                        }else{
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("mensaje", "mensaje");
                            startActivity(intent);
                            finish();
                        }

                    }
                },
                5000);
    }
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        Log.i("nombre", data.getString("nombre"));
                        Log.i("empresa", data.getString("empresa"));
                        Datos datos= new Datos();
                        datos.nombre = data.getString("nombre");
                        datos.empresa = data.getString("empresa");
                        listaDatos.add(datos);

                    } catch (JSONException e) {
                        return;
                    }

                }
            });
        }
    };

    class Datos{
        public String nombre;
        public String empresa;
    }
}

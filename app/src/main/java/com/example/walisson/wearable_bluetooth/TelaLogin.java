package com.example.walisson.wearable_bluetooth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TelaLogin extends AppCompatActivity {

    EditText editSenha, editLogin;
    Button btLogar;
    TextView txtCadastro;

    WebView wv = (WebView) findViewById(R.id.webView_cadastro);

    WebSettings ws = wv.getSettings();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_login);

        editLogin = (EditText) findViewById(R.id.editLogin);
        editSenha = (EditText) findViewById(R.id.editSenha);
        btLogar = (Button) findViewById(R.id.entrar);
        txtCadastro = (TextView) findViewById(R.id.label_cadastro);

        ws.setSupportZoom(false);

        txtCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wv.loadUrl("http://www.walissonsilva.com");
            }
        });
    }
}

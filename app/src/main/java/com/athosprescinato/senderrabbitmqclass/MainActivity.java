package com.athosprescinato.senderrabbitmqclass;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final static String QUEUE_NAME = ""; // Put QUEUE Name here || Insira o nome da fila aqui
    Button publish, receive;
    EditText editText;
    TextView textView;
    RabbitMQConnection rabbitConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        publish = findViewById(R.id.publish);
        receive = findViewById(R.id.receive);
        editText = findViewById(R.id.Edittext);
        textView = findViewById(R.id.textView);

// Starts the receiving method automatically || Inicia o metodo de recebimento automaticamente

        new Thread(new Runnable() {
            @Override
            public void run() {
                rabbitConnection = RabbitMQConnection.getInstance(QUEUE_NAME);
                rabbitConnection.setActionListener(new RabbitMQConnection.ActionListenerCallback() {
                    @Override
                    public void onMessage(String successMessage, Object consumerTag) {

                        System.out.println( successMessage + " [x] Callback success message ");
                        textView.setText(successMessage);

                    }
                });

                //connection.receive();
            }
        }).start();

        // Send the message written in EditText || Envia a mensagem escrita no EditText

        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("[x] send", "init");
                        String message = editText.getText().toString();
                        rabbitConnection.send(message);
                    }
                }).start();
            }
        });

// Starts the receiving method with a button click || Inicia o metodo de recebimento com o click no botão


        receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("[x] receive", "init");
                        rabbitConnection.receive();
                    }
                }).start();
            }
        });


    }

}

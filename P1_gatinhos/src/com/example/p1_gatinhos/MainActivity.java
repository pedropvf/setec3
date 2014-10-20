package com.example.p1_gatinhos;

import java.util.ArrayList;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class MainActivity extends ActionBarActivity implements OnClickListener{

	private Button add_button, show_button;
	ArrayList<Gato> lista_gato = new ArrayList<Gato>();
	int num_gatos;
	TextView ngatos_text;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        add_button = (Button)findViewById(R.id.AddGato);
        add_button.setOnClickListener(this);
        
        show_button = (Button)findViewById(R.id.ApreGatos);
        show_button.setOnClickListener(this);
        
        show_button = (Button)findViewById(R.id.ApagarGatos);
        show_button.setOnClickListener(this);
        
        ngatos_text = (TextView)findViewById(R.id.ngatos);
        
    }
    
    public void onClick(View v) {
		
		switch(v.getId()) {
			case R.id.AddGato: funcAddGatos(); break;
			case R.id.ApreGatos: funcShowGatos(); break;
			case R.id.ApagarGatos: funcEraseGatos(); break;
		}
	}
    
    private void funcEraseGatos() {
    	lista_gato.clear();
    	num_gatos = lista_gato.size();
    	ngatos_text.setText(String.valueOf(num_gatos));
	}

	private void funcShowGatos() {

		String total = "";
		for (Gato g:lista_gato){
			String name = g.getName();
			String idade = String.valueOf(g.getAge());
			total = total + name + " " + idade + "\n";
		}
		
		showToast(total);
		
	}

	private void funcAddGatos() {
		EditText nomeEdit;
		EditText idadeEdit;
    	String nome_gato;
    	String idade_gato_string;
    	int idade_gato;
    	Gato gato;
    	
    	nomeEdit = (EditText)findViewById(R.id.EditNome);
    	nome_gato = nomeEdit.getText().toString();
    	if(nome_gato.equals("")){
    		showToast("Insira nome do gato");
    		return;
    	}
    	
    	idadeEdit = (EditText)findViewById(R.id.EditIdade);
    	idade_gato_string = idadeEdit.getText().toString();
    	/*if(idade_gato_string.equals("")){
    		showToast("Insira idade do gato");
    		return;
    	}*/
    	try{
    		idade_gato = Integer.parseInt(idade_gato_string);
    	}catch (NumberFormatException e){
    		showToast("Insira idade do gato");
    		return;
    	}
    	
    	gato = new Gato(nome_gato, idade_gato);
    	lista_gato.add(gato);
    	num_gatos = lista_gato.size();
    	ngatos_text.setText(String.valueOf(num_gatos));
    	
    	showToast("Gato " + nome_gato + " adicionado");
    	nomeEdit.setText("");
    	idadeEdit.setText("");
    	
	}

	public void showToast(String text){
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(this, text, duration);
		toast.show();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

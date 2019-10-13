package com.luizpaulo.jogovelha;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class MenuJogarActivity extends Activity {
	
	protected Typeface font;
	protected View view;
	protected int selecionado;
	
	@SuppressLint("InflateParams") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

		@SuppressWarnings("deprecation")
		int width = display.getWidth();	

		if(width <= 240){
			setView(getLayoutInflater().inflate(R.layout.activity_menu_jogar_320, null));
		} else {
			setView(getLayoutInflater().inflate(R.layout.activity_menu_jogar, null));
		}
		
		setContentView(getView());

		font = Typeface.createFromAsset(getAssets(), "fonts/Cocogoose.otf");
		
		TextView txtJogarVelha = (TextView) findViewById(R.id.txtJogoVelha);
		TextView txtJogarVelhaBluetooth = (TextView) findViewById(R.id.txtJogoVelhaBluetooth);
		txtJogarVelha.setTypeface(font);
		txtJogarVelhaBluetooth.setTypeface(font);
		txtJogarVelhaBluetooth.setTextColor(Color.CYAN);
		
		for(int i = 1; i <= 10; i++){
			getTextView(i).setTypeface(font);
		}
	}
	
	protected TextView getTextView(int tagNum){
		return (TextView) getView().findViewWithTag("texto" + tagNum);

	}
	
	public void jogar(View v){
		
		for(int i = 1; i <= 10; i++){
			
			if(i != 1 && i != 7 && i != 10){
				getTextView(i).setTextColor(Color.rgb(32, 32, 32));
			}
		}
		
		selecionado = Integer.parseInt(v.getTag().toString().substring(5));
		
		getTextView(selecionado).setTextColor(Color.RED);
	}
	
	public void iniciarJogo(View v){
		
		final Intent intent = new Intent(getApplicationContext(), JogarAndroidActivity.class);
		final Intent intentOff = new Intent(getApplicationContext(), JogoOfflineActivity.class);
		
		switch(selecionado){
			case 2:
				intent.putExtra("nivel", Integer.parseInt(getResources().getString(R.string.nivel_facil)));
				startActivity(intent);
				break;
				
			case 3:
				intent.putExtra("nivel", Integer.parseInt(getResources().getString(R.string.nivel_medio)));
				startActivity(intent);
				break;
				
			case 4: 
				intent.putExtra("nivel", Integer.parseInt(getResources().getString(R.string.nivel_dificil)));
				startActivity(intent);
				break;
				
			case 5:
				intentOff.putExtra("iniciar", "X");
				startActivity(intentOff);
				break;
				
			case 6:
				intentOff.putExtra("iniciar", "O");
				startActivity(intentOff);
				break;
				
			case 8:
				startActivity(new Intent(MenuJogarActivity.this, JogarServidorActivity.class));
				break;
				
			case 9:
				startActivity(new Intent(MenuJogarActivity.this, JogarClientActivity.class));
				break;
		}
	}
	
	public void voltar(View v){
		finish();
	}
		
	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}
}

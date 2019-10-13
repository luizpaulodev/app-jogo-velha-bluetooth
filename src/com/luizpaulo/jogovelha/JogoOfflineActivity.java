package com.luizpaulo.jogovelha;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.luizpaulo.Banco.DatabaseHelper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class JogoOfflineActivity extends ActionBarActivity {

	protected View view;
	protected final String QUADRADO = "quadrado";
	protected final String PLACAR = "placar";
	protected final String NOVO_JOGO = "novo_jogo";

	protected final String BOLA = "O";
	protected final String XIS = "X";
	protected String lastPlay = BOLA;

	protected int empate = 0;
	protected int jogadorX = 0;
	protected int jogadorO = 0;

	protected int contadorVelha;

	protected TextView prxJogada;
	protected Button btnNovo;

	protected boolean ganhador = false;

	protected Typeface font;

	protected int contadorInterstial;
	protected InterstitialAd interstial;
	protected AdRequest adRequest;

	private DatabaseHelper helper;
	
	protected int[][] estadoFinal = new int[][] {

	{ 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 },

	{ 1, 4, 7 }, { 2, 5, 8 }, { 3, 6, 9 },

	{ 1, 5, 9 }, { 3, 5, 7 } };

	@SuppressWarnings("deprecation")
	protected int tamanhoDisplay() {
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
				.getDefaultDisplay();

		int width = display.getWidth();
		// int height = display.getHeight();

		return width;
	}
	
	protected void exibirToast(final CharSequence strToast){
		
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(this, strToast, duration);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	protected void atualizarPontos(String campo, String tipo) {
		SQLiteDatabase db = helper.getWritableDatabase();
		SQLiteDatabase db1 = helper.getReadableDatabase();
		
		Cursor cursor = db1.rawQuery("SELECT " + campo + " FROM jogovelha WHERE tipo = ?", new String[]{tipo});
		cursor.moveToFirst();
		int valor = cursor.getInt(0);
		valor+=1;
				
		ContentValues values = new ContentValues();
		values.put(campo, valor);
		
		db.update("jogovelha", values, "tipo = ?", new String[]{tipo});
	}

	@SuppressLint("InflateParams") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		helper = new DatabaseHelper(this);
		
		Intent it = getIntent();
		String jogadaInicial = "";
		
		if(it != null){
			jogadaInicial = it.getStringExtra("iniciar");
			
			if(jogadaInicial.equals(XIS)){
				lastPlay = BOLA;
			} else {
				lastPlay = XIS;
			}
		}
		
		if(tamanhoDisplay() <= 240){
			setView(getLayoutInflater().inflate(R.layout.activity_jogo_velha_320, null));
		} else {
			setView(getLayoutInflater().inflate(R.layout.activity_jogo_velha, null));
		}
		
		setContentView(getView());

		font = Typeface.createFromAsset(getAssets(), "fonts/Cocogoose.otf");

		prxJogada = (TextView) getView().findViewById(R.id.prxJogada);
		btnNovo = (Button) getView().findViewById(R.id.btnNovoJogo);
		
		prxJogada.setText(jogadaInicial + getResources().getString(R.string.comeca));

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		try {
			contadorInterstial = 0;
			interstial = new InterstitialAd(this);
			interstial.setAdUnitId("ca-app-pub-2167493564469787/4398760152");
			adRequest = new AdRequest.Builder().build();
			interstial.loadAd(adRequest);
		} catch (Exception e) {
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			interstial.loadAd(adRequest);
		} catch (Exception e) {
		}
	}

	public Typeface getAssetsAndroid() {
		Typeface fonte = null;
		font = Typeface.createFromAsset(getAssets(), "fonts/Cocogoose.otf");
		return fonte;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuinfo) {
		super.onCreateContextMenu(menu, view, menuinfo);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		switch (id) {
		case android.R.id.home:
			finish();
			break;

		case R.id.compartilharPlayer:
			Intent share = new Intent(Intent.ACTION_SEND);
			share.setType("text/plain");
			share.putExtra(
					Intent.EXTRA_TEXT,
					getResources().getString(R.string.mensagemCompartilhar));
			startActivity(Intent.createChooser(share, getResources().getString(R.string.compartilhar) + "..."));
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void clickQuadrado(View v) {

		if (((Button) v).getText().equals("")) {

			((Button) v).setTypeface(font);

			if (getLastPlay().equals(XIS)) {
				((Button) v).setText(BOLA);
				setLastPlay(BOLA);
			} else {
				((Button) v).setText(XIS);
				setLastPlay(XIS);
			}

			vibrar();
			contadorVelha++;
			proximaJogada();
			isFim();

			if (contadorVelha == 9 && ganhador == false) {
				//Toast.makeText(this, getResources().getString(R.string.empate_jogo), Toast.LENGTH_SHORT).show();
				exibirToast(getResources().getString(R.string.empate_jogo));
				empate++;
				getPlacar("Empate").setText(String.valueOf(empate));
				atualizarPontos("empate", "pp");
				showInterstial();
			}
		}
	}

	public void setColorQuadrados(int btn, int colorX) {
		getQuadrado(btn).setTextColor(getResources().getColor(colorX));
	}

	public void isFim() {

		for (int x = 0; x < 8; ++x) {

			final String s1 = getQuadrado(estadoFinal[x][0]).getText()
					.toString();
			String s2 = getQuadrado(estadoFinal[x][1]).getText().toString();
			String s3 = getQuadrado(estadoFinal[x][2]).getText().toString();

			if (!s1.equals("") && !s2.equals("") && !s3.equals("")) {
				if (s1.equals(s2) && s2.equals(s3)) {

					int cor = 0;

					if (s1.equals(BOLA)) {
						jogadorO += 1;
						getPlacar("O").setText(String.valueOf(jogadorO));
						//Toast.makeText(getView().getContext(), getResources().getString(R.string.jogador_o_ganhou), Toast.LENGTH_SHORT).show();
						exibirToast(getResources().getString(R.string.jogador_o_ganhou));
						cor = R.color.red;
						atualizarPontos("jogador2", "pp");
						showInterstial();
					} else {
						jogadorX += 1;
						getPlacar("X").setText(String.valueOf(jogadorX));
						//Toast.makeText(getView().getContext(), getResources().getString(R.string.jogador_x_ganhou), Toast.LENGTH_SHORT).show();
						exibirToast(getResources().getString(R.string.jogador_x_ganhou));
						cor = R.color.azul_claro;
						atualizarPontos("jogador1", "pp");
						showInterstial();
					}

					setColorQuadrados(estadoFinal[x][0], cor);
					setColorQuadrados(estadoFinal[x][1], cor);
					setColorQuadrados(estadoFinal[x][2], cor);

					ganhador = true;

					setEnableQuadrado(false);
					break;

				}
			}
		}
	}

	public void proximaJogada() {

		if (getLastPlay().equals(BOLA)) {
			prxJogada.setText(getResources().getString(R.string.proximo) + XIS);
		} else {
			prxJogada.setText(getResources().getString(R.string.proximo) + BOLA);
		}
	}

	public void newGame(View v) {

		for (int i = 1; i <= 9; i++) {
			if (getQuadrado(i) != null) {
				getQuadrado(i).setText("");
				getQuadrado(i).setTextColor(
						getResources().getColor(R.color.branco));
			}
		}
		//showInterstial();
		ganhador = false;
		vibrar();
		contadorVelha = 0;
		setEnableQuadrado(true);

	}

	private void showInterstial() {

		if (contadorInterstial == 4) {

			try {
				interstial.show();
				contadorInterstial = 0;
			} catch (Exception e) {
				contadorInterstial = 0;
			}

		} else {
			contadorInterstial++;
		}
	}

	public void setEnableQuadrado(boolean b) {
		for (int i = 1; i <= 9; i++) {
			if (getQuadrado(i) != null) {
				getQuadrado(i).setClickable(b);
			}
		}
	}

	protected void vibrar() {
		Vibrator v1 = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		v1.vibrate(50);
	}

	public Button getQuadrado(int tagNum) {
		return (Button) getView().findViewWithTag(QUADRADO + tagNum);
	}

	public TextView getPlacar(String tagPlacar) {
		return (TextView) getView().findViewWithTag(PLACAR + tagPlacar);
	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public String getLastPlay() {
		return lastPlay;
	}

	public void setLastPlay(String lastPlay) {
		this.lastPlay = lastPlay;
	}
}

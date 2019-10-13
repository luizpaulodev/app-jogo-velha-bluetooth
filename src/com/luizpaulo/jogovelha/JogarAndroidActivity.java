package com.luizpaulo.jogovelha;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.luizpaulo.Banco.DatabaseHelper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class JogarAndroidActivity extends ActionBarActivity {

	protected View view;
	protected final String QUADRADO = "quadrado";
	protected final String PLACAR = "placar";

	public Typeface font;

	private static int TAM = 3;
	private int nTabuleiro[][] = new int[TAM][TAM];
	private int nGanhador = -1;

	private TextView nomeJogador1, nomeJogador2, prxJogada;
	private int placarJogador, placarAndroid, placarEmpate;

	private boolean vezJogador;
	private boolean ganhador = false;
	private boolean empate = false;

	protected List<Integer> listTabuleiro = new ArrayList<Integer>();
	
	protected int[][] estadoFinal = new int[][] {

	{ 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 },

	{ 1, 4, 7 }, { 2, 5, 8 }, { 3, 6, 9 },

	{ 1, 5, 9 }, { 3, 5, 7 } };

	protected int contadorInterstial;
	protected InterstitialAd interstial;
	protected AdRequest adRequest;
	
	protected int nivel = 0;
	protected int nivel_desc = 0;

	protected Random random = new Random();
	
	private String dificuldade = "";
	
	private DatabaseHelper helper;
		
	@SuppressLint("InflateParams") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		font = Typeface.createFromAsset(getAssets(), "fonts/Cocogoose.otf");
		
		
		
		Intent it = getIntent();
		
		if(it != null){
			nivel = it.getIntExtra("nivel", -1);
		}

		if(nivel == Integer.parseInt(getResources().getString(R.string.nivel_facil))){
			setTitle(getResources().getString(R.string.player_vs_android) + " - " + getResources().getString(R.string.facil));
			dificuldade = "f";
		} else if(nivel == Integer.parseInt(getResources().getString(R.string.nivel_medio))){
			setTitle(getResources().getString(R.string.player_vs_android) + " - " + getResources().getString(R.string.medio));
			dificuldade = "m";
		} else if(nivel == Integer.parseInt(getResources().getString(R.string.nivel_dificil))){
			setTitle(getResources().getString(R.string.player_vs_android) + " - " + getResources().getString(R.string.dificil));
			dificuldade = "d";
		}
		
		nivel_desc = nivel;
		
		helper = new DatabaseHelper(this);
		
		int tela = tamanhoDisplay();
		setView(getLayoutInflater().inflate(R.layout.activity_jogo_velha, null));

		if(tela <= 400 && tela > 0){
			setView(getLayoutInflater().inflate(R.layout.activity_jogo_velha_320, null));	
		} else {
			setView(getLayoutInflater().inflate(R.layout.activity_jogo_velha, null));
		}
		
		setContentView(getView());
		
		nomeJogador1 = (TextView) getView().findViewById(R.id.jogador1);
		nomeJogador2 = (TextView) getView().findViewById(R.id.jogador2);
		prxJogada = (TextView) getView().findViewById(R.id.prxJogada);

		nomeJogador1.setText(getResources().getString(R.string.voce));
		nomeJogador2.setText(getResources().getString(R.string.android));
		prxJogada.setText(getResources().getString(R.string.voce_comeca));

		iniciarPartida();
		posicoesTabuleiro();
		nTabuleiro = getTabuleiro();
		vezJogador = true;
		
		android.support.v7.app.ActionBar a = getSupportActionBar();
		
		a.setDisplayHomeAsUpEnabled(true);
		a.setDisplayShowHomeEnabled(true);
        
        try{
        	contadorInterstial = 0;
    		interstial = new InterstitialAd(this);				      
    		interstial.setAdUnitId("ca-app-pub-2167493564469787/4398760152");
    		adRequest = new AdRequest.Builder().build();
    		interstial.loadAd(adRequest);
        } catch(Exception e){}  
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
	
	@SuppressWarnings("deprecation")
	protected int tamanhoDisplay(){
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
				.getDefaultDisplay();
		
		int width = display.getWidth();
				
		return width;
	}

	@Override
	protected void onResume(){
		super.onResume();
		try{
			interstial.loadAd(adRequest);
		} catch(Exception e){}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
		
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		int id = item.getItemId();
		
		switch(id){
			case android.R.id.home:
				finish();
				break;
				
			case R.id.compartilharPlayer:
				Intent share = new Intent(Intent.ACTION_SEND);
				share.setType("text/plain");
				share.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.mensagemCompartilhar));
				startActivity(Intent.createChooser(share, getResources().getString(R.string.compartilhar) + "..."));
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void posicoesTabuleiro(){
		listTabuleiro.clear();
				
		for(int i = 1; i <= 9; i++){
			listTabuleiro.add(i);
			
		}
	}
		
	public void clickQuadrado(View v) {

		if (!ganhador) {
			Button btn = ((Button) v);
			int posicao = Integer.parseInt(btn.getTag().toString().substring(8));
			
			if (btn.getText().equals("")) {
				vibrar();
				
				for(int i = 0; i < listTabuleiro.size(); i++){
					if(listTabuleiro.get(i) == posicao){
						listTabuleiro.remove(i);
						break;
					}
				}
				
				btn.setTypeface(font);
				btn.setText("X");

				int n = -1;
				int m = -1;

				switch (posicao) {
				case 1:
					n = 0;
					m = 0;
					break;
				case 2:
					n = 0;
					m = 1;
					break;
				case 3:
					n = 0;
					m = 2;
					break;
				case 4:
					n = 1;
					m = 0;
					break;
				case 5:
					n = 1;
					m = 1;
					break;
				case 6:
					n = 1;
					m = 2;
					break;
				case 7:
					n = 2;
					m = 0;
					break;
				case 8:
					n = 2;
					m = 1;
					break;
				case 9:
					n = 2;
					m = 2;
					break;
				}
				prxJogada.setText(getResources().getString(R.string.vez_android));
				clickBotao(n, m);

				isFim();
				
				if(!ganhador){
					jogarAndroid();
				}
			}
		}
	}
	
	protected int jogarAndroidAleatorio(){
		
		int n = 0, m = 0, jogar = 0;
		
		if(listTabuleiro.size() != 0){
			jogar = random.nextInt(listTabuleiro.size());

			jogar = listTabuleiro.get(jogar);
			switch (jogar) {
			case 1:
				n = 0;
				m = 0;
				break;
			case 2:
				n = 0;
				m = 1;
				break;
			case 3:
				n = 0;
				m = 2;
				break;
			case 4:
				n = 1;
				m = 0;
				break;
			case 5:
				n = 1;
				m = 1;
				break;
			case 6:
				n = 1;
				m = 2;
				break;
			case 7:
				n = 2;
				m = 0;
				break;
			case 8:
				n = 2;
				m = 1;
				break;
			case 9:
				n = 2;
				m = 2;
				break;
			}

			nTabuleiro[n][m] = 1;

			for(int i = 0; i < listTabuleiro.size(); i++){
				if(listTabuleiro.get(i) == jogar ){
					listTabuleiro.remove(i);
					break;
				}
			}
		}
		
		nGanhador = ganharPartida();
		return jogar;
	}
	
	protected int jogarAndroidMinimax() {
		int tag = -1;
		
		int[] jogada = ponerFichaOrdenador();
		
		int f = jogada[0];
		int c = jogada[1];
		
		if ((f == 0) && (c == 0)) {
			tag = 1;
		}

		if ((f == 0) && (c == 1)) {
			tag = 2;
		}

		if ((f == 0) && (c == 2)) {
			tag = 3;
		}

		if ((f == 1) && (c == 0)) {
			tag = 4;
		}

		if ((f == 1) && (c == 1)) {
			tag = 5;
		}

		if ((f == 1) && (c == 2)) {
			tag = 6;
		}

		if ((f == 2) && (c == 0)) {
			tag = 7;
		}

		if ((f == 2) && (c == 1)) {
			tag = 8;
		}

		if ((f == 2) && (c == 2)) {
			tag = 9;
		}

		for(int i = 0; i < listTabuleiro.size(); i++){
			if(listTabuleiro.get(i) == tag){
				listTabuleiro.remove(i);
				break;
			}
		}
		
		return tag;
	}
	
	
	protected void jogarAndroid(){
		
		int quadrado = 0;
		nivel_desc--;
		int tipoJogada = random.nextInt(nivel_desc) + 1;

		if(tipoJogada == 1 || tipoJogada == nivel_desc){
			quadrado = jogarAndroidAleatorio();
		} else {
			quadrado = jogarAndroidMinimax();
			
			if(quadrado < 1){
				quadrado = jogarAndroidAleatorio();
			}
			
			nGanhador = ganharPartida();
		}
		
		try{
			Button b = getQuadrado(quadrado);
			if(b.getText().toString().equals("")){
				b.setTypeface(font);
				b.setText("O");
			}
			prxJogada.setText(getResources().getString(R.string.sua_vez));
		}catch (Exception e){
			
		}
		isFim();
	}

	public void newGame(View v) {
		iniciarPartida();
		vibrar();
		posicoesTabuleiro();
		vezJogador = vezJogador ? false : true;
		nTabuleiro = getTabuleiro();
		ganhador = false;
		empate = false;
		nivel_desc = nivel;
		
		for (int i = 1; i < 10; i++) {
			Button b = getQuadrado(i);
			b.setText("");
			b.setTextColor(Color.WHITE);
		}

		if (!vezJogador) {
			Random rdn = new Random();
			int jogar = rdn.nextInt(9);
			jogar = listTabuleiro.get(jogar);
			
			Button b = getQuadrado(jogar);
			b.setTypeface(font);
			b.setText("O");

			int n = -1;
			int m = -1;

			switch (jogar) {
			case 1:
				n = 0;
				m = 0;
				break;
			case 2:
				n = 0;
				m = 1;
				break;
			case 3:
				n = 0;
				m = 2;
				break;
			case 4:
				n = 1;
				m = 0;
				break;
			case 5:
				n = 1;
				m = 1;
				break;
			case 6:
				n = 1;
				m = 2;
				break;
			case 7:
				n = 2;
				m = 0;
				break;
			case 8:
				n = 2;
				m = 1;
				break;
			case 9:
				n = 2;
				m = 2;
				break;
			}
			
			for(int i = 0; i < listTabuleiro.size(); i++){
				if(listTabuleiro.get(i) == jogar){
					listTabuleiro.remove(i);
					break;
				}
			}

			nTabuleiro[n][m] = 1;
			prxJogada.setText(getResources().getString(R.string.sua_vez));
		}
		
		
		//showInterstial();
	}
	
	private void showInterstial(){
		
		if(contadorInterstial == 4){
			
			try{
				contadorInterstial = 0;
				interstial.show();
			} catch(Exception e){ }

		} else {
			contadorInterstial++;
		}
	}
	
	protected void vibrar(){
		Vibrator v1 = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		v1.vibrate(50);
	}

	public void setColorQuadrados(int btn, int colorX) {
		getQuadrado(btn).setTextColor(getResources().getColor(colorX));
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

	public void isFim() {

		int cor = 0;
		
		if (nGanhador == 1) {
			//Toast.makeText(this, getResources().getString(R.string.android_venceu), Toast.LENGTH_SHORT).show();
			exibirToast(getResources().getString(R.string.android_venceu));
			placarAndroid++;
			getPlacar("O").setText("" + placarAndroid);
			verifica();
			ganhador = true;
			cor = R.color.red;
			atualizarPontos("jogador2", dificuldade);
			showInterstial();
		}

		if (nGanhador == 0) {
			Toast.makeText(this, getResources().getString(R.string.voce_venceu), Toast.LENGTH_SHORT).show();
			exibirToast(getResources().getString(R.string.voce_venceu));
			placarJogador++;
			getPlacar("X").setText("" + placarJogador);
			verifica();
			ganhador = true;
			cor = R.color.azul_claro;
			atualizarPontos("jogador1", dificuldade);
			showInterstial();
		}

		if (tabuleiroCompleto() && nGanhador == -1 && !empate) {
			//Toast.makeText(this, getResources().getString(R.string.empate_jogo), Toast.LENGTH_SHORT).show();
			exibirToast(getResources().getString(R.string.empate_jogo));
			placarEmpate++;
			getPlacar("Empate").setText("" + placarEmpate);
			verifica();
			empate = true;
			atualizarPontos("empate", dificuldade);
			showInterstial();
		}	
		
		if (nGanhador != -1) {
			for (int x = 0; x < 8; ++x) {

				String s1 = getQuadrado(estadoFinal[x][0]).getText().toString();
				String s2 = getQuadrado(estadoFinal[x][1]).getText().toString();
				String s3 = getQuadrado(estadoFinal[x][2]).getText().toString();

				if (!s1.equals("") && !s2.equals("") && !s3.equals("")) {

					if (s1.equals(s2) && s2.equals(s3)) {

						setColorQuadrados(estadoFinal[x][0], cor);
						setColorQuadrados(estadoFinal[x][1], cor);
						setColorQuadrados(estadoFinal[x][2], cor);

						break;
					}
				}
			}
		}

		
	}

	private void verifica() {
		if (vezJogador) {
			prxJogada.setText(getResources().getString(R.string.android_comeca));
		}
	}

	// ///////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////

	public int[][] getTabuleiro() {
		return nTabuleiro;
	}

	public void iniciarPartida() {
		for (int n = 0; n < TAM; n++)
			for (int m = 0; m < TAM; m++)
				nTabuleiro[n][m] = -1;
		nGanhador = -1;
	}

	public void clickBotao(int n, int m) {
		if (n >= 0 && m >= 0 && n < TAM && m < TAM && nTabuleiro[n][m] == -1) {
			if (nGanhador == -1) {
				nTabuleiro[n][m] = 0;
				nGanhador = ganharPartida();
				//ponerFichaOrdenador();
			}
		}
	}

	public int ganharPartida() {
		if (nTabuleiro[0][0] != -1 && nTabuleiro[0][0] == nTabuleiro[1][1]
				&& nTabuleiro[0][0] == nTabuleiro[2][2])
			return nTabuleiro[0][0];

		if (nTabuleiro[0][2] != -1 && nTabuleiro[0][2] == nTabuleiro[1][1]
				&& nTabuleiro[0][2] == nTabuleiro[2][0])
			return nTabuleiro[0][2];

		for (int n = 0; n < TAM; n++) {
			if (nTabuleiro[n][0] != -1 && nTabuleiro[n][0] == nTabuleiro[n][1]
					&& nTabuleiro[n][0] == nTabuleiro[n][2])
				return nTabuleiro[n][0];
			if (nTabuleiro[0][n] != -1 && nTabuleiro[0][n] == nTabuleiro[1][n]
					&& nTabuleiro[0][n] == nTabuleiro[2][n])
				return nTabuleiro[0][n];
		}
		return -1;
	}

	public int getGanador() {
		return nGanhador;
	}

	// Algoritmo minimax
	private boolean tabuleiroCompleto() {
		for (int n = 0; n < TAM; n++)
			for (int m = 0; m < TAM; m++)
				if (nTabuleiro[n][m] == -1)
					return false;
		return true;
	}

	private boolean fimPartida() {
		return tabuleiroCompleto() || ganharPartida() != -1;
	}

	private int[] ponerFichaOrdenador() {

		int f = 0, c = 0;

		if (!fimPartida()) {

			int v = Integer.MIN_VALUE;
			int aux;
			for (int n = 0; n < TAM; n++) {
				for (int m = 0; m < TAM; m++) {
					if (nTabuleiro[n][m] == -1) {
						nTabuleiro[n][m] = 1;
						aux = min();
						if (aux > v) {
							v = aux;
							f = n;
							c = m;
						}
						nTabuleiro[n][m] = -1;
					}
				}
			}
			nTabuleiro[f][c] = 1;
		}
		nGanhador = ganharPartida();

		int posicao[] = new int[2];
		posicao[0] = f;
		posicao[1] = c;

		return posicao;
	}

	private int max() {
		if (fimPartida()) {
			if (ganharPartida() != -1)
				return -1;
			else
				return 0;
		}
		int v = Integer.MIN_VALUE;
		int aux;
		for (int n = 0; n < TAM; n++) {
			for (int m = 0; m < TAM; m++) {
				if (nTabuleiro[n][m] == -1) {
					nTabuleiro[n][m] = 1;
					aux = min();
					if (aux > v)
						v = aux;
					nTabuleiro[n][m] = -1;

				}
			}
		}
		return v;
	}

	private int min() {
		if (fimPartida()) {
			if (ganharPartida() != -1)
				return 1;
			else
				return 0;
		}
		int v = Integer.MAX_VALUE;
		int aux;
		for (int n = 0; n < TAM; n++) {
			for (int m = 0; m < TAM; m++) {
				if (nTabuleiro[n][m] == -1) {
					nTabuleiro[n][m] = 0;
					aux = max();
					if (aux < v)
						v = aux;
					nTabuleiro[n][m] = -1;
				}
			}
		}
		return v;
	}

}

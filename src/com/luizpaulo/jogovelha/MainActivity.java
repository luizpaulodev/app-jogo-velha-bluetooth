package com.luizpaulo.jogovelha;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.luizpaulo.Banco.DatabaseHelper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
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


@SuppressLint("InflateParams") 
public class MainActivity extends ActionBarActivity {
	
	protected BluetoothAdapter bluetooth;
	
	private View view;
	protected final String QUADRADO = "quadrado";
	protected final String PLACAR = "placar";
	protected final String NOVO_JOGO = "novo_jogo";
	protected final int CLOSE = 10;
	
	protected final String BOLA = "O";
	protected final String XIS = "X";
	protected String lastPlay = BOLA;
	
	protected int jogadorX = 0;
	protected int jogadorO = 0;
	protected int empate = 0;
	protected int contadorVelha = 0;
	
	protected BluetoothDevice device;
	
	protected TextView prxJogada;
	protected TextView txtJogada;
	
	protected Button btnNovo;
	
	protected Handler handler = new Handler();
		
	protected boolean jogar = false;
	protected boolean ganhador = false;
	protected boolean comecar;
	
	protected Typeface font;  
	
	protected int contadorInterstial;
	protected InterstitialAd interstial;
	protected AdRequest adRequest;
	
	protected DatabaseHelper helper;
	
	private int[][] estadoFinal = new int[][]{
			
			{1, 2, 3},
			{4, 5, 6},
			{7, 8, 9},
			
			{1, 4, 7},
			{2, 5, 8},
			{3, 6, 9},
			
			{1, 5, 9},
			{3, 5, 7}
	}; 
	@SuppressWarnings("deprecation")
	protected int tamanhoDisplay(){
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
				.getDefaultDisplay();
		
		int width = display.getWidth();

		//int height = display.getHeight();
		
		return width;
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        helper = new DatabaseHelper(this);
        
        if(tamanhoDisplay() <= 400){
        	setView(getLayoutInflater().inflate(R.layout.activity_jogo_velha_320, null));
        } else {
        	setView(getLayoutInflater().inflate(R.layout.activity_jogo_velha, null));
        }
                
        setContentView(getView());
        setEnableQuadrado(false);
                
        font = Typeface.createFromAsset(getAssets(), "fonts/Cocogoose.otf"); 
        
        prxJogada = (TextView) getView().findViewById(R.id.prxJogada);
        txtJogada = (TextView) getView().findViewById(R.id.jogada);
        btnNovo = (Button) getView().findViewById(R.id.btnNovoJogo);
                
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
                        
        // Bluetooth adapter
 		bluetooth = BluetoothAdapter.getDefaultAdapter();

 		if (bluetooth == null) {
 			Toast.makeText(this, getResources().getString(R.string.bluetooth_nao_disponivel), Toast.LENGTH_LONG).show();
 			// Vamos fechar a activity neste caso
 			finish();
 		}
 		
 		try{
        	contadorInterstial = 1;
    		interstial = new InterstitialAd(this);				      
    		interstial.setAdUnitId("ca-app-pub-2167493564469787/4398760152");
    		adRequest = new AdRequest.Builder().build();
    		interstial.loadAd(adRequest);
        } catch(Exception e){}
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		try{
			interstial.loadAd(adRequest);
		} catch(Exception e){}
		// Se o bluetooth não está ligado
		if (bluetooth.isEnabled()) {
			//Toast.makeText(this, "Bluetooth está ligado!", Toast.LENGTH_LONG).show();
		} else {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, 0);
		}
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
		
	public void setColorQuadrados(int btn, int colorX){
		getQuadrado(btn).setTextColor(getResources().getColor(colorX));
	}
	
	public void isFim(){
		
		if(!ganhador){
			for(int x = 0; x < 8; ++x){
				
				final String s1 = getQuadrado( estadoFinal[x][0]).getText().toString();
				String s2 = getQuadrado( estadoFinal[x][1]).getText().toString();
				String s3 = getQuadrado( estadoFinal[x][2]).getText().toString();
							
				if(!s1.equals("") && !s2.equals("") && !s3.equals("")){
					
					if(s1.equals(s2) && s2.equals(s3)){
						ganhador = true;
						defineGanhador(s1, x);

						setEnableQuadrado(false);
						break;
					}
				}
			} 
		}
	}
	
	public void defineGanhador(final String s1, final int x){
		runOnUiThread(new Runnable() {
			int cor = 0;
			@Override
			public void run() {
				
				if(s1.equals(BOLA)){
					jogadorO+=1;
					getPlacar("O").setText(String.valueOf(jogadorO));
					//Toast.makeText(getView().getContext(), getResources().getString(R.string.jogador_o_ganhou), Toast.LENGTH_SHORT).show();
					exibirToast(getResources().getString(R.string.jogador_o_ganhou));
					cor = R.color.red;
					atualizarPontos("jogador2", "b");
					showInterstial();
				} else {
					jogadorX+=1;
					getPlacar("X").setText(String.valueOf(jogadorX));
					//Toast.makeText(getView().getContext(), getResources().getString(R.string.jogador_x_ganhou), Toast.LENGTH_SHORT).show();
					exibirToast(getResources().getString(R.string.jogador_x_ganhou));
					cor = R.color.azul_claro;
					atualizarPontos("jogador1", "b");
					showInterstial();
				}
				
				setColorQuadrados(estadoFinal[x][0], cor);
				setColorQuadrados(estadoFinal[x][1], cor);
				setColorQuadrados(estadoFinal[x][2], cor);
			}
		});
	}
	
	public void proximaJogada(){
		
		if(getLastPlay().equals(BOLA)){
			prxJogada.setText(XIS);
		} else {
			prxJogada.setText(BOLA);
		}
	}
	
	public void newGame(View v){
		for(int i = 1; i <= 9; i++){
			if(getQuadrado(i) != null){
				getQuadrado(i).setText("");
				getQuadrado(i).setTextColor(getResources().getColor(R.color.branco));
			}
		}
		contadorVelha = 0;
		//showInterstial();
	}
	
	protected void showInterstial(){
		
		if(contadorInterstial == 4){
			
			try{
				contadorInterstial = 0;
				interstial.show();
			} catch(Exception e){ }

		} else {
			contadorInterstial++;
		}
	}

	public void setEnableQuadrado(boolean b){
		for(int i = 1; i <= 9; i++){
			if(getQuadrado(i) != null){
				getQuadrado(i).setClickable(b);
			}
		}
	}
	
	public void clickQuadrado(View v){
		
	}
	
	public Button getQuadrado(int tagNum){
		return(Button) getView().findViewWithTag(QUADRADO+tagNum);
	}
	
	public TextView getPlacar(String tagPlacar){
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch(resultCode){
			case ActionBarActivity.RESULT_CANCELED:
				Toast.makeText(this, getResources().getString(R.string.bluetooth_nao_ativado), Toast.LENGTH_SHORT).show();
				finish();
				break;
				
			case 9: 
				
				device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				break;
				
			case CLOSE:
				break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.jogo_bluetooth, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		int id = item.getItemId();
		
		switch(id){
			case android.R.id.home:
				setResult(CLOSE);
				finish();
				break;
				 
			case R.id.ficar_visivel: ficarVisivel();
				break;
				
			case R.id.devices_pareados: 
				Intent intent = new Intent(this, ListaPareadosActivity.class);
				startActivityForResult(intent, 9);
				break;
				
			case R.id.instrucoes:
				
				try{
					PackageInfo packageInfo = getPackageManager().getPackageInfo("com.luizpaulo.jogovelha", 0);

					AlertDialog.Builder alertaInfo = new AlertDialog.Builder(this);
					alertaInfo.setTitle(getResources().getString(R.string.informacoes));
					alertaInfo.setMessage(getResources().getString(R.string.informacoes_modo_bluetooth, packageInfo.versionName));
					alertaInfo.show();
				} catch(NameNotFoundException e){
					e.printStackTrace();
				}
				break;
				
			case R.id.compartilhar:
				Intent share = new Intent(Intent.ACTION_SEND);
				share.setType("text/plain");
				share.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.mensagemCompartilhar));
				startActivity(Intent.createChooser(share, getResources().getString(R.string.compartilhar) + "..."));
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	protected void vibrar(){
		Vibrator v1 = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		v1.vibrate(50);
	}
	
	protected void ativarBluetooth(){
		
		if(bluetooth.isEnabled()){
			Toast.makeText(this, getResources().getString(R.string.bluetooth_ligado), Toast.LENGTH_SHORT).show();
			//finish();
		} else {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, 0);
		}	
	}
	
	protected void ficarVisivel() {
		// Garante que alguém pode te encontrar
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivity(discoverableIntent);
	}
}

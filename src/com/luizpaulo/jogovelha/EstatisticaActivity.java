package com.luizpaulo.jogovelha;

import java.util.Random;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.luizpaulo.Banco.DatabaseHelper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class EstatisticaActivity extends ActionBarActivity {

	protected View view;
	protected Typeface font;
	private DatabaseHelper helper;
	private boolean[] zerarCampos = new boolean[3];
	
	protected InterstitialAd interstial;
	protected AdRequest adRequest;
		
	@SuppressLint("InflateParams") @Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setView(getLayoutInflater().inflate(R.layout.estatistica_activity, null));
		setContentView(getView());

		android.support.v7.app.ActionBar a = getSupportActionBar();
		
		a.setDisplayHomeAsUpEnabled(true);
		a.setDisplayShowHomeEnabled(true);
		
		helper = new DatabaseHelper(this);
		
		font = Typeface.createFromAsset(getAssets(), "fonts/Cocogoose.otf");
		
		for(int i = 1; i <= 12; i++){
			getTextView(i).setTypeface(font);					
		}
		
		listarEstatisticas();
		
		try{
    		interstial = new InterstitialAd(this);				      
    		interstial.setAdUnitId("ca-app-pub-2167493564469787/4398760152");
    		adRequest = new AdRequest.Builder().build();
    		interstial.loadAd(adRequest);
        } catch(Exception e){}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.estatistica, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		int id = item.getItemId();
		
		switch(id){
			case android.R.id.home:
				showInterstial();
				finish();
				break;
				
			case R.id.zerarPlacares:
				zerar();
				
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		showInterstial();		
	}
	
	private void showInterstial(){
		int valor = new Random().nextInt(10) + 1;
		
		if(valor == 3 || valor == 7){
			try{
				interstial.show();
			} catch(Exception e){ }
		}
	}
	
	protected void zerar(){
		AlertDialog.Builder alerta = new AlertDialog.Builder(this);
		
		String[] items = {getResources().getString(R.string.player_vs_android),
				getResources().getString(R.string.player_vs_player),
				getResources().getString(R.string.via_bluetooth)};
		final boolean[] checkedItems = {false, false, false};
		alerta.setTitle(getResources().getString(R.string.selecione));
		
		alerta.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
				zerarCampos[0] = checkedItems[0];
				zerarCampos[1] = checkedItems[1];
				zerarCampos[2] = checkedItems[2];
			}
		});
		
		alerta.setPositiveButton(getResources().getString(R.string.zerar), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				ContentValues values;	
				SQLiteDatabase db = helper.getWritableDatabase();
				
				if(zerarCampos[0]){
					values = new ContentValues();
					values.put("jogador1", 0);
					values.put("jogador2", 0);
					values.put("empate", 0);
					
					db.update("jogovelha", values, "tipo = ?", new String[]{"f"});					
					db.update("jogovelha", values, "tipo = ?", new String[]{"m"});
					db.update("jogovelha", values, "tipo = ?", new String[]{"d"});
					
					for(int i = 100; i <= 300; i+=100){
						for(int x = 1; x <= 3; x++){
							atualizarCampos(i + x).setText("0");
						}
					}
				}
				
				if(zerarCampos[1]){
					values = new ContentValues();
					values.put("jogador1", 0);
					values.put("jogador2", 0);
					values.put("empate", 0);
					
					String[] whereArgs = {"pp"};
					
					db.update("jogovelha", values, "tipo = ?", whereArgs);	
					for(int i = 401; i <= 403; i++){
						atualizarCampos(i).setText("0");
					}
				}
				
				if(zerarCampos[2]){
					values = new ContentValues();
					values.put("jogador1", 0);
					values.put("jogador2", 0);
					values.put("empate", 0);
					
					String[] whereArgs = {"b"};
					
					db.update("jogovelha", values, "tipo = ?", whereArgs);
					for(int i = 501; i <= 503; i++){
						atualizarCampos(i).setText("0");
						
					}
				}
				
				Toast.makeText(EstatisticaActivity.this, getResources().getString(R.string.pontuacao_zerada), Toast.LENGTH_SHORT).show();
			}
		});
		
		alerta.setNegativeButton(getResources().getString(R.string.cancelar), null);
		
		alerta.show();
	}
	
	protected void listarEstatisticas(){
		SQLiteDatabase db = helper.getReadableDatabase();
		
		String SQL = "SELECT jogador1, jogador2, empate FROM jogovelha";
		Cursor cursor = db.rawQuery(SQL, null);
		cursor.moveToFirst();

		for(int i = 100; i <= 500; i+=100){
			for(int x = 1; x <= 3; x++){
				atualizarCampos(i + x).setText(String.valueOf(cursor.getInt(x - 1)));
			}
			cursor.moveToNext();
		}
		
	}
	
	protected TextView getTextView(int tagNum){
		return (TextView) getView().findViewWithTag("texto" + tagNum);

	}
	
	protected TextView atualizarCampos(int tagNum){
		return (TextView) getView().findViewWithTag("campo" + tagNum);

	}
	
	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}
}

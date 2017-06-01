package com.reserva;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TimePicker;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnSeekBarChangeListener,
		OnClickListener, OnDateSetListener, OnTimeSetListener {

	EditText nombre;
	TextView cuantasPersonas, datos;
	Button fecha, hora;
	SeekBar barraPersonas;

	SimpleDateFormat horaFormato, fechaFormato;

	String nombreReserva = "";
	String numPersonas = "";
	String fechaSel = "", horaSel = "";
	Date fechaConv;
	String cuantasPersonasFormat = "";
	int personas = 1; // Valor por omision, al menos 1 persona tiene que
						// reservar, no?

	Calendar calendario;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		cuantasPersonas = (TextView) findViewById(R.id.cuantasPersonas);
		barraPersonas = (SeekBar) findViewById(R.id.personas);

		fecha = (Button) findViewById(R.id.fecha);
		hora = (Button) findViewById(R.id.hora);

		barraPersonas.setOnSeekBarChangeListener(this);

		nombre = (EditText) findViewById(R.id.nombre);
		datos = (TextView) findViewById(R.id.datos);
		datos.setText("");
		cuantasPersonasFormat = cuantasPersonas.getText().toString();
		// cuantasPersonasFormat = "personas: %d";
		cuantasPersonas.setText("Personas: 1"); // condicion inicial

		// Para seleccionar la fecha y la hora
		Calendar fechaSeleccionada = Calendar.getInstance();
		fechaSeleccionada.set(Calendar.HOUR_OF_DAY, 12); // hora inicial
		fechaSeleccionada.clear(Calendar.MINUTE); // 0
		fechaSeleccionada.clear(Calendar.SECOND); // 0

		// formatos de la fecha y hora
		fechaFormato = new SimpleDateFormat(fecha.getText().toString());
		horaFormato = new SimpleDateFormat("HH:mm");
		// horaFormato = new SimpleDateFormat(hora.getText().toString());

		// La primera vez mostramos la fecha actual
		Date fechaReservacion = fechaSeleccionada.getTime();
		fechaSel = fechaFormato.format(fechaReservacion);
		fecha.setText(fechaSel); // fecha en el

		horaSel = horaFormato.format(fechaReservacion);
		// boton
		hora.setText(horaSel); // hora en el boton

		// Otra forma de ocupar los botones
		fecha.setOnClickListener(this);
		hora.setOnClickListener(this);

	}

	@Override
	public void onProgressChanged(SeekBar barra, int progreso,
			boolean delUsuario) {

		numPersonas = String.format(cuantasPersonasFormat,
				barraPersonas.getProgress() + 1);
		personas = barraPersonas.getProgress() + 1; // este es el valor que se
													// guardara en la BD
		// Si no se mueve la barra, enviamos el valor personas = 1
		cuantasPersonas.setText(numPersonas);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
	}

	@Override
	public void onClick(View v) {
		if (v == fecha) {
			Calendar calendario = parseCalendar(fecha.getText(), fechaFormato);
			new DatePickerDialog(this, this, calendario.get(Calendar.YEAR),
					calendario.get(Calendar.MONTH),
					calendario.get(Calendar.DAY_OF_MONTH)).show();
		} else if (v == hora) {
			Calendar calendario = parseCalendar(hora.getText(), horaFormato);
			new TimePickerDialog(this, this,
					calendario.get(Calendar.HOUR_OF_DAY),
					calendario.get(Calendar.MINUTE), false) // /true = 24 horas
					.show();
		}
	}

	private Calendar parseCalendar(CharSequence text,
			SimpleDateFormat fechaFormat2) {
		try {
			fechaConv = fechaFormat2.parse(text.toString());
		} catch (ParseException e) { // import java.text.ParsedExc
			throw new RuntimeException(e);
		}
		Calendar calendario = Calendar.getInstance();
		calendario.setTime(fechaConv);
		return calendario;
	}

	@Override
	public void onDateSet(DatePicker picker, int anio, int mes, int dia) {
		calendario = Calendar.getInstance();
		calendario.set(Calendar.YEAR, anio);
		calendario.set(Calendar.MONTH, mes);
		calendario.set(Calendar.DAY_OF_MONTH, dia);

		fechaSel = fechaFormato.format(calendario.getTime());
		fecha.setText(fechaSel);

	}

	public void onTimeSet(TimePicker picker, int horas, int minutos) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, horas);
		calendar.set(Calendar.MINUTE, minutos);

		horaSel = horaFormato.format(calendar.getTime());
		hora.setText(horaSel);
	}

	public void reserva(View v) {
		Intent envia = new Intent(this, Actividad2.class);
		Bundle datos = new Bundle();
		datos.putString("nombre", nombre.getText().toString().trim());
		datos.putInt("personas", personas);
		datos.putString("fecha", fechaSel);
		datos.putString("hora", horaSel);
		envia.putExtras(datos);
		finish();
		startActivity(envia);
	}
	
	public void verTabla(View v){

		// Ver registros de Reservacion.txt
		String fichero = "/data/data/com.reserva/files/Restaurant.txt";
		File f=new File(fichero);
		StringBuffer strContent = new StringBuffer("");
		FileInputStream fis=null;
		int renglon;
		try {
			fis=new FileInputStream(f);

			Toast.makeText(getApplicationContext(),
					"Total del archivo (en bytes) : " + fis.available(),
					Toast.LENGTH_LONG).show();

			while((renglon =fis.read())!=-1)
			{
				strContent.append((char)renglon);
			}
			datos.append(strContent);
			fis.close();
		} catch (FileNotFoundException e) {
			Toast.makeText(getApplicationContext(),
						"Error en Lectura " + e,
						Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(),
						"Error en Lectura " + e,
						Toast.LENGTH_LONG).show();
		}


		// Ver registros de SD EXTERNA Reservacion.txt

		//		Almacenamiento en Fichero en Memoria xterna
//		boolean mExternalStorageAvailable = false;
//		String state = Environment.getExternalStorageState();
//
//		// COMPROBACION DEL ALMACENAMIENTO EXTERNO
//		if (Environment.MEDIA_MOUNTED.equals(state)) {
//			// Podremos leer y escribir en ella
//			mExternalStorageAvailable = true;
//		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
//			// En este caso solo podremos leer los datos
//			mExternalStorageAvailable = true;
//		}
//
//		// CREACION DE ARCHIVOS PUBLICOS
//		if (mExternalStorageAvailable == true)
//		{
//			// Obtenemos la ruta de la carpeta creada "MisBD" dentro del directorio "DCIM"
//			String fichero = Environment.getExternalStoragePublicDirectory(
//					Environment.DIRECTORY_DCIM) + "/MisBD/Restaurant.txt";
//			File f=new File(fichero);
//			StringBuffer strContent = new StringBuffer("");
//			int renglon;
//			try {
//				FileInputStream fis=new FileInputStream(f);
//				Toast.makeText(getApplicationContext(),
//						"Total del archivo (en bytes) : " + fis.available(),
//						Toast.LENGTH_LONG).show();
//
//				while((renglon =fis.read())!=-1)
//				{
//					strContent.append((char)renglon);
//				}
//				datos.append(strContent);
//				fis.close();
//			} catch (FileNotFoundException e) {
//				Toast.makeText(getApplicationContext(),
//							"Error en Lectura " + e,
//							Toast.LENGTH_LONG).show();
//			} catch (IOException e) {
//				Toast.makeText(getApplicationContext(),
//							"Error en Lectura " + e,
//							Toast.LENGTH_LONG).show();
//			}
//
//		} else {
//			Toast.makeText(getApplicationContext(),
//					"No ha sido posible acceder al archivo ",
//					Toast.LENGTH_LONG).show();
//			Log.d("ERROR", "No ha sido posible acceder al archivo ");
//		}

		// Ver registros de la tabla BD_Reservacion
//		SQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
//		Create a helper object to create, open, and/or manage a database.
//		AuxiliarSQL sql = new AuxiliarSQL(this,	"DB_Restaurant", null, 1);
//		final SQLiteDatabase db = sql.getWritableDatabase();
//		String[] campos = {"_id", "Nombre", "Personas", "Fecha", "Hora"};
//		query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
//		Query the given table, returning a Cursor over the result set.
//		Cursor selectAll = db.query("Reservacion", campos, null, null, null, null, null);
//
//		int Total = selectAll.getCount();
//		for(int i = 1; i <= Total; i++){
//			selectAll.moveToNext();
//			String nombreSelect = selectAll.getString(1);
//			datos.append(selectAll.getInt(0) + "|"+
//			nombreSelect + "|" +
//					selectAll.getString(2) + "|" +
//					selectAll.getString(3) + "\n");
//		}
//		db.close();


	}
}

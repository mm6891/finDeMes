package globalsolutions.findemes.pantallas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import globalsolutions.findemes.R;
import globalsolutions.findemes.database.dao.MovimientoDAO;
import globalsolutions.findemes.database.model.InformeItem;
import globalsolutions.findemes.database.model.MovimientoItem;
import globalsolutions.findemes.database.util.Constantes;

import static android.app.PendingIntent.getActivity;

/**
 * Created by Manuel on 23/02/2015.
 */
public class InformesActivity extends Activity {

    public enum Periodos{
        MENSUAL, TRIMESTRAL, QUINCENAL
    }

    private Spinner spTipoMovimiento;
    private Spinner spPeriodo;
    private Spinner spPeriodoFiltro;

    private ListView listViewMovsInforme;

    //this counts how many Spinner's are on the UI
    private int mSpinnerCount=0;

    //this counts how many Spinner's have been initialized
    private int mSpinnerInitializedCount=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informes);

        //boton retroceder
        ImageButton btnReturn = (ImageButton) findViewById(R.id.btnBackButton);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                backActivity();
            }
        });

        //spinner periodo
        spPeriodo = (Spinner) findViewById(R.id.spPeriodo);
        spPeriodo.setAdapter(new ArrayAdapter<Periodos>(this, android.R.layout.simple_spinner_dropdown_item, Periodos.values()));

        mSpinnerCount++;
        spPeriodo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mSpinnerInitializedCount < mSpinnerCount)
                {
                    mSpinnerInitializedCount++;
                }
                else {
                    String periodoFiltro = spPeriodoFiltro.getSelectedItem() != null ? (String) spPeriodoFiltro.getSelectedItem() : "-1";
                    filtraInforme(view, (String) spTipoMovimiento.getSelectedItem(), ((Periodos) spPeriodo.getSelectedItem()).toString(), periodoFiltro);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spPeriodoFiltro = (Spinner) findViewById(R.id.spPeriodoFiltro);
        //lista movimientos
        //recuperamos movimientos
        final ArrayList<MovimientoItem> movs = new MovimientoDAO().cargaMovimientos(getApplicationContext());
        //cargamos anyos
        ArrayList<String> anyos = new ArrayList<String>();
        if(movs.size() <= 0 )
            showToast("NO HAY INFORMES ACTUALMENTE");

        for(MovimientoItem mov : movs){
            String fecha = mov.getFecha();
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
            Calendar cal  = Calendar.getInstance();
            try {
                cal.setTime(formato.parse(fecha));
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            int year = cal.get(Calendar.YEAR);
            if(!anyos.contains(String.valueOf(new Integer(year))))
                anyos.add(String.valueOf(new Integer(year)));
        }
        spPeriodoFiltro.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, anyos));

        mSpinnerCount++;
        spPeriodoFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mSpinnerInitializedCount < mSpinnerCount)
                {
                    mSpinnerInitializedCount++;
                }
                else {
                    String periodoFiltro = spPeriodoFiltro.getSelectedItem() != null ? (String) spPeriodoFiltro.getSelectedItem() : "-1";
                    filtraInforme(view, (String) spTipoMovimiento.getSelectedItem(), ((Periodos) spPeriodo.getSelectedItem()).toString(), periodoFiltro);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //spinner movimiento
        ArrayList<String> tiposMovimientos = new ArrayList<String>();
        tiposMovimientos.add(Constantes.TIPO_FILTRO_RESETEO.toString());
        tiposMovimientos.add(Constantes.TIPO_MOVIMIENTO_INGRESO.toString());
        tiposMovimientos.add(Constantes.TIPO_MOVIMIENTO_GASTO.toString());
        spTipoMovimiento = (Spinner) findViewById(R.id.spTipoMovimiento);
        spTipoMovimiento.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, tiposMovimientos));
        spTipoMovimiento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String periodoFiltro = spPeriodoFiltro.getSelectedItem() != null ? (String) spPeriodoFiltro.getSelectedItem() : "-1";
                filtraInforme(view, (String)spTipoMovimiento.getSelectedItem(), ((Periodos) spPeriodo.getSelectedItem()).toString(), periodoFiltro);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //cargamos adaptador de informes
        listViewMovsInforme = (ListView) findViewById(R.id.listViewMovInforme);
        listViewMovsInforme.setAdapter(new InformeAdapter(getApplicationContext(), new ArrayList<InformeItem>()));
        ((InformeAdapter)listViewMovsInforme.getAdapter()).setOnDataChangeListener(new InformeAdapter.OnDataChangeListener() {
            @Override
            public void onDataChanged(final ArrayList<InformeItem> informes) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //load resumen
                        Double ingresos = new Double(0.00);
                        Double gastos = new Double(0.00);
                        Double saldo = new Double(0.00);

                        for(InformeItem inf : informes){
                            ingresos += Double.valueOf(inf.getIngresoValor());
                            gastos += Double.valueOf(inf.getGastoValor());
                        }
                        saldo = ingresos - gastos;
                        // This code will always run on the UI thread, therefore is safe to modify UI elements.
                        ((TextView) findViewById(R.id.tvIngresosInformesValor)).setText(String.valueOf(ingresos));
                        TextView tvGastosTotal = (TextView) findViewById(R.id.tvGastosInformesValor);
                        tvGastosTotal.setText(String.valueOf(gastos));
                        TextView tvSaldoTotal = (TextView) findViewById(R.id.tvSaldoInformesValor);
                        tvSaldoTotal.setText(String.valueOf(saldo));
                    }
                });
            }
        });
    }

    public void filtraInforme(View v, String tipoMovimiento, String periodo, String periodoFiltro){
        String filtro = tipoMovimiento + ";" + periodo + ";" + periodoFiltro;
        ((InformeAdapter)listViewMovsInforme.getAdapter()).getFilter().filter(filtro);
    }

    public void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        backActivity();
    }

    private void backActivity(){
        Intent in = new Intent(InformesActivity.this, MainActivity.class);
        startActivity(in);
        setResult(RESULT_OK);
        finish();
    }

}

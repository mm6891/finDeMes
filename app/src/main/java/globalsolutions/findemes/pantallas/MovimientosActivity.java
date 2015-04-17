package globalsolutions.findemes.pantallas;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import globalsolutions.findemes.R;
import globalsolutions.findemes.database.dao.GastoDAO;
import globalsolutions.findemes.database.dao.IngresoDAO;
import globalsolutions.findemes.database.dao.MovimientoDAO;
import globalsolutions.findemes.database.model.MovimientoItem;
import globalsolutions.findemes.database.util.Constantes;
import globalsolutions.findemes.pantallas.util.Util;

public class MovimientosActivity extends FragmentActivity implements GastoDialog.OnGastoDialogListener, IngresoDialog.OnIngresoDialogListener{

    @Override
    public void onGastoDialogSubmit(String result) {
        actualizarFiltro(result);
    }

    @Override
    public void onIngresoDialogSubmit(String result) {
        actualizarFiltro(result);
    }

    public void actualizarFiltro(String result) {
        if(result.equals(String.valueOf(Activity.RESULT_OK))){
            ((MovimientoAdapter)listViewMovs.getAdapter()).setMesSeleccionado(spFiltroMes.getSelectedItemPosition());
            ((MovimientoAdapter)listViewMovs.getAdapter()).setAnyoSeleccionado(new Integer((String) spFitroAnyo.getSelectedItem()).intValue());

            if(!((CheckBox) findViewById(R.id.cbIconMinus)).isChecked() && ((CheckBox) findViewById(R.id.cbIconPlus)).isChecked())
                ((MovimientoAdapter) listViewMovs.getAdapter()).getFilter().filter(getResources().getString(R.string.TIPO_MOVIMIENTO_INGRESO));
            if(((CheckBox) findViewById(R.id.cbIconMinus)).isChecked() && !((CheckBox) findViewById(R.id.cbIconPlus)).isChecked())
                ((MovimientoAdapter) listViewMovs.getAdapter()).getFilter().filter(getResources().getString(R.string.TIPO_MOVIMIENTO_GASTO));
            else
                ((MovimientoAdapter)listViewMovs.getAdapter()).getFilter().filter(getResources().getString(R.string.TIPO_FILTRO_RESETEO));
        }
    }

   /* public enum Meses {
        ENERO, FEBRERO, MARZO, ABRIL,
        MAYO, JUNIO, JULIO, AGOSTO, SEPTIEMBRE, OCTUBRE, NOVIEMBRE, DICIEMBRE
    }*/

    private ListView listViewMovs;
    private Spinner spFiltroMes;
    private Spinner spFitroAnyo;

    //this counts how many Spinner's are on the UI
    private int mSpinnerCount=0;

    //this counts how many Spinner's have been initialized
    private int mSpinnerInitializedCount=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimientos);

        //boton retroceder
        ImageButton btnReturn = (ImageButton) findViewById(R.id.btnBackButton);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                backActivity();
            }
        });

        spFitroAnyo = (Spinner) findViewById(R.id.spAnyos);

        //recuperamos movimientos
        final ArrayList<MovimientoItem> movs = new MovimientoDAO().cargaMovimientos(getApplicationContext());
        //cargamos anyos
        ArrayList<String> anyos = new ArrayList<String>();
        if(movs.size() <= 0 )
            Util.showToast(getApplicationContext(), getResources().getString(R.string.No_Movimientos));
        /*else{*/
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
            spFitroAnyo.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, anyos));
            mSpinnerCount++;
            spFitroAnyo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (mSpinnerInitializedCount < mSpinnerCount)
                    {
                        mSpinnerInitializedCount++;
                    }
                    else {
                        int anyoSpinner = spFitroAnyo.getSelectedItem() != null ? new Integer((String) spFitroAnyo.getSelectedItem()).intValue() : -1;
                        filtraMesAnyo(view, spFiltroMes.getSelectedItemPosition(), anyoSpinner);
                    }
                }
                public void onNothingSelected(AdapterView<?> parent) {
                    int year = Calendar.getInstance().get(Calendar.YEAR);
                    spFitroAnyo.setSelection(year);
                }
            });

            listViewMovs = (ListView) findViewById(R.id.listViewMov);
            listViewMovs.setAdapter(new MovimientoAdapter(getApplicationContext(), movs));
            //cargamos meses
            spFiltroMes = (Spinner) findViewById(R.id.spMeses);
            spFiltroMes.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, creaMeses()));

            spFiltroMes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int anyoSpinner = spFitroAnyo.getSelectedItem() != null ? new Integer((String) spFitroAnyo.getSelectedItem()).intValue() : -1;
                    filtraMesAnyo(view, position, anyoSpinner);
                }
                public void onNothingSelected(AdapterView<?> parent) {
                    int month = Calendar.getInstance().get(Calendar.MONTH);
                    spFiltroMes.setSelection(month);
                }
            });
            spFiltroMes.setSelection(Calendar.getInstance().get(Calendar.MONTH));
            spFitroAnyo.setSelection(spFitroAnyo.getSelectedItemPosition());

            listViewMovs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position,
                                        long id) {

                    final MovimientoItem movSeleccionado = (MovimientoItem) listViewMovs.getItemAtPosition(position);
                    final CharSequence[] items = {Constantes.ACCION_MODIFICAR, Constantes.ACCION_ELIMINAR};

                    AlertDialog.Builder builder = new AlertDialog.Builder(MovimientosActivity.this);
                    builder.setTitle(getResources().getString(R.string.MENU_OPCIONES));
                    //builder.setIcon(R.drawable.delete);
                    //builder.setIcon(R.drawable.edit);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    //Eliminar Movimiento
                                    String accion = (String) items[item];
                                    boolean realizado;

                                    if (accion.equals(Constantes.ACCION_ELIMINAR)) {
                                        if (movSeleccionado.getTipoMovimiento().trim().equals(getResources().getString(R.string.TIPO_MOVIMIENTO_GASTO))) {
                                            GastoDAO gastoDAO = new GastoDAO(MovimientosActivity.this);
                                            realizado = gastoDAO.deleteGasto(movSeleccionado.get_id());
                                            if (realizado) {
                                                Util.showToast(getApplicationContext(), getResources().getString(R.string.Eliminado));
                                                ArrayList<MovimientoItem> newList = new MovimientoDAO().cargaMovimientos(getApplicationContext());
                                                ((MovimientoAdapter) listViewMovs.getAdapter()).updateReceiptsList(newList);
                                            } else
                                                Util.showToast(getApplicationContext(), getResources().getString(R.string.No_Eliminado));
                                        }
                                        if (movSeleccionado.getTipoMovimiento().trim().equals(getResources().getString(R.string.TIPO_MOVIMIENTO_INGRESO))) {
                                            IngresoDAO ingresoDAO = new IngresoDAO(MovimientosActivity.this);
                                            realizado = ingresoDAO.deleteIngreso(movSeleccionado.get_id());
                                            if (realizado) {
                                                Util.showToast(getApplicationContext(), getResources().getString(R.string.Eliminado));
                                                ArrayList<MovimientoItem> newList = new MovimientoDAO().cargaMovimientos(getApplicationContext());
                                                ((MovimientoAdapter) listViewMovs.getAdapter()).updateReceiptsList(newList);
                                            } else
                                                Util.showToast(getApplicationContext(), getResources().getString(R.string.No_Eliminado));
                                        }
                                    }
                                    if (accion.equals(Constantes.ACCION_MODIFICAR)) {
                                        Bundle bundle = new Bundle();
                                        bundle.putString("_id", String.valueOf(movSeleccionado.get_id()));
                                        bundle.putString("valor", movSeleccionado.getValor());
                                        bundle.putString("descripcion", movSeleccionado.getDescripcion());
                                        bundle.putString("categoria", movSeleccionado.getCategoria());
                                        bundle.putString("fecha", movSeleccionado.getFecha());

                                        if (movSeleccionado.getTipoMovimiento().trim().equals(getResources().getString(R.string.TIPO_MOVIMIENTO_GASTO))){
                                            // Create an instance of the dialog fragment and show it*/
                                            showGastoDialog(view, bundle);
                                        }
                                        else if (movSeleccionado.getTipoMovimiento().trim().equals(getResources().getString(R.string.TIPO_MOVIMIENTO_INGRESO))) {
                                            // Create an instance of the dialog fragment and show it*/
                                            showIngresoDialog(view, bundle);
                                        }
                                    }
                                }
                            }
                    ).show();
                }
            });
        //}
    }

    public void showGastoDialog(View v, Bundle bundle) {
        DialogFragment newFragment = new GastoDialog();
        newFragment.setArguments(bundle);
        newFragment.show(getFragmentManager(),"MODIFICACION");
    }

    public void showIngresoDialog(View v, Bundle bundle) {
        DialogFragment newFragment = new IngresoDialog();
        newFragment.setArguments(bundle);
        newFragment.show(getFragmentManager(),"MODIFICACION");
    }

    //eventos click filtro gasto e ingreso
    public void filtraGasto(View v){
        int mes = spFiltroMes != null ? spFiltroMes.getSelectedItemPosition() : -1;
        int anyo = spFitroAnyo != null && spFitroAnyo.getSelectedItem() != null ? new Integer((String) spFitroAnyo.getSelectedItem()).intValue() : -1;
        ((MovimientoAdapter)listViewMovs.getAdapter()).setMesSeleccionado(mes);
        ((MovimientoAdapter)listViewMovs.getAdapter()).setAnyoSeleccionado(anyo);

        ((CheckBox) findViewById(R.id.cbIconPlus)).setChecked(false);
        if(!((CheckBox) findViewById(R.id.cbIconMinus)).isChecked())
            ((MovimientoAdapter)listViewMovs.getAdapter()).getFilter().filter(getResources().getString(R.string.TIPO_FILTRO_RESETEO));
        else
            ((MovimientoAdapter) listViewMovs.getAdapter()).getFilter().filter(getResources().getString(R.string.TIPO_MOVIMIENTO_GASTO));
    }
    public void filtraIngreso(View v){
         int mes = spFiltroMes != null ? spFiltroMes.getSelectedItemPosition() : -1;
         int anyo = spFitroAnyo != null && spFitroAnyo.getSelectedItem() != null ? new Integer((String) spFitroAnyo.getSelectedItem()).intValue() : -1;
        ((MovimientoAdapter)listViewMovs.getAdapter()).setMesSeleccionado(mes);
        ((MovimientoAdapter)listViewMovs.getAdapter()).setAnyoSeleccionado(anyo);

        ((CheckBox) findViewById(R.id.cbIconMinus)).setChecked(false);
        if(!((CheckBox) findViewById(R.id.cbIconPlus)).isChecked())
            ((MovimientoAdapter)listViewMovs.getAdapter()).getFilter().filter(getResources().getString(R.string.TIPO_FILTRO_RESETEO));
        else
            ((MovimientoAdapter) listViewMovs.getAdapter()).getFilter().filter(getResources().getString(R.string.TIPO_MOVIMIENTO_INGRESO));
    }

    public void filtraMesAnyo(View v, int mes, int anyo){
        ((MovimientoAdapter)listViewMovs.getAdapter()).setMesSeleccionado(mes);
        ((MovimientoAdapter)listViewMovs.getAdapter()).setAnyoSeleccionado(anyo);

        if(!((CheckBox) findViewById(R.id.cbIconMinus)).isChecked() && ((CheckBox) findViewById(R.id.cbIconPlus)).isChecked())
            ((MovimientoAdapter) listViewMovs.getAdapter()).getFilter().filter(getResources().getString(R.string.TIPO_MOVIMIENTO_INGRESO));
        if(((CheckBox) findViewById(R.id.cbIconMinus)).isChecked() && !((CheckBox) findViewById(R.id.cbIconPlus)).isChecked())
            ((MovimientoAdapter) listViewMovs.getAdapter()).getFilter().filter(getResources().getString(R.string.TIPO_MOVIMIENTO_GASTO));
        else
            ((MovimientoAdapter)listViewMovs.getAdapter()).getFilter().filter(getResources().getString(R.string.TIPO_FILTRO_RESETEO));
    }

    public String[] creaMeses(){
        String[] meses = new String[11];
        for(int i = 0 ; i < 12 ; i++){
            meses[i] = new DateFormatSymbols().getMonths()[i].toUpperCase();
        }
        return meses;
    }

    @Override
    public void onBackPressed() {
        backActivity();
    }

    private void backActivity(){
        Intent in = new Intent(MovimientosActivity.this, MainActivity.class);
        startActivity(in);
        setResult(RESULT_OK);
        finish();
    }
}

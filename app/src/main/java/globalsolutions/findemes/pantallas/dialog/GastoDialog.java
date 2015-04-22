package globalsolutions.findemes.pantallas.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import globalsolutions.findemes.R;
import globalsolutions.findemes.database.dao.GastoDAO;
import globalsolutions.findemes.database.dao.GrupoGastoDAO;
import globalsolutions.findemes.database.model.Gasto;
import globalsolutions.findemes.database.model.GrupoGasto;
import globalsolutions.findemes.pantallas.util.MoneyValueFilter;
import globalsolutions.findemes.pantallas.util.Util;

/**
 * Created by manuel.molero on 16/02/2015.
 */
public class GastoDialog extends DialogFragment {

    private OnGastoDialogListener callback;

    public interface OnGastoDialogListener {
        public void onGastoDialogSubmit(String result);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        try {
            callback = (OnGastoDialogListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnGastoDialogListener");
        }

        final View view = inflater.inflate(R.layout.edit_gasto_dialog, container, false);

        //establecemos listener de limitador de digitos
        ((EditText) view.findViewById(R.id.txtGasto)).setKeyListener(new MoneyValueFilter());

        //cargamos el combo de categorias
        Spinner categoria = (Spinner) view.findViewById(R.id.spCategoriaGasto);

        List<String> list = new ArrayList<String>();
        GrupoGastoDAO grupoGastoDAO = new GrupoGastoDAO(view.getContext());
        String[] categoriasGastos = grupoGastoDAO.selectGrupos();
        list = Arrays.asList(categoriasGastos);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoria.setAdapter(dataAdapter);

        //se cargan las propiedades del item seleccionado
        String valor = getArguments().getString("valor");
        String descripcion = getArguments().getString("descripcion");
        String categoriaStr = getArguments().getString("categoria");
        String fecha = getArguments().getString("fecha");
        String _id = getArguments().getString("_id");

        ((EditText) view.findViewById(R.id.txtGasto)).setText(valor);
        ((EditText) view.findViewById(R.id.txtDecripcion)).setText(descripcion);
         int spinnerPostion = dataAdapter.getPosition(categoriaStr);
        categoria.setSelection(spinnerPostion);
        ((TextView) view.findViewById(R.id.tvDia)).setText(fecha.split(" ")[0]);
        ((TextView) view.findViewById(R.id.tvHora)).setText(fecha.split(" ")[1]);

        final Gasto aMod = new Gasto();
        aMod.set_id(Integer.valueOf(_id).intValue());

        Button btnModificar = (Button) view.findViewById(R.id.btnGuardarGasto);

        btnModificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //descripcion , valor , fecha
                String valor = (String)((EditText) view.findViewById(R.id.txtGasto)).getText().toString();
                if(valor == null || valor.isEmpty()) {
                    ((EditText) v.findViewById(R.id.txtGasto)).setError(getResources().getString(R.string.Validacion_Cantidad));
                    return;
                }
                String descripcion = (String)((EditText) view.findViewById(R.id.txtDecripcion)).getText().toString();
                if(descripcion == null || descripcion.isEmpty()) {
                    ((EditText) view.findViewById(R.id.txtDecripcion)).setError(getResources().getString(R.string.Validacion_Descripcion));
                    return;
                }
                //obtenemos categoria de gasto
                String categoriaGasto = (String)((Spinner) view.findViewById(R.id.spCategoriaGasto)).getSelectedItem();
                if(categoriaGasto != null && !categoriaGasto.isEmpty()) {
                    Gasto nuevoGasto = new Gasto();
                    nuevoGasto.setDescripcion(descripcion);
                    nuevoGasto.setValor(valor);
                    String fecha = (String) ((TextView) view.findViewById(R.id.tvDia)).getText();
                    String hora = (String) ((TextView) view.findViewById(R.id.tvHora)).getText();
                    nuevoGasto.setFecha(fecha + " " + hora);

                    GrupoGasto grupo = new GrupoGasto();
                    grupo.setGrupo(categoriaGasto);
                    nuevoGasto.setGrupoGasto(grupo);
                    GastoDAO gastoDAO = new GastoDAO(view.getContext());
                    boolean actualizado = gastoDAO.updateGasto(aMod, nuevoGasto);
                    if(actualizado){
                        Util.showToast(view.getContext(), getResources().getString(R.string.Actualizado));
                        callback.onGastoDialogSubmit(String.valueOf(Activity.RESULT_OK));
                        dismiss();
                    }
                    else
                        Util.showToast(view.getContext(), getResources().getString(R.string.No_Actualizado));
                }
                else{
                    Util.showToast(view.getContext(), getResources().getString(R.string.Selecciona_categoria));
                    return;
                }
            }
        });

        // Inflate the layout to use as dialog or embedded fragment
        return view;
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }
}
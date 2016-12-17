package jose.tab_tesis;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;


public class MainActivity extends Activity {

    /**
     * Es el tab que permite el cambio de actividades
     */
    TabHost Tab_Museo;

    /**
     * Adaptador NFC
     */
    NfcAdapter nfcAdapter;

    /**
     * Mensajes NFC
     */
    public static final String INTENTO_NFC = "Intento de obtener NFC";
    public static final String ERROR_ENABLE = "No esta activada la funcionalidad NFC";
    public static final String ERROR_NFC = "Este dispositivo no soporta NFC";
    public static final String ERROR_MSGS = "Mensajes de NFC no encontrados";
    public static final String ERROR_LECT = "No se puede leer NFC";

    /**
     * TextView NFC
     */
    private TextView nfc_nombre, nfc_autor, nfc_creacion, nfc_resumen;

    /**
     * TextView SQLite
     */
    private TextView local_nombre, local_autor, local_creacion, local_resumen,
                     local_tipo, local_estilo, local_tecnica, local_ingreso,
                     local_nacion, local_dimension, local_peso;

    private ImageView im_local;
    String imagen_local;

    /**
     * TextView Web
     */
    private TextView web_nombre, web_autor, web_creacion, web_resumen,
            web_tipo, web_estilo, web_tecnica, web_ingreso,
            web_nacion, web_dimension, web_peso;

    private ImageView im_web;

    private Button web_historia, web_audio, web_video;

    /**
     * Base de datos sqlite
     */
    BD db;
    String [] obra;

    /**
     * Coordenada X para el movimiento del slide de la tabla
     */
    private float inicialX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Nombre de los textview de nfc
        nfc_nombre = (TextView)findViewById(R.id.nfc_nombre);
        nfc_autor = (TextView)findViewById(R.id.nfc_autor);
        nfc_creacion = (TextView)findViewById(R.id.nfc_creacion);
        nfc_resumen = (TextView)findViewById(R.id.nfc_resumen);

        //Nombre de los textview de sqlite
        local_nombre = (TextView)findViewById(R.id.local_nombre);
        local_autor = (TextView)findViewById(R.id.local_autor);
        local_creacion = (TextView)findViewById(R.id.local_creacion);
        local_resumen = (TextView)findViewById(R.id.local_resumen);
        local_tipo = (TextView)findViewById(R.id.local_tipo);
        local_estilo = (TextView)findViewById(R.id.local_estilo);
        local_tecnica = (TextView)findViewById(R.id.local_tecnica);
        local_ingreso = (TextView)findViewById(R.id.local_ingreso);
        local_nacion = (TextView)findViewById(R.id.local_nacion);
        local_dimension = (TextView)findViewById(R.id.local_dimension);
        local_peso = (TextView)findViewById(R.id.local_peso);
        im_local = (ImageView)findViewById(R.id.im_local);

        //Nombre de los textview de web
        web_nombre = (TextView)findViewById(R.id.web_nombre);
        web_autor = (TextView)findViewById(R.id.web_autor);
        web_creacion = (TextView)findViewById(R.id.web_creacion);
        web_resumen = (TextView)findViewById(R.id.web_resumen);
        web_tipo = (TextView)findViewById(R.id.web_tipo);
        web_estilo = (TextView)findViewById(R.id.web_estilo);
        web_tecnica = (TextView)findViewById(R.id.web_tecnica);
        web_ingreso = (TextView)findViewById(R.id.web_ingreso);
        web_nacion = (TextView)findViewById(R.id.web_nacion);
        web_dimension = (TextView)findViewById(R.id.web_dimension);
        web_peso = (TextView)findViewById(R.id.web_peso);
        im_web = (ImageView)findViewById(R.id.im_web);
        web_historia = (Button)findViewById(R.id.btn_historia);
        web_audio = (Button)findViewById(R.id.btn_audio);
        web_video = (Button)findViewById(R.id.btn_video);

        //Se llama al TabHost
        Tab_Museo = (TabHost) findViewById(R.id.tabHost);
        //Se activa el tabhost
        Tab_Museo.setup();

        //Se define cada tab
        TabHost.TabSpec tab1 = Tab_Museo.newTabSpec("tab1");  //aspectos de cada Tab
        TabHost.TabSpec tab2 = Tab_Museo.newTabSpec("tab2");
        TabHost.TabSpec tab3 = Tab_Museo.newTabSpec("tab3");

        //Se define cada tab
        tab1.setIndicator("NFC");//Que quiere que aparezca en las tab
        tab1.setContent(R.id.ejemplo1); //Se define el id de cada Tab

        tab2.setIndicator("Local");
        tab2.setContent(R.id.ejemplo2);

        tab3.setIndicator("Web");
        tab3.setContent(R.id.ejemplo3);

        //Se aniade los tabs ya programados
        Tab_Museo.addTab(tab1);
        Tab_Museo.addTab(tab2);
        Tab_Museo.addTab(tab3);

        /**
         * Leer desde el NFC
         */
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter==null){
            //detecta si el dispositivo tiene o no NFC
            Toast.makeText(this, ERROR_NFC, Toast.LENGTH_LONG).show();
            finish();
        }
        if(!nfcAdapter.isEnabled()){
            //detecta si el dispositivo tiene activado su NFC
            Toast.makeText(this,ERROR_ENABLE, Toast.LENGTH_LONG).show();
            finish();
        }

    }

    /**
     * Resume la actividad manteniendo la consistencia
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(!nfcAdapter.isEnabled()){
            //detecta si el dispositivo tiene activado su NFC
            Toast.makeText(this,ERROR_ENABLE, Toast.LENGTH_LONG).show();
            finish();
        }
        enableForegroundDispatchSystem();
    }

    /**
     * Da pausa a la actividad
     */
    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatchSystem();
    }

    /**
     * Definicion de la actividad a retomar
     */
    private void enableForegroundDispatchSystem() {
        // definicion de la actividad a retomar
        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        // recupera la actividad intent
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        // empareja intent con pendingintent
        IntentFilter[] intentFilters = new IntentFilter[]{};
        // retoma la actividad del nfcadapter
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    /**
     * Deshabilita la actividad del adaptador nfc nfcadapter
     */
    private void disableForegroundDispatchSystem() {
        nfcAdapter.disableForegroundDispatch(this);
    }

    /**
     * El funcionamiento del NFC se hace en un newintent
     * debido a que no se desea perder la instanca
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)){
            Toast.makeText(this,INTENTO_NFC, Toast.LENGTH_LONG).show();
            // obtener el numero de serie del tag qu es crucial para el
            // resto del proceso porque actua como PK de las bases de datos
            byte[] tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            String serie = new String();
            for (int i = 0; i < tagId.length; i++) {
                String x = Integer.toHexString(((int) tagId[i] & 0xff));
                if (x.length() == 1) {
                    x = '0' + x;
                }
                serie += x + ' ';
            }
            //se busca el mensaje mediante el parcelable
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            // si se encuentra el mensaje
            if(parcelables != null && parcelables.length > 0){
                readTextFromMessage((NdefMessage) parcelables[0], serie);
            }else{
                Toast.makeText(this,ERROR_MSGS, Toast.LENGTH_LONG).show();
            }

            /**
             * Leer desde la base de datos SQLite
             */
            db = new BD(getApplicationContext(),null,null,1);
            obra = db.buscar(serie);

            // Poner los diferentes datos de la obra de arte para
            // ser mostrados en pantalla

            arreglo_local(obra);
        }
    }

    /**
     * Lee la codificacion del mensaje
     * @param ndefMessage
     * @param serie
     */
    private void readTextFromMessage(NdefMessage ndefMessage, String serie) {
        //Se convierte el gran mensaje en un arreglo de records
        NdefRecord[] ndefRecords = ndefMessage.getRecords();
        // si hay mas de un arreglo de records se procesa o
        if(ndefRecords != null && ndefRecords.length > 0){
            NdefRecord ndefRecord = ndefRecords[0];
            String tagContent = getTextFromNdefRecord(ndefRecord);
            if(tagContent.equals(";;;") || tagContent.equals("")){
                nfc_nombre.setText("");
                nfc_autor.setText("");
                nfc_creacion.setText("");
                nfc_resumen.setText("");
                Toast.makeText(this,ERROR_MSGS, Toast.LENGTH_LONG).show();
            }else{
                String[] exit = tagContent.split(";");
                nfc_nombre.setText(exit[0]);
                nfc_autor.setText(exit[1]);
                nfc_creacion.setText(exit[2]);
                nfc_resumen.setText(exit[3]);
                Toast.makeText(this, "Número de serie: " + serie,  Toast.LENGTH_LONG).show();
            }


        }else {
            Toast.makeText(this,ERROR_LECT, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Se obtiene la codificacion de cada mensaje en el formato UTF-8
     * @param ndefRecord
     * @return
     */
    private String getTextFromNdefRecord(NdefRecord ndefRecord) {
        String tagContent = null;
        try {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding;
            if ((payload[0] & 128) == 0) textEncoding = "UTF-8";
            else textEncoding = "UTF-16";
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize + 1,
                    payload.length - languageSize - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("getTextFromNdefRecord", e.getMessage(), e);
        }
        return tagContent;
    }

    /**
     * Mostrar en pantalla el arreglo local
     * @param obra
     */
    private void arreglo_local(String[] obra) {
        // se pone cada componente de la obra en los textview que se definieron para ello
        local_nombre.setText(obra[1]);
        local_autor.setText(obra[2]);
        local_creacion.setText(obra[3]);
        local_resumen.setText(obra[4]);
        local_tipo.setText(obra[5]);
        local_estilo.setText(obra[6]);
        local_tecnica.setText(obra[7]);
        local_ingreso.setText(obra[8]);
        local_nacion.setText(obra[9]);
        local_dimension.setText(obra[10]);
        local_peso.setText(obra[11]);
        imagen_local = obra[12].toString();
        new descargarImagen(im_local).execute(imagen_local);
    }

    /**
     * Clase creada para poder mostrar mediante asyntask la imagen almacenada en la
     * base de datos SQLite
     */
    private class descargarImagen extends AsyncTask<String, Void, Bitmap> {
        ImageView imagen;

        public descargarImagen(ImageView imagen) {
            this.imagen = imagen;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream input = new java.net.URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {

                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            imagen.setImageBitmap(result);
        }
    }


    /**
     * Metodo override (nativo de Activity) que permite el movimiento por slide
     * No funciona sobre scrollview
     * @param touchevent
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {
            // Cuando se hace contacto con la pantalla la primera vez
            // Se define una posicion inicial
            case MotionEvent.ACTION_DOWN:
            {
                inicialX = touchevent.getX();
                break;
            }
            case MotionEvent.ACTION_UP: {
                // Posicion actual de la coordenada X
                float actualX = touchevent.getX();

                // Si es menor la posicion actual con respecto a la inicial
                // entonces se mueve de izquierda a derecha
                if (inicialX < actualX) {
                    cambioTabs(false);
                }

                // De no ser asi se mueve de derecha a izquierda
                if (inicialX > actualX) {
                    cambioTabs(true);
                }

                break;
            }
        }
        return false;
    }

    /**
     * Se definen la direccion que indican los movimientos
     * @param direccion
     */
    public void cambioTabs(boolean direccion) {
        if (direccion) // true es igual a movimiento a la izquierda
        {
            if (Tab_Museo.getCurrentTab() == 0)
                Tab_Museo.setCurrentTab(Tab_Museo.getTabWidget().getTabCount() - 1);
            else
                Tab_Museo.setCurrentTab(Tab_Museo.getCurrentTab() - 1);
        }
        else // Si es false se mueve a la derecha
        {
            if (Tab_Museo.getCurrentTab() != (Tab_Museo.getTabWidget().getTabCount() - 1))
                Tab_Museo.setCurrentTab(Tab_Museo.getCurrentTab() + 1);
            else
                Tab_Museo.setCurrentTab(0);
        }
    }
}

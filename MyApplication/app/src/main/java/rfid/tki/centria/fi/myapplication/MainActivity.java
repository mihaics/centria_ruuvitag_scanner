package rfid.tki.centria.fi.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        byte[] bytes = {32,40,20,22,99,29,93,1,2,5};

        calculate(bytes,bytes.length);
        int min = getMin();
        int max = getMax();

        TextView tv = (TextView) this.findViewById(R.id.sample_text);
tv.setText("min:" + min + " max:" + max);
        close();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    public native int calculate(byte[] data, int dataLegnth);
    public native int getMin();
    public native int getMax();

    public native void init();
    public native  void close();
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
